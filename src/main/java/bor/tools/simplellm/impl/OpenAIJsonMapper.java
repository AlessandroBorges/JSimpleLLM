package bor.tools.simplellm.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.ModelEmbedding;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.Reasoning_Effort;
import bor.tools.simplellm.Utils;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.ContentWrapper;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Utility class for converting between internal POJOs and OpenAI API JSON
 * format.
 * <p>
 * This class handles the marshalling and unmarshalling of requests and
 * responses
 * between the JSimpleLLM internal object model and the OpenAI API JSON format
 * using Map&lt;String,Object&gt; as an intermediate representation.
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 */
public class OpenAIJsonMapper {

	/**
	 * Default embedding dimensions if not specified
	 */
	private Integer DEFAULT_EMBEDDING_DIMENSIONS = 768;
	
	private final ObjectMapper objectMapper;

	public OpenAIJsonMapper() {
		this.objectMapper = new ObjectMapper();
	}

	public OpenAIJsonMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Converts a Chat object and parameters into an OpenAI chat completion request
	 * payload.
	 * 
	 * @param chat   the chat session containing messages
	 * @param query  additional user query to append (can be null)
	 * @param params additional parameters like temperature, max_tokens, etc.
	 * @param model  the model to use for completion
	 * 
	 * @return Map representing the JSON request payload
	 * 
	 * @throws LLMException if message conversion fails (e.g., image processing)
	 */
	public Map<String, Object> toChatCompletionRequest(Chat chat, String query, MapParam params) throws LLMException {
		
		Map<String, Object> request = new HashMap<>();
		String modelName = params.getModel();
		// Set model
		if(modelName == null) {
			modelName = chat.getModel();
		}		
		if (modelName == null) {
			throw new IllegalArgumentException("Model must be specified for chat completion request");
		}
				
		request.put("model", modelName);

		// Convert messages
		List<Map<String, Object>> messages = new ArrayList<>();

		if (chat.getMessages() != null) {
			for (Message msg : chat.getMessages()) {
				messages.add(messageToMap(msg));
			}
		}

		// Add user query if provided
		if (query != null && !query.trim().isEmpty()) {
			Map<String, Object> userMsg = new HashMap<>();
			userMsg.put("role", "user");
			userMsg.put("content", query.trim());
			messages.add(userMsg);
		}

		request.put("messages", messages);

		// Add parameters from MapParam
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();
				if (value == null)
					continue;
				 
				// Map common parameter names
				switch (key.toLowerCase()) {
					case "temperature":
					case "max_tokens":
					case "top_p":
					case "frequency_penalty":
					case "presence_penalty":
					case "stop":
					case "stream":
						request.put(key, value);
						break;
						
					case "model_obj":
						// skip this one
						break;
						
					default:
						// Include other parameters as-is
						if (key.equalsIgnoreCase("reasoning_effort") && value instanceof Reasoning_Effort) {
							value = ((Reasoning_Effort) value).toString().toLowerCase();
						}
						request.put(key, value);
						break;
				}
			}
		}
		// Order properties for readability
		request = orderProperty(request,
		                        "model",
		                        "reasoning_effort",
		                        "messages",
		                        "temperature",
		                        "max_tokens",
		                        "top_p",
		                        "frequency_penalty",
		                        "presence_penalty",
		                        "stop",
		                        "stream");
		return request;
	}

	/**
	 * Orders the properties of a Map according to the specified key order.
	 * Keys not in the order list are appended at the end in their original order.
	 * 
	 * @param source    the original Map
	 * @param keysOrder the desired key order
	 * 
	 * @return a new LinkedHashMap with properties ordered
	 */
	protected LinkedHashMap<String, Object> orderProperty(Map<String, Object> source, String... keysOrder) {
		LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
		for (String key : keysOrder) {
			if (source.containsKey(key)) {
				copy.put(key, source.remove(key));
			}
		}
		// copy remaining entries in original order
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	/**
	 * Converts a Completion (non-chat) object and parameters into an OpenAI
	 * completion request payload.<br>
	 * <h3>Note:</h3> OpenAI has deprecated the classic completion endpoint
	 * in favor of chat completions. But is still used in compatible providers
	 * 
	 * @param prompt The system prompt for this completion
	 * @param query  additional user query to append (can be null)
	 * @param params additional parameters like temperature, max_tokens, etc.
	 * @param model  the model to use for completion
	 * 
	 * @return Map representing the JSON request payload
	 */
	public Map<String, Object> toCompletionRequest(String prompt, String query, MapParam params) {
		Map<String, Object> request = new HashMap<>();

		Object model = params.getModel();
		// Set model
		request.put("model", model.toString());

		// Convert messages
		prompt = prompt == null ? "" : prompt.trim();
		String fullPrompt = prompt
		            + "\n\n"
		            + (query != null ? query.trim() : "");

		request.put("prompt", fullPrompt.trim());

		// Add parameters from MapParam
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();

				// Map common parameter names
				switch (key.toLowerCase()) {
					case "temperature":
					case "max_tokens":
					case "top_p":
					case "frequency_penalty":
					case "presence_penalty":
					case "stop":
					case "stream":
					case "n":
					case "logprobs":
						request.put(key, value);
						break;
					default:
						// Include other parameters as-is
						request.put(key, value);
						break;
				}
			}
		}

		return request;
	}

	/**
	 * Converts an OpenAI chat completion response to a CompletionResponse object.
	 * 
	 * @param response the response Map from OpenAI API
	 * 
	 * @return CompletionResponse object
	 * 
	 * @throws LLMException if conversion fails
	 */
	@SuppressWarnings("unchecked")
	public CompletionResponse fromChatCompletionResponse(Map<String, Object> response) throws LLMException {
		CompletionResponse completionResponse = new CompletionResponse();

		try {
			// Extract choices
			List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
			if (choices != null && !choices.isEmpty()) {
				Map<String, Object> firstChoice = choices.get(0);
				Map<String, Object> message     = (Map<String, Object>) firstChoice.get("message");

				// Extract usage information and other metadata
				MapParam info = new MapParam();
				info.putAll(response);
				completionResponse.setInfo(info);

				// get Usage
				MapParam usage = new MapParam((Map<String, Object>) response.get("usage"));
				completionResponse.setUsage(usage);

				// Extract message content
				if (message != null) {
					String content = (String) message.get("content");
					if (content != null) {
						completionResponse.setResponse(new ContentWrapper(ContentType.TEXT, content));
					}
					////////////
					String   reasoning = null;
					String[] keys      = { "reasoning_content", "reasoning", "thoughts", "thoughts_content", "think" };

					for (String key : keys) {
						if (message.containsKey(key)) {
							reasoning = message.get(key).toString();
							break;
						}
					}
					completionResponse.setReasoningContent(reasoning);
					/////////
				} else {
					// Fallback to text field for non-chat completions
					String text = (String) firstChoice.get("text");
					if (text != null) {
						completionResponse.setResponse(new ContentWrapper(ContentType.TEXT, text));
					}
				}

				// Extract finish reason
				String finishReason = (String) firstChoice.get("finish_reason");
				completionResponse.setEndReason(finishReason);
			}

		} catch (Exception e) {
			throw new LLMException("Failed to parse OpenAI response: "
			            + e.getMessage(), e);
		}

		return completionResponse;
	}

	/**
	 * Converts a streaming SSE chunk to a CompletionResponse.
	 * 
	 * @param sseData the SSE data line (without "data: " prefix)
	 * 
	 * @return CompletionResponse object or null if chunk should be ignored
	 * 
	 * @throws LLMException if parsing fails
	 */
	@SuppressWarnings("unchecked")
	public CompletionResponse fromStreamingChunk(String sseData) throws LLMException {
		if (sseData == null || sseData.trim().isEmpty() || "[DONE]".equals(sseData.trim())) {
			return null;
		}

		try {
			Map<String, Object> chunk = objectMapper.readValue(sseData, Map.class);

			CompletionResponse response = new CompletionResponse();

			List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
			if (choices != null && !choices.isEmpty()) {
				Map<String, Object> firstChoice = choices.get(0);
				Map<String, Object> delta       = (Map<String, Object>) firstChoice.get("delta");

				if (delta != null) {
					String content = (String) delta.get("content");
					if (content != null) {
						response.setResponse(new ContentWrapper(ContentType.TEXT, content));
					}
					if (delta.get("reasoning") != null || delta.get("reasoning_content") != null) {
						String reasoning = (String) delta.get("reasoning");
						if (reasoning == null) {
							reasoning = (String) delta.get("reasoning_content");
						}
						response.setReasoningContent(reasoning);
					}
				}

				String finishReason = (String) firstChoice.get("finish_reason");
				response.setEndReason(finishReason);
			}

			// Store chunk info
			MapParam info = new MapParam();
			info.putAll(chunk);
			response.setInfo(info);

			return response;

		} catch (JsonProcessingException e) {
			throw new LLMException("Failed to parse streaming chunk: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Converts an embeddings request to OpenAI API format.
	 * 
	 * @param input      the text to embed
	 * @param model      the embedding model
	 * @param dimensions optional dimension parameter
	 * @param encodingFormat optional encoding format (e.g., "base64")
	 * 
	 * @return request payload Map for requesting embeddings
	 */
	public Map<String, Object> toEmbeddingsRequest(String input, 
	                                               Model model, 
	                                               Integer dimensions, 
	                                               String encodingFormat) 
	{
		Map<String, Object> request = new HashMap<>();
		request.put("input", input);
		if (model == null) {
			throw new IllegalArgumentException("Model must be specified for embeddings request");
		}
		request.put("model", model.getName());

		if (dimensions != null) {
			request.put("dimensions", dimensions);
		}
		if (encodingFormat != null) {
			request.put("encoding_format", encodingFormat);
		}

		return request;
	}
	
	/**
	 * Converts an embeddings request to OpenAI API format.
	 * 
	 * @param input      the text array to embed
	 * @param model      the embedding model
	 * @param dimensions optional dimension parameter
	 * @param encodingFormat optional encoding format (e.g., "base64")
	 * 
	 * @return request payload Map for requesting embeddings
	 */
	public Map<String, Object> toEmbeddingsRequest(String[] input, 
	                                               Model model, 
	                                               Integer dimensions, 
	                                               String encodingFormat) 
	{
		Map<String, Object> request = new HashMap<>();
		request.put("input", input);
		if (model == null) {
			throw new IllegalArgumentException("Model must be specified for embeddings request");
		}
		request.put("model", model.getName());

		if (dimensions != null) {
			request.put("dimensions", dimensions);
		}
		if (encodingFormat != null) {
			request.put("encoding_format", encodingFormat);
		}

		return request;
	}

	/**
	 * Converts an OpenAI models response to a Map of Model objects.
	 * 
	 * @param response the response Map from OpenAI API
	 * 
	 * @return Map of model ID to Model objects
	 * 
	 * @throws LLMException if conversion fails
	 */
	public Map<String, Model> fromModelsRequest(Map<String, Object> response) throws LLMException {
		Map<String, Model> models = new HashMap<>();
		try {

			Object dataObj = response.get("data");
			if (dataObj == null || !(dataObj instanceof List)) {
				throw new LLMException("No model data found in response", null);
			}
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
			if (data != null && !data.isEmpty()) {
				for (Map<String, Object> modelData : data) {
					String id = (String) modelData.get("id");
					if (id == null || id.isEmpty()) {
						id = (String) modelData.get("name");
					}
					if (id == null || id.isEmpty()) {
						continue;
					}
					Integer contextLength = null;
					if (modelData.containsKey("context_length")) {
						contextLength = (Integer) modelData.get("context_length");
					} else if (modelData.containsKey("max_context_length")) {
						contextLength = (Integer) modelData.get("max_context_length");
					}
					
					// Use ModelFeatureDetector for sophisticated capability detection
					List<Model_Type> detectedTypes = ModelFeatureDetector.detectCapabilities(id, modelData);
					Model model = null;
					if(detectedTypes.isEmpty()) {
						// Fallback to basic type detection
						if (id.toLowerCase().contains("embed")) {
							detectedTypes.add(Model_Type.EMBEDDING);
						} else if (id.toLowerCase().contains("code")) {
							detectedTypes.add(Model_Type.CODING);
						} else {
							detectedTypes.add(Model_Type.LANGUAGE);
							detectedTypes.add(Model_Type.TEXT);
						}
						
					}
					
					if(detectedTypes.contains(Model_Type.EMBEDDING) ){ 
						model = new ModelEmbedding(id,						                           
						                           contextLength,
						                           DEFAULT_EMBEDDING_DIMENSIONS,
						                           detectedTypes.toArray(new Model_Type[0]));	
						
					} else {
						model = new Model(id, contextLength, detectedTypes.toArray(new Model_Type[0]));
					}
					models.put(id, model);
				}
			}
		} catch (Exception e) {
			throw new LLMException("Failed to parse models response: "
			            + e.getMessage(), e);
		}
		return models;

	}

	/**
	 * Converts an OpenAI embeddings response to a float array.
	 * 
	 * @param response the response Map from OpenAI API
	 * @param vecSize  the expected vector size
	 * 
	 * @return float array of embeddings
	 * 
	 * @throws LLMException if conversion fails
	 */
	@SuppressWarnings("unchecked")
	public float[] fromEmbeddingsResponse(Map<String, Object> response, Integer vecSize) throws LLMException {
		try {
			List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
			if (data == null || data.isEmpty()) {
				throw new LLMException("No embedding data found in response", null);
			}
			
			Object embeddingObj = data.get(0).get("embedding");
			return convertToFloatArray(embeddingObj, vecSize);
			
		} catch (Exception e) {
			throw new LLMException("Failed to parse embeddings response: " + e.getMessage(), e);
		}
	}
	

	/**
	 * Converts an OpenAI embeddings response to a list of float arrays.
	 * 
	 * @param response the response Map from OpenAI API
	 * @param vecSize  the expected vector size
	 * 
	 * @return List of float arrays of embeddings
	 * 
	 * @throws LLMException if conversion fails
	 */
	@SuppressWarnings("unchecked")
	public List<float[]> fromEmbeddingsArrayResponse(Map<String, Object> response, Integer vecSize) 
				throws LLMException {
	    try {
	        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
	        if (data == null || data.isEmpty()) {
	            throw new LLMException("No embedding data found in response", null);
	        }

	        List<float[]> result = new ArrayList<>();
	        
	        for (Map<String, Object> item : data) {
	            Object embeddingObj = item.get("embedding");
	            if (embeddingObj != null) {
	                float[] embedding = convertToFloatArray(embeddingObj, vecSize);
	                result.add(embedding);
	            }
	        }
	        data.clear();
	        return result;
	        
	    } catch (ClassCastException e) {
	        throw new LLMException("Invalid response format for embeddings array: " + e.getMessage(), e);
	    } catch (Exception e) {
	        throw new LLMException("Failed to parse embeddings response: " + e.getMessage(), e);
	    }
	}


	/**
	 * Converts an embedding object to a float array.
	 * Handles both List&lt;Number&gt; and base64 String formats.
	 * 
	 * @param embeddingObj the embedding object
	 * @param vecSize      the expected vector size (nullable)
	 * 
	 * @return normalized float array
	 * 
	 * @throws LLMException if conversion fails
	 */
	@SuppressWarnings("unchecked")
	private float[] convertToFloatArray(Object embeddingObj, Integer vecSize) throws LLMException {
		if (embeddingObj == null) {
			throw new LLMException("Embedding object is null", null);
		}
		
		float[] vec;
		
		// Handle base64 encoded string
		if (embeddingObj instanceof String) {
			vec = convertBase64ToFloatArray((String) embeddingObj, vecSize);
		}
		// Handle list of numbers
		else if (embeddingObj instanceof List) {
			List<Number> embedding = (List<Number>) embeddingObj;
			vec = convertNumberListToFloatArray(embedding, vecSize);
			embedding.clear();
		}
		// Handle existing float array
		else if (embeddingObj instanceof float[]) {
			vec = (float[]) embeddingObj;
			vec = resizeIfNeeded(vec, vecSize);
			vec = Utils.normalize(vec);
		}
		else {
			throw new LLMException("Unexpected embedding format: " + embeddingObj.getClass().getName(), null);
		}
		
		return vec;
	}

	/**
	 * Converts a base64 encoded string to a normalized float array.
	 * 
	 * @param base64Str the base64 encoded string
	 * @param vecSize   the expected vector size (nullable)
	 * 
	 * @return normalized float array
	 * 
	 * @throws LLMException if conversion fails
	 */
	private float[] convertBase64ToFloatArray(String base64Str, Integer vecSize) throws LLMException {
		float[] vec = Utils.base64ToFloatArray(base64Str);
		vec = resizeIfNeeded(vec, vecSize);
		return Utils.normalize(vec);
	}

	/**
	 * Converts a List&lt;Number&gt; to a normalized float array.
	 * 
	 * @param numberList the list of numbers
	 * @param vecSize    the expected vector size (nullable)
	 * 
	 * @return normalized float array
	 */
	private float[] convertNumberListToFloatArray(List<Number> numberList, Integer vecSize) {
		int size = (vecSize != null && vecSize >= 16) ? vecSize : numberList.size();
		float[] vec = new float[size];
		
		int copyLength = Math.min(vec.length, numberList.size());
		for (int i = 0; i < copyLength; i++) {
			vec[i] = numberList.get(i).floatValue();
		}
		
		return Utils.normalize(vec);
	}

	/**
	 * Resizes the float array if needed based on the expected vector size.
	 * 
	 * @param vec     the original float array
	 * @param vecSize the expected vector size (nullable)
	 * 
	 * @return resized array or original if no resize needed
	 */
	private float[] resizeIfNeeded(float[] vec, Integer vecSize) {
		if (vecSize != null && vecSize >= 16 && vec.length != vecSize) {
			float[] temp = new float[vecSize];
			System.arraycopy(vec, 0, temp, 0, Math.min(vec.length, vecSize));
			return temp;
		}
		return vec;
	}

	/**
	 * Converte embeddings para float[] (4 bytes).
	 *
	 * @return este embeddings como float.
	 * 
	 * @throws LLMException
	 *
	 * @see Embedding#isFloat()
	 * @see Embedding#isDouble()
	 */

	@SuppressWarnings("unchecked")
	protected final float[] convertEmbeddigsToFloats(Object embedding, boolean normalized) throws LLMException {
		try {
			if (embedding == null) {
				return null;
			}
			if (embedding instanceof String) {
				// base64 encoding
				float[] arr = Utils.base64ToFloatArray((String) embedding);
				embedding = Utils.normalize(arr);
				normalized = true;
				return arr;
			}
			if (embedding instanceof List) {
				List<Float> list = (List<Float>) embedding;
				float[]     arr  = new float[list.size()];
				for (int i = 0; i < list.size(); i++) {
					arr[i] = list.get(i);
				}
				list.clear();
				var normy = Utils.normalize(arr);
				normalized = true;
				return normy;
			}
			if (embedding instanceof float[]) {
				if (normalized) {
					return (float[]) embedding;
				} else {
					var normy = Utils.normalize((float[]) embedding);
					normalized = true;
					return normy;
				}
			} else {
				throw new LLMException("Embedding  unknow: "
				            + embedding.getClass().getName());
			}
		} catch (Exception e) {
			throw new LLMException("Failed to convert embeddings: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Converts a Message object to a Map for OpenAI API.
	 * 
	 * @param message the message to convert
	 * 
	 * @return Map representing the message
	 * 
	 * @throws LLMException if image content conversion fails
	 */
	private Map<String, Object> messageToMap(Message message) throws LLMException {
		Map<String, Object> msgMap = new HashMap<>();

		// Convert role
		MessageRole role = message.getRole();
		if (role != null) {
			switch (role) {
				case SYSTEM:
					msgMap.put("role", "system");
					break;
				case USER:
					msgMap.put("role", "user");
					break;
				case ASSISTANT:
					msgMap.put("role", "assistant");
					break;
				case DEVELOPER:
					msgMap.put("role", "developer");
					break;
				default:
					msgMap.put("role", "user");
					break;
			}
		}

		// Convert content
		ContentWrapper content = message.getContent();
		if (content != null) {
			if (content.getType() == ContentType.TEXT && content.getContent() instanceof String) {
				msgMap.put("content", content.getContent());
			} else if (content.getType() == ContentType.IMAGE) {
				// Handle image content for vision models
				Object imageContent = convertImageToMultimodalContent(content);
				msgMap.put("content", imageContent);
			} else {
				// Fallback for other content types
				msgMap.put("content", content.getText());
			}
		}

		return msgMap;
	}

	/**
	 * Converts an image ContentWrapper to OpenAI's multimodal message format.
	 * Supports both URL-based and base64-encoded images.
	 * 
	 * @param content the ContentWrapper containing image data
	 * 
	 * @return Map representing the image content in OpenAI format
	 * 
	 * @throws LLMException if image conversion fails
	 */
	private Map<String, Object> convertImageToMultimodalContent(ContentWrapper content) throws LLMException {
		if (!(content instanceof ContentWrapper.ImageContent)) {
			// If it's not ImageContent but has IMAGE type, try to handle it
			if (content.getContent() instanceof String) {
				// Assume it's a URL
				return Map.of("type", "image_url", "image_url", Map.of("url", (String) content.getContent()));
			} else if (content.getContent() instanceof byte[]) {
				// Convert byte array to base64
				try {
					String base64 = Base64.getEncoder().encodeToString((byte[]) content.getContent());
					return Map.of("type",
					              "image_url",
					              "image_url",
					              Map.of("url",
					                     "data:image/jpeg;base64,"
					                                 + base64));
				} catch (Exception e) {
					throw new LLMException("Failed to encode image to base64: "
					            + e.getMessage(), e);
				}
			}
			throw new LLMException("Unsupported image content type: "
			            + content.getContent().getClass().getName());
		}

		ContentWrapper.ImageContent imageContent = (ContentWrapper.ImageContent) content;

		// Handle URL-based images
		if (imageContent.getUrl() != null) {
			Map<String, Object> imageUrl = new HashMap<>();
			imageUrl.put("url", imageContent.getUrl());

			// Add detail level if specified in metadata
			if (imageContent.getMetadata() != null && imageContent.getMetadata().containsKey("detail")) {
				imageUrl.put("detail", imageContent.getMetadata().get("detail"));
			}

			return Map.of("type", "image_url", "image_url", imageUrl);
		}

		// Handle raw image data
		if (imageContent.getImageData() != null) {
			try {
				String base64 = Base64.getEncoder().encodeToString(imageContent.getImageData());

				// Determine MIME type from metadata or default to JPEG
				String mimeType = "image/jpeg";
				if (imageContent.getMetadata() != null && imageContent.getMetadata().containsKey("mimeType")) {
					mimeType = (String) imageContent.getMetadata().get("mimeType");
				}

				Map<String, Object> imageUrl = new HashMap<>();
				imageUrl.put("url",
				             "data:"
				                         + mimeType
				                         + ";base64,"
				                         + base64);

				// Add detail level if specified
				if (imageContent.getMetadata() != null && imageContent.getMetadata().containsKey("detail")) {
					imageUrl.put("detail", imageContent.getMetadata().get("detail"));
				}

				return Map.of("type", "image_url", "image_url", imageUrl);
			} catch (Exception e) {
				throw new LLMException("Failed to encode image data to base64: "
				            + e.getMessage(), e);
			}
		}

		throw new LLMException("ImageContent must contain either URL or image data");
	}

	/**
	 * Converts messages with mixed text and image content to OpenAI's multimodal
	 * format.
	 * This method handles cases where a message contains both text and images.
	 * 
	 * @param textContent   the text part of the message
	 * @param imageContents list of image contents to include
	 * 
	 * @return List representing the multimodal content array
	 * 
	 * @throws LLMException if content conversion fails
	 */
	public List<Map<String, Object>> createMultimodalContent(String textContent,
	                                                         List<ContentWrapper.ImageContent> imageContents)
	            throws LLMException {
		List<Map<String, Object>> contentArray = new ArrayList<>();

		// Add text content if provided
		if (textContent != null && !textContent.trim().isEmpty()) {
			contentArray.add(Map.of("type", "text", "text", textContent.trim()));
		}

		// Add image contents
		if (imageContents != null) {
			for (ContentWrapper.ImageContent imageContent : imageContents) {
				contentArray.add(convertImageToMultimodalContent(imageContent));
			}
		}

		return contentArray;
	}

	/**
	 * Creates a multimodal message map with both text and a single image.
	 * Convenience method for the common case of text + one image.
	 * 
	 * @param role         the message role (user, assistant, system)
	 * @param textContent  the text part of the message
	 * @param imageContent the image content
	 * 
	 * @return Map representing a complete multimodal message
	 * 
	 * @throws LLMException if content conversion fails
	 */
	public Map<String, Object> createMultimodalMessage(String role,
	                                                   String textContent,
	                                                   ContentWrapper.ImageContent imageContent)
	            throws LLMException {
		Map<String, Object> message = new HashMap<>();
		message.put("role", role);

		List<Map<String, Object>> contentArray = new ArrayList<>();

		// Add text if provided
		if (textContent != null && !textContent.trim().isEmpty()) {
			contentArray.add(Map.of("type", "text", "text", textContent.trim()));
		}

		// Add image
		if (imageContent != null) {
			contentArray.add(convertImageToMultimodalContent(imageContent));
		}

		message.put("content", contentArray);
		return message;
	}

	// ================== IMAGE GENERATION REQUEST METHODS ==================

	/**
	 * Converts image generation parameters to OpenAI images API format.
	 * 
	 * @param prompt the text description for image generation
	 * @param params additional parameters like size, quality, n, etc.
	 * 
	 * @return request payload Map for /images/generations endpoint
	 */
	public Map<String, Object> toImageGenerationRequest(String prompt, MapParam params) {
		Map<String, Object> request = new HashMap<>();
		request.put("prompt", prompt);

		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();

				// Map common image generation parameters
				switch (key.toLowerCase()) {
					case "model":
					case "size":
					case "quality":
					case "n":
					case "response_format":
					case "style":
					case "user":
						request.put(key, value);
						break;
					default:
						// Include other parameters as-is
						request.put(key, value);
						break;
				}
			}
		}

		return request;
	}

	/**
	 * Converts image editing parameters to OpenAI images API format.
	 * 
	 * @param originalImage the original image data
	 * @param prompt        the edit prompt
	 * @param maskImage     optional mask image data
	 * @param params        additional parameters
	 * 
	 * @return request payload Map for /images/edits endpoint
	 */
	public Map<String, Object> toImageEditRequest(byte[] originalImage,
	                                              String prompt,
	                                              byte[] maskImage,
	                                              MapParam params) {
		Map<String, Object> request = new HashMap<>();

		// Note: For image editing, the actual image data needs to be sent as
		// multipart/form-data
		// This method prepares the non-file parameters
		request.put("prompt", prompt);

		// Store image data in the request for later multipart processing
		request.put("_image_data", originalImage);
		if (maskImage != null) {
			request.put("_mask_data", maskImage);
		}

		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();

				switch (key.toLowerCase()) {
					case "model":
					case "size":
					case "n":
					case "response_format":
					case "user":
						request.put(key, value);
						break;
					default:
						request.put(key, value);
						break;
				}
			}
		}

		return request;
	}

	/**
	 * Converts image variation parameters to OpenAI images API format.
	 * 
	 * @param originalImage the original image data
	 * @param params        additional parameters
	 * 
	 * @return request payload Map for /images/variations endpoint
	 */
	public Map<String, Object> toImageVariationRequest(byte[] originalImage, MapParam params) {
		Map<String, Object> request = new HashMap<>();

		// Store image data for multipart processing
		request.put("_image_data", originalImage);

		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();

				switch (key.toLowerCase()) {
					case "model":
					case "size":
					case "n":
					case "response_format":
					case "user":
						request.put(key, value);
						break;
					default:
						request.put(key, value);
						break;
				}
			}
		}

		return request;
	}

	/**
	 * Converts an OpenAI image generation response to a CompletionResponse object.
	 * Handles both URL and base64 response formats.
	 * 
	 * @param response the response Map from OpenAI images API
	 * 
	 * @return CompletionResponse containing the generated images as
	 *         ContentWrapper.ImageContent
	 * 
	 * @throws LLMException if conversion fails
	 */
	@SuppressWarnings("unchecked")
	public CompletionResponse fromImageGenerationResponse(Map<String, Object> response) throws LLMException {
		CompletionResponse completionResponse = new CompletionResponse();

		try {
			// Extract image data
			List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
			if (data != null && !data.isEmpty()) {
				// For now, return the first image. In the future, we might support multiple
				// images
				Map<String, Object> imageData = data.get(0);

				ContentWrapper.ImageContent imageContent = null;

				// Check if it's a URL response
				if (imageData.containsKey("url")) {
					String imageUrl = (String) imageData.get("url");
					imageContent = new ContentWrapper.ImageContent(imageUrl);
				}
				// Check if it's a base64 response
				else if (imageData.containsKey("b64_json")) {
					String base64Data = (String) imageData.get("b64_json");
					try {
						byte[] imageBytes = Base64.getDecoder().decode(base64Data);
						imageContent = new ContentWrapper.ImageContent(imageBytes);

						// Add metadata for MIME type
						Map<String, Object> metadata = new HashMap<>();
						metadata.put("mimeType", "image/png"); // OpenAI typically returns PNG
						imageContent.setMetadata(metadata);
					} catch (IllegalArgumentException e) {
						throw new LLMException("Failed to decode base64 image data: "
						            + e.getMessage(), e);
					}
				}

				if (imageContent != null) {
					// Add generation metadata if available
					if (imageData.containsKey("revised_prompt")) {
						Map<String, Object> metadata = imageContent.getMetadata();
						if (metadata == null) {
							metadata = new HashMap<>();
							imageContent.setMetadata(metadata);
						}
						metadata.put("revised_prompt", imageData.get("revised_prompt"));
					}

					completionResponse.setResponse(imageContent);
				} else {
					throw new LLMException("No valid image data found in response");
				}

				// Set completion reason for images
				completionResponse.setEndReason("image_generated");
			} else {
				throw new LLMException("No image data found in response");
			}

			// Store full response as metadata
			MapParam info = new MapParam();
			info.putAll(response);
			completionResponse.setInfo(info);

		} catch (Exception e) {
			if (e instanceof LLMException) {
				throw e;
			}
			throw new LLMException("Failed to parse image generation response: "
			            + e.getMessage(), e);
		}

		return completionResponse;
	}

	/**
	 * Converts a Map to JSON string.
	 * 
	 * @param data the data to convert
	 * 
	 * @return JSON string
	 * 
	 * @throws LLMException if conversion fails
	 */
	public String toJson(Map<String, Object> data) throws LLMException {
		// some object MUST be converted to string, using toString()
		// before serializing to JSON
		Class<?>[] toStringJson = { Model.class, Reasoning_Effort.class, Enum.class };
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			if (entry.getValue() != null) {
				for (Class<?> cls : toStringJson) {
					if (cls.isAssignableFrom(entry.getValue().getClass())) {
						entry.setValue(entry.getValue().toString());
						break;
					}
				}
			}
		}

		try {
			return objectMapper.writeValueAsString(data);
		} catch (JsonProcessingException e) {
			throw new LLMException("Failed to convert to JSON: "
			            + e.getMessage(), e);
		}
	}

	/**
	 * Converts JSON string to Map.
	 * 
	 * @param json the JSON string
	 * 
	 * @return Map representation
	 * 
	 * @throws LLMException if parsing fails
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> fromJson(String json) throws LLMException {
		try {
			return objectMapper.readValue(json, Map.class);
		} catch (JsonProcessingException e) {
			throw new LLMException("Failed to parse JSON: "
			            + e.getMessage(), e);
		}
	}
}
