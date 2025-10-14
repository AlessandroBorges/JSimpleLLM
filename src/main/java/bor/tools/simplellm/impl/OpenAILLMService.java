/**
 * 
 */
package bor.tools.simplellm.impl;

import static bor.tools.simplellm.Model_Type.BATCH;
import static bor.tools.simplellm.Model_Type.CODING;
import static bor.tools.simplellm.Model_Type.EMBEDDING;
import static bor.tools.simplellm.Model_Type.EMBEDDING_DIMENSION;
import static bor.tools.simplellm.Model_Type.FAST;
import static bor.tools.simplellm.Model_Type.GPT5_CLASS;
import static bor.tools.simplellm.Model_Type.IMAGE;
import static bor.tools.simplellm.Model_Type.LANGUAGE;
import static bor.tools.simplellm.Model_Type.REASONING;
import static bor.tools.simplellm.Model_Type.RESPONSES_API;
import static bor.tools.simplellm.Model_Type.TOOLS;
import static bor.tools.simplellm.Model_Type.VISION;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.ModelEmbedding;
import bor.tools.simplellm.ModelEmbedding.Embeddings_Op;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.ContentWrapper;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMAuthenticationException;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.exceptions.LLMIllegalArgumentException;
import bor.tools.simplellm.exceptions.LLMNetworkException;
import bor.tools.simplellm.exceptions.LLMRateLimitException;
import bor.tools.simplellm.exceptions.LLMTimeoutException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	Logger logger = LoggerFactory.getLogger(OpenAILLMService.class.getName());

	protected static final String DEFAULT_PROMPT =
	            "You are a helpful assistant that follow the instructions carefully. "
	                        + "If you don't know the answer, just say that you don't know. "
	                        + "Don't try to make up an answer.";

	private static final String DEFAULT_MODEL = "gpt-4.1-mini";

	private static final LLMConfig defaultLLMConfig;

	static {
		MapModels map = new MapModels();

		Model text_emb_3_small =
		            new ModelEmbedding("text-embedding-3-small", "3-small", 8000, EMBEDDING, EMBEDDING_DIMENSION, BATCH);
		Model text_emb_3_large =
		            new ModelEmbedding("text-embedding-3-large", "3-large", 8000, EMBEDDING, EMBEDDING_DIMENSION, BATCH);
		Model text_emb_2_ADA   = new ModelEmbedding("text-embedding-ada-002", "ada-002", 8000, EMBEDDING, BATCH);

		Model gpt_5_nano  = new Model("gpt-5-nano",
		            400000,
		            GPT5_CLASS,
		            LANGUAGE,
		            FAST,
		            VISION,
		            CODING,
		            BATCH,
		            TOOLS,
		            RESPONSES_API);
		Model gpt_5_mini  = new Model("gpt-5-mini",
		            400000,
		            GPT5_CLASS,
		            REASONING,
		            FAST,
		            LANGUAGE,
		            VISION,
		            CODING,
		            BATCH,
		            TOOLS,
		            RESPONSES_API);
		Model gpt_5       = new Model("gpt-5",
		            400000,
		            GPT5_CLASS,
		            REASONING,
		            LANGUAGE,
		            VISION,
		            CODING,
		            BATCH,
		            TOOLS,
		            RESPONSES_API);
		Model gpt_4_1     = new Model("gpt-4.1", 1047576, LANGUAGE, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_4_mini  =
		            new Model("gpt-4.1-mini", 1047576, LANGUAGE, FAST, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_4o_mini = new Model("gpt-4o-mini", 128000, LANGUAGE, FAST, VISION, CODING, BATCH, TOOLS, RESPONSES_API);
		Model gpt_o3_mini = new Model("o3-mini",
		            128000,
		            GPT5_CLASS,
		            REASONING,
		            FAST,
		            LANGUAGE,
		            CODING,
		            BATCH,
		            TOOLS,
		            RESPONSES_API);

		// Image Generation Models (DALL-E)
		Model dall_e_3 = new Model("dall-e-3", 4000, IMAGE); // 4000 character prompt limit
		Model dall_e_2 = new Model("dall-e-2", 1000, IMAGE); // 1000 character prompt limit

		map.add(gpt_4o_mini);
		map.add(gpt_4_mini);
		map.add(gpt_4_1);		
		
		map.add(gpt_5_nano);
		map.add(gpt_5_mini);
		map.add(gpt_5);

		map.add(gpt_o3_mini);
		map.add(dall_e_3);
		map.add(dall_e_2);
		
		// embedding models
		map.add(text_emb_3_small);
		map.add(text_emb_2_ADA);
		map.add(text_emb_3_large);

		defaultLLMConfig = LLMConfig.builder()
		            .apiTokenEnvironment("OPENAI_API_KEY")
		            .baseUrl("https://api.openai.com/v1/")
		            .modelMap(map)
		            .build();
	}

	protected LLMConfig config;

	protected boolean useResponsesAPI = false;

	protected final OkHttpClient     httpClient;
	protected final OpenAIJsonMapper jsonMapper;
	protected final StreamingUtil    streamingUtil;
	protected static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

	private static final String PROMPT_SUMMARY = null;

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
		if(config==null)
			config = getDefaultLLMConfig();
		this.config = config;
		this.jsonMapper = new OpenAIJsonMapper();
		this.streamingUtil = new StreamingUtil(this.jsonMapper);
		this.httpClient = new OkHttpClient.Builder()
		            .connectTimeout(30, TimeUnit.SECONDS)
		            .readTimeout(300, TimeUnit.SECONDS)  // Longer timeout for streaming
		            .writeTimeout(30, TimeUnit.SECONDS)
		            .callTimeout(300, TimeUnit.SECONDS)  // Overall call timeout for long streams
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
	protected Map<String, Object> postRequest(String endpoint, Map<String, Object> payload) throws LLMException {
		try {
			String      url         = buildUrl(endpoint);
			String      jsonPayload = jsonMapper.toJson(payload);
			RequestBody body        = RequestBody.create(jsonPayload, JSON_MEDIA_TYPE);

			Request request = new Request.Builder().url(url)
			            .header("Authorization", "Bearer " + getApiToken())
			            .header("Content-Type", "application/json")
			            .post(body)
			            .build();

			try (Response response = httpClient.newCall(request).execute()) {
				return handleHttpResponse(response);
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Network error during API request: {}", e.getMessage(), e);
			throw new LLMNetworkException("Network error during API request: "
			            + e.getMessage(), e);
		}
	}
	
	/**
	 * Performs a HTTP GET request to the specified endpoint.
	 * @param endpoint
	 * @return
	 * @throws LLMException
	 */
	protected Map<String,Object> getRequest(String endpoint) throws LLMException {
		try {
			String  url     = buildUrl(endpoint);
			Request request = new Request.Builder().url(url)
			            .header("Authorization",
			                    "Bearer "
			                                + getApiToken())
			            .header("Content-Type", "application/json")
			            .get()
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
	 * Builds the full URL for the given endpoint.
	 * 
	 * @param endpoint the API endpoint (relative to baseUrl)
	 * 
	 * @return the full URL as a string
	 */
	private String buildUrl(String endpoint) {
	    String baseUrl = config.getBaseUrl();
	    
	    // Remove trailing slash from baseUrl if present
	    if (baseUrl.endsWith("/")) {
	        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
	    }
	    
	    // Ensure endpoint starts with /
	    if (!endpoint.startsWith("/")) {
	        endpoint = "/" + endpoint;
	    }	    
	    endpoint = endpoint.replaceAll("//+", "/"); // Remove double slashes in endpoint	    
	    return baseUrl + endpoint;
	}

	/**
	 * Retrieves the list of models currently installed and available in the server.<br>
	 * 
	 * It performs a HTPP GET to /models endpoint.
	 * 
	 * 
	 * @return Map of model names to Model objects
	 * @throws LLMException
	 */
	public MapModels getInstalledModels() throws LLMException {
		try {
			var map = getRequest("/models");			
			var models = jsonMapper.fromModelsRequest(map);	
			MapModels installed = new MapModels();
			installed.putAll(models);
			return installed;
		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error retrieving models: "
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
	protected Map<String, Object> handleHttpResponse(Response response) throws LLMException {
		try (ResponseBody responseBody = response.body()) {
			if (responseBody == null) {
				throw new LLMException("Empty response from API");
			}

			String responseText = responseBody.string();

			// Handle different HTTP status codes
			switch (response.code()) {
				case 200:{
					Map<String,Object> map=null;
					// response can be a JSON or a plain text (for images) 
					if(response.header("Content-Type","").contains("application/json")==true) {
						map = jsonMapper.fromJson(responseText)	;				
						map.put("response", responseText);
						return map;
					}else {		
						map = new LinkedHashMap<>();
						map.put("data", responseText);
						return map;
					}
					
					
				}
				case 401:
					throw new LLMAuthenticationException("Authentication failed: Invalid API key.\n\t"
					            + responseText
					            + "\n");
				case 429:
					throw new LLMRateLimitException("Rate limit exceeded. \n\t"
					            + responseText
					            + "\n");
				case 408:
				case 504:
					throw new LLMTimeoutException("Request timeout.\n\t"
					            + responseText
					            + "\n");
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
					throw new LLMException("API request failed with status: \n\t"
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
	protected String getApiToken() throws LLMException {
		String token = config.getApiToken();
		if (token == null || token.trim().isEmpty()) {
			// Try environment variable
			String envVar = config.getApiTokenEnvironment();
			if (envVar != null) {
				token = System.getenv(envVar);
			}
		}

		if (token == null || token.trim().isEmpty()) {
			throw new LLMAuthenticationException("API token not configured. "
			            + "Set OPENAI_API_KEY environment variable or provide token in config.");
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
	public List<Model> getRegisteredModels() throws LLMException {
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
	public float[] embeddings(Embeddings_Op op, String texto, MapParam params) throws LLMException {

		if (texto == null || texto.trim().isEmpty()) {
			throw new LLMException("Text cannot be null or empty");
		}
		if(params==null || params.isEmpty()) {
		  throw new LLMException("params cannot be null or empty");
		}		
		
        Object modelObj = params.getModel();
        if(modelObj==null) {
			throw new LLMException("Model must be specified in parameters for embeddings.");
		}
        
        Model model = (Model) getLLMConfig().getModel(modelObj.toString());
        
		// Use default model if not specified
		if (model == null || model.toString().trim().isEmpty()) {
			throw new LLMException("Model must be specified in parameters for embeddings.");
		}

		if (isModelType(model, EMBEDDING) == false) {
			throw new LLMException("Embeddings not supported for model "
			            + model
			            + ". Use a dedicated embedding model like text-embedding-3-small.");
		}
		
		Integer dimensions = params.getDimension();	
		if (isModelType(model, EMBEDDING_DIMENSION) == false && dimensions != null) {
			throw new LLMException("Model "
			            + model
			            + " does not support custom embedding dimensions.");
		}

		String encodingFormat = null;
		if (isOpenAIEndpoint()) {
			encodingFormat = "base64";
		}

		try {
			if (model instanceof ModelEmbedding) {
				ModelEmbedding me = (ModelEmbedding) model;
				texto = me.applyOperationPrefix(op, texto);
			} else {
				logger.warn("Model {} is not a ModelEmbedding instance", model);
			}

			// Create request payload
			Map<String, Object> payload = jsonMapper.toEmbeddingsRequest(texto.trim(), model, dimensions, encodingFormat);

			// Make API request
			Map<String, Object> response = postRequest("embeddings", payload);

			// Convert response to float array
			return jsonMapper.fromEmbeddingsResponse(response, dimensions);

		} catch (LLMException e) {
			logger.error("Error generating embeddings: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected error generating embeddings: {}", e.getMessage(), e);
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
	public CompletionResponse completion(String system, String query, MapParam params) throws LLMException {

		if (system==null && (query == null || query.trim().isEmpty())) {
			throw new LLMException("Query cannot be null or empty");
		}
		params = fixParams(params);

		// Use chat completions endpoint (recommended approach)
		Chat chat = new Chat();
		if (system != null && !system.trim().isEmpty()) {
			chat.addSystemMessage(system.trim());
		}
		return chatCompletion(chat, query, params);
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
			return modelConfig.getTypes().contains(RESPONSES_API) || modelConfig.isType(GPT5_CLASS);
		}
		return false;
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
	protected CompletionResponse parseResponsesAPIResponse(Map<String, Object> response) throws LLMException {
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
			MapParam info = new MapParam();
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
	public CompletionResponse chatCompletion(Chat chat, String query, MapParam params) throws LLMException {

		if (chat == null) {
			throw new LLMException("Chat session cannot be null");
		}

		var checkedParams = checkParams(chat, params);
		if(params.isStream()==Boolean.TRUE) {
			params.stream(null); // ensureStreaming is disabled
		}
		// Create request payload
		Map<String, Object> payload = jsonMapper.toChatCompletionRequest(chat, query, checkedParams);

		try {
			// Make API request
			Map<String, Object> response = postRequest("chat/completions", payload);
			// Convert response
			CompletionResponse completionResponse = jsonMapper.fromChatCompletionResponse(response);

			if (chat.getId() == null) {
				chat.setId(completionResponse.getChatId());
			} else {
				completionResponse.setChatId(chat.getId());
			}
			// Add the user query to chat if provided
			if (chat != null && query != null) {
				chat.addUserMessage(query.trim());
			}

			// Add assistant response to chat
			var responseMessage = completionResponse.getResponse();
			if (responseMessage != null && responseMessage.getText() != null) {
				String respText  = responseMessage.getText().trim();
				String reasoning = completionResponse.getReasoningContent();
				if (reasoning != null) {
					chat.addAssistantMessage(respText, reasoning);					
					completionResponse.setReasoningEffort(params.getReasoningEffort());
				} else
					chat.addAssistantMessage(respText);
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
	 * Fix params, assuming Chat is null.
	 * 
	 * @param params parameters to test.
	 * @return
	 */
	protected MapParam fixParams(MapParam params) throws LLMException {	
		return fixParams(params, null);
	}
	/**
	 * Fix MapParams for specific endpoints and models.
	 * 
	 * @param params the parameters to fix, may be null
	 * @param chat Chat instance, to help fix some parameters
	 */
	protected MapParam fixParams(MapParam params, Chat chat) throws LLMException {
		if (params == null) {
			params = new MapParam();
		}
        
		if(chat != null && params.getModel()==null && chat.getModel()!=null) {
			params.model(chat.getModel().toString());
		}
		
		// Ensure model is properly set
		Model model = resolveModel(params);
		params.model(model);
		
		if(chat!=null && chat.getModel()==null) {
			chat.setModel(model);
		}

		// Apply OpenAI-specific parameter adjustments
		if (requiresOpenAIParameterAdjustment(model)) {
			adjustParametersForOpenAI(params);
		}
		
		return params;
	}

	/**
	 * Resolves the model name from parameters or uses default.
	 * 
	 * @param params the parameters containing potential model information
	 * 
	 * @return the resolved model name
	 */
	protected Model resolveModel(MapParam params) throws LLMException {
		Object modelObj = params.getModel();
		if (modelObj instanceof Model)
			return (Model) modelObj;
		else {
			String modelName = modelObj != null ? modelObj.toString() : getDefaultModelName();
			Model  m         = getLLMConfig().getModel(modelName);
			if (m == null) {
				logger.warn("Model {} not found in configuration, using default model {}",
				            modelName,
				            getDefaultModelName());
				m = getLLMConfig().getModel(getDefaultModelName());
			}
			if (m == null) {
				throw new LLMIllegalArgumentException ("Model "
				            + modelName
				            + " not found in configuration, and default model "
				            + getDefaultModelName()
				            + " also not found.");
			}
			return m;
		}

	}

	/**
	 * Checks if the model requires OpenAI-specific parameter adjustments.
	 * 
	 * @param modelName the model name to check
	 * 
	 * @return true if adjustments are needed
	 */
    boolean requiresOpenAIParameterAdjustment(Model model) {
		if (!isOpenAIEndpoint()) {
			return false;
		}		
		return isResponsesAPIModel(model.toString()) || (model != null && model.isType(GPT5_CLASS));
	}

	/**
	 * Applies OpenAI-specific parameter adjustments.
	 * 
	 * @param params the parameters to adjust
	 */
	private void adjustParametersForOpenAI(MapParam params) {
		params.replaceKeys("max_tokens", "max_completion_tokens");
		params.remove("temperature");
	}

	/**
	 * Check if the configured endpoint is an OpenAI official endpoint.
	 * 
	 * @return
	 */
	protected boolean isOpenAIEndpoint() { 
		return config.getBaseUrl().toLowerCase().contains("openai.com"); 
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Uses OpenAI's tokenization to count tokens, ensuring accurate counting
	 * for the specified model's tokenizer.
	 * </p>
	 */
	@Override
	public int tokenCount(String text, String model) throws LLMException {
		if (text == null) {
			return 0;
		}

		// Normalize line endings
		text = text.replace("\r\n", "\n").replace("\r", "\n").replace("\n\n", "\n");

		// Use default model if not specified
		if (model == null || model.trim().isEmpty()) {
			model = "gpt-4o-mini";
		}

		// Get encoding for the model
		Encoding encoding = getEncodingForModel(model);
		if (encoding == null) {
			throw new LLMException("Unsupported model for token counting: "
			            + model);
		}

		// Encode the text and count the tokens
		return encoding.encode(text).size();
	}

	/**
	 * Gets the encoding instance for a specific model.
	 * 
	 * @param model the model name
	 * 
	 * @return the Encoding instance, or null if not found
	 */
	protected Encoding getEncodingForModel(String model) {
		try {
			// Get the encoding registry
			EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();

			// Map OpenAI models to their appropriate encodings
			if (model.startsWith("gpt-4") || model.startsWith("gpt-3.5") || model.startsWith("text-")) {
				return registry.getEncoding("cl100k_base").orElse(null);
			}

			// For newer models (GPT-5, O3, etc.)
			if (model.startsWith("gpt-5") || model.startsWith("o3") || model.startsWith("o1")) {
				return registry.getEncoding("o200k_base").orElse(registry.getEncoding("cl100k_base").orElse(null));
			}

			// For embedding models
			if (model.contains("embedding")) {
				return registry.getEncoding("cl100k_base").orElse(null);
			}

			// Default fallback to cl100k_base (used by most modern OpenAI models)
			return registry.getEncoding("cl100k_base").orElse(null);

		} catch (Exception e) {
			// Fallback to a safe default
			try {
				return Encodings.newDefaultEncodingRegistry().getEncoding("cl100k_base").orElse(null);
			} catch (Exception fallbackException) {
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Summarizes chat conversations using OpenAI's completion models,
	 * preserving conversation context while reducing token usage.
	 * </p>
	 */
	@Override
	public Chat sumarizeChat(Chat chat, String summaryPrompt, MapParam params) throws LLMException {
		Chat summ     = new Chat();
		var  messages = chat.getMessages();
		for (Message message : messages) {
			if (message.getRole() == MessageRole.SYSTEM || message.getRole() == MessageRole.DEVELOPER) {
				summ.addMessage(message);
				continue;
			}
			// Only summarize user and assistant messages
			if (message.getContent().getType() == ContentType.TEXT) {
				String  txt      = message.getText();
				String  summy    = sumarizeText(summaryPrompt, txt, params);
				Message nMessage = new Message(message.getRole(), summy);
				summ.addMessage(nMessage);
				continue;
			} else {
				// keep non-text messages as is
				summ.addMessage(message);
			}
		}
		return summ;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Summarizes text using OpenAI's completion models with the provided
	 * summarization prompt and parameters.
	 * </p>
	 */
	@Override
	public String sumarizeText(String summaryPrompt, String text, MapParam params) throws LLMException {
		if (summaryPrompt == null || summaryPrompt.isEmpty()) {
			summaryPrompt = PROMPT_SUMMARY;
		}
		CompletionResponse res = this.completion(summaryPrompt, text, params);
		return res.getText();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Provides streaming text completion using OpenAI's server-sent events (SSE)
	 * for real-time response generation.
	 * </p>
	 * <p>
	 * This implementation uses true real-time streaming that processes data as it
	 * arrives from the network, calling onToken() immediately when chunks are received.
	 * </p>
	 */
	@Override
	public CompletionResponse completionStream(ResponseStream stream, String system, String query, MapParam params)
	            throws LLMException {

		if (query == null || query.trim().isEmpty()) {
			throw new LLMException("Query cannot be null or empty");
		}

		if (stream == null) {
			throw new LLMException("ResponseStream cannot be null");
		}

		params = fixParams(params);
		// Create a temporary chat for the completion
		Chat tempChat = new Chat();
		if (system != null && !system.trim().isEmpty()) {
			tempChat.addSystemMessage(system);
		}
		tempChat.addUserMessage(query);

		// Use chatCompletionStream for the actual streaming
		return chatCompletionStream(stream, tempChat, null, params);
	}
	
	/**
	 * Check if all params are OK
	 * @param chat
	 * @return
	 */
	protected MapParam checkParams(Chat chat, MapParam params) throws LLMException {	
		params = fixParams(params, chat);			
		Model model = (Model) params.getModel();
			
		// Create parameters copy and enable streaming
		MapParam checkedParams = new MapParam(params);		
			
		// apply reasoning effort if model supports it
		if(model.isTypeReasoning() && params.getReasoningEffort()!=null) {
			checkedParams.reasoningEffort(params.getReasoningEffort()); 
		}else {
			checkedParams.reasoningEffort(null);
		 }		
		return checkedParams;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Provides streaming chat completion using OpenAI's server-sent events (SSE)
	 * for real-time conversation response generation.
	 * </p>
	 * <p>
	 * This implementation uses true real-time streaming that processes data as it
	 * arrives from the network, calling onToken() immediately when chunks are received.
	 * </p>
	 */
	@Override
	public CompletionResponse chatCompletionStream(ResponseStream stream, Chat chat, String query, MapParam params)
	            throws LLMException {

		if (chat == null) {
			throw new LLMException("Chat session cannot be null");
		}
		if (stream == null) {
			throw new LLMException("ResponseStream cannot be null");
		}		
		
        var checkedParams = checkParams(chat, params); 
        checkedParams.put("stream", true); // Ensure streaming is enabled
		
		// Create request payload
		Map<String, Object> payload = jsonMapper.toChatCompletionRequest(chat, query, checkedParams);

		try {
			// Make streaming HTTP request
			String  requestBody = jsonMapper.toJson(payload);
			Request request     = new Request.Builder().url(config.getBaseUrl()
			            + "chat/completions")
			            .header("Authorization",
			                    "Bearer "
			                                + getApiToken())
			            .header("Content-Type", "application/json")
			            .header("Accept", "text/event-stream")
			            .post(okhttp3.RequestBody.create(requestBody, JSON_MEDIA_TYPE))
			            .build();

			Response response = httpClient.newCall(request).execute();

			if (!response.isSuccessful()) {
				String errorBody = response.body() != null ? response.body().string() : "Unknown error";
				throw new LLMException("Streaming request failed with status: "
				            + response.code()
				            + " - "
				            + errorBody);
			}

			// Process streaming response using real-time streaming
			java.util.concurrent.Future<CompletionResponse> future =
			            streamingUtil.processStreamingResponse(response, stream);

			// Wait for completion and return final response
			CompletionResponse finalResponse = future.get();

			// Add the query and response to chat history if query was provided
			if (query != null && !query.trim().isEmpty()) {
				chat.addUserMessage(query);
			}

			if (finalResponse.getResponse() != null) {
				String assistantResponse = finalResponse.getResponse().getText();
				if (assistantResponse != null && !assistantResponse.trim().isEmpty()) {
					chat.addAssistantMessage(assistantResponse);
				}
			}

			// Set chat ID in response
			finalResponse.setChatId(chat.getId());

			return finalResponse;

		} catch (Exception e) {
			if (e instanceof LLMException) {
				throw (LLMException) e;
			}
			throw new LLMException("Failed to process streaming chat completion: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LLMConfig getLLMConfig() { return this.config; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDefaultModelName() {
		var txt = getLLMConfig().getModelMap().getModel(DEFAULT_MODEL);

		return txt != null ? txt.getName() : DEFAULT_MODEL;
	}

	// ================== IMAGE GENERATION METHODS ==================

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionResponse generateImage(String prompt, MapParam params) throws LLMException {
		if (prompt == null || prompt.trim().isEmpty()) {
			throw new LLMException("Image generation prompt cannot be null or empty");
		}

		params = fixParams(params);

		// Ensure we have an image generation model
		String model = findModel(params);
		if (model != null && !isModelType(model, Model_Type.IMAGE)) {
			// Try to find a suitable image generation model
			Model imageModel = findModel(Model_Type.IMAGE);
			if (imageModel != null) {
				params.put("model", imageModel.getName());
			} else {
				throw new LLMException("No image generation model available. Please configure a DALL-E model.");
			}
		}

		try {
			// Create request payload
			Map<String, Object> payload = jsonMapper.toImageGenerationRequest(prompt, params);

			// Make API request to images/generations endpoint
			Map<String, Object> response = postRequest("/images/generations", payload);

			// Convert response
			return jsonMapper.fromImageGenerationResponse(response);

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during image generation: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionResponse editImage(byte[] originalImage, String prompt, byte[] maskImage, MapParam params)
	            throws LLMException {
		if (originalImage == null || originalImage.length == 0) {
			throw new LLMException("Original image data cannot be null or empty");
		}
		if (prompt == null || prompt.trim().isEmpty()) {
			throw new LLMException("Image edit prompt cannot be null or empty");
		}

		params = fixParams(params);

		try {
			// Create request payload (note: actual multipart handling would be in
			// postMultipartRequest)
			Map<String, Object> payload = jsonMapper.toImageEditRequest(originalImage, prompt, maskImage, params);

			// Make API request to images/edits endpoint
			// Note: This would require a special multipart request method
			Map<String, Object> response = postMultipartRequest("/images/edits", payload);

			// Convert response
			return jsonMapper.fromImageGenerationResponse(response);

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during image editing: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionResponse createImageVariation(byte[] originalImage, MapParam params) throws LLMException {
		if (originalImage == null || originalImage.length == 0) {
			throw new LLMException("Original image data cannot be null or empty");
		}

		params = fixParams(params);

		try {
			// Create request payload
			Map<String, Object> payload = jsonMapper.toImageVariationRequest(originalImage, params);

			// Make API request to images/variations endpoint
			Map<String, Object> response = postMultipartRequest("/images/variations", payload);

			// Convert response
			return jsonMapper.fromImageGenerationResponse(response);

		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during image variation: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Helper method to handle multipart requests for image upload endpoints.
	 * This is a placeholder - full implementation would require OkHttp multipart
	 * support.
	 * 
	 * @param endpoint the API endpoint
	 * @param payload  the request payload containing image data and parameters
	 * 
	 * @return response Map
	 * 
	 * @throws LLMException if request fails
	 */
	private Map<String, Object> postMultipartRequest(String endpoint, Map<String, Object> payload) throws LLMException {
		// TODO: Implement full multipart/form-data support with OkHttp
		// For now, throw an informative exception
		throw new LLMException(
		            "Image editing and variations require multipart upload support, which is not yet implemented. "
		                        + "Only image generation from text prompts is currently supported.");
	}

}
