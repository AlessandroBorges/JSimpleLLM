package bor.tools.simplellm.impl;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMProvider;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.websearch.SearchResponse;
import bor.tools.simplellm.websearch.WebSearch;
import bor.tools.simplellm.websearch.WebSearchFactory;
import bor.tools.simplellm.websearch.WebSearchFactory.WEBSEARCH_PROVIDER;

/**
 * Comprehensive examples demonstrating WebSearchFactory usage.
 * <p>
 * This class provides practical examples showing:
 * <ul>
 * <li>Basic usage of WebSearchFactory</li>
 * <li>Comparison with LLMServiceFactory approach</li>
 * <li>Different creation methods</li>
 * <li>Configuration options</li>
 * <li>Real-world use cases</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> These examples require a valid PERPLEXITY_API_KEY environment variable.
 * </p>
 *
 * @author AlessandroBorges
 */
public class WebSearchFactoryExample {

    /**
     * Example 1: Basic usage - Direct and simple
     * <p>
     * Demonstrates the simplest way to use WebSearchFactory for quick searches.
     * </p>
     */
    public static void example1_BasicUsage() throws LLMException {
        System.out.println("=== Example 1: Basic Usage ===\n");

        // Create search service directly
        WebSearch search = WebSearchFactory.createPerplexity();

        // Perform simple search
        SearchResponse response = search.webSearch(
            "What are the latest developments in quantum computing?",
            null  // Use default parameters
        );

        // Display results
        System.out.println("Answer:");
        System.out.println(response.getResponse().getText());

        System.out.println("\nSources:");
        for (String citation : response.getCitations()) {
            System.out.println("  • " + citation);
        }

        if (response.hasRelatedQuestions()) {
            System.out.println("\nRelated Questions:");
            for (String question : response.getRelatedQuestions()) {
                System.out.println("  • " + question);
            }
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 2: Comparison - LLMServiceFactory vs WebSearchFactory
     * <p>
     * Shows the difference between the two approaches and why WebSearchFactory
     * provides a cleaner API for web search use cases.
     * </p>
     */
    public static void example2_FactoryComparison() throws LLMException {
        System.out.println("=== Example 2: Factory Comparison ===\n");

        // OLD WAY - Via LLMServiceFactory (requires cast)
        System.out.println("Old approach (LLMServiceFactory):");
        LLMProvider service = LLMServiceFactory.createPerplexity();
        WebSearch searchOld = (WebSearch) service;  // Cast required
        System.out.println("  - Requires cast to WebSearch");
        System.out.println("  - Returns LLMProvider interface");
        System.out.println("  - Less semantic clarity");

        // NEW WAY - Via WebSearchFactory (direct)
        System.out.println("\nNew approach (WebSearchFactory):");
        WebSearch searchNew = WebSearchFactory.createPerplexity();  // No cast!
        System.out.println("  - No cast required");
        System.out.println("  - Returns WebSearch interface directly");
        System.out.println("  - Clear semantic intent");

        // Both return the same implementation
        System.out.println("\nBoth return same implementation:");
        System.out.println("  - Old: " + searchOld.getClass().getSimpleName());
        System.out.println("  - New: " + searchNew.getClass().getSimpleName());
        System.out.println("  - Same? " + (searchOld.getClass() == searchNew.getClass()));

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 3: Different creation methods
     * <p>
     * Demonstrates all available ways to create a WebSearch instance.
     * </p>
     */
    public static void example3_CreationMethods() throws LLMException {
        System.out.println("=== Example 3: Creation Methods ===\n");

        // Method 1: Direct factory method with defaults
        System.out.println("Method 1: createPerplexity()");
        WebSearch search1 = WebSearchFactory.createPerplexity();
        System.out.println("  ✓ Uses default configuration");
        System.out.println("  ✓ API key from PERPLEXITY_API_KEY env var");

        // Method 2: Direct factory method with custom config
        System.out.println("\nMethod 2: createPerplexity(config)");
        LLMConfig customConfig = LLMConfig.builder()
            .apiTokenEnvironment("PERPLEXITY_API_KEY")
            .defaultModelName("sonar-pro")
            .defaultParams(new MapParam().temperature(0.5f))
            .build();
        WebSearch search2 = WebSearchFactory.createPerplexity(customConfig);
        System.out.println("  ✓ Custom configuration");
        System.out.println("  ✓ Custom default model and parameters");

        // Method 3: Via enum with defaults
        System.out.println("\nMethod 3: createWebSearch(PERPLEXITY, null)");
        WebSearch search3 = WebSearchFactory.createWebSearch(
            WEBSEARCH_PROVIDER.PERPLEXITY,
            null
        );
        System.out.println("  ✓ Enum-based creation");
        System.out.println("  ✓ Null config uses defaults");

        // Method 4: Via enum with custom config
        System.out.println("\nMethod 4: createWebSearch(PERPLEXITY, config)");
        WebSearch search4 = WebSearchFactory.createWebSearch(
            WEBSEARCH_PROVIDER.PERPLEXITY,
            customConfig
        );
        System.out.println("  ✓ Enum-based with custom config");
        System.out.println("  ✓ Most flexible approach");

        System.out.println("\nAll methods return WebSearch instances:");
        System.out.println("  - search1: " + (search1 != null ? "✓" : "✗"));
        System.out.println("  - search2: " + (search2 != null ? "✓" : "✗"));
        System.out.println("  - search3: " + (search3 != null ? "✓" : "✗"));
        System.out.println("  - search4: " + (search4 != null ? "✓" : "✗"));

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 4: Custom configuration
     * <p>
     * Shows how to configure WebSearch with custom defaults and parameters.
     * </p>
     */
    public static void example4_CustomConfiguration() throws LLMException {
        System.out.println("=== Example 4: Custom Configuration ===\n");

        // Create config with academic search defaults
        MapParam academicDefaults = new MapParam()
            .searchMode("academic")
            .searchDomainFilter(new String[]{
                "arxiv.org",
                "scholar.google.com",
                "pubmed.ncbi.nlm.nih.gov",
                "-wikipedia.org"
            })
            .temperature(0.3f)  // More precise for academic content
            .returnRelatedQuestions(true);

        LLMConfig config = LLMConfig.builder()
            .apiTokenEnvironment("PERPLEXITY_API_KEY")
            .defaultModelName("sonar-pro")
            .defaultParams(academicDefaults)
            .build();

        WebSearch search = WebSearchFactory.createPerplexity(config);

        System.out.println("Configuration:");
        System.out.println("  • Default model: sonar-pro");
        System.out.println("  • Search mode: academic");
        System.out.println("  • Domains: arxiv.org, scholar.google.com, pubmed");
        System.out.println("  • Temperature: 0.3 (precise)");

        // Perform academic search
        SearchResponse response = search.webSearch(
            "Recent breakthroughs in CRISPR gene editing",
            new MapParam().maxTokens(1000)  // Only override maxTokens
        );

        System.out.println("\nResults from academic sources:");
        if (response.hasSearchResults()) {
            for (int i = 0; i < Math.min(3, response.getSearchResults().size()); i++) {
                var result = response.getSearchResults().get(i);
                System.out.println("\n  " + (i+1) + ". " + result.getTitle());
                System.out.println("     " + result.getUrl());
            }
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 5: Conversational search
     * <p>
     * Demonstrates using WebSearchFactory for conversational web search
     * with context preservation.
     * </p>
     */
    public static void example5_ConversationalSearch() throws LLMException {
        System.out.println("=== Example 5: Conversational Search ===\n");

        WebSearch search = WebSearchFactory.createPerplexity();
        Chat chat = new Chat();
        chat.setModel("sonar-pro");

        MapParam params = new MapParam()
            .maxTokens(500)
            .returnRelatedQuestions(true);

        // First question
        System.out.println("Q1: What is quantum computing?");
        SearchResponse r1 = search.webSearchChat(
            chat,
            "What is quantum computing?",
            params
        );
        System.out.println("A1: " + r1.getResponse().getText().substring(0, 200) + "...");

        // Follow-up question (uses context!)
        System.out.println("\nQ2: What are its main applications?");
        SearchResponse r2 = search.webSearchChat(
            chat,
            "What are its main applications?",
            params
        );
        System.out.println("A2: " + r2.getResponse().getText().substring(0, 200) + "...");

        // Show related questions
        if (r2.hasRelatedQuestions()) {
            System.out.println("\nRelated questions:");
            r2.getRelatedQuestions().forEach(q -> System.out.println("  • " + q));
        }

        System.out.println("\nConversation has " + chat.getMessages().size() + " messages");

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 6: Streaming search
     * <p>
     * Shows how to use WebSearchFactory with streaming for real-time results.
     * </p>
     */
    public static void example6_StreamingSearch() throws LLMException {
        System.out.println("=== Example 6: Streaming Search ===\n");

        WebSearch search = WebSearchFactory.createPerplexity();

        // Create streaming handler
        ResponseStream stream = new ResponseStream() {
            @Override
            public void onToken(String token, ContentType type) {
                System.out.print(token);
                System.out.flush();
            }

            @Override
            public void onComplete() {
                System.out.println("\n\n[Streaming completed]");
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("\nError: " + error.getMessage());
            }
        };

        MapParam params = new MapParam()
            .model("sonar")
            .maxTokens(400);

        System.out.println("Streaming response:\n");

        SearchResponse response = search.webSearchStream(
            stream,
            "Explain artificial intelligence in simple terms",
            params
        );

        // After streaming completes, show citations
        System.out.println("Citations:");
        for (String citation : response.getCitations()) {
            System.out.println("  • " + citation);
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 7: Multiple provider pattern (future-ready)
     * <p>
     * Demonstrates how to structure code for multiple web search providers,
     * even though only Perplexity is currently implemented.
     * </p>
     */
    public static void example7_MultiProviderPattern() {
        System.out.println("=== Example 7: Multi-Provider Pattern (Future) ===\n");

        System.out.println("Current implementation:");
        try {
            WebSearch perplexity = WebSearchFactory.createPerplexity();
            System.out.println("  ✓ Perplexity: Available -  " + perplexity.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("  ✗ Perplexity: " + e.getMessage());
        }

        System.out.println("\nPlanned implementations:");
        try {
            WebSearchFactory.createDeepSeek();
            System.out.println("  ✓ DeepSeek: Available");
        } catch (UnsupportedOperationException e) {
            System.out.println("  ⏳ DeepSeek: Not yet implemented");
        }

        try {
            WebSearchFactory.createGemini();
            System.out.println("  ✓ Gemini: Available");
        } catch (UnsupportedOperationException e) {
            System.out.println("  ⏳ Gemini: Not yet implemented");
        }

        try {
            WebSearchFactory.createWikipedia();
            System.out.println("  ✓ Wikipedia: Available");
        } catch (UnsupportedOperationException e) {
            System.out.println("  ⏳ Wikipedia: Not yet implemented");
        }

        System.out.println("\nFuture usage pattern:");
        System.out.println("```java");
        System.out.println("class MultiSourceSearchEngine {");
        System.out.println("    private WebSearch perplexity = WebSearchFactory.createPerplexity();");
        System.out.println("    private WebSearch gemini = WebSearchFactory.createGemini();");
        System.out.println("    private WebSearch wikipedia = WebSearchFactory.createWikipedia();");
        System.out.println("");
        System.out.println("    public SearchResponse searchAll(String query) {");
        System.out.println("        // Aggregate results from all sources");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 8: Error handling
     * <p>
     * Demonstrates proper error handling when using WebSearchFactory.
     * </p>
     */
    public static void example8_ErrorHandling() {
        System.out.println("=== Example 8: Error Handling ===\n");

        // Handle null provider
        System.out.println("1. Null provider:");
        try {
            WebSearchFactory.createWebSearch(null, null);
            System.out.println("   ✗ Should have thrown exception");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✓ Correctly throws: " + e.getMessage());
        }

        // Handle unsupported provider
        System.out.println("\n2. Unsupported provider:");
        try {
            WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.DEEPSEEK, null);
            System.out.println("   ✗ Should have thrown exception");
        } catch (UnsupportedOperationException e) {
            System.out.println("   ✓ Correctly throws: UnsupportedOperationException");
        }

        // Handle missing API key (would need to unset env var)
        System.out.println("\n3. Missing API key (simulated):");
        System.out.println("   If PERPLEXITY_API_KEY is not set:");
        System.out.println("   → LLMAuthenticationException will be thrown");
        System.out.println("   → Message: 'Perplexity API key not found...'");

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Example 9: Practical use case - Research assistant
     * <p>
     * A real-world example of using WebSearchFactory to build a research assistant.
     * </p>
     */
    public static void example9_ResearchAssistant() throws LLMException {
        System.out.println("=== Example 9: Research Assistant ===\n");

        // Create research-optimized configuration
        MapParam researchDefaults = new MapParam()
            .searchDomainFilter(new String[]{
                "arxiv.org",
                "scholar.google.com",
                "pubmed.ncbi.nlm.nih.gov",
                "nature.com",
                "sciencedirect.com"
            })
            .searchRecencyFilter("year")  // Last year
            .returnRelatedQuestions(true)
            .temperature(0.2f);  // Very precise

        LLMConfig config = LLMConfig.builder()
            .apiTokenEnvironment("PERPLEXITY_API_KEY")
            .defaultModelName("sonar-pro")
            .defaultParams(researchDefaults)
            .build();

        WebSearch researcher = WebSearchFactory.createPerplexity(config);

        System.out.println("Research Assistant Configuration:");
        System.out.println("  • Model: sonar-pro");
        System.out.println("  • Sources: Academic only");
        System.out.println("  • Recency: Last year");
        System.out.println("  • Temperature: 0.2 (highly precise)");

        // Perform research query
        String topic = "machine learning interpretability techniques";
        System.out.println("\nResearching: " + topic);

        SearchResponse response = researcher.webSearch(
            "What are the latest " + topic + "?",
            new MapParam().maxTokens(1500)
        );

        System.out.println("\nFindings:");
        System.out.println(response.getResponse().getText().substring(0, 300) + "...");

        System.out.println("\nAcademic Sources (" + response.getCitations().size() + "):");
        for (int i = 0; i < Math.min(5, response.getCitations().size()); i++) {
            System.out.println("  " + (i+1) + ". " + response.getCitations().get(i));
        }

        if (response.hasRelatedQuestions()) {
            System.out.println("\nFurther Research Topics:");
            for (String question : response.getRelatedQuestions()) {
                System.out.println("  • " + question);
            }
        }

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    /**
     * Main method to run all examples.
     * <p>
     * <b>Note:</b> Requires PERPLEXITY_API_KEY environment variable to be set.
     * </p>
     */
    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("WebSearchFactory Examples");
        System.out.println("=".repeat(60) + "\n");

        try {
            // Run examples that don't require API calls
            example2_FactoryComparison();
            example3_CreationMethods();
            example7_MultiProviderPattern();
            example8_ErrorHandling();

            // Examples below require valid API key
            String apiKey = System.getenv("PERPLEXITY_API_KEY");
            if (apiKey != null && !apiKey.isEmpty()) {
                example1_BasicUsage();
                example4_CustomConfiguration();
                example5_ConversationalSearch();
                example6_StreamingSearch();
                example9_ResearchAssistant();
            } else {
                System.out.println("⚠️  PERPLEXITY_API_KEY not set. Skipping API-dependent examples.");
                System.out.println("   Set the environment variable to run all examples.\n");
            }

        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=".repeat(60));
        System.out.println("Examples completed!");
        System.out.println("=".repeat(60) + "\n");
    }
}
