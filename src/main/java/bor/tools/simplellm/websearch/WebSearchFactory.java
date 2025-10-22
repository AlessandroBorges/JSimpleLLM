package bor.tools.simplellm.websearch;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.websearch.impl.PerplexityLLMService;

/**
 * Factory class for creating Web Search service implementations.
 * <p>
 * This factory provides a centralized way to instantiate different web search
 * service providers that implement the {@link WebSearch} interface. Unlike
 * {@code LLMServiceFactory} which creates general-purpose LLM services, this
 * factory focuses exclusively on services with real-time web search capabilities.
 * </p>
 *
 * <h2>Difference from LLMServiceFactory</h2>
 * <p>
 * While {@code LLMServiceFactory} returns {@code LLMService} instances (which may
 * or may not support web search), {@code WebSearchFactory} returns {@code WebSearch}
 * instances (guaranteed to support web search operations).
 * </p>
 * <pre>{@code
 * // Via LLMServiceFactory (requires cast)
 * LLMService service = LLMServiceFactory.createPerplexity();
 * WebSearch search = (WebSearch) service;
 *
 * // Via WebSearchFactory (direct, no cast)
 * WebSearch search = WebSearchFactory.createPerplexity();
 * }</pre>
 *
 * <h2>Currently Supported Providers</h2>
 * <ul>
 * <li><b>Perplexity AI</b> - Real-time web search with citations, domain filtering, and related questions</li>
 * </ul>
 *
 * <h2>Future Providers</h2>
 * <p>
 * The following providers are planned for future implementation:
 * </p>
 * <ul>
 * <li><b>DeepSeek</b> - Chinese web search with advanced reasoning capabilities</li>
 * <li><b>Google Gemini</b> - Google's web search integration via Grounding API</li>
 * <li><b>Wikipedia</b> - Structured knowledge base search via MediaWiki API</li>
 * <li><b>Tavily</b> - Research-focused search API for academic and technical content</li>
 * <li><b>Brave Search</b> - Privacy-focused independent search API</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Simple search
 * WebSearch search = WebSearchFactory.createPerplexity();
 * SearchResponse response = search.webSearch("Latest AI news", null);
 * System.out.println(response.getResponse().getText());
 *
 * // With custom configuration
 * LLMConfig config = LLMConfig.builder()
 *     .apiToken("your-api-key")
 *     .defaultModelName("sonar-pro")
 *     .build();
 * WebSearch search = WebSearchFactory.createPerplexity(config);
 *
 * // Multiple search providers
 * WebSearch perplexity = WebSearchFactory.createPerplexity();
 * WebSearch gemini = WebSearchFactory.createGemini();  // Future
 * }</pre>
 *
 * @author AlessandroBorges
 * @since 1.1
 *
 * @see WebSearch
 * @see SearchResponse
 * @see bor.tools.simplellm.LLMServiceFactory
 */
public class WebSearchFactory {

    /**
     * Enumeration of supported web search service providers.
     * <p>
     * This enum lists all web search providers that can be created through
     * this factory, including both currently implemented and planned future
     * implementations.
     * </p>
     */
    public enum WEBSEARCH_PROVIDER {
        /**
         * Perplexity AI - Real-time web search with citations.
         * <p>
         * <b>Status:</b> ✅ Implemented<br>
         * <b>Models:</b> sonar, sonar-pro, sonar-deep-research, sonar-reasoning, sonar-reasoning-pro<br>
         * <b>Features:</b> Citations, domain filters, recency filters, related questions, images
         * </p>
         */
        PERPLEXITY,

        /**
         * DeepSeek - Chinese web search with advanced reasoning.
         * <p>
         * <b>Status:</b> 🔜 Planned<br>
         * <b>Features:</b> Chinese language optimization, reasoning models, web search
         * </p>
         */
        DEEPSEEK,

        /**
         * Google Gemini - Google's web search integration.
         * <p>
         * <b>Status:</b> 🔜 Planned<br>
         * <b>Features:</b> Grounding with Google Search, grounding chunks, grounding images
         * </p>
         */
        GEMINI,

        /**
         * Wikipedia - Structured knowledge base search.
         * <p>
         * <b>Status:</b> 🔜 Planned<br>
         * <b>Features:</b> MediaWiki API, structured articles, infoboxes, categories
         * </p>
         */
        WIKIPEDIA,

        /**
         * Tavily - Research-focused search API.
         * <p>
         * <b>Status:</b> 🔜 Planned<br>
         * <b>Features:</b> Academic sources, technical content, research optimization
         * </p>
         */
        TAVILY,

        /**
         * Brave Search - Privacy-focused independent search.
         * <p>
         * <b>Status:</b> 🔜 Planned<br>
         * <b>Features:</b> Privacy-first, independent index, no tracking
         * </p>
         */
        BRAVE
    }

    /**
     * Creates a web search service based on the specified provider and configuration.
     * <p>
     * This is the primary factory method that handles all supported web search providers.
     * For providers that are not yet implemented, it throws an {@code UnsupportedOperationException}.
     * </p>
     * <p>
     * <b>Example:</b>
     * </p>
     * <pre>{@code
     * LLMConfig config = LLMConfig.builder()
     *     .apiToken("your-api-key")
     *     .defaultModelName("sonar-pro")
     *     .build();
     *
     * WebSearch search = WebSearchFactory.createWebSearch(
     *     WEBSEARCH_PROVIDER.PERPLEXITY,
     *     config
     * );
     * }</pre>
     *
     * @param provider the web search provider to use (must not be null)
     * @param config optional configuration (null to use provider's defaults)
     * @return a WebSearch implementation for the specified provider
     * @throws IllegalArgumentException if provider is null
     * @throws UnsupportedOperationException if the provider is not yet implemented
     */
    public static WebSearch createWebSearch(WEBSEARCH_PROVIDER provider, LLMConfig config) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider must not be null");
        }

        switch (provider) {
            case PERPLEXITY:
                return config != null ? createPerplexity(config) : createPerplexity();

            case DEEPSEEK:
                throw new UnsupportedOperationException(
                    "DeepSeek web search is not yet implemented. " +
                    "This feature is planned for a future release.");

            case GEMINI:
                throw new UnsupportedOperationException(
                    "Google Gemini web search is not yet implemented. " +
                    "This feature is planned for a future release.");

            case WIKIPEDIA:
                throw new UnsupportedOperationException(
                    "Wikipedia search is not yet implemented. " +
                    "This feature is planned for a future release.");

            case TAVILY:
                throw new UnsupportedOperationException(
                    "Tavily search is not yet implemented. " +
                    "This feature is planned for a future release.");

            case BRAVE:
                throw new UnsupportedOperationException(
                    "Brave Search is not yet implemented. " +
                    "This feature is planned for a future release.");

            default:
                throw new IllegalArgumentException(
                    "Unsupported web search provider: " + provider);
        }
    }

    // ========================================================================
    // PERPLEXITY AI - Real-time Web Search with Citations
    // ========================================================================

    /**
     * Creates a Perplexity AI web search service with custom configuration.
     * <p>
     * Perplexity AI provides real-time web search with automatic citations,
     * domain filtering, recency controls, and related questions. All sonar
     * models include native web search capabilities.
     * </p>
     *
     * <h3>Available Models</h3>
     * <ul>
     * <li><b>sonar</b> - Fast general-purpose model (128k context)</li>
     * <li><b>sonar-pro</b> - Advanced analysis model (200k context)</li>
     * <li><b>sonar-deep-research</b> - Exhaustive research (128k context)</li>
     * <li><b>sonar-reasoning</b> - Reasoning with web search (128k context)</li>
     * <li><b>sonar-reasoning-pro</b> - Advanced reasoning (128k context)</li>
     * <li><b>r1-1776</b> - Offline model without web search (128k context)</li>
     * </ul>
     *
     * <h3>Key Features</h3>
     * <ul>
     * <li>Real-time web search integration</li>
     * <li>Automatic source citations</li>
     * <li>Domain filtering (include/exclude domains)</li>
     * <li>Recency filters (hour, day, week, month, year)</li>
     * <li>Date range filtering (before/after specific dates)</li>
     * <li>Related questions suggestions</li>
     * <li>Optional image results</li>
     * <li>Geographic localization support</li>
     * <li>Streaming support</li>
     * </ul>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * // Custom configuration
     * MapParam defaults = new MapParam()
     *     .searchMode("academic")
     *     .searchDomainFilter(new String[]{"arxiv.org", "scholar.google.com"})
     *     .temperature(0.3f);
     *
     * LLMConfig config = LLMConfig.builder()
     *     .apiToken("pplx-xxxxx")
     *     .defaultModelName("sonar-pro")
     *     .defaultParams(defaults)
     *     .build();
     *
     * WebSearch search = WebSearchFactory.createPerplexity(config);
     *
     * // Perform search
     * SearchResponse response = search.webSearch(
     *     "Recent advances in quantum computing",
     *     new MapParam().maxTokens(1000)
     * );
     *
     * // Access results
     * System.out.println(response.getResponse().getText());
     * System.out.println("Citations: " + response.getCitations());
     * }</pre>
     *
     * @param config the LLM configuration containing:
     *               <ul>
     *               <li>API key (or use PERPLEXITY_API_KEY env var)</li>
     *               <li>Base URL (defaults to https://api.perplexity.ai)</li>
     *               <li>Default model (defaults to "sonar")</li>
     *               <li>Default parameters (search mode, filters, etc.)</li>
     *               </ul>
     * @return a WebSearch instance configured for Perplexity AI
     * @throws IllegalArgumentException if config contains invalid parameters
     * @throws bor.tools.simplellm.exceptions.LLMAuthenticationException if API key is missing or invalid
     *
     * @see PerplexityLLMService
     * @see WebSearch
     * @see SearchResponse
     */
    public static WebSearch createPerplexity(LLMConfig config) {
        return new PerplexityLLMService(config);
    }

    /**
     * Creates a Perplexity AI web search service with default configuration.
     * <p>
     * This is a convenience method that creates a Perplexity service with
     * pre-configured settings suitable for most use cases. The API key is
     * read from the {@code PERPLEXITY_API_KEY} environment variable.
     * </p>
     *
     * <h3>Default Configuration</h3>
     * <ul>
     * <li><b>API Key:</b> From PERPLEXITY_API_KEY environment variable</li>
     * <li><b>Base URL:</b> https://api.perplexity.ai</li>
     * <li><b>Default Model:</b> sonar</li>
     * <li><b>Search Mode:</b> web</li>
     * <li><b>Temperature:</b> 0.7</li>
     * <li><b>Related Questions:</b> enabled</li>
     * </ul>
     *
     * <h3>Environment Setup</h3>
     * <pre>
     * # Linux/Mac
     * export PERPLEXITY_API_KEY="pplx-xxxxxxxxxxxxx"
     *
     * # Windows PowerShell
     * $env:PERPLEXITY_API_KEY="pplx-xxxxxxxxxxxxx"
     *
     * # Windows CMD
     * set PERPLEXITY_API_KEY=pplx-xxxxxxxxxxxxx
     * </pre>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * // Simple usage with defaults
     * WebSearch search = WebSearchFactory.createPerplexity();
     *
     * SearchResponse response = search.webSearch(
     *     "What are the latest developments in AI?",
     *     null  // Use all defaults
     * );
     *
     * System.out.println(response.getResponse().getText());
     *
     * // Override specific parameters
     * MapParam params = new MapParam()
     *     .model("sonar-pro")
     *     .searchRecencyFilter("week")
     *     .maxTokens(1500);
     *
     * response = search.webSearch("Recent AI breakthroughs", params);
     * }</pre>
     *
     * @return a WebSearch instance with default Perplexity configuration
     * @throws bor.tools.simplellm.exceptions.LLMAuthenticationException
     *         if PERPLEXITY_API_KEY environment variable is not set
     *
     * @see #createPerplexity(LLMConfig)
     * @see PerplexityLLMService#getDefaultLLMConfig()
     */
    public static WebSearch createPerplexity() {
        return new PerplexityLLMService();
    }

    // ========================================================================
    // DEEPSEEK - Chinese Web Search (Future Implementation)
    // ========================================================================

    /**
     * Creates a DeepSeek web search service with custom configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     * <p>
     * DeepSeek will provide Chinese-language optimized web search with
     * advanced reasoning capabilities. It will support both Chinese and
     * English queries with specialized handling for Chinese content.
     * </p>
     *
     * <h3>Planned Features</h3>
     * <ul>
     * <li>Chinese language optimization</li>
     * <li>Baidu/Bing search integration</li>
     * <li>Advanced reasoning models</li>
     * <li>Bilingual support (Chinese/English)</li>
     * </ul>
     *
     * @param config the configuration for DeepSeek service
     * @return a WebSearch instance for DeepSeek
     * @throws UnsupportedOperationException always (not yet implemented)
     */
    public static WebSearch createDeepSeek(LLMConfig config) {
        throw new UnsupportedOperationException(
            "DeepSeek web search is not yet implemented. " +
            "This feature is planned for a future release. " +
            "Track progress at: https://github.com/your-repo/issues");
    }

    /**
     * Creates a DeepSeek web search service with default configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     *
     * @return a WebSearch instance for DeepSeek
     * @throws UnsupportedOperationException always (not yet implemented)
     * @see #createDeepSeek(LLMConfig)
     */
    public static WebSearch createDeepSeek() {
        throw new UnsupportedOperationException(
            "DeepSeek web search is not yet implemented. " +
            "This feature is planned for a future release.");
    }

    // ========================================================================
    // GOOGLE GEMINI - Google Search Integration (Future Implementation)
    // ========================================================================

    /**
     * Creates a Google Gemini web search service with custom configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     * <p>
     * Gemini will provide Google's web search integration via the Grounding
     * API, allowing access to Google Search results with grounding chunks
     * and grounding images.
     * </p>
     *
     * <h3>Planned Features</h3>
     * <ul>
     * <li>Google Search grounding</li>
     * <li>Grounding chunks with source attribution</li>
     * <li>Grounding images</li>
     * <li>Dynamic retrieval</li>
     * <li>Web verification</li>
     * </ul>
     *
     * @param config the configuration for Gemini service
     * @return a WebSearch instance for Gemini
     * @throws UnsupportedOperationException always (not yet implemented)
     */
    public static WebSearch createGemini(LLMConfig config) {
        throw new UnsupportedOperationException(
            "Google Gemini web search is not yet implemented. " +
            "This feature is planned for a future release. " +
            "Track progress at: https://github.com/your-repo/issues");
    }

    /**
     * Creates a Google Gemini web search service with default configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     *
     * @return a WebSearch instance for Gemini
     * @throws UnsupportedOperationException always (not yet implemented)
     * @see #createGemini(LLMConfig)
     */
    public static WebSearch createGemini() {
        throw new UnsupportedOperationException(
            "Google Gemini web search is not yet implemented. " +
            "This feature is planned for a future release.");
    }

    // ========================================================================
    // WIKIPEDIA - Structured Knowledge Base (Future Implementation)
    // ========================================================================

    /**
     * Creates a Wikipedia search service with custom configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     * <p>
     * Wikipedia search will provide structured access to Wikipedia articles
     * via the MediaWiki API, with support for article content, infoboxes,
     * categories, and cross-references.
     * </p>
     *
     * <h3>Planned Features</h3>
     * <ul>
     * <li>MediaWiki API integration</li>
     * <li>Article full text and summaries</li>
     * <li>Infobox extraction</li>
     * <li>Category browsing</li>
     * <li>Cross-references and links</li>
     * <li>Multi-language support</li>
     * </ul>
     *
     * @param config the configuration for Wikipedia service
     * @return a WebSearch instance for Wikipedia
     * @throws UnsupportedOperationException always (not yet implemented)
     */
    public static WebSearch createWikipedia(LLMConfig config) {
        throw new UnsupportedOperationException(
            "Wikipedia search is not yet implemented. " +
            "This feature is planned for a future release. " +
            "Track progress at: https://github.com/your-repo/issues");
    }

    /**
     * Creates a Wikipedia search service with default configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     *
     * @return a WebSearch instance for Wikipedia
     * @throws UnsupportedOperationException always (not yet implemented)
     * @see #createWikipedia(LLMConfig)
     */
    public static WebSearch createWikipedia() {
        throw new UnsupportedOperationException(
            "Wikipedia search is not yet implemented. " +
            "This feature is planned for a future release.");
    }

    // ========================================================================
    // TAVILY - Research-Focused Search (Future Implementation)
    // ========================================================================

    /**
     * Creates a Tavily search service with custom configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     * <p>
     * Tavily will provide research-focused web search optimized for academic
     * and technical content, with emphasis on authoritative sources and
     * comprehensive coverage.
     * </p>
     *
     * <h3>Planned Features</h3>
     * <ul>
     * <li>Research-optimized search</li>
     * <li>Academic source prioritization</li>
     * <li>Technical content extraction</li>
     * <li>Source quality scoring</li>
     * <li>Comprehensive result coverage</li>
     * </ul>
     *
     * @param config the configuration for Tavily service
     * @return a WebSearch instance for Tavily
     * @throws UnsupportedOperationException always (not yet implemented)
     */
    public static WebSearch createTavily(LLMConfig config) {
        throw new UnsupportedOperationException(
            "Tavily search is not yet implemented. " +
            "This feature is planned for a future release. " +
            "Track progress at: https://github.com/your-repo/issues");
    }

    /**
     * Creates a Tavily search service with default configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     *
     * @return a WebSearch instance for Tavily
     * @throws UnsupportedOperationException always (not yet implemented)
     * @see #createTavily(LLMConfig)
     */
    public static WebSearch createTavily() {
        throw new UnsupportedOperationException(
            "Tavily search is not yet implemented. " +
            "This feature is planned for a future release.");
    }

    // ========================================================================
    // BRAVE SEARCH - Privacy-Focused Search (Future Implementation)
    // ========================================================================

    /**
     * Creates a Brave Search service with custom configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     * <p>
     * Brave Search will provide privacy-focused independent search with no
     * user tracking, using Brave's own search index.
     * </p>
     *
     * <h3>Planned Features</h3>
     * <ul>
     * <li>Privacy-first design (no tracking)</li>
     * <li>Independent search index</li>
     * <li>Web, news, and image search</li>
     * <li>Goggles (custom search rankings)</li>
     * <li>Freshness and relevance controls</li>
     * </ul>
     *
     * @param config the configuration for Brave Search service
     * @return a WebSearch instance for Brave Search
     * @throws UnsupportedOperationException always (not yet implemented)
     */
    public static WebSearch createBrave(LLMConfig config) {
        throw new UnsupportedOperationException(
            "Brave Search is not yet implemented. " +
            "This feature is planned for a future release. " +
            "Track progress at: https://github.com/your-repo/issues");
    }

    /**
     * Creates a Brave Search service with default configuration.
     * <p>
     * <b>Status:</b> Not yet implemented. Planned for future release.
     * </p>
     *
     * @return a WebSearch instance for Brave Search
     * @throws UnsupportedOperationException always (not yet implemented)
     * @see #createBrave(LLMConfig)
     */
    public static WebSearch createBrave() {
        throw new UnsupportedOperationException(
            "Brave Search is not yet implemented. " +
            "This feature is planned for a future release.");
    }
}
