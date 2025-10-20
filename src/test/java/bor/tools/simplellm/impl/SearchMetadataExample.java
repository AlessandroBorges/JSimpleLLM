package bor.tools.simplellm.impl;

import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.SearchMetadata;
import bor.tools.simplellm.websearch.SearchResponse;
import bor.tools.simplellm.websearch.WebSearch;

/**
 * Example demonstrating how to access and use SearchMetadata from chat messages.
 * <p>
 * This example shows how Perplexity search results, citations, and related questions
 * are automatically attached to assistant messages and can be accessed later.
 * </p>
 *
 * @author AlessandroBorges
 * @since 1.1
 */
public class SearchMetadataExample {

    public static void main(String[] args) {
        try {
            example1_BasicSearchMetadata();
            System.out.println("\n" + "=".repeat(80) + "\n");
            example2_ConversationalSearchWithHistory();
            System.out.println("\n" + "=".repeat(80) + "\n");
            example3_AccessingSearchResults();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure PERPLEXITY_API_KEY environment variable is set.");
        }
    }

    /**
     * Example 1: Basic web search with SearchMetadata
     * Shows how search metadata is automatically attached to assistant messages.
     */
    public static void example1_BasicSearchMetadata() throws Exception {
        System.out.println("=== Example 1: Basic Search with Metadata ===\n");

        // Create Perplexity service
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        // Create a chat
        Chat chat = new Chat();
        chat.setModel("sonar");

        // Perform web search
        MapParam params = new MapParam()
                .maxTokens(300)
                .returnRelatedQuestions(true);

        System.out.println("Query: What are the latest developments in quantum computing?");
        SearchResponse response = searchService.webSearchChat(chat,
                "What are the latest developments in quantum computing?", params);

        // The assistant message was automatically added to chat with SearchMetadata
        Message lastMessage = chat.getLastMessage();

        System.out.println("\n--- Response ---");
        System.out.println(lastMessage.getText());

        // Check if message has search metadata
        if (lastMessage.hasSearchMetadata()) {
            SearchMetadata metadata = lastMessage.getSearchMetadata();

            // Display citations
            if (metadata.hasCitations()) {
                System.out.println("\n--- Citations (" + metadata.getCitations().size() + ") ---");
                for (int i = 0; i < metadata.getCitations().size(); i++) {
                    System.out.println((i + 1) + ". " + metadata.getCitations().get(i));
                }
            }

            // Display related questions
            if (metadata.hasRelatedQuestions()) {
                System.out.println("\n--- Related Questions ---");
                for (String question : metadata.getRelatedQuestions()) {
                    System.out.println("• " + question);
                }
            }
        }
    }

    /**
     * Example 2: Conversational search with history
     * Shows how multiple messages can have their own SearchMetadata.
     */
    public static void example2_ConversationalSearchWithHistory() throws Exception {
        System.out.println("=== Example 2: Conversational Search with History ===\n");

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        Chat chat = new Chat();
        chat.setModel("sonar-pro");

        MapParam params = new MapParam().maxTokens(200);

        // First query
        System.out.println("Query 1: What is the current population of Brazil?");
        searchService.webSearchChat(chat, "What is the current population of Brazil?", params);

        // Second query (uses context from first)
        System.out.println("\nQuery 2: What is its GDP?");
        searchService.webSearchChat(chat, "What is its GDP?", params);

        // Display all messages and their metadata
        System.out.println("\n--- Chat History (" + chat.messageCount() + " messages) ---");
        for (int i = 0; i < chat.messageCount(); i++) {
            Message msg = chat.getMessage(i);
            System.out.println("\n[" + msg.getRole() + "]");
            System.out.println(msg.getText());

            if (msg.hasSearchMetadata()) {
                SearchMetadata metadata = msg.getSearchMetadata();
                System.out.println("  → Citations: " +
                    (metadata.hasCitations() ? metadata.getCitations().size() : 0));
                System.out.println("  → Search Results: " +
                    (metadata.hasSearchResults() ? metadata.getSearchResults().size() : 0));
            }
        }
    }

    /**
     * Example 3: Accessing detailed search results
     * Shows how to access search result metadata including titles, URLs, and snippets.
     */
    public static void example3_AccessingSearchResults() throws Exception {
        System.out.println("=== Example 3: Detailed Search Results ===\n");

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        Chat chat = new Chat();
        chat.setModel("sonar");

        MapParam params = new MapParam()
                .maxTokens(200)
                .searchDomainFilter(new String[]{"wikipedia.org", "britannica.com"});

        System.out.println("Query: Tell me about the James Webb Space Telescope");
        System.out.println("Domain filter: wikipedia.org, britannica.com\n");

        searchService.webSearchChat(chat, "Tell me about the James Webb Space Telescope", params);

        Message lastMessage = chat.getLastMessage();
        System.out.println("--- Response ---");
        System.out.println(lastMessage.getText());

        if (lastMessage.hasSearchMetadata()) {
            SearchMetadata metadata = lastMessage.getSearchMetadata();

            // Display detailed search results
            if (metadata.hasSearchResults()) {
                System.out.println("\n--- Search Results (" +
                    metadata.getSearchResults().size() + ") ---");

                for (SearchMetadata.SearchResultMetadata result : metadata.getSearchResults()) {
                    System.out.println("\nTitle: " + result.getTitle());
                    System.out.println("URL: " + result.getUrl());
                    if (result.getDate() != null) {
                        System.out.println("Date: " + result.getDate());
                    }
                    if (result.getSnippet() != null) {
                        System.out.println("Snippet: " + result.getSnippet());
                    }
                    System.out.println("-".repeat(60));
                }
            }

            // Display search metadata summary
            System.out.println("\n--- Search Metadata Summary ---");
            System.out.println(metadata.toString());
        }
    }

    /**
     * Bonus: Helper method to display SearchMetadata in a formatted way
     */
    public static void displaySearchMetadata(Message message) {
        if (!message.hasSearchMetadata()) {
            System.out.println("No search metadata available.");
            return;
        }

        SearchMetadata metadata = message.getSearchMetadata();
        System.out.println("=== Search Metadata ===");

        // Citations
        if (metadata.hasCitations()) {
            System.out.println("\nCitations (" + metadata.getCitations().size() + "):");
            metadata.getCitations().forEach(citation -> System.out.println("  • " + citation));
        }

        // Search Results
        if (metadata.hasSearchResults()) {
            System.out.println("\nSearch Results (" + metadata.getSearchResults().size() + "):");
            for (SearchMetadata.SearchResultMetadata result : metadata.getSearchResults()) {
                System.out.println("  • " + result.getTitle() + " - " + result.getUrl());
            }
        }

        // Related Questions
        if (metadata.hasRelatedQuestions()) {
            System.out.println("\nRelated Questions (" + metadata.getRelatedQuestions().size() + "):");
            metadata.getRelatedQuestions().forEach(q -> System.out.println("  • " + q));
        }

        // Images
        if (metadata.hasImages()) {
            System.out.println("\nImages (" + metadata.getImages().size() + "):");
            for (SearchMetadata.ImageResult image : metadata.getImages()) {
                System.out.println("  • " + image.getTitle() + " - " + image.getUrl());
            }
        }

        // Search queries count
        if (metadata.getSearchQueriesCount() != null) {
            System.out.println("\nSearch Queries Executed: " + metadata.getSearchQueriesCount());
        }

        System.out.println("======================");
    }
}
