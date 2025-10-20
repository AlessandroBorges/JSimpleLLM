package bor.tools.simplellm.websearch.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.ContentWrapper;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.impl.OpenAIJsonMapper;
import bor.tools.simplellm.websearch.SearchResponse;

/**
 * Utility class for converting between internal POJOs and Perplexity API JSON format.
 * <p>
 * This class handles the marshalling and unmarshalling of requests and responses
 * between the JSimpleLLM internal object model and the Perplexity API JSON format.
 * </p>
 * <p>
 * Key differences from OpenAI format:
 * <ul>
 * <li>Endpoint: /chat/completions (no /v1/ prefix)</li>
 * <li>Supports web search-specific parameters (search_domain_filter, search_recency_filter, etc.)</li>
 * <li>Returns citations, search_results, and related questions in responses</li>
 * <li>No embeddings or image generation support</li>
 * </ul>
 * </p>
 *
 * @author AlessandroBorges
 * @since 1.1
 *
 * @see SearchResponse
 * @see OpenAIJsonMapper
 */
public class PerplexityJsonMapper {

    private final ObjectMapper objectMapper;

    /**
     * Default constructor with a new ObjectMapper.
     */
    public PerplexityJsonMapper() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Constructor with custom ObjectMapper.
     *
     * @param objectMapper the Jackson ObjectMapper to use
     */
    public PerplexityJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a Chat object and parameters into a Perplexity chat completion request payload.
     * <p>
     * Perplexity API format is similar to OpenAI but with additional search parameters:
     * <ul>
     * <li>search_domain_filter - Array of domains to include/exclude</li>
     * <li>search_recency_filter - Time period for results ("hour", "day", "week", "month", "year")</li>
     * <li>return_images - Boolean to include images</li>
     * <li>return_related_questions - Boolean to include related questions</li>
     * <li>search_context - Context size ("low", "medium", "high")</li>
     * </ul>
     * </p>
     *
     * @param chat   the chat session containing messages
     * @param query  additional user query to append (can be null)
     * @param params additional parameters like temperature, max_tokens, search filters, etc.
     * @return Map representing the JSON request payload
     * @throws LLMException if message conversion fails
     */
    public Map<String, Object> toChatCompletionRequest(Chat chat, String query, MapParam params) throws LLMException {
        Map<String, Object> request = new HashMap<>();

        // Get model
        Object modelObj = params != null ? params.getModel() : null;
        modelObj = modelObj != null ? modelObj : (chat != null ? chat.getModel() : null);
        if (modelObj == null) {
            throw new IllegalArgumentException("Model must be specified for chat completion request");
        }
        request.put("model", modelObj.toString());

        // Convert messages
        List<Map<String, Object>> messages = new ArrayList<>();

        if (chat != null && chat.getMessages() != null) {
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

                if (value == null) {
                    continue;
                }

                // Skip "model" as we already added it
                if ("model".equals(key)) {
                    continue;
                }

                // Map parameters
                switch (key.toLowerCase()) {
                    case "temperature":
                    case "max_tokens":
                    case "top_p":
                    case "top_k":
                    case "stream":
                    case "presence_penalty":
                    case "frequency_penalty":
                    // Perplexity-specific parameters
                    case "search_domain_filter":
                    case "search_recency_filter":
                    case "return_images":
                    case "return_related_questions":
                    case "search_context":
                    case "search_mode":
                        request.put(key, value);
                        break;
                    case "reasoning_effort":
                        // Perplexity supports reasoning_effort for reasoning models
                        if (value != null) {
                            request.put("reasoning_effort", value.toString());
                        }
                        break;
                    default:
                        // Include other parameters as-is
                        request.put(key, value);
                        break;
                }
            }
        }

        // Order properties for readability
        request = orderProperty(request, "model", "messages", "temperature", "max_tokens", "top_p",
                "stream", "search_domain_filter", "search_recency_filter", "return_images",
                "return_related_questions", "search_context", "reasoning_effort");

        return request;
    }

    /**
     * Converts a simple prompt and query into a Perplexity chat completion request.
     *
     * @param systemPrompt the system prompt
     * @param query        the user query
     * @param params       additional parameters
     * @return Map representing the JSON request payload
     * @throws LLMException if conversion fails
     */
    public Map<String, Object> toCompletionRequest(String systemPrompt, String query, MapParam params)
            throws LLMException {
        Map<String, Object> request = new HashMap<>();

        // Get model
        Object modelObj = params != null ? params.getModel() : null;
        if (modelObj == null) {
            throw new IllegalArgumentException("Model must be specified for completion request");
        }
        request.put("model", modelObj.toString());

        // Create messages array
        List<Map<String, Object>> messages = new ArrayList<>();

        // Add system message if provided
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt.trim());
            messages.add(systemMsg);
        }

        // Add user query
        if (query != null && !query.trim().isEmpty()) {
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", query.trim());
            messages.add(userMsg);
        }

        request.put("messages", messages);

        // Add parameters
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null || "model".equals(key)) {
                    continue;
                }

                // Add relevant parameters
                switch (key.toLowerCase()) {
                    case "temperature":
                    case "max_tokens":
                    case "top_p":
                    case "stream":
                    case "search_domain_filter":
                    case "search_recency_filter":
                    case "return_images":
                    case "return_related_questions":
                    case "search_context":
                    case "search_mode":
                    case "reasoning_effort":
                        request.put(key, value);
                        break;
                }
            }
        }

        return request;
    }

    /**
     * Converts a Perplexity chat completion response to a SearchResponse object.
     * <p>
     * Extracts standard completion fields plus Perplexity-specific fields:
     * <ul>
     * <li>citations - Array of source URLs</li>
     * <li>search_results - Array of search result metadata</li>
     * <li>related_questions - Array of suggested follow-up questions</li>
     * <li>images - Array of related images (if requested)</li>
     * </ul>
     * </p>
     *
     * @param response the response Map from Perplexity API
     * @return SearchResponse object with citations and search metadata
     * @throws LLMException if conversion fails
     */
    @SuppressWarnings("unchecked")
    public SearchResponse fromChatCompletionResponse(Map<String, Object> response) throws LLMException {
        SearchResponse searchResponse = new SearchResponse();

        try {
        	// extract id
			if (response.containsKey("id")) {
				searchResponse.setId((String) response.get("id"));
			}
            // Extract choices
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

                // Extract usage information
                MapParam usage = new MapParam((Map<String, Object>) response.get("usage"));
                searchResponse.setUsage(usage);

                // Extract message content
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null) {
                        searchResponse.setResponse(new ContentWrapper(ContentType.TEXT, content));
                    }

                    // Extract reasoning content if present
                    String[] reasoningKeys = {"reasoning_content", "reasoning", "thinking"};
                    for (String key : reasoningKeys) {
                        if (message.containsKey(key)) {
                            String reasoning = (String) message.get(key);
                            searchResponse.setReasoningContent(reasoning);
                            break;
                        }
                    }
                }

                // Extract finish reason
                String finishReason = (String) firstChoice.get("finish_reason");
                searchResponse.setEndReason(finishReason);
            }

            // Extract Perplexity-specific fields
            extractPerplexityFields(response, searchResponse);

            // Store full response info
            MapParam info = new MapParam();
            info.putAll(response);
            searchResponse.setInfo(info);

        } catch (Exception e) {
            throw new LLMException("Failed to parse Perplexity response: " + e.getMessage(), e);
        }

        return searchResponse;
    }

    /**
     * Extracts Perplexity-specific fields from the response.
     *
     * @param response       the raw API response
     * @param searchResponse the SearchResponse to populate
     */
    @SuppressWarnings("unchecked")
    private void extractPerplexityFields(Map<String, Object> response, SearchResponse searchResponse) {
        // Extract citations
        List<String> citations = (List<String>) response.get("citations");
        if (citations != null && !citations.isEmpty()) {
            searchResponse.setCitations(new ArrayList<>(citations));
        }

        // Extract search results
        List<Map<String, Object>> searchResults = (List<Map<String, Object>>) response.get("search_results");
        if (searchResults != null && !searchResults.isEmpty()) {
            List<SearchResponse.SearchResultMetadata> results = new ArrayList<>();
            for (Map<String, Object> result : searchResults) {
                SearchResponse.SearchResultMetadata metadata = new SearchResponse.SearchResultMetadata();
                metadata.setTitle((String) result.get("title"));
                metadata.setUrl((String) result.get("url"));
                metadata.setDate((String) result.get("date"));
                metadata.setSnippet((String) result.get("snippet"));
                results.add(metadata);
            }
            searchResponse.setSearchResults(results);
        }

        // Extract related questions
        List<String> relatedQuestions = (List<String>) response.get("related_questions");
        if (relatedQuestions != null && !relatedQuestions.isEmpty()) {
            searchResponse.setRelatedQuestions(new ArrayList<>(relatedQuestions));
        }

        // Extract images if present
        List<Map<String, Object>> images = (List<Map<String, Object>>) response.get("images");
        if (images != null && !images.isEmpty()) {
            List<SearchResponse.ImageResult> imageResults = new ArrayList<>();
            for (Map<String, Object> image : images) {
                SearchResponse.ImageResult imageResult = new SearchResponse.ImageResult();
                imageResult.setUrl((String) image.get("url"));
                imageResult.setTitle((String) image.get("title"));
                imageResult.setAlt((String) image.get("alt"));
                imageResults.add(imageResult);
            }
            searchResponse.setImages(imageResults);
        }

        // Extract search queries count if present
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            Object numSearchQueries = usage.get("num_search_queries");
            if (numSearchQueries instanceof Number) {
                searchResponse.setSearchQueriesCount(((Number) numSearchQueries).intValue());
            }
        }
    }

    /**
     * Converts a streaming SSE chunk to a SearchResponse.
     *
     * @param sseData the SSE data line (without "data: " prefix)
     * @return SearchResponse object or null if chunk should be ignored
     * @throws LLMException if parsing fails
     */
    @SuppressWarnings("unchecked")
    public SearchResponse fromStreamingChunk(String sseData) throws LLMException {
        if (sseData == null || sseData.trim().isEmpty() || "[DONE]".equals(sseData.trim())) {
            return null;
        }

        try {
            Map<String, Object> chunk = objectMapper.readValue(sseData, Map.class);

            SearchResponse response = new SearchResponse();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> delta = (Map<String, Object>) firstChoice.get("delta");

                if (delta != null) {
                    String content = (String) delta.get("content");
                    if (content != null) {
                        response.setResponse(new ContentWrapper(ContentType.TEXT, content));
                    }

                    // Check for reasoning content in delta
                    String[] reasoningKeys = {"reasoning_content", "reasoning"};
                    for (String key : reasoningKeys) {
                        if (delta.containsKey(key)) {
                            String reasoning = (String) delta.get(key);
                            response.setReasoningContent(reasoning);
                            break;
                        }
                    }
                }

                String finishReason = (String) firstChoice.get("finish_reason");
                response.setEndReason(finishReason);
            }

            // Perplexity may include citations/search results in streaming (typically at the end)
            extractPerplexityFields(chunk, response);

            // Store chunk info
            MapParam info = new MapParam();
            info.putAll(chunk);
            response.setInfo(info);

            return response;

        } catch (JsonProcessingException e) {
            throw new LLMException("Failed to parse streaming chunk: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a Message to a Map for API request.
     *
     * @param message the Message object
     * @return Map representation
     * @throws LLMException if conversion fails
     */
    private Map<String, Object> messageToMap(Message message) throws LLMException {
        Map<String, Object> msgMap = new HashMap<>();

        // Add role
        if (message.getRole() != null) {
            msgMap.put("role", message.getRole().name().toLowerCase());
        }

        // Add content
        ContentWrapper wrapper = message.getContent();
        if (wrapper != null) {
            if (wrapper.getType() == ContentType.TEXT) {
                msgMap.put("content", wrapper.getContent());
            } else {
                // For non-text content, convert to appropriate format
                msgMap.put("content", wrapper.getContent().toString());
            }
        }

        return msgMap;
    }

    /**
     * Orders the properties of a Map according to the specified key order.
     * Keys not in the order list are appended at the end in their original order.
     *
     * @param source    the original Map
     * @param keysOrder the desired key order
     * @return a new LinkedHashMap with properties ordered
     */
    protected LinkedHashMap<String, Object> orderProperty(Map<String, Object> source, String... keysOrder) {
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        for (String key : keysOrder) {
            if (source.containsKey(key)) {
                copy.put(key, source.remove(key));
            }
        }
        // Copy remaining entries in original order
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    /**
     * Converts the request Map to JSON string.
     *
     * @param request the request Map
     * @return JSON string
     * @throws LLMException if serialization fails
     */
    public String toJson(Map<String, Object> request) throws LLMException {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new LLMException("Failed to convert request to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a JSON response string to a Map.
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
            throw new LLMException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }
}
