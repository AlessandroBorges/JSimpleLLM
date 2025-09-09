package bor.tools.simplellm.impl;

import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING_DIMENSION;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.LANGUAGE;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.REASONING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.Model;

/**
 * Implementation of the LLMService interface for LM Studio's local Large Language Model server.
 * <p>
 * This class extends OpenAILLMService and adapts it to work with LM Studio's API endpoints.
 * LM Studio provides a local server that runs various open-source LLM models with an OpenAI-compatible API.
 * LM Studio is particularly popular for running models locally with a user-friendly interface.
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
 * @since 1.0
 * 
 * @see OpenAILLMService
 */
public class LMStudioLLMService extends OpenAILLMService {

    protected static final MapModels defaultModelMap;
    protected static final LLMConfig defaultLLMConfig;

    static {
        MapModels map = new MapModels();

        // Common LM Studio model definitions - these are examples of models typically used
        // Note: Actual available models depend on what user has loaded in LM Studio
        
        // Language models commonly used in LM Studio
        Model llama3_1_8b = new Model("llama-3.1-8b-instruct", "llama3.1-8b", 128000, LANGUAGE, REASONING, CODING);
        Model llama3_2_3b = new Model("llama-3.2-3b-instruct", "llama3.2-3b", 128000, LANGUAGE, CODING);
        Model mistral_7b = new Model("mistral-7b-instruct-v0.3", "mistral-7b", 32000, LANGUAGE, CODING);
        Model codellama_7b = new Model("codellama-7b-instruct", "codellama-7b", 16000, CODING, LANGUAGE);
        Model phi3_5_mini = new Model("phi-3.5-mini-instruct", "phi3.5-mini", 128000, LANGUAGE, REASONING);
        
        // Vision models that might be available
        Model llava_7b = new Model("llava-1.5-7b", "llava-7b", 4096, LANGUAGE, VISION);
        
        // Embedding models (if user has loaded embedding models)
        Model nomic_embed = new Model("nomic-embed-text-v1.5", "nomic-embed", 8192, EMBEDDING, EMBEDDING_DIMENSION);

        map.add(llama3_1_8b);
        map.add(llama3_2_3b);
        map.add(mistral_7b);
        map.add(codellama_7b);
        map.add(phi3_5_mini);
        map.add(llava_7b);
        map.add(nomic_embed);

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
     * This configuration includes default local server endpoints, model definitions,
     * and settings optimized for LM Studio's local LLM server.
     * </p>
     *
     * @return the default LLMConfig instance for LM Studio
     */
    public static LLMConfig getDefaultLLMConfig() {
        return defaultLLMConfig;
    }

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
     * @param config the LLM configuration containing LM Studio API settings and parameters
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
     * This is useful for LM Studio where model capabilities depend on what's loaded.
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

        // Fallback to name-based detection for common LM Studio models
        String lowerName = modelName.toLowerCase();
        switch (type) {
            case VISION:
                return lowerName.contains("llava") || 
                       lowerName.contains("vision") ||
                       lowerName.contains("bakllava");
                       
            case CODING:
                return lowerName.contains("code") ||
                       lowerName.contains("codellama") ||
                       lowerName.contains("deepseek") ||
                       lowerName.contains("starcoder") ||
                       lowerName.contains("wizardcoder");
                       
            case EMBEDDING:
                return lowerName.contains("embed") ||
                       lowerName.contains("bge") ||
                       lowerName.contains("nomic") ||
                       lowerName.contains("e5");
                       
            case REASONING:
                return lowerName.contains("llama") ||
                       lowerName.contains("mistral") ||
                       lowerName.contains("phi") ||
                       lowerName.contains("qwen") ||
                       lowerName.contains("gemma");
                       
            default:
                return true; // Most LM Studio models support basic language tasks
        }
    }

    /**
     * Override embeddings to handle LM Studio-specific embedding model behavior.
     * LM Studio requires the embedding model to be explicitly loaded.
     */
    @Override
    public float[] embeddings(String texto, String model, Integer vecSize) 
            throws bor.tools.simplellm.exceptions.LLMException {
        
        // Use default embedding model if available
        if (model == null || model.trim().isEmpty()) {
            // Check if we have any embedding models configured
            boolean hasEmbeddingModel = config.getModelMap().values().stream()
                    .anyMatch(m -> isModelType(m.getName(), bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING));
            
            if (!hasEmbeddingModel) {
                throw new bor.tools.simplellm.exceptions.LLMException(
                    "No embedding model available in LM Studio configuration. " +
                    "Please load an embedding model in LM Studio (e.g., nomic-embed-text, bge-large).");
            }
            
            // Find first available embedding model
            model = config.getModelMap().values().stream()
                    .filter(m -> isModelType(m.getName(), bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING))
                    .map(Model::getName)
                    .findFirst()
                    .orElse("nomic-embed-text-v1.5");
        }

        // Call parent implementation
        return super.embeddings(texto, model, vecSize);
    }

    /**
     * Override getDefaultModelName to provide a sensible default for LM Studio.
     * Since models are user-loaded, we pick a commonly available one.
     */
    @Override
    public String getDefaultModelName() {
        // Try to find the first available language model
        return config.getModelMap().values().stream()
                .filter(m -> isModelType(m.getName(), bor.tools.simplellm.LLMConfig.MODEL_TYPE.LANGUAGE))
                .map(Model::getName)
                .findFirst()
                .orElse("llama-3.1-8b-instruct"); // Fallback to common model name
    }
}