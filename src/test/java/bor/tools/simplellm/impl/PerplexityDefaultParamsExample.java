package bor.tools.simplellm.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.websearch.SearchResponse;
import bor.tools.simplellm.websearch.WebSearch;
import bor.tools.simplellm.websearch.impl.PerplexityLLMService;

/**
 * Examples demonstrating how to configure and use default parameters
 * for Perplexity WebSearch.
 * <p>
 * Default parameters are set at the service/config level and automatically
 * applied to all requests unless overridden.
 * </p>
 *
 * @author AlessandroBorges
 * @since 1.1
 */
public class PerplexityDefaultParamsExample {

    public static void main(String[] args) {
        try {
            example1_UsingBuiltInDefaults();
            System.out.println("\n" + "=".repeat(80) + "\n");
            example2_CustomDefaultParams();
            System.out.println("\n" + "=".repeat(80) + "\n");
            example3_OverridingDefaults();
            System.out.println("\n" + "=".repeat(80) + "\n");
            example4_LocationSpecificDefaults();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure PERPLEXITY_API_KEY environment variable is set.");
        }
    }

    /**
     * Example 1: Using built-in default parameters
     * <p>
     * The default Perplexity service comes with sensible defaults:
     * - search_mode: "web"
     * - return_related_questions: true
     * - temperature: 0.7
     * </p>
     */
    public static void example1_UsingBuiltInDefaults() throws Exception {
        System.out.println("=== Example 1: Built-in Defaults ===\n");

        // Create service with built-in defaults
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        // No params needed - defaults are applied automatically
        SearchResponse response = searchService.webSearch(
                "What is the latest news about artificial intelligence?",
                null  // null params will use only defaults
        );

        System.out.println("Response: " + response.getResponse().getText());
        System.out.println("\nDefault params applied:");
        System.out.println("  - search_mode: web");
        System.out.println("  - return_related_questions: true");
        System.out.println("  - temperature: 0.7");

        if (response.hasRelatedQuestions()) {
            System.out.println("\nRelated questions (from default setting):");
            response.getRelatedQuestions().forEach(q -> System.out.println("  • " + q));
        }
    }

    /**
     * Example 2: Configuring custom default parameters
     * <p>
     * Shows how to create a service with custom defaults
     * that apply to all requests.
     * </p>
     */
    public static void example2_CustomDefaultParams() throws Exception {
        System.out.println("=== Example 2: Custom Default Parameters ===\n");

        // Get default config and modify default params
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();

        // Configure custom defaults for academic research
        MapParam customDefaults = new MapParam()
                .searchMode("academic")                                           // Focus on academic sources
                .searchDomainFilter(new String[]{"arxiv.org", "scholar.google.com"})  // Academic domains
                .returnRelatedQuestions(true)
                .returnImages(false)                                              // No images for academic research
                .temperature(0.3f)                                                // Lower temperature for accuracy
                .maxTokens(1500);                                                 // Longer responses

        // Set the custom defaults
        config.setDefaultParams(customDefaults);

        // Create service with custom config
        LLMService service = new PerplexityLLMService(config);
        WebSearch searchService = (WebSearch) service;

        System.out.println("Custom defaults configured:");
        System.out.println("  - search_mode: academic");
        System.out.println("  - search_domain_filter: arxiv.org, scholar.google.com");
        System.out.println("  - temperature: 0.3");
        System.out.println("  - max_tokens: 1500");

        // Use service - custom defaults apply automatically
        SearchResponse response = searchService.webSearch(
                "Explain quantum entanglement",
                null  // null - uses custom defaults
        );

        System.out.println("\nResponse (with academic focus):");
        System.out.println(response.getResponse().getText());

        if (response.hasCitations()) {
            System.out.println("\nCitations (should be from academic sources):");
            response.getCitations().stream()
                    .limit(3)
                    .forEach(c -> System.out.println("  • " + c));
        }
    }

    /**
     * Example 3: Overriding default parameters per request
     * <p>
     * Shows how request-specific params override defaults.
     * </p>
     */
    public static void example3_OverridingDefaults() throws Exception {
        System.out.println("=== Example 3: Overriding Defaults ===\n");

        // Service has built-in defaults
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        Chat chat = new Chat();
        chat.setModel("sonar");

        // First query: use defaults
        System.out.println("Query 1: Using defaults");
        SearchResponse response1 = searchService.webSearchChat(chat,
                "What is machine learning?",
                null  // Use defaults
        );
        System.out.println("Response length: " + response1.getResponse().getText().length() + " chars");
        System.out.println("Related questions: " + (response1.hasRelatedQuestions() ? "Yes (default)" : "No"));

        // Second query: override specific defaults
        System.out.println("\nQuery 2: Overriding specific defaults");
        MapParam customParams = new MapParam()
                .returnRelatedQuestions(false)      // Override default (was true)
                .temperature(0.2f)                  // Override default (was 0.7)
                .searchRecencyFilter("day")         // Add new param
                .maxTokens(200);                    // Shorter response

        SearchResponse response2 = searchService.webSearchChat(chat,
                "Latest developments in machine learning?",
                customParams  // Override defaults
        );
        System.out.println("Response length: " + response2.getResponse().getText().length() + " chars");
        System.out.println("Related questions: " + (response2.hasRelatedQuestions() ? "Yes" : "No (overridden)"));
        System.out.println("Recency filter: day (custom param)");
    }

    /**
     * Example 4: Location-specific defaults
     * <p>
     * Shows how to configure defaults for localized searches.
     * </p>
     */
    public static void example4_LocationSpecificDefaults() throws Exception {
        System.out.println("=== Example 4: Location-Specific Defaults ===\n");

        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();

        // Configure user location for Brazilian user
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("latitude", -15.7933);   // Brasília
        location.put("longitude", -47.8827);
        location.put("country", "br");

        MapParam locationDefaults = new MapParam()
                .userLocation(location)
                .searchMode("web")
                .returnRelatedQuestions(true)
                .temperature(0.7f);

        config.setDefaultParams(locationDefaults);

        LLMService service = new PerplexityLLMService(config);
        WebSearch searchService = (WebSearch) service;

        System.out.println("Default location configured:");
        System.out.println("  - latitude: -15.7933 (Brasília)");
        System.out.println("  - longitude: -47.8827");
        System.out.println("  - country: br");

        // Search with location context
        SearchResponse response = searchService.webSearch(
                "What are the best universities?",
                null  // Uses location defaults
        );

        System.out.println("\nResponse (with Brazilian location context):");
        System.out.println(response.getResponse().getText());
        System.out.println("\n(Results should be biased towards Brazilian universities)");
    }

    /**
     * Bonus: Example showing all supported Perplexity-specific parameters
     */
    public static void exampleBonus_AllPerplexityParams() throws Exception {
        System.out.println("=== Bonus: All Perplexity-Specific Parameters ===\n");

        MapParam allParams = new MapParam()
                // Basic LLM params
                .model("sonar-pro")
                .temperature(0.7f)
                .maxTokens(1000)
                .reasoningEffort(bor.tools.simplellm.Reasoning_Effort.medium)

                // Search mode
                .searchMode("web")  // or "academic"

                // Domain filtering
                .searchDomainFilter(new String[]{"arxiv.org", "-reddit.com"})  // include/exclude

                // Time filtering
                .searchRecencyFilter("week")  // "hour", "day", "week", "month", "year"
                .searchAfterDateFilter("01/01/2025")  // MM/DD/YYYY
                .searchBeforeDateFilter("12/31/2025")  // MM/DD/YYYY

                // Response options
                .returnImages(false)
                .returnRelatedQuestions(true)

                // Search context
                .searchContext("high")  // "low", "medium", "high"

                // User location
                .userLocation(-15.7933, -47.8827, "br");

        System.out.println("Complete parameter configuration:");
        System.out.println(allParams);
    }
}
