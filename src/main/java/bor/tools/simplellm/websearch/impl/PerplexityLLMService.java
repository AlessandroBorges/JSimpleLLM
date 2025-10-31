package bor.tools.simplellm.websearch.impl;

import static bor.tools.simplellm.Model_Type.CITATIONS;
import static bor.tools.simplellm.Model_Type.DEEP_RESEARCH;
import static bor.tools.simplellm.Model_Type.FAST;
import static bor.tools.simplellm.Model_Type.LANGUAGE;
import static bor.tools.simplellm.Model_Type.REASONING;
import static bor.tools.simplellm.Model_Type.WEBSEARCH;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.Embeddings_Op;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.SERVICE_PROVIDER;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMAuthenticationException;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.exceptions.LLMNetworkException;
import bor.tools.simplellm.exceptions.LLMRateLimitException;
import bor.tools.simplellm.exceptions.LLMTimeoutException;
import bor.tools.simplellm.impl.OpenAIJsonMapper;
import bor.tools.simplellm.impl.StreamingUtil;
import bor.tools.simplellm.websearch.SearchResponse;
import bor.tools.simplellm.websearch.WebSearch;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Implementation of LLMService with WebSearch capabilities for Perplexity AI.
 * <p>
 * Perplexity AI provides LLM services with real-time web search integration.
 * Most models have native access to web search and return citations to source materials.
 * </p>
 * <p>
 * <b>Available Models:</b>
 * <ul>
 * <li><b>sonar</b> - Fast general-purpose model with web search (128k context)</li>
 * <li><b>sonar-pro</b> - Advanced model with deeper analysis (200k context)</li>
 * <li><b>sonar-deep-research</b> - Exhaustive research model (128k context)</li>
 * <li><b>sonar-reasoning</b> - Reasoning model with web search (128k context)</li>
 * <li><b>sonar-reasoning-pro</b> - Advanced reasoning with web search (128k context)</li>
 * <li><b>r1-1776</b> - Offline model without web search (128k context)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li>Real-time web search integration</li>
 * <li>Source citations for all web-enabled models</li>
 * <li>Domain filtering and recency filters</li>
 * <li>Related questions suggestions</li>
 * <li>Optional image results</li>
 * <li>Streaming support</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example Usage:</b>
 * </p>
 * <pre>{@code
 * // Create service
 * LLMService service = LLMServiceFactory.createPerplexity();
 *
 * // Use as WebSearch
 * WebSearch searchService = (WebSearch) service;
 *
 * // Configure search parameters
 * MapParam params = new MapParam()
 *     .model("sonar-pro")
 *     .searchDomainFilter(new String[]{"arxiv.org", "nature.com"})
 *     .searchRecencyFilter("week")
 *     .returnRelatedQuestions(true)
 *     .maxTokens(1000);
 *
 * // Execute search
 * SearchResponse response = searchService.webSearch(
 *     "Latest developments in quantum computing",
 *     params
 * );
 *
 * // Access results
 * System.out.println(response.getContent());
 * System.out.println("Citations: " + response.getCitations());
 * }</pre>
 *
 * @author AlessandroBorges
 * @since 1.1
 *
 * @see WebSearch
 * @see SearchResponse
 * @see LLMService
 */
public class PerplexityLLMService implements LLMService, WebSearch {

    Logger logger = LoggerFactory.getLogger(PerplexityLLMService.class.getName());

    private static final String DEFAULT_BASE_URL = "https://api.perplexity.ai";
    private static final String DEFAULT_MODEL = "sonar";
    
    @SuppressWarnings("unused")
	private static final String DEFAULT_PROMPT =
            "You are a helpful assistant that provides accurate, well-researched information. "
            + "Always cite your sources and acknowledge when information is uncertain.";

    protected static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final LLMConfig defaultLLMConfig;

    static {
        MapModels map = new MapModels();

        // Web Search Models
        Model sonar = new Model("sonar", "sonar", 128000,
                LANGUAGE, FAST, WEBSEARCH, CITATIONS);

        Model sonar_pro = new Model("sonar-pro", "sonar-pro", 200000,
                LANGUAGE, WEBSEARCH, CITATIONS);

        Model sonar_deep_research = new Model("sonar-deep-research", "deep-research", 128000,
                LANGUAGE, WEBSEARCH, CITATIONS, DEEP_RESEARCH);

        Model sonar_reasoning = new Model("sonar-reasoning", "reasoning", 128000,
                LANGUAGE, WEBSEARCH, CITATIONS, REASONING);

        Model sonar_reasoning_pro = new Model("sonar-reasoning-pro", "reasoning-pro", 128000,
                LANGUAGE, WEBSEARCH, CITATIONS, REASONING);

        // Offline Model (no web search)
        Model r1_1776 = new Model("r1-1776", "r1", 128000, LANGUAGE, REASONING);

        map.add(sonar);
        map.add(sonar_pro);
        map.add(sonar_deep_research);
        map.add(sonar_reasoning);
        map.add(sonar_reasoning_pro);
        map.add(r1_1776);

        // Configure sensible defaults for Perplexity
        MapParam defaultParams = new MapParam()
                .searchMode("web")                    // Default to web search mode
                .returnRelatedQuestions(true)         // Enable related questions by default
                .temperature(0.7f)                   // Balanced creativity
                .userLocation(-15.7933, -47.8827, "br")
                .searchDomainFilter(new String[]{"-facebook.com", "-twitter.com", "-instagram.com"})
                
                ;
                
        defaultLLMConfig = LLMConfig.builder()
                .apiTokenEnvironment("PERPLEXITY_API_KEY")
                .baseUrl(DEFAULT_BASE_URL)
                .registeredModelMap(map)
                .defaultModelName(DEFAULT_MODEL)
                .defaultParams(defaultParams)
                .build();
    }

    protected LLMConfig config;
    protected final OkHttpClient httpClient;
    protected final PerplexityJsonMapper jsonMapper;
    protected final StreamingUtil streamingUtil;

    /**
     * Default constructor using default configuration.
     * Reads API key from PERPLEXITY_API_KEY environment variable.
     */
    public PerplexityLLMService() {
        this(getDefaultLLMConfig());
    }

    /**
     * Constructor with custom configuration.
     *
     * @param config the LLM configuration
     */
    public PerplexityLLMService(LLMConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("LLMConfig cannot be null");
        }
        this.config = config;
        this.httpClient = createHttpClient();
        this.jsonMapper = new PerplexityJsonMapper();
        this.streamingUtil = new StreamingUtil(new OpenAIJsonMapper()); // Reuse for streaming
    }

    /**
     * Gets the default Perplexity LLM configuration.
     *
     * @return default LLMConfig
     */
    public static LLMConfig getDefaultLLMConfig() {
        return defaultLLMConfig.clone();
    }

    /**
     * Creates the HTTP client with appropriate timeouts.
     *
     * @return configured OkHttpClient
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // ==================== LLMService Interface Methods ====================

    @Override
    public SERVICE_PROVIDER getServiceProvider() {
        return SERVICE_PROVIDER.PERPLEXITY;
    }

    @Override
    public LLMConfig getLLMConfig() {
        return this.config;
    }

    @Override
    public MapModels getRegisteredModels() {
        return config.getRegisteredModelMap();
    }

    @Override
    public MapModels getInstalledModels() throws LLMException {
        // Perplexity doesn't provide a /models endpoint in the public API
        // Return registered models
        return getRegisteredModels();
    }

    @Override
    public String getDefaultModelName() {
        String defaultName = config.getDefaultModelName();
        if (defaultName == null || defaultName.isEmpty()) {
            return DEFAULT_MODEL;
        }
        return defaultName;
    }

    @Override
    public boolean isModelType(String modelName, Model_Type type) {
        Model model = config.getModel(modelName);
        if (model == null) {
            return false;
        }
        return model.isType(type);
    }

    @Override
    public CompletionResponse completion(String systemPrompt, String query, MapParam params) throws LLMException {
        // Merge with defaults
        params = config.mergeWithDefaults(params);

        // Ensure model is set
        if (params.getModel() == null) {
            params.model(getDefaultModelName());
        }

        // Build request payload
        var requestPayload = jsonMapper.toCompletionRequest(systemPrompt, query, params);

        // Execute request
        var responseMap = executeRequest("/chat/completions", requestPayload, false);

        // Parse response
        return jsonMapper.fromChatCompletionResponse(responseMap);
    }

    @Override
    public CompletionResponse chatCompletion(Chat chat, String query, MapParam params) throws LLMException {
        // Merge with defaults
        params = config.mergeWithDefaults(params);

        // Ensure model is set
        if (params.getModel() == null) {
            Object chatModel = chat.getModel();
            if (chatModel != null) {
                params.model(chatModel.toString());
            } else {
                params.model(getDefaultModelName());
            }
        }

        // Build request payload
        var requestPayload = jsonMapper.toChatCompletionRequest(chat, query, params);

        // Execute request
        var responseMap = executeRequest("/chat/completions", requestPayload, false);

        // Parse response
        SearchResponse response = jsonMapper.fromChatCompletionResponse(responseMap);

        // Add response to chat
        if (response.getResponse() != null) {
            String id = response.getId();
        	String content = response.getResponse().getText();
            String reasoning = response.getReasoningContent();

            // update the chat ID for next chat rounds
            chat.setId(id);

            // Create SearchMetadata from response if search data is present
            bor.tools.simplellm.chat.SearchMetadata searchMetadata = null;
            if (response.hasCitations() || response.hasSearchResults() ||
                response.hasRelatedQuestions() || response.hasImages()) {
                searchMetadata = new bor.tools.simplellm.chat.SearchMetadata(response);
            }

            // Add message with appropriate metadata
            if (reasoning != null && searchMetadata != null) {
                chat.addAssistantMessage(content, reasoning, searchMetadata);
            } else if (reasoning != null) {
                chat.addAssistantMessage(content, reasoning);
            } else if (searchMetadata != null) {
                chat.addAssistantMessage(content, searchMetadata);
            } else {
                chat.addAssistantMessage(content);
            }
        }

        return response;
    }

    @Override
    public CompletionResponse completionStream(ResponseStream responseStream, String systemPrompt,
            String query, MapParam params) throws LLMException {
        // Merge with defaults
        params = config.mergeWithDefaults(params);

        // Ensure model is set
        if (params.getModel() == null) {
            params.model(getDefaultModelName());
        }

        // Enable streaming
        params.stream(true);

        // Build request payload
        var requestPayload = jsonMapper.toCompletionRequest(systemPrompt, query, params);

        // Execute streaming request
        Response response = executeStreamingRequest("/chat/completions", requestPayload);

        // Process stream
        Future<CompletionResponse> future = streamingUtil.processStreamingResponse(response, responseStream);
        try {
            return future.get();
        } catch (Exception e) {
            throw new LLMException("Failed to complete streaming: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletionResponse chatCompletionStream(ResponseStream responseStream, Chat chat, String query,
            MapParam params) throws LLMException {
        // Merge with defaults
        params = config.mergeWithDefaults(params);

        // Ensure model is set
        if (params.getModel() == null) {
            Object chatModel = chat.getModel();
            if (chatModel != null) {
                params.model(chatModel.toString());
            } else {
                params.model(getDefaultModelName());
            }
        }

        // Enable streaming
        params.stream(true);

        // Build request payload
        var requestPayload = jsonMapper.toChatCompletionRequest(chat, query, params);

        // Execute streaming request
        Response response = executeStreamingRequest("/chat/completions", requestPayload);

        // Process stream
        Future<CompletionResponse> future = streamingUtil.processStreamingResponse(response, responseStream);
        try {
            SearchResponse searchResponse = (SearchResponse) future.get();
            // Add response to chat
            if (searchResponse.getResponse() != null) {
                String content = searchResponse.getResponse().getText();
                String reasoning = searchResponse.getReasoningContent();

                // Create SearchMetadata from response if search data is present
                bor.tools.simplellm.chat.SearchMetadata searchMetadata = null;
                if (searchResponse.hasCitations() || searchResponse.hasSearchResults() ||
                    searchResponse.hasRelatedQuestions() || searchResponse.hasImages()) {
                    searchMetadata = new bor.tools.simplellm.chat.SearchMetadata(searchResponse);
                }

                // Add message with appropriate metadata
                if (reasoning != null && searchMetadata != null) {
                    chat.addAssistantMessage(content, reasoning, searchMetadata);
                } else if (reasoning != null) {
                    chat.addAssistantMessage(content, reasoning);
                } else if (searchMetadata != null) {
                    chat.addAssistantMessage(content, searchMetadata);
                } else {
                    chat.addAssistantMessage(content);
                }
            }
            return searchResponse;
        } catch (Exception e) {
            throw new LLMException("Failed to complete streaming chat: " + e.getMessage(), e);
        }
    }

    // ==================== WebSearch Interface Methods ====================

    @Override
    public SearchResponse webSearch(String query, MapParam params) throws LLMException {
        return (SearchResponse) completion(null, query, params);
    }

    @Override
    public SearchResponse webSearchChat(Chat chat, String query, MapParam params) throws LLMException {
        return (SearchResponse) chatCompletion(chat, query, params);
    }

    @Override
    public SearchResponse webSearchStream(ResponseStream responseStream, String query, MapParam params)
            throws LLMException {
        CompletionResponse response = completionStream(responseStream, null, query, params);
        return (SearchResponse) response;
    }

    @Override
    public SearchResponse webSearchChatStream(ResponseStream responseStream, Chat chat, String query, MapParam params)
            throws LLMException {
        CompletionResponse response = chatCompletionStream(responseStream, chat, query, params);
        return (SearchResponse) response;
    }

    // ==================== Helper Methods ====================

    /**
     * Executes an HTTP request to the Perplexity API.
     *
     * @param endpoint       the API endpoint (e.g., "/chat/completions")
     * @param requestPayload the request payload as a Map
     * @param streaming      whether this is a streaming request
     * @return response Map
     * @throws LLMException if request fails
     */
    private java.util.Map<String, Object> executeRequest(String endpoint,
            java.util.Map<String, Object> requestPayload,
            boolean streaming) throws LLMException {

        String apiKey = getApiKey();
        String url = config.getBaseUrl() + endpoint;

        // Convert payload to JSON
        String jsonPayload;
        try {
            jsonPayload = jsonMapper.toJson(requestPayload);
            logger.debug("Request to {}: {}", url, jsonPayload);
        } catch (LLMException e) {
            throw new LLMException("Failed to serialize request payload: " + e.getMessage(), e);
        }

        // Build request
        RequestBody body = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new LLMException("Empty response body from Perplexity API");
            }

            String responseJson = responseBody.string();
            logger.debug("Response from {}: {}", url, responseJson);

            return jsonMapper.fromJson(responseJson);

        } catch (IOException e) {
            throw new LLMNetworkException("Network error calling Perplexity API: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a streaming HTTP request to the Perplexity API.
     *
     * @param endpoint       the API endpoint
     * @param requestPayload the request payload as a Map
     * @return Response object for streaming
     * @throws LLMException if request fails
     */
    private Response executeStreamingRequest(String endpoint, java.util.Map<String, Object> requestPayload)
            throws LLMException {

        String apiKey = getApiKey();
        String url = config.getBaseUrl() + endpoint;

        // Convert payload to JSON
        String jsonPayload;
        try {
            jsonPayload = jsonMapper.toJson(requestPayload);
            logger.debug("Streaming request to {}: {}", url, jsonPayload);
        } catch (LLMException e) {
            throw new LLMException("Failed to serialize request payload: " + e.getMessage(), e);
        }

        // Build request
        RequestBody body = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .post(body)
                .build();

        // Execute request
        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }
            return response;
        } catch (IOException e) {
            throw new LLMNetworkException("Network error calling Perplexity API: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the API key from configuration or environment.
     *
     * @return the API key
     * @throws LLMAuthenticationException if API key is not found
     */
    private String getApiKey() throws LLMAuthenticationException {
        String apiKey = config.getApiToken();
        if (apiKey == null || apiKey.isEmpty()) {
            String envVar = config.getApiTokenEnvironment();
            if (envVar != null) {
                apiKey = System.getenv(envVar);
            }
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new LLMAuthenticationException(
                    "Perplexity API key not found. Set PERPLEXITY_API_KEY environment variable or configure apiToken in LLMConfig.");
        }
        return apiKey;
    }

    /**
     * Handles error responses from the API.
     *
     * @param response the error response
     * @throws LLMException appropriate exception based on status code
     */
    private void handleErrorResponse(Response response) throws LLMException {
        int statusCode = response.code();
        String errorBody = "";
        try {
            ResponseBody body = response.body();
            if (body != null) {
                errorBody = body.string();
            }
        } catch (IOException e) {
            // Ignore
        }

        String errorMessage = String.format("Perplexity API error (HTTP %d): %s", statusCode, errorBody);

        switch (statusCode) {
            case 401:
                throw new LLMAuthenticationException("Invalid API key: " + errorMessage);
            case 429:
                throw new LLMRateLimitException("Rate limit exceeded: " + errorMessage);
            case 408:
            case 504:
                throw new LLMTimeoutException("Request timeout: " + errorMessage);
            case 500:
            case 502:
            case 503:
                throw new LLMNetworkException("Perplexity service error: " + errorMessage);
            default:
                throw new LLMException(errorMessage);
        }
    }

    @Override
    public int tokenCount(String text, String model) throws LLMException {
        // Perplexity uses similar tokenization to OpenAI
        // Rough estimation: ~4 characters per token for English
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / 4.0);
    }

    @Override
    public String sumarizeText(String text, String summaryPrompt, MapParam params) throws LLMException {
        String prompt = summaryPrompt==null? "Please provide a concise summary of the following text:\n\n" + text
        			: summaryPrompt + "\n\n" + text;
        CompletionResponse response = completion(null, prompt, params);
        return response.getResponse().getText();
    }

	@Override
	public float[] embeddings(Embeddings_Op op, String texto, MapParam params) throws LLMException {
		throw new UnsupportedOperationException(
                "Perplexity does not support embeddings. Use OpenAI or other embedding services instead.");
	}

	@Override
	public CompletionResponse editImage(byte[] originalImage, String prompt, byte[] maskImage, MapParam params)
	            throws LLMException {
		throw new UnsupportedOperationException(
                "Perplexity does not support image editing.");
	}

	@Override
	public CompletionResponse createImageVariation(byte[] originalImage, MapParam params) throws LLMException {
		throw new UnsupportedOperationException("Perplexity does not support image variations.");	
	}

	@Override
	public CompletionResponse generateImage(String prompt, MapParam params) throws LLMException {
        throw new UnsupportedOperationException(
                "Perplexity does not support image generation. Use DALL-E, Stable Diffusion, or other image generation services.");
    }

	public int tokenCount(String text) throws LLMException {
		return this.tokenCount(text, null);
	}

	@Override
	public Chat sumarizeChat(Chat chat, String summaryPrompt, MapParam params) throws LLMException {
        // Use Perplexity to summarize the chat
        StringBuilder conversationText = new StringBuilder();
        if (chat.getMessages() != null) {
            for (var message : chat.getMessages()) {
                conversationText.append(message.getRole()).append(": ");
                conversationText.append(message.getContent()).append("\n");
            }
        }

        summaryPrompt = summaryPrompt==null? "Please provide a concise summary of the following conversation:\n\n" : 
        			                          summaryPrompt + "\n\n";
        String prompt = summaryPrompt + conversationText.toString();

        CompletionResponse response = completion(null, prompt, params);
        String summary = response.getResponse().getText();
        Chat summarizedChat = new Chat();
        summarizedChat.addAssistantMessage(summary);
        return summarizedChat;
    }

}