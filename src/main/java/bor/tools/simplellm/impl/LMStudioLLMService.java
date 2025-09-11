package bor.tools.simplellm.impl;

import static bor.tools.simplellm.Model_Type.CODING;
import static bor.tools.simplellm.Model_Type.EMBEDDING;
import static bor.tools.simplellm.Model_Type.EMBEDDING_DIMENSION;
import static bor.tools.simplellm.Model_Type.LANGUAGE;
import static bor.tools.simplellm.Model_Type.REASONING;
import static bor.tools.simplellm.Model_Type.TOOLS;
import static bor.tools.simplellm.Model_Type.VISION;

import java.util.Map;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.ModelEmbedding;
import bor.tools.simplellm.ModelEmbedding.Emb_Operation;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Implementation of the LLMService interface for LM Studio's local Large
 * Language Model server.
 * <p>
 * This class extends OpenAILLMService and adapts it to work with LM Studio's
 * API endpoints.
 * LM Studio provides a local server that runs various open-source LLM models
 * with an OpenAI-compatible API.
 * LM Studio is particularly popular for running models locally with a
 * user-friendly interface.
 * </p>
 * <p>
 * Key differences from OpenAI:
 * - Uses local server (default: http://localhost:1234)
 * - Uses "lm-studio" as default API key
 * - Different model names based on what's loaded in LM Studio
 * - No responses API support (uses chat completions only)
 * - Models are dynamically loaded/unloaded by user in LM Studio UI
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 * 
 * @see OpenAILLMService
 */
public class LMStudioLLMService extends OpenAILLMService {

	protected static final MapModels defaultModelMap;
	protected static final LLMConfig defaultLLMConfig;

	protected static final String DEFAULT_MODEL_NAME = "qwen3-1.7b";

	static {
		MapModels map = new MapModels();

		// Common LM Studio model definitions - these are examples of models typically
		// used
		// Note: Actual available models depend on what user has loaded in LM Studio
		Model qwen3_1_7b = new Model("qwen/qwen3-1.7b", "qwen3-1.7b", 32000, LANGUAGE, REASONING, TOOLS, CODING);

		
		Model phi4_mini   = new Model("phi-4-mini-instruct", "phi4-mini", 32768, LANGUAGE, REASONING, CODING);
		Model qwen3_4b    = new Model("qwen/qwen3-4b", "qwen3-4b", 32000, LANGUAGE, REASONING, TOOLS, CODING);
		Model gtp_oss_20b = new Model("openai/gpt-oss-20b", "gpt-oss", 32768, LANGUAGE, REASONING, TOOLS, CODING);
		Model phi3_5_mini = new Model("phi-3.5-mini-instruct", "phi3.5-mini", 128000, LANGUAGE, REASONING);

		// Vision models that might be available
		Model llava_7b = new Model("llava-1.5-7b", "llava-7b", 8096, LANGUAGE, VISION);

		// Embedding models (if user has loaded embedding models
		// Embedding models (if user has loaded embedding models)
    	Model nomic_embed = new ModelEmbedding("nomic-embed-text-v1.5", "nomic-embed", 8192, EMBEDDING, EMBEDDING_DIMENSION);
		Model snowflake   = new ModelEmbedding("text-embedding-snowflake-arctic-embed-l-v2.0",
		            "snowflake",
		            8192,
		            EMBEDDING,
		            EMBEDDING_DIMENSION);
		Model nomic     =
		            new ModelEmbedding("text-embedding-nomic-embed-text-v1.5@q8_0", "nomic", 8192, EMBEDDING, EMBEDDING_DIMENSION);

		// Add models to map
		map.add(qwen3_1_7b);
		map.add(phi4_mini);
		map.add(qwen3_4b);
		map.add(gtp_oss_20b);

		map.add(phi3_5_mini);
		map.add(llava_7b);
		map.add(nomic_embed);

		map.add(snowflake);
		map.add(nomic);

		// Make the defaultModelMap unmodifiable
		defaultModelMap = map;

		defaultLLMConfig = LLMConfig.builder()
		            .apiTokenEnvironment("LMSTUDIO_API_KEY")
		            .apiToken("lm-studio") // Default API key for LM Studio
		            .baseUrl("http://localhost:1234/v1/")
		            .modelMap(defaultModelMap)
		            .build();
	}

	/**
	 * Retrieves the default LLM configuration for LM Studio services.
	 * <p>
	 * This configuration includes default local server endpoints, model
	 * definitions,
	 * and settings optimized for LM Studio's local LLM server.
	 * </p>
	 *
	 * @return the default LLMConfig instance for LM Studio
	 */
	public static LLMConfig getDefaultLLMConfig() { return defaultLLMConfig; }

	/**
	 * Default constructor for LMStudioLLMService.
	 * <p>
	 * Creates a new instance with default LM Studio configuration settings.
	 * </p>
	 */
	public LMStudioLLMService() {
		this(getDefaultLLMConfig());
	}

	/**
	 * Constructor for LMStudioLLMService with custom configuration.
	 * <p>
	 * Creates a new instance with the specified LLM configuration,
	 * including API settings and model definitions for LM Studio.
	 * </p>
	 *
	 * @param config the LLM configuration containing LM Studio API settings and
	 *               parameters
	 */
	public LMStudioLLMService(LLMConfig config) {
		super(config);
		// LM Studio doesn't support responses API, so disable it
		this.useResponsesAPI = false;
	}

	/**
	 * Override to check if endpoint is LM Studio (not OpenAI).
	 * This affects parameter mapping and API behavior.
	 */
	@Override
	protected boolean isOpenAIEndpoint() {
		return false; // LM Studio uses OpenAI-compatible API but isn't OpenAI
	}

	/**
	 * LM Studio doesn't support the responses API, so this always returns false.
	 */
	@Override
	protected boolean isResponsesAPIModel(String model) {
		return false; // LM Studio doesn't support responses API
	}

	/**
	 * Gets the API token from configuration, with LM Studio-specific defaults.
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

		// If still no token, use default "lm-studio" for local server
		if (token == null || token.trim().isEmpty()) {
			token = "lm-studio";
			config.setApiToken(token);
		}

		return token.trim();
	}

	/**
	 * Override parameter conversion for LM Studio-specific requirements.
	 * LM Studio has some parameter differences from standard OpenAI.
	 */
	@Override
	protected bor.tools.simplellm.MapParam convert2ResponseAPI(bor.tools.simplellm.MapParam params) {
		// LM Studio doesn't use responses API, so return original params
		return params;
	}

	/**
	 * Check if a model supports a specific capability.
	 * This is useful for LM Studio where model capabilities depend on what's
	 * loaded.
	 */
	@Override
	public boolean isModelType(String modelName, bor.tools.simplellm.Model_Type type) {
		if (modelName == null) {
			return false;
		}

		Model model = config.getModelMap().get(modelName);
		if (model != null) {
			return model.getTypes().contains(type);
		}

		// Fallback to name-based detection for common LM Studio models
		String lowerName = modelName.toLowerCase();
		switch (type) {
			case VISION:
				return lowerName.contains("llava") || lowerName.contains("vision") || lowerName.contains("bakllava");

			case CODING:
				return lowerName.contains("code") || lowerName.contains("codellama")
				       || lowerName.contains("deepseek")
				       || lowerName.contains("starcoder")
				       || lowerName.contains("wizardcoder");

			case EMBEDDING:
				return lowerName.contains("embed") || lowerName.contains("bge")
				       || lowerName.contains("nomic")
				       || lowerName.contains("e5");

			case REASONING:
				return lowerName.contains("llama") || lowerName.contains("mistral")
				       || lowerName.contains("phi")
				       || lowerName.contains("qwen")
				       || lowerName.contains("gemma");

			default:
				return true; // Most LM Studio models support basic language tasks
		}
	}

	/**
	 * Override embeddings to handle LM Studio-specific embedding model behavior.
	 * LM Studio requires the embedding model to be explicitly loaded.
	 */
	@Override
	public float[] embeddings(Emb_Operation op, String texto, MapParam params)
	            throws bor.tools.simplellm.exceptions.LLMException {
		
		// Call parent implementation
		return super.embeddings(op, texto, params);
	}

	/**
	 * Override getDefaultModelName to provide a sensible default for LM Studio.
	 * Since models are user-loaded, we pick a commonly available one.
	 */
	@Override
	public String getDefaultModelName() {
		// Try to find the first available language model
		return config.getModelMap()
		            .values()
		            .stream()
		            .filter(m -> isModelType(m.getName(), bor.tools.simplellm.Model_Type.LANGUAGE))
		            .map(Model::getName)
		            .findFirst()
		            .orElse(DEFAULT_MODEL_NAME); // Fallback to common model name
	}

	/**
	 * Override completion to ensure Ollama-specific behavior.
	 * This method adapts the completion call to Ollama's API nuances.
	 * Oposite to OpenAI, which deprecated chat completions,
	 * LM_Studio does support both classic completion and 'chatCompletion endpoints.
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
