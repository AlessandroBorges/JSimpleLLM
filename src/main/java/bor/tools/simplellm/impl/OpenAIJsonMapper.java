package bor.tools.simplellm.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ContentType;
import bor.tools.simplellm.ContentWrapper;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.VecUtil;
import bor.tools.simplellm.chat.Chat;
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

		String model = params.getModel();
		// Set model
		request.put("model", model != null ? model : chat.getModel());

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

		String model = params.getModel();
		// Set model
		request.put("model", model);

		// Convert messages
		prompt = prompt == null ? "" : prompt.trim();
		String fullPrompt = prompt
		            + "\n\n"
		            + (query != null ? query.trim() : "");
		request.put("prompt", fullPrompt);

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

				if (message != null) {
					String content = (String) message.get("content");
					if (content != null) {
						completionResponse.setResponse(new ContentWrapper(ContentType.TEXT, content));
					}
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

			// Extract usage information and other metadata
			Map<String, Object> info = new HashMap<>();
			info.putAll(response);
			completionResponse.setInfo(info);

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
				}

				String finishReason = (String) firstChoice.get("finish_reason");
				response.setEndReason(finishReason);
			}

			// Store chunk info
			Map<String, Object> info = new HashMap<>();
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
	 * 
	 * @return request payload Map
	 */
	public Map<String, Object> toEmbeddingsRequest(String input,
	                                               String model,
	                                               Integer dimensions,
	                                               String encodingFormat) {
		Map<String, Object> request = new HashMap<>();
		request.put("input", input);
		request.put("model", model);

		if (dimensions != null) {
			request.put("dimensions", dimensions);
		}
		if (encodingFormat != null) {
			request.put("encoding_format", encodingFormat);
		}

		return request;
	}

	/**
	 * Converts an OpenAI embeddings response to a float array.
	 * 
	 * @param response the response Map from OpenAI API
	 * 
	 * @return float array of embeddings
	 * 
	 * @throws LLMException if conversion fails
	 */
	@SuppressWarnings("unchecked")
	public float[] fromEmbeddingsResponse(Map<String, Object> response) throws LLMException {
		try {
			List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
			if (data != null && !data.isEmpty()) {
				Object embeddingObj = data.get(0).get("embedding");
				if ((embeddingObj instanceof List)) {
					// Handle list of numbers
					List<Number> embedding = (List<Number>) data.get(0).get("embedding");
					if (embedding != null) {
						float[] result = new float[embedding.size()];
						for (int i = 0; i < embedding.size(); i++) {
							result[i] = embedding.get(i).floatValue();
						}
						embedding.clear();
						result = VecUtil.normalize(result);
						return result;
					}
				} else if (embeddingObj instanceof String) {
					// Handle base64 encoded embeddings if returned as string
					String base64Str = (String) embeddingObj;
					// Decode base64 to byte array and convert to float array as needed
					float[] vec = VecUtil.base64ToFloatArray(base64Str);
					vec = VecUtil.normalize(vec);
					return vec;
				} else {
					throw new LLMException("Unexpected embedding format "
					            + embeddingObj, null);
				}
			} else {
				throw new LLMException("No embedding data found in response", null);
			}
		} catch (Exception e) {
			throw new LLMException("Failed to parse embeddings response: "
			            + e.getMessage(), e);
		}
		throw new LLMException("No embedding data found in response");
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

	protected final float[] convertEmbeddigsToFloats(Object embedding, boolean normalized) throws LLMException {
		if (embedding == null) {
			return null;
		}
		if (embedding instanceof String) {
			float[] arr = VecUtil.base64ToFloatArray((String) embedding);
			embedding = VecUtil.normalize(arr);
			normalized = true;
			return arr;
		}
		if (embedding instanceof List) {
			List<Float> list = (List<Float>) embedding;
			float[]     arr  = new float[list.size()];
			for (int i = 0; i < list.size(); i++) {
				arr[i] = list.get(i);
			}
			var normy = VecUtil.normalize((float[]) embedding);
			normalized = true;
			return normy;
		}
		if (embedding instanceof float[]) {
			if (normalized) {
				return (float[]) embedding;
			} else {
				var normy = VecUtil.normalize((float[]) embedding);
				normalized = true;
				return normy;
			}
		} else {
			throw new LLMException("Embedding  unknow: "
			            + embedding.getClass().getName());
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
				return Map.of("type", "image_url", 
							"image_url", Map.of("url", (String) content.getContent()));
			} else if (content.getContent() instanceof byte[]) {
				// Convert byte array to base64
				try {
					String base64 = Base64.getEncoder().encodeToString((byte[]) content.getContent());
					return Map.of("type", "image_url",
								"image_url", Map.of("url", "data:image/jpeg;base64," + base64));
				} catch (Exception e) {
					throw new LLMException("Failed to encode image to base64: " + e.getMessage(), e);
				}
			}
			throw new LLMException("Unsupported image content type: " + content.getContent().getClass().getName());
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
				imageUrl.put("url", "data:" + mimeType + ";base64," + base64);
				
				// Add detail level if specified
				if (imageContent.getMetadata() != null && imageContent.getMetadata().containsKey("detail")) {
					imageUrl.put("detail", imageContent.getMetadata().get("detail"));
				}
				
				return Map.of("type", "image_url", "image_url", imageUrl);
			} catch (Exception e) {
				throw new LLMException("Failed to encode image data to base64: " + e.getMessage(), e);
			}
		}
		
		throw new LLMException("ImageContent must contain either URL or image data");
	}

	/**
	 * Converts messages with mixed text and image content to OpenAI's multimodal format.
	 * This method handles cases where a message contains both text and images.
	 * 
	 * @param textContent the text part of the message
	 * @param imageContents list of image contents to include
	 * 
	 * @return List representing the multimodal content array
	 * 
	 * @throws LLMException if content conversion fails
	 */
	public List<Map<String, Object>> createMultimodalContent(String textContent, List<ContentWrapper.ImageContent> imageContents) throws LLMException {
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
	 * @param role the message role (user, assistant, system)
	 * @param textContent the text part of the message
	 * @param imageContent the image content
	 * 
	 * @return Map representing a complete multimodal message
	 * 
	 * @throws LLMException if content conversion fails
	 */
	public Map<String, Object> createMultimodalMessage(String role, String textContent, ContentWrapper.ImageContent imageContent) throws LLMException {
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
				String key = entry.getKey();
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
	 * @param prompt the edit prompt
	 * @param maskImage optional mask image data
	 * @param params additional parameters
	 * 
	 * @return request payload Map for /images/edits endpoint
	 */
	public Map<String, Object> toImageEditRequest(byte[] originalImage, String prompt, 
													byte[] maskImage, MapParam params) {
		Map<String, Object> request = new HashMap<>();
		
		// Note: For image editing, the actual image data needs to be sent as multipart/form-data
		// This method prepares the non-file parameters
		request.put("prompt", prompt);
		
		// Store image data in the request for later multipart processing
		request.put("_image_data", originalImage);
		if (maskImage != null) {
			request.put("_mask_data", maskImage);
		}

		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key = entry.getKey();
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
	 * @param params additional parameters
	 * 
	 * @return request payload Map for /images/variations endpoint
	 */
	public Map<String, Object> toImageVariationRequest(byte[] originalImage, MapParam params) {
		Map<String, Object> request = new HashMap<>();
		
		// Store image data for multipart processing
		request.put("_image_data", originalImage);

		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key = entry.getKey();
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
	 * @return CompletionResponse containing the generated images as ContentWrapper.ImageContent
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
				// For now, return the first image. In the future, we might support multiple images
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
						throw new LLMException("Failed to decode base64 image data: " + e.getMessage(), e);
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
			Map<String, Object> info = new HashMap<>();
			info.putAll(response);
			completionResponse.setInfo(info);

		} catch (Exception e) {
			if (e instanceof LLMException) {
				throw e;
			}
			throw new LLMException("Failed to parse image generation response: " + e.getMessage(), e);
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
