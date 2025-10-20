package bor.tools.simplellm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Extended response object for web search-enhanced completions.
 * <p>
 * This class extends {@link CompletionResponse} to include web search-specific
 * information such as citations, related questions, and search result metadata.
 * It is designed to work with LLM services that provide real-time web search
 * capabilities (e.g., Perplexity AI).
 * </p>
 * <p>
 * In addition to the standard completion response fields (content, usage, etc.),
 * SearchResponse provides:
 * <ul>
 * <li><b>Citations</b> - URLs of sources used to generate the response</li>
 * <li><b>Search Results</b> - Metadata about the web pages consulted</li>
 * <li><b>Related Questions</b> - Suggested follow-up questions</li>
 * <li><b>Images</b> - Related images (if requested)</li>
 * <li><b>Search Queries Count</b> - Number of searches performed</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example usage:</b>
 * </p>
 * <pre>{@code
 * SearchResponse response = webSearchService.webSearch("Latest AI news", params);
 *
 * // Access standard fields
 * System.out.println(response.getContent());
 * System.out.println("Tokens used: " + response.getUsage());
 *
 * // Access search-specific fields
 * System.out.println("\nSources:");
 * for (String citation : response.getCitations()) {
 *     System.out.println("- " + citation);
 * }
 *
 * System.out.println("\nSearch Results:");
 * for (SearchResultMetadata result : response.getSearchResults()) {
 *     System.out.println(result.getTitle() + " - " + result.getUrl());
 * }
 *
 * System.out.println("\nRelated Questions:");
 * for (String question : response.getRelatedQuestions()) {
 *     System.out.println("- " + question);
 * }
 * }</pre>
 *
 * @author AlessandroBorges
 * @since 1.1
 *
 * @see CompletionResponse
 * @see WebSearch
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({
    "chatId", "model", "reasoning_effort", "reasoning_content", "response",
    "citations", "searchResults", "relatedQuestions", "images",
    "searchQueriesCount", "endReason", "usage", "info"
})
public class SearchResponse extends CompletionResponse {

    /**
     * List of URLs cited in the response.
     * <p>
     * These citations provide sources for the information in the response,
     * allowing users to verify facts and explore topics further. Each citation
     * is a complete URL to the source document.
     * </p>
     * <p>
     * Example: {@code ["https://arxiv.org/abs/2301.00001", "https://nature.com/articles/..."]}
     * </p>
     */
    private List<String> citations;

    /**
     * Related questions suggested by the model.
     * <p>
     * These questions help users explore related topics or dig deeper
     * into specific aspects of their query. They are generated based on
     * the search results and the user's original question.
     * </p>
     * <p>
     * Example: {@code ["What are the applications?", "How does it compare to...?"]}
     * </p>
     */
    private List<String> relatedQuestions;

    /**
     * Metadata about search results used to generate the response.
     * <p>
     * Each entry contains information about a source document including
     * title, URL, publication date, and a snippet of relevant content.
     * This provides more context than simple citations alone.
     * </p>
     *
     * @see SearchResultMetadata
     */
    private List<SearchResultMetadata> searchResults;

    /**
     * Images related to the search query (if requested).
     * <p>
     * This field is populated when {@link MapParam#returnImages(Boolean)} is set to true.
     * Each image result includes the URL, title, and alt text.
     * </p>
     *
     * @see ImageResult
     */
    private List<ImageResult> images;

    /**
     * Number of search queries performed by the model.
     * <p>
     * Some providers (e.g., Perplexity's deep research models) may perform
     * multiple search queries to gather comprehensive information. This field
     * indicates how many queries were executed.
     * </p>
     */
    private Integer searchQueriesCount;

    /**
     * Default constructor.
     * Initializes empty lists for collections to avoid null pointer exceptions.
     */
    public SearchResponse() {
        super();
        this.citations = new ArrayList<>();
        this.relatedQuestions = new ArrayList<>();
        this.searchResults = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    /**
     * Adds a citation URL to the citations list.
     *
     * @param citation the citation URL to add
     * @return this SearchResponse for method chaining
     */
    public SearchResponse addCitation(String citation) {
        if (this.citations == null) {
            this.citations = new ArrayList<>();
        }
        this.citations.add(citation);
        return this;
    }

    /**
     * Adds a related question to the related questions list.
     *
     * @param question the related question to add
     * @return this SearchResponse for method chaining
     */
    public SearchResponse addRelatedQuestion(String question) {
        if (this.relatedQuestions == null) {
            this.relatedQuestions = new ArrayList<>();
        }
        this.relatedQuestions.add(question);
        return this;
    }

    /**
     * Adds a search result metadata entry.
     *
     * @param result the search result metadata to add
     * @return this SearchResponse for method chaining
     */
    public SearchResponse addSearchResult(SearchResultMetadata result) {
        if (this.searchResults == null) {
            this.searchResults = new ArrayList<>();
        }
        this.searchResults.add(result);
        return this;
    }

    /**
     * Adds an image result.
     *
     * @param image the image result to add
     * @return this SearchResponse for method chaining
     */
    public SearchResponse addImage(ImageResult image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
        return this;
    }

    /**
     * Checks if this response contains any citations.
     *
     * @return true if citations are present, false otherwise
     */
    public boolean hasCitations() {
        return citations != null && !citations.isEmpty();
    }

    /**
     * Checks if this response contains any related questions.
     *
     * @return true if related questions are present, false otherwise
     */
    public boolean hasRelatedQuestions() {
        return relatedQuestions != null && !relatedQuestions.isEmpty();
    }

    /**
     * Checks if this response contains any search results metadata.
     *
     * @return true if search results are present, false otherwise
     */
    public boolean hasSearchResults() {
        return searchResults != null && !searchResults.isEmpty();
    }

    /**
     * Checks if this response contains any images.
     *
     * @return true if images are present, false otherwise
     */
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    /**
     * Metadata about a single search result.
     * <p>
     * Contains information extracted from a web page that was used
     * to generate the response, including title, URL, publication date,
     * and a relevant snippet of text.
     * </p>
     */
    @Data
    public static class SearchResultMetadata {
        /**
         * The title of the web page or document.
         */
        private String title;

        /**
         * The URL of the web page or document.
         */
        private String url;

        /**
         * The publication date (format may vary by provider).
         * Example: "2025-01-15" or "2025-01-15T10:30:00Z"
         */
        private String date;

        /**
         * A relevant snippet or excerpt from the document.
         * This is typically a sentence or paragraph that is most relevant
         * to the search query.
         */
        private String snippet;

        /**
         * Constructor with all fields.
         *
         * @param title the title of the document
         * @param url the URL of the document
         * @param date the publication date
         * @param snippet a relevant excerpt
         */
        public SearchResultMetadata(String title, String url, String date, String snippet) {
            this.title = title;
            this.url = url;
            this.date = date;
            this.snippet = snippet;
        }

        /**
         * Default constructor.
         */
        public SearchResultMetadata() {
        }
    }

    /**
     * Metadata about an image result.
     * <p>
     * Contains information about an image related to the search query,
     * including the URL, title, and alternative text.
     * </p>
     */
    @Data
    public static class ImageResult {
        /**
         * The URL of the image.
         */
        private String url;

        /**
         * The title or caption of the image.
         */
        private String title;

        /**
         * Alternative text describing the image.
         */
        private String alt;

        /**
         * Constructor with all fields.
         *
         * @param url the image URL
         * @param title the image title
         * @param alt the alternative text
         */
        public ImageResult(String url, String title, String alt) {
            this.url = url;
            this.title = title;
            this.alt = alt;
        }

        /**
         * Default constructor.
         */
        public ImageResult() {
        }
    }
}
