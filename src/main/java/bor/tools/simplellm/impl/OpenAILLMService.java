/**
 * 
 */
package bor.tools.simplellm.impl;

import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.BATCH;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.LANGUAGE;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.REASONING;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.RESPONSES_API;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.TOOLS;
import static bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ContentType;
import bor.tools.simplellm.ContentWrapper;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMConfig.Model;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMAuthenticationException;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.exceptions.LLMNetworkException;
import bor.tools.simplellm.exceptions.LLMRateLimitException;
import bor.tools.simplellm.exceptions.LLMTimeoutException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Implementation of the LLMService interface for OpenAI's Large Language Model
 * API.
 * <p>
 * This class provides concrete implementations for all LLM operations including
 * text completion, chat functionality, embeddings generation, and text
 * summarization
 * using OpenAI's API endpoints. It handles authentication, request formatting,
 * response parsing, and error handling according to OpenAI API specifications.
 * </p>
 * <p>
 * The service supports both streaming and non-streaming operations, allowing
 * for real-time response processing or batch operations depending on the use
 * case.
 * </p>
 * All Connections are made using OkHttp3 and JSON internally.
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 * 
 * @see LLMService
 */
public class OpenAILLMService implements LLMService {

	protected static final Map<String, Model> defaultModelMap;

	protected static final LLMConfig defaultLLMConfig;

	static {
		Map<String, Model> map = new LinkedHashMap<>();

		Model text_emb_3_small = new Model("text-embedding-3-small", 8000, EMBEDDING, BATCH);
		Model gpt_5_nano       = new Model("gpt-5-nano", 400000, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_5_mini       =
		            new Model("gpt-5-mini", 400000, REASONING, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_5            =
		            new Model("gpt-5", 400000, REASONING, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_4_1          = new Model("gpt-4.1", 1047576, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_4_mini       =
		            new Model("gpt-4.1-mini", 1047576, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_4o_mini      = new Model("gpt-4o-mini", 128000, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_o3_mini      = new Model("o3-mini", 128000, REASONING, LANGUAGE, CODING, BATCH, TOOLS, RESPONSES_API);

		map.put(gpt_5_nano.getName(), gpt_5_nano);
		map.put(gpt_5_mini.getName(), gpt_5_mini);
		map.put(gpt_5.getName(), gpt_5);
		map.put(gpt_4_1.getName(), gpt_4_1);
		map.put(gpt_4o_mini.getName(), gpt_4o_mini);
		map.put(gpt_4_mini.getName(), gpt_4_mini);
		map.put(text_emb_3_small.getName(), text_emb_3_small);
		map.put(gpt_o3_mini.getName(), gpt_o3_mini);

		// make the defaultModelMap unmodifiable
		defaultModelMap = Map.copyOf(map);

		defaultLLMConfig = LLMConfig.builder()
		            .apiTokenEnvironment("OPENAI_API_KEY")
		            .baseUrl("https://api.openai.com/v1/")
		            .modelMap(defaultModelMap)
		            .build();

	}

	protected LLMConfig config;

	private boolean useResponsesAPI = false;

	private final OkHttpClient     httpClient;
	private final OpenAIJsonMapper jsonMapper;
	private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

	/**
	 * Default constructor for OpenAILLMService.
	 * <p>
	 * Creates a new instance with default configuration settings.
	 * Additional configuration may be required before using the service.
	 * </p>
	 */
	public OpenAILLMService() {
		this(getDefaultLLMConfig());
	}

	/**
	 * Constructor for OpenAILLMService with configuration.
	 * <p>
	 * Creates a new instance with the specified LLM configuration,
	 * including API keys, endpoints, and other service parameters.
	 * </p>
	 *
	 * @param config the LLM configuration containing API settings and parameters
	 */
	public OpenAILLMService(LLMConfig config) {
		this.config = config;
		this.jsonMapper = new OpenAIJsonMapper();
		this.httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
		            .readTimeout(60, TimeUnit.SECONDS)
		            .writeTimeout(30, TimeUnit.SECONDS)
		            .build();
	}

	/**
	 * Retrieves the default LLM configuration for OpenAI services.
	 * <p>
	 * This configuration includes default API endpoints, models definitions, and
	 * other settings optimized for OpenAI's Large Language Model services.
	 * </p>
	 *
	 * @return the default LLMConfig instance for OpenAI
	 */
	public static LLMConfig getDefaultLLMConfig() { return defaultLLMConfig; }

	/**
	 * Core HTTP POST method for making requests to OpenAI API.
	 * 
	 * @param endpoint the API endpoint (relative to baseUrl)
	 * @param payload  the request payload as Map
	 * 
	 * @return response as Map
	 * 
	 * @throws LLMException if request fails
	 */
	protected Map<String, Object> postRequest(String endpoint, Map<String, Object> payload)
	            throws LLMException {
		try {
			String url = config.getBaseUrl().endsWith("/") ? config.getBaseUrl() + endpoint : config.getBaseUrl()
			            + "/"
			            + endpoint;

			String      jsonPayload = jsonMapper.toJson(payload);
			RequestBody body        = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);

			Request request = new Request.Builder().url(url)
			            .header("Authorization",
			                    "Bearer "
			                                + getApiToken())
			            .header("Content-Type", "application/json")
			            .post(body)
			            .build();

			try (Response response = httpClient.newCall(request).execute()) {
				return handleHttpResponse(response);
			}

		} catch (IOException e) {
			throw new LLMNetworkException("Network error during API request: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Handles HTTP response and converts to Map, handling errors appropriately.
	 * 
	 * @param response the HTTP response
	 * 
	 * @return response body as Map
	 * 
	 * @throws LLMException if response indicates error
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object> handleHttpResponse(Response response)
	            throws LLMException {
		try (ResponseBody responseBody = response.body()) {
			if (responseBody == null) {
				throw new LLMException("Empty response from API");
			}

			String responseText = responseBody.string();

			// Handle different HTTP status codes
			switch (response.code()) {
				case 200:
					return jsonMapper.fromJson(responseText);
				case 401:
					throw new LLMAuthenticationException("Authentication failed: Invalid API key");
				case 429:
					throw new LLMRateLimitException("Rate limit exceeded");
				case 408:
				case 504:
					throw new LLMTimeoutException("Request timeout");
				default:
					// Try to parse error response
					try {
						Map<String, Object> errorResponse = jsonMapper.fromJson(responseText);
						Map<String, Object> error         = (Map<String, Object>) errorResponse.get("error");
						if (error != null) {
							String message = (String) error.get("message");
							String type    = (String) error.get("type");
							throw new LLMException(String.format("API Error [%s]: %s", type, message));
						}
					} catch (Exception ignored) {
						// Fall through to generic error
					}
					throw new LLMException("API request failed with status: "
					            + response.code()
					            + " - "
					            + responseText);
			}
		} catch (IOException e) {
			throw new LLMNetworkException("Error reading response: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Gets the API token from configuration.
	 * 
	 * @return the API token
	 * 
	 * @throws LLMException if token is not available
	 */
	protected String getApiToken()
	            throws LLMException {
		String token = config.getApiToken();
		if (token == null || token.trim().isEmpty()) {
			// Try environment variable
			String envVar = config.getApiTokenEnvironment();
			if (envVar != null) {
				token = System.getenv(envVar);
			}
		}

		if (token == null || token.trim().isEmpty()) {
			throw new LLMAuthenticationException(
			            "API token not configured. Set OPENAI_API_TOKEN environment variable or provide token in config.");
		}

		token = token.trim();
		config.setApiToken(token); // Cache the trimmed token

		return token;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Retrieves the list of registered models through LLMConfig.
	 * </p>
	 */
	@Override
	public List<Model> models()
	            throws LLMException {
		return config.getModelMap().values().stream().toList();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Generates embeddings using OpenAI's embedding models such as
	 * text-embedding-3-small.
	 * </p>
	 */
	@Override
	public float[] embeddings(String texto, String model, Integer vecSize)
	            throws LLMException {

		if (texto == null || texto.trim().isEmpty()) {
			throw new LLMException("Text cannot be null or empty");
		}

		// Use default model if not specified
		if (model == null || model.trim().isEmpty()) {
			model = "text-embedding-3-small";
		}

		try {
			// Create request payload
			Map<String, Object> payload = jsonMapper.toEmbeddingsRequest(texto.trim(), model, vecSize);

			// Make API request
			Map<String, Object> response = postRequest("embeddings", payload);

			// Convert response to float array
			return jsonMapper.fromEmbeddingsResponse(response);

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during embeddings generation: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Performs text completion using OpenAI's completion or chat completion
	 * endpoints
	 * depending on the model specified in the parameters.
	 * </p>
	 * Notes:
	 * <p>
	 * If using OpenAI official endpoints, it uses the responses endpoint. <br>
	 * Example for response using CURL:
	 * 
	 * <pre>
	 * curl "https://api.openai.com/v1/responses" \
	 * -H "Content-Type: application/json" \
	 * -H "Authorization: Bearer $OPENAI_API_KEY" \
	 * -d '{
	 *   "model": "gpt-5",
	 *   "input": "Write a one-sentence bedtime story about a unicorn."
	 *  }'
	 * </pre>
	 * </p>
	 * <li>If using other endpoints, it uses the chatCompletions endpoint.
	 */
	@Override
	public CompletionResponse completion(String system, String query, MapParam params)
	            throws LLMException {

		if (query == null || query.trim().isEmpty()) {
			throw new LLMException("Query cannot be null or empty");
		}

		boolean isOpenAIEndpoint = isOpenAIEndpoint();

		// Check if we should use the responses endpoint (for newer models like gpt-5)
		String model = params != null ? (String) params.get("model") : "gpt-4o-mini";
		if (model == null || model.trim().isEmpty()) {
			model = "gpt-4o-mini";
		}

		// Check if model supports responses API
		boolean useResponsesAPI = this.useResponsesAPI && isOpenAIEndpoint && isResponsesAPIModel(model);

		if (useResponsesAPI) {
			// Use responses endpoint for compatible models
			MapParam paramsAPI = convert2ResponseAPI(params);
			return completionWithResponsesAPI(system, query, paramsAPI, model);
		} else {
			// Use chat completions endpoint (recommended approach)
			Chat chat = new Chat();
			if (system != null && !system.trim().isEmpty()) {
				chat.addSystemMessage(system.trim());
			}
			return chatCompletion(chat, query, params);
		}
	}

	/**
	 * Checks if a model supports the responses API.
	 * 
	 * @param model the model name
	 * 
	 * @return true if the model supports responses API
	 */
	protected boolean isResponsesAPIModel(String model) {
		if (model == null) {
			return false;
		}

		// Check if the model is configured with RESPONSES_API capability
		Model modelConfig = config.getModelMap().get(model);
		if (modelConfig != null) {
			return modelConfig.getTypes().contains(RESPONSES_API);
		}

		// Fall back to name-based detection for known models
		return model.startsWith("gpt-5") || model.startsWith("o3")
		       || model.equals("o1-preview")
		       || model.equals("o1-mini");
	}

	/**
	 * Convert parameters to those supported by the responses API.
	 * Exemple:
	 * max_tokens -> max_output_tokens, temperature, top_p
	 * 
	 * @param params
	 * 
	 * @return
	 */
	protected MapParam convert2ResponseAPI(MapParam params) {
		if (params == null) {
			return null;
		}
		MapParam newParams = new MapParam();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String key   = entry.getKey();
			Object value = entry.getValue();

			// Include relevant parameters for responses API
			switch (key.toLowerCase()) {

				case "max_tokens":
					newParams.put("max_output_tokens", value);
					break;

				case "prompt":
					newParams.put("input", value);
					break;

				case "temperature":
					// newParams.put(key, value);
					break;
			}
		}
		return newParams;
	}

	/**
	 * Performs completion using OpenAI's responses endpoint.
	 * 
	 * @param system the system prompt
	 * @param query  the user query
	 * @param params additional parameters
	 * @param model  the model to use
	 * 
	 * @return CompletionResponse
	 * 
	 * @throws LLMException if request fails
	 */
	protected CompletionResponse completionWithResponsesAPI(String system, String query, MapParam params, String model)
	            throws LLMException {

		Map<String, Object> payload = new HashMap<>();
		payload.put("model", model);

		// Combine system and user input
		StringBuilder input = new StringBuilder();
		if (system != null && !system.trim().isEmpty()) {
			input.append(system.trim()).append("\n\n");
		}
		input.append(query.trim());

		payload.put("input", input.toString());

		// Add parameters from MapParam
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();

				// Include relevant parameters for responses API
				switch (key.toLowerCase()) {
					case "temperature":
					case "max_tokens":
					case "top_p":
						payload.put(key, value);
						break;
				}
			}
		}

		try {
			// Make API request to responses endpoint
			Map<String, Object> response = postRequest("responses", payload);

			// Parse response - responses API has different format than chat completions
			CompletionResponse completionResponse = parseResponsesAPIResponse(response);

			return completionResponse;

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during completion: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Parses a response from the responses API endpoint.
	 * 
	 * @param response the API response
	 * 
	 * @return CompletionResponse object
	 * 
	 * @throws LLMException if parsing fails
	 */
	@SuppressWarnings("unchecked")
	protected CompletionResponse parseResponsesAPIResponse(Map<String, Object> response)
	            throws LLMException {
		CompletionResponse completionResponse = new CompletionResponse();

		try {
			// Responses API format might be different - adapt as needed
			String content = (String) response.get("response");
			if (content == null) {
				// Try alternative field names
				content = (String) response.get("output");
				if (content == null) {
					content = (String) response.get("text");
				}
			}

			if (content != null) {
				completionResponse.setResponse(new ContentWrapper(ContentType.TEXT, content));
			}

			// Extract metadata
			Map<String, Object> info = new HashMap<>();
			info.putAll(response);
			completionResponse.setInfo(info);

			// Set default end reason
			completionResponse.setEndReason("stop");

		} catch (Exception e) {
			throw new LLMException("Failed to parse responses API response: "
			            + e.getMessage(), e);
		}

		return completionResponse;
	}

	/**
	 * Performs a chat completion within an existing chat session.
	 * <p>
	 * This method continues a conversation by adding the user's query to the
	 * existing
	 * chat context and generating a response using OpenAI's chat completion
	 * endpoint.
	 * </p>
	 *
	 * @param chat   the chat session containing conversation history
	 * @param query  the user's input or question
	 * @param params additional parameters such as temperature, max_tokens, etc.
	 * 
	 * @return a CompletionResponse containing the generated response and metadata
	 * 
	 * @throws LLMException if there's an error during chat completion
	 */
	@Override
	public CompletionResponse chatCompletion(Chat chat, String query, MapParam params)
	            throws LLMException {

		if (chat == null) {
			throw new LLMException("Chat session cannot be null");
		}

		// Determine model to use
		String model = chat.getModel();
		if (model == null || model.trim().isEmpty()) {
			// Use default model from params or fall back to gpt-4o-mini
			model = params != null ? (String) params.get("model") : null;
			if (model == null || model.trim().isEmpty()) {
				model = "gpt-4o-mini";
			}
		}

		if (params != null && isOpenAIEndpoint() && isResponsesAPIModel(model)) {
			params.replaceKeys("max_tokens", "max_completion_tokens");
		}

		// Create request payload
		Map<String, Object> payload = jsonMapper.toChatCompletionRequest(chat, query, params, model);

		try {
			// Make API request
			Map<String, Object> response = postRequest("chat/completions", payload);

			// Convert response
			CompletionResponse completionResponse = jsonMapper.fromChatCompletionResponse(response);
			completionResponse.setChatId(chat.getId());

			// Add the user query to chat if provided
			if (query != null && !query.trim().isEmpty()) {
				chat.addUserMessage(query.trim());
			}

			// Add assistant response to chat
			if (completionResponse.getResponse() != null && completionResponse.getResponse().getText() != null) {
				chat.addAssistantMessage(completionResponse.getResponse().getText());
			}

			return completionResponse;

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during chat completion: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Check if the configured endpoint is an OpenAI official endpoint.
	 * 
	 * @return
	 */
	protected boolean isOpenAIEndpoint() { return config.getBaseUrl().toLowerCase().contains("openai"); }

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses OpenAI's tokenization to count tokens, ensuring accurate counting
	 * for the specified model's tokenizer.
	 * </p>
	 */
	@Override
	public int tokenCount(String text, String model)
	            throws LLMException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Summarizes chat conversations using OpenAI's completion models,
	 * preserving conversation context while reducing token usage.
	 * </p>
	 */
	@Override
	public Chat sumarizeChat(Chat chat, String summaryPrompt, MapParam params)
	            throws LLMException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Summarizes text using OpenAI's completion models with the provided
	 * summarization prompt and parameters.
	 * </p>
	 */
	@Override
	public String sumarizeText(String text, String summaryPrompt, MapParam params)
	            throws LLMException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Provides streaming text completion using OpenAI's server-sent events (SSE)
	 * for real-time response generation.
	 * </p>
	 */
	@Override
	public CompletionResponse completionStream(ResponseStream stream, String system, String query, MapParam params)
	            throws LLMException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Provides streaming chat completion using OpenAI's server-sent events (SSE)
	 * for real-time conversation response generation.
	 * </p>
	 */
	@Override
	public CompletionResponse chatCompletionStream(ResponseStream stream, Chat chat, String query, MapParam params)
	            throws LLMException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LLMConfig getLLMConfig() { return this.config; }

}
