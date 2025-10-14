package bor.tools.simplellm.impl;

import static bor.tools.simplellm.Model_Type.EMBEDDING;
import static bor.tools.simplellm.Model_Type.EMBEDDING_DIMENSION;
import static bor.tools.simplellm.Model_Type.LANGUAGE;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.ModelEmbedding;

/**
 * Implementation of the LLMService interface for Ollama's local Large Language
 * Model server.
 * <p>
 * This class extends OpenAILLMService and adapts it to work with Ollama's API
 * endpoints.
 * Ollama provides a local server that runs various open-source LLM models with
 * an OpenAI-compatible API.
 * </p>
 * <p>
 * Key differences from OpenAI:
 * - Uses local server (default: http://localhost:11434)
 * - Uses "ollama" as default API key
 * - Different model names and capabilities
 * - No responses API support (uses chat completions only)
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 * 
 * @see OpenAILLMService
 */
public class OllamaLLMService extends LMStudioLLMService {

	static final String DEFAULT_PROMPT = "You are a helpful assistant who responds in the same language used as input.";

	private static final LLMConfig defaultLLMConfig;

	private static final String DEFAULT_MODEL = "qwen3-1.7b";

	static {
		MapModels map = new MapModels();

		// Ollama model definition - using phi-4-mini as requested by user

		// nomic-embed-text:latest
		Model snowflake  = new ModelEmbedding("snowflake-arctic-embed2", "snowflake", 8000, EMBEDDING, EMBEDDING_DIMENSION);
		Model nomic      = new ModelEmbedding("nomic-embed-text", "nomic", 8000, EMBEDDING, EMBEDDING_DIMENSION);
		Model gemma      = new ModelEmbedding("embeddinggemma", "embeddinggemma", 8000, EMBEDDING, EMBEDDING_DIMENSION);
		
		Model qwen3_17b   = new Model("qwen3:1.7b", "qwen3-1.7b", 8192, LANGUAGE);		
		Model phi35_mini = new Model("phi3.5:3.8b-mini-instruct-q5_K_M", "phi3.5-mini", 16000, LANGUAGE);
		Model phi4_mini  = new Model("kwangsuklee/phi4-mini-inst-q5-250228", "phi4-mini", 16000, LANGUAGE);

		map.add(qwen3_17b);
		map.add(phi4_mini);
		map.add(phi35_mini);
		
		map.add(snowflake);
		map.add(nomic);
		map.add(gemma);

		defaultLLMConfig = LLMConfig.builder()
		            .apiTokenEnvironment("OLLAMA_API_KEY")
		            .apiToken("ollama") // Default API key for Ollama
		            .baseUrl("http://localhost:11434/v1/")
		            .modelMap(map)
		            .build();
	}
	
	/**
	 * Constructor for OllamaLLMService with custom configuration.
	 * <p>
	 * Creates a new instance with the specified LLM configuration,
	 * including API settings and model definitions for Ollama.
	 * </p>
	 *
	 * @param config the LLM configuration containing Ollama API settings and
	 *               parameters
	 */
	public OllamaLLMService(LLMConfig config) {
		super(config==null?getDefaultLLMConfig():config);
		// Ollama doesn't support responses API, so disable it
		this.useResponsesAPI = false;
	}

	/**
	 * Retrieves the default LLM configuration for Ollama services.
	 * <p>
	 * This configuration includes default local server endpoints, model
	 * definitions,
	 * and settings optimized for Ollama's local LLM server.
	 * </p>
	 *
	 * @return the default LLMConfig instance for Ollama
	 */
	public static LLMConfig getDefaultLLMConfig() { return defaultLLMConfig; }

	/**
	 * Default constructor for OllamaLLMService.
	 * <p>
	 * Creates a new instance with default Ollama configuration settings.
	 * </p>
	 */
	public OllamaLLMService() {
		this(OllamaLLMService.getDefaultLLMConfig());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultModelName() {
		MapModels models = getLLMConfig().getModelMap();
		var       name   = models.getModel(DEFAULT_MODEL);
		if (name == null) {
			// If default model not found, fallback to first available model
			logger.warn("Warning: Default model '"
			            + DEFAULT_MODEL
			            + "' not found in configuration. "
			            + "Falling back to first available model.");
			// pick first language model
			name = models.values()
			            .stream()
			            .filter(model -> isModelType(model.getName(), LANGUAGE))
			            .findFirst()
			            .orElse(null);
		}
		return name.toString();
	}

	/**
	 * Override to check if endpoint is Ollama (not OpenAI).
	 * This affects parameter mapping and API behavior.
	 */
	@Override
	protected boolean isOpenAIEndpoint() {
		return false; // Ollama uses OpenAI-compatible API but isn't OpenAI
	}

	/**
	 * Ollama doesn't support the responses API, so this always returns false.
	 */
	@Override
	protected boolean isResponsesAPIModel(String model) {
		return false; // Ollama doesn't support responses API
	}

	/**
	 * Gets the API token from configuration, with Ollama-specific defaults.
	 */
	@Override
	protected String getApiToken() throws bor.tools.simplellm.exceptions.LLMException {
		String token = config.getApiToken();
		if (token == null || token.trim().isEmpty()) {
			// Try environment variable
			String envVar = config.getApiTokenEnvironment();
			if (envVar != null) {
				token = System.getenv(envVar);
			}
		}

		// If still no token, use default "ollama" for local server
		if (token == null || token.trim().isEmpty()) {
			token = "ollama";
			config.setApiToken(token);
		}
		return token.trim();
	}

	/**
	 * Override parameter conversion for Ollama-specific requirements.
	 * Ollama might have different parameter names or limits.
	 */
	@Override
	protected bor.tools.simplellm.MapParam convert2ResponseAPI(bor.tools.simplellm.MapParam params) {
		// Ollama doesn't use responses API, so return original params
		return params;
	}


}
