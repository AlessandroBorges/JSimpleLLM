package bor.tools.simplellm.impl;

import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING_DIMENSION;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.LANGUAGE;

import java.util.Map;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.exceptions.LLMException;

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
public class OllamaLLMService extends OpenAILLMService {

	static final String              DEFAULT_PROMPT =
	            "You are helpful assistant, which ansewer in same language used as input.";
	protected static final MapModels defaultModelMap;
	protected static final LLMConfig defaultLLMConfig;

	static {
		MapModels map = new MapModels();

		// Ollama model definition - using phi-4-mini as requested by user

		// nomic-embed-text:latest
		Model snowflake  = new Model("snowflake-arctic-embed2:latest", "snowflake", 8000, EMBEDDING, EMBEDDING_DIMENSION);
		Model nomic      = new Model("nomic-embed-text:latest ", "nomic", 8000, EMBEDDING, EMBEDDING_DIMENSION);
		Model phi35_mini = new Model("phi3.5:3.8b-mini-instruct-q5_K_M", "phi3.5-mini", 16000, LANGUAGE);
		Model phi4_mini  = new Model("kwangsuklee/phi4-mini-inst-q5-250228", "phi4-mini", 16000, LANGUAGE);

		map.add(snowflake);
		map.add(nomic);
		map.add(phi35_mini);
		map.add(phi4_mini);

		// Make the defaultModelMap unmodifiable
		defaultModelMap = map;

		defaultLLMConfig = LLMConfig.builder()
		            .apiTokenEnvironment("OLLAMA_API_KEY")
		            .apiToken("ollama") // Default API key for Ollama
		            .baseUrl("http://localhost:11434/v1/")
		            .modelMap(defaultModelMap)
		            .build();
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
		super(config);
		// Ollama doesn't support responses API, so disable it
		this.useResponsesAPI = false;
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

	/**
	 * Check if a model supports a specific capability.
	 * This is useful for Ollama where model capabilities might vary.
	 */
	@Override
	public boolean isModelType(String modelName, bor.tools.simplellm.LLMConfig.MODEL_TYPE type) {
		if (modelName == null) {
			return false;
		}

		Model model = config.getModelMap().get(modelName);
		if (model != null) {
			return model.getTypes().contains(type);
		}

		// Fallback to name-based detection for common Ollama models
		switch (type) {
			case VISION:
				return modelName.toLowerCase().contains("llava") || modelName.toLowerCase().contains("vision");
			case CODING:
				return modelName.toLowerCase().contains("code") || modelName.toLowerCase().contains("starcoder")
				       || modelName.toLowerCase().contains("codestral");
			case EMBEDDING:
				return modelName.toLowerCase().contains("embed") || modelName.toLowerCase().contains("bge")
				       || modelName.toLowerCase().contains("nomic");
			default:
				return true; // Most Ollama models support basic language tasks
		}
	}

	/**
	 * Override embeddings to throw exception since most Ollama models don't support
	 * embeddings
	 * unless specifically configured with an embedding model.
	 */
	@Override
	public float[] embeddings(String texto, String model, Integer vecSize)
	            throws bor.tools.simplellm.exceptions.LLMException {

		// Use default embedding model if available
		if (model == null || model.trim().isEmpty()) {
			// Check if we have any embedding models configured
			boolean hasEmbeddingModel =
			            config.getModelMap().values().stream().anyMatch(m -> isModelType(m.getName(), EMBEDDING));

			if (!hasEmbeddingModel) {
				throw new bor.tools.simplellm.exceptions.LLMException(
				            "No embedding model available in Ollama configuration. "
				                        + "Please install an embedding model like 'nomic-embed-text' or 'bge-large'.");
			}

			// Find first available embedding model
			model = config.getModelMap()
			            .values()
			            .stream()
			            .filter(m -> isModelType(m.getName(), EMBEDDING))
			            .map(Model::getName)
			            .findFirst()
			            .orElse("nomic-embed-text");
		}

		// Call parent implementation
		return super.embeddings(texto, model, vecSize);
	}

	/**
	 * Override completion to ensure Ollama-specific behavior.
	 * This method adapts the completion call to Ollama's API nuances.
	 * Oposite to OpenAI, which deprecated chat completions,
	 * Ollama does support both classic completion and 'chatCompletion endpoints.
	 */
	@Override
	public CompletionResponse completion(String prompt, String query, MapParam params) throws LLMException {

		prompt = prompt == null ? DEFAULT_PROMPT : prompt;
		query = query == null ? "" : query;
		params = fixParams(params);
		// Create request payload
		Map<String, Object> payload = jsonMapper.toCompletionRequest(prompt, query, params);

		try {
			// Make API request
			Map<String, Object> response = postRequest("/completions", payload);

			// Convert response
			CompletionResponse completionResponse = jsonMapper.fromChatCompletionResponse(response);

			return completionResponse;

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during chat completion: "
			            + e.getMessage(), e);
		}
	}

}
