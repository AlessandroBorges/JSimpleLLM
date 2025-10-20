package bor.tools.simplellm.impl;

import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.SearchResponse;
import bor.tools.simplellm.WebSearch;
import bor.tools.simplellm.chat.Chat;

/**
 * Example demonstrating usage of Perplexity AI with web search capabilities.
 * <p>
 * This class provides practical examples of how to use the Perplexity integration
 * for various web search scenarios including basic searches, domain filtering,
 * recency filters, and streaming responses.
 * </p>
 * <p>
 * <b>Prerequisites:</b>
 * <ul>
 * <li>Set PERPLEXITY_API_KEY environment variable with your API key</li>
 * <li>Ensure internet connectivity for web search</li>
 * </ul>
 * </p>
 *
 * @author AlessandroBorges
 * @since 1.1
 */
public class PerplexityExample {

    /**
     * Example 1: Basic web search query.
     * Demonstrates the simplest way to perform a web search with Perplexity.
     */
    public static void example1_BasicWebSearch() {
        System.out.println("=== Example 1: Basic Web Search ===\n");

        try {
            // Create Perplexity service
            LLMService service = LLMServiceFactory.createPerplexity();
            WebSearch searchService = (WebSearch) service;

            // Simple search query
            MapParam params = new MapParam()
                    .model("sonar")
                    .maxTokens(500);

            SearchResponse response = searchService.webSearch(
                    "What are the latest developments in quantum computing?",
                    params
            );

            // Display results
            System.out.println("Response:");
            System.out.println(response.getResponse().getText());
            System.out.println("\nCitations:");
            if (response.hasCitations()) {
                for (String citation : response.getCitations()) {
                    System.out.println("- " + citation);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example 2: Web search with domain filtering.
     * Shows how to restrict search results to specific academic/research domains.
     */
    public static void example2_DomainFiltering() {
        System.out.println("\n=== Example 2: Domain Filtering ===\n");

        try {
            LLMService service = LLMServiceFactory.createPerplexity();
            WebSearch searchService = (WebSearch) service;

            // Search only on academic domains
            MapParam params = new MapParam()
                    .model("sonar-pro")
                    .searchDomainFilter(new String[]{
                            "arxiv.org",
                            "nature.com",
                            "sciencedirect.com",
                            "-wikipedia.org"  // Exclude Wikipedia
                    })
                    .maxTokens(800);

            SearchResponse response = searchService.webSearch(
                    "Recent breakthroughs in CRISPR gene editing",
                    params
            );

            System.out.println("Response:");
            System.out.println(response.getResponse().getText());
            System.out.println("\nSearch Results:");
            if (response.hasSearchResults()) {
                for (var result : response.getSearchResults()) {
                    System.out.println("- " + result.getTitle());
                    System.out.println("  URL: " + result.getUrl());
                    System.out.println("  Date: " + result.getDate());
                    System.out.println();
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example 3: Web search with recency filter and related questions.
     * Demonstrates time-based filtering and getting suggested follow-up questions.
     */
    public static void example3_RecencyAndRelatedQuestions() {
        System.out.println("\n=== Example 3: Recency Filter & Related Questions ===\n");

        try {
            LLMService service = LLMServiceFactory.createPerplexity();
            WebSearch searchService = (WebSearch) service;

            // Search recent content and get related questions
            MapParam params = new MapParam()
                    .model("sonar-pro")
                    .searchRecencyFilter("week")  // Last week only
                    .returnRelatedQuestions(true)
                    .maxTokens(600);

            SearchResponse response = searchService.webSearch(
                    "Latest AI model releases and benchmarks",
                    params
            );

            System.out.println("Response:");
            System.out.println(response.getResponse().getText());

            System.out.println("\nRelated Questions:");
            if (response.hasRelatedQuestions()) {
                for (String question : response.getRelatedQuestions()) {
                    System.out.println("- " + question);
                }
            }

            System.out.println("\nCitations:");
            if (response.hasCitations()) {
                for (String citation : response.getCitations()) {
                    System.out.println("- " + citation);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example 4: Conversational web search with chat context.
     * Shows how to maintain conversation context while performing web searches.
     */
    public static void example4_ConversationalSearch() {
        System.out.println("\n=== Example 4: Conversational Search ===\n");

        try {
            LLMService service = LLMServiceFactory.createPerplexity();
            WebSearch searchService = (WebSearch) service;

            // Create chat session
            Chat chat = new Chat();
            chat.setModel("sonar-pro");

            MapParam params = new MapParam()
                    .maxTokens(500);

            // First query
            System.out.println("User: What is the capital of Brazil?");
            SearchResponse response1 = searchService.webSearchChat(
                    chat,
                    "What is the capital of Brazil?",
                    params
            );
            System.out.println("Assistant: " + response1.getResponse().getText());

            // Follow-up query (uses context)
            System.out.println("\nUser: What is its population?");
            SearchResponse response2 = searchService.webSearchChat(
                    chat,
                    "What is its population?",
                    params
            );
            System.out.println("Assistant: " + response2.getResponse().getText());

            System.out.println("\nCitations:");
            if (response2.hasCitations()) {
                for (String citation : response2.getCitations()) {
                    System.out.println("- " + citation);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example 5: Streaming web search.
     * Demonstrates real-time streaming of search results as they are generated.
     */
    public static void example5_StreamingSearch() {
        System.out.println("\n=== Example 5: Streaming Search ===\n");

        try {
            LLMService service = LLMServiceFactory.createPerplexity();
            WebSearch searchService = (WebSearch) service;

            // Create streaming handler
            ResponseStream stream = new ResponseStream() {
                @Override
                public void onToken(String token, ContentType type) {
                    System.out.print(token);
                    System.out.flush();
                }

                @Override
                public void onComplete() {
                    System.out.println("\n\n[Stream completed]");
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
            SearchResponse response = searchService.webSearchStream(
                    stream,
                    "Explain the concept of quantum entanglement in simple terms",
                    params
            );

            // After streaming completes, show citations
            System.out.println("\nCitations:");
            if (response.hasCitations()) {
                for (String citation : response.getCitations()) {
                    System.out.println("- " + citation);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example 6: Deep research with comprehensive analysis.
     * Uses the sonar-deep-research model for exhaustive multi-step research.
     */
    public static void example6_DeepResearch() {
        System.out.println("\n=== Example 6: Deep Research ===\n");

        try {
            LLMService service = LLMServiceFactory.createPerplexity();
            WebSearch searchService = (WebSearch) service;

            MapParam params = new MapParam()
                    .model("sonar-deep-research")
                    .searchRecencyFilter("month")
                    .returnRelatedQuestions(true)
                    .maxTokens(2000);

            System.out.println("Performing deep research (this may take longer)...\n");

            SearchResponse response = searchService.webSearch(
                    "Comprehensive analysis of renewable energy adoption trends in 2024-2025",
                    params
            );

            System.out.println("Response:");
            System.out.println(response.getResponse().getText());

            if (response.getSearchQueriesCount() != null) {
                System.out.println("\nSearch queries performed: " + response.getSearchQueriesCount());
            }

            System.out.println("\nCitations (" + response.getCitations().size() + " sources):");
            if (response.hasCitations()) {
                for (int i = 0; i < Math.min(10, response.getCitations().size()); i++) {
                    System.out.println((i + 1) + ". " + response.getCitations().get(i));
                }
                if (response.getCitations().size() > 10) {
                    System.out.println("... and " + (response.getCitations().size() - 10) + " more sources");
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method to run all examples.
     * <p>
     * <b>Note:</b> Comment out examples you don't want to run to save API credits.
     * Each example makes at least one API call to Perplexity.
     * </p>
     */
    public static void main(String[] args) {
        System.out.println("Perplexity AI Web Search Examples");
        System.out.println("==================================\n");

        // Check for API key
        String apiKey = System.getenv("PERPLEXITY_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ERROR: PERPLEXITY_API_KEY environment variable not set!");
            System.err.println("Please set your API key: export PERPLEXITY_API_KEY=your_key_here");
            return;
        }

        // Run examples
        // Uncomment the examples you want to run:

        example1_BasicWebSearch();
        // example2_DomainFiltering();
        // example3_RecencyAndRelatedQuestions();
        // example4_ConversationalSearch();
        // example5_StreamingSearch();
        // example6_DeepResearch();

        System.out.println("\n=== Examples completed ===");
    }
}
