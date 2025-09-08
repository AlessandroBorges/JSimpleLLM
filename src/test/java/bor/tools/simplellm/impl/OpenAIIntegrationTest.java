package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Integration tests that test complete workflows with the OpenAI LLM Service.
 * These tests require a valid OPENAI_API_TOKEN to run.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpenAIIntegrationTest extends OpenAILLMServiceTestBase {

    @Test
    @Order(1)
    @DisplayName("Integration Test: Complete conversation flow")
    void testCompleteConversationFlow() throws LLMException {
        System.out.println("\n=== Complete Conversation Flow Test ===");
        
        // Step 1: Create a chat session
        Chat chat = new Chat("integration-test-chat");
        chat.setModel("gpt-4o-mini");
        chat.addSystemMessage("You are a helpful assistant that provides concise, informative answers.");
        
        MapParam params = new MapParam();
        params.put("max_tokens", 100);
        params.put("temperature", 0.3);
        
        System.out.println("1. Created chat session with ID: " + chat.getId());
        
        // Step 2: First interaction
        CompletionResponse response1 = llmService.chatCompletion(
            chat, 
            "What is the capital of Japan?", 
            params
        );
        
        assertNotNull(response1);
        assertTrue(response1.getResponse().getText().toLowerCase().contains("tokyo"));
        assertEquals(3, chat.messageCount()); // system, user, assistant
        
        System.out.println("2. First response: " + response1.getResponse().getText());
        
        // Step 3: Follow-up question (testing context retention)
        CompletionResponse response2 = llmService.chatCompletion(
            chat, 
            "What is the population of that city?", 
            params
        );
        
        assertNotNull(response2);
        String response2Text = response2.getResponse().getText().toLowerCase();
        assertTrue(response2Text.contains("tokyo") || response2Text.contains("million"));
        assertEquals(5, chat.messageCount()); // system, user, assistant, user, assistant
        
        System.out.println("3. Follow-up response: " + response2.getResponse().getText());
        
        // Step 4: Test completion method
        CompletionResponse response3 = llmService.completion(
            "You are a math tutor.", 
            "What is 15 × 8?", 
            params
        );
        
        assertNotNull(response3);
        assertTrue(response3.getResponse().getText().contains("120"));
        
        System.out.println("4. Math completion: " + response3.getResponse().getText());
        
        // Step 5: Test embeddings
        float[] embeddings = llmService.embeddings(
            "Tokyo is the capital of Japan.", 
            "text-embedding-3-small", 
            null
        );
        
        assertNotNull(embeddings);
        assertTrue(embeddings.length > 0);
        
        System.out.println("5. Generated embeddings with " + embeddings.length + " dimensions");
        
        System.out.println("=== Integration test completed successfully ===\n");
    }

    @Test
    @Order(2)
    @DisplayName("Integration Test: Error handling and recovery")
    void testErrorHandlingAndRecovery() throws LLMException {
        System.out.println("\n=== Error Handling and Recovery Test ===");
        
        // Test with invalid parameters but valid service
        MapParam params = new MapParam();
        params.put("model", "gpt-4o-mini");
        params.put("max_tokens", 10);  // Very small limit
        params.put("temperature", 0.0);
        
        CompletionResponse response = llmService.completion(
            "Be very brief.", 
            "Explain artificial intelligence in detail.", 
            params
        );
        
        assertNotNull(response);
        // Response should be truncated due to low token limit
        assertTrue(response.getEndReason().equals("length") || 
                  response.getEndReason().equals("stop"));
        
        System.out.println("1. Handled low token limit - End reason: " + response.getEndReason());
        System.out.println("   Response: " + response.getResponse().getText());
        
        // Test recovery with normal parameters
        params.put("max_tokens", 100);
        CompletionResponse normalResponse = llmService.completion(
            "You are helpful.", 
            "Say hello.", 
            params
        );
        
        assertNotNull(normalResponse);
        assertTrue(normalResponse.getResponse().getText().toLowerCase().contains("hello"));
        
        System.out.println("2. Successfully recovered with normal parameters");
        System.out.println("   Response: " + normalResponse.getResponse().getText());
        
        System.out.println("=== Error handling test completed ===\n");
    }

    @Test
    @Order(3)
    @DisplayName("Integration Test: Performance and metadata")
    void testPerformanceAndMetadata() throws LLMException {
        System.out.println("\n=== Performance and Metadata Test ===");
        
        MapParam params = new MapParam();
        params.put("model", "gpt-4o-mini");
        params.put("max_tokens", 50);
        
        long startTime = System.currentTimeMillis();
        
        CompletionResponse response = llmService.completion(
            "You are precise.", 
            "Count from 1 to 5.", 
            params
        );
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull(response);
        assertNotNull(response.getInfo());
        assertNotNull(response.getEndReason());
        
        System.out.println("1. Response time: " + duration + "ms");
        System.out.println("2. End reason: " + response.getEndReason());
        System.out.println("3. Response: " + response.getResponse().getText());
        System.out.println("4. Metadata keys: " + response.getInfo().keySet());
        
        // Check for common metadata fields
        if (response.getInfo().containsKey("usage")) {
            System.out.println("5. Usage info available: " + response.getInfo().get("usage"));
        }
        
        if (response.getInfo().containsKey("model")) {
            System.out.println("6. Model used: " + response.getInfo().get("model"));
        }
        
        // Performance assertion (should complete within reasonable time)
        assertTrue(duration < 30000, "Response should complete within 30 seconds");
        
        System.out.println("=== Performance test completed ===\n");
    }

    @Test
    @Order(4)
    @DisplayName("Integration Test: Multiple model comparison")
    void testMultipleModelComparison() throws LLMException {
        System.out.println("\n=== Multiple Model Comparison Test ===");
        
        String query = "What is machine learning?";
        String system = "Provide a brief, technical explanation.";
        
        // Test with gpt-4o-mini
        MapParam params1 = new MapParam();
        params1.put("model", "gpt-4o-mini");
        params1.put("max_tokens", 80);
        params1.put("temperature", 0.0);
        
        CompletionResponse response1 = llmService.completion(system, query, params1);
        
        assertNotNull(response1);
        System.out.println("1. GPT-4o-mini response:");
        System.out.println("   " + response1.getResponse().getText());
        
        // Test embeddings with the same content
        float[] embeddings = llmService.embeddings(
            query, 
            "text-embedding-3-small", 
            null
        );
        
        assertNotNull(embeddings);
        System.out.println("2. Generated embeddings for query");
        
        // Verify both models can handle the same input differently
        assertNotNull(response1.getResponse().getText());
        assertTrue(embeddings.length > 0);
        
        System.out.println("=== Model comparison test completed ===\n");
    }

    @Test
    @Order(5)
    @DisplayName("Integration Test: Edge cases and robustness")
    void testEdgeCasesAndRobustness() throws LLMException {
        System.out.println("\n=== Edge Cases and Robustness Test ===");
        
        MapParam params = new MapParam();
        params.put("model", "gpt-4o-mini");
        params.put("max_tokens", 50);
        
        // Test 1: Very short input
        CompletionResponse shortResponse = llmService.completion(
            "Be helpful.", 
            "Hi.", 
            params
        );
        assertNotNull(shortResponse);
        System.out.println("1. Short input handled: " + shortResponse.getResponse().getText());
        
        // Test 2: Input with special characters
        CompletionResponse specialResponse = llmService.completion(
            "You understand all languages.", 
            "Hello! ¿Cómo estás? 你好！ 🚀", 
            params
        );
        assertNotNull(specialResponse);
        System.out.println("2. Special characters handled: " + specialResponse.getResponse().getText());
        
        // Test 3: Empty chat with just a query
        Chat emptyChat = new Chat();
        CompletionResponse chatResponse = llmService.chatCompletion(
            emptyChat, 
            "What's 2+2?", 
            params
        );
        assertNotNull(chatResponse);
        assertTrue(chatResponse.getResponse().getText().contains("4"));
        System.out.println("3. Empty chat handled: " + chatResponse.getResponse().getText());
        
        // Test 4: Embeddings with short text
        float[] shortEmbeddings = llmService.embeddings("Hi", null, null);
        assertNotNull(shortEmbeddings);
        assertTrue(shortEmbeddings.length > 0);
        System.out.println("4. Short text embeddings: " + shortEmbeddings.length + " dimensions");
        
        System.out.println("=== Edge cases test completed ===\n");
    }
}