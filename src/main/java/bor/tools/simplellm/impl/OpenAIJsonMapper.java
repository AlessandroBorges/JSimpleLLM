package bor.tools.simplellm.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ContentType;
import bor.tools.simplellm.ContentWrapper;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Utility class for converting between internal POJOs and OpenAI API JSON format.
 * <p>
 * This class handles the marshalling and unmarshalling of requests and responses
 * between the JSimpleLLM internal object model and the OpenAI API JSON format
 * using Map&lt;String,Object&gt; as an intermediate representation.
 * </p>
 * 
 * @author AlessandroBorges
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
     * Converts a Chat object and parameters into an OpenAI chat completion request payload.
     * 
     * @param chat the chat session containing messages
     * @param query additional user query to append (can be null)
     * @param params additional parameters like temperature, max_tokens, etc.
     * @param model the model to use for completion
     * @return Map representing the JSON request payload
     */
    public Map<String, Object> toChatCompletionRequest(Chat chat, String query, MapParam params, String model) {
        Map<String, Object> request = new HashMap<>();
        
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
                String key = entry.getKey();
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
     * Converts an OpenAI chat completion response to a CompletionResponse object.
     * 
     * @param response the response Map from OpenAI API
     * @return CompletionResponse object
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
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null) {
                        completionResponse.setResponse(new ContentWrapper(ContentType.TEXT, content));
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
            throw new LLMException("Failed to parse OpenAI response: " + e.getMessage(), e);
        }
        
        return completionResponse;
    }

    /**
     * Converts a streaming SSE chunk to a CompletionResponse.
     * 
     * @param sseData the SSE data line (without "data: " prefix)
     * @return CompletionResponse object or null if chunk should be ignored
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
                Map<String, Object> delta = (Map<String, Object>) firstChoice.get("delta");
                
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
            throw new LLMException("Failed to parse streaming chunk: " + e.getMessage(), e);
        }
    }

    /**
     * Converts an embeddings request to OpenAI API format.
     * 
     * @param input the text to embed
     * @param model the embedding model
     * @param dimensions optional dimension parameter
     * @return request payload Map
     */
    public Map<String, Object> toEmbeddingsRequest(String input, String model, Integer dimensions) {
        Map<String, Object> request = new HashMap<>();
        request.put("input", input);
        request.put("model", model);
        
        if (dimensions != null) {
            request.put("dimensions", dimensions);
        }
        
        return request;
    }

    /**
     * Converts an OpenAI embeddings response to a float array.
     * 
     * @param response the response Map from OpenAI API
     * @return float array of embeddings
     * @throws LLMException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public float[] fromEmbeddingsResponse(Map<String, Object> response) throws LLMException {
        try {
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data != null && !data.isEmpty()) {
                List<Number> embedding = (List<Number>) data.get(0).get("embedding");
                if (embedding != null) {
                    float[] result = new float[embedding.size()];
                    for (int i = 0; i < embedding.size(); i++) {
                        result[i] = embedding.get(i).floatValue();
                    }
                    return result;
                }
            }
            throw new LLMException("No embedding data found in response", null);
        } catch (Exception e) {
            throw new LLMException("Failed to parse embeddings response: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Message object to a Map for OpenAI API.
     * 
     * @param message the message to convert
     * @return Map representing the message
     */
    private Map<String, Object> messageToMap(Message message) {
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
            } else {
                // For multimodal content, we might need more complex handling
                msgMap.put("content", content.getText());
            }
        }
        
        return msgMap;
    }

    /**
     * Converts a Map to JSON string.
     * 
     * @param data the data to convert
     * @return JSON string
     * @throws LLMException if conversion fails
     */
    public String toJson(Map<String, Object> data) throws LLMException {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new LLMException("Failed to convert to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Converts JSON string to Map.
     * 
     * @param json the JSON string
     * @return Map representation
     * @throws LLMException if parsing fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fromJson(String json) throws LLMException {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new LLMException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }
}