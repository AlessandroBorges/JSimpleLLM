package bor.tools.simplellm.websearch;

import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Interface for LLM services with real-time web search capabilities.
 * <p>
 * This interface defines methods for performing web searches, filtering results,
 * and accessing citations and search metadata. Services implementing this interface
 * can augment their responses with real-time information from the web.
 * </p>
 * <p>
 * Not all LLM services support web search. Only implementations that provide
 * native search capabilities (such as Perplexity AI) should implement this interface.
 * </p>
 * <p>
 * <b>Example usage:</b>
 * </p>
 * <pre>{@code
 * LLMService service = LLMServiceFactory.createPerplexity();
 * if (service instanceof WebSearch) {
 *     WebSearch searchService = (WebSearch) service;
 *
 *     MapParam params = new MapParam()
 *         .model("sonar-pro")
 *         .searchDomainFilter(new String[]{"arxiv.org", "nature.com"})
 *         .searchRecencyFilter("week")
 *         .returnRelatedQuestions(true);
 *
 *     SearchResponse response = searchService.webSearch(
 *         "Latest developments in quantum computing",
 *         params
 *     );
 *
 *     System.out.println(response.getContent());
 *     System.out.println("Citations: " + response.getCitations());
 * }
 * }</pre>
 *
 * @author AlessandroBorges
 * @since 1.1
 *
 * @see LLMService
 * @see SearchResponse
 * @see MapParam
 */
public interface WebSearch {

    /**
     * Performs a web search-enhanced query.
     * <p>
     * This method executes a query that leverages real-time web search to provide
     * up-to-date information. The response includes the generated content along with
     * citations and search metadata.
     * </p>
     * <p>
     * The search behavior can be customized using parameters such as:
     * <ul>
     * <li>{@link MapParam#searchDomainFilter(String[])} - Filter by specific domains</li>
     * <li>{@link MapParam#searchRecencyFilter(String)} - Filter by time period</li>
     * <li>{@link MapParam#searchContext(String)} - Control search depth</li>
     * <li>{@link MapParam#returnRelatedQuestions(Boolean)} - Get related questions</li>
     * <li>{@link MapParam#returnImages(Boolean)} - Include image results</li>
     * </ul>
     * </p>
     *
     * @param query the search query or question
     * @param params additional parameters (model, domains, recency, etc.)
     * @return SearchResponse containing content, citations, and search results
     * @throws LLMException if there's an error during the search
     * @throws bor.tools.simplellm.exceptions.LLMAuthenticationException if API key is invalid
     * @throws bor.tools.simplellm.exceptions.LLMNetworkException if network error occurs
     * @throws bor.tools.simplellm.exceptions.LLMRateLimitException if rate limit is exceeded
     *
     * @see SearchResponse
     * @see MapParam
     */
    SearchResponse webSearch(String query, MapParam params) throws LLMException;

    /**
     * Performs a web search-enhanced chat completion.
     * <p>
     * This method continues a conversation with web search capabilities enabled,
     * allowing the model to access real-time information while maintaining context
     * from previous messages in the chat.
     * </p>
     * <p>
     * The chat history is preserved and the new query is added as a user message.
     * The model's response will be added to the chat as an assistant message with
     * citations and search metadata.
     * </p>
     *
     * @param chat the current chat session containing conversation history
     * @param query the user's query
     * @param params additional parameters (domains, recency, etc.)
     * @return SearchResponse containing content, citations, and search results
     * @throws LLMException if there's an error during the search
     * @throws IllegalArgumentException if chat or query is null
     *
     * @see Chat
     * @see SearchResponse
     */
    SearchResponse webSearchChat(Chat chat, String query, MapParam params) throws LLMException;

    /**
     * Performs a streaming web search query.
     * <p>
     * This method returns results in real-time as they are generated, allowing
     * for progressive rendering of the response. The {@link ResponseStream} callback
     * interface receives tokens as they arrive.
     * </p>
     * <p>
     * The final {@link SearchResponse} returned contains the complete accumulated
     * response including all citations and search metadata.
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>{@code
     * ResponseStream stream = new ResponseStream() {
     *     public void onToken(String token, ContentType type) {
     *         System.out.print(token);
     *     }
     *     public void onComplete() {
     *         System.out.println("\n[Done]");
     *     }
     *     public void onError(Throwable error) {
     *         System.err.println("Error: " + error.getMessage());
     *     }
     * };
     *
     * SearchResponse response = webSearchStream(stream, "query", params);
     * }</pre>
     *
     * @param responseStream callback interface for handling streamed tokens
     * @param query the search query
     * @param params additional parameters
     * @return SearchResponse containing final accumulated results
     * @throws LLMException if there's an error during the search
     *
     * @see ResponseStream
     * @see SearchResponse
     */
    SearchResponse webSearchStream(ResponseStream responseStream, String query, MapParam params)
            throws LLMException;

    /**
     * Performs a streaming web search chat completion.
     * <p>
     * Combines the features of {@link #webSearchChat(Chat, String, MapParam)} and
     * {@link #webSearchStream(ResponseStream, String, MapParam)}, providing both
     * conversation context and real-time streaming of results.
     * </p>
     *
     * @param responseStream callback interface for handling streamed tokens
     * @param chat the current chat session
     * @param query the user's query
     * @param params additional parameters
     * @return SearchResponse containing final accumulated results
     * @throws LLMException if there's an error during the search
     *
     * @see Chat
     * @see ResponseStream
     * @see SearchResponse
     */
    SearchResponse webSearchChatStream(ResponseStream responseStream, Chat chat, String query, MapParam params)
            throws LLMException;

    /**
     * Checks if the specified model supports web search capabilities.
     * <p>
     * This is a convenience method that checks if the model has the
     * {@link Model_Type#WEBSEARCH} type.
     * </p>
     *
     * @param modelName the model name to check
     * @return true if the model supports web search, false otherwise
     *
     * @see Model_Type#WEBSEARCH
     */
    default boolean supportsWebSearch(String modelName) {
        return isModelType(modelName, Model_Type.WEBSEARCH);
    }

    /**
     * Checks if the specified model supports citations.
     * <p>
     * This is a convenience method that checks if the model has the
     * {@link Model_Type#CITATIONS} type.
     * </p>
     *
     * @param modelName the model name to check
     * @return true if the model supports citations, false otherwise
     *
     * @see Model_Type#CITATIONS
     */
    default boolean supportsCitations(String modelName) {
        return isModelType(modelName, Model_Type.CITATIONS);
    }

    /**
     * Helper method to check if a model supports a specific type.
     * <p>
     * This method should be implemented to delegate to the underlying
     * {@link LLMService} implementation.
     * </p>
     *
     * @param modelName the model name
     * @param type the model type to check
     * @return true if model supports the type, false otherwise
     *
     * @see Model_Type
     * @see LLMService#isModelType(String, Model_Type)
     */
    boolean isModelType(String modelName, Model_Type type);
}
