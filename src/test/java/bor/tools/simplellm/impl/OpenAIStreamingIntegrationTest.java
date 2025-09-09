package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integration tests for OpenAI streaming functionality.
 * <p>
 * These tests require a valid OpenAI API key and make real API calls.
 * They are ordered to minimize API usage and test progressively complex scenarios.
 * </p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OpenAIStreamingIntegrationTest extends OpenAILLMServiceTestBase {

    @Test
    @Order(1)
    @DisplayName("Integration Test: Basic streaming functionality")
    void testBasicStreamingIntegration() throws LLMException, InterruptedException {
        System.out.println("\n=== Basic Streaming Integration Test ===");
        
        // Create a monitoring stream
        StreamingMonitor monitor = new StreamingMonitor();
        
        MapParam params = new MapParam();
        params.put("max_tokens", 50);
        params.put("temperature", 0.1);
        
        long startTime = System.currentTimeMillis();
        
        CompletionResponse response = llmService.completionStream(
            monitor, 
            "You are helpful. Respond very briefly.", 
            "Say hello and count to 3.", 
            params
        );
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Wait for completion
        assertTrue(monitor.waitForCompletion(30, TimeUnit.SECONDS), "Stream should complete within 30 seconds");
        
        // Verify response
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getResponse(), "Response content should not be null");
        
        String finalText = response.getResponse().getText();
        assertNotNull(finalText, "Final text should not be null");
        assertFalse(finalText.trim().isEmpty(), "Final text should not be empty");
        
        // Verify streaming behavior
        assertTrue(monitor.isCompleted(), "Monitor should show completion");
        assertFalse(monitor.getTokens().isEmpty(), "Should have received streaming tokens");
        assertTrue(monitor.getTokenCount() > 1, "Should have received multiple tokens");
        assertNull(monitor.getError(), "Should not have streaming errors");
        
        System.out.println("1. Streaming completed successfully");
        System.out.println("2. Response time: " + duration + "ms");
        System.out.println("3. Tokens received: " + monitor.getTokenCount());
        System.out.println("4. Final response: " + finalText);
        System.out.println("5. First few tokens: " + monitor.getTokens().subList(0, Math.min(3, monitor.getTokens().size())));
        System.out.println("=== Basic streaming test completed ===\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("Integration Test: Chat streaming with context")
    void testChatStreamingIntegration() throws LLMException, InterruptedException {
        System.out.println("\n=== Chat Streaming Integration Test ===");
        
        StreamingMonitor monitor = new StreamingMonitor();
        
        Chat chat = new Chat("streaming-integration-test");
        chat.addSystemMessage("You are a math tutor. Give very brief answers.");
        
        MapParam params = new MapParam();
        params.put("max_tokens", 40);
        params.put("temperature", 0.1);
        
        // First interaction
        CompletionResponse response1 = llmService.chatCompletionStream(
            monitor, 
            chat, 
            "What is 5 + 7?", 
            params
        );
        
        assertTrue(monitor.waitForCompletion(30, TimeUnit.SECONDS), "First stream should complete");
        
        assertNotNull(response1);
        assertTrue(response1.getResponse().getText().contains("12"), "Should contain the answer");
        assertEquals(3, chat.messageCount(), "Chat should have 3 messages after first interaction");
        
        System.out.println("1. First interaction completed");
        System.out.println("   Response: " + response1.getResponse().getText());
        System.out.println("   Tokens: " + monitor.getTokenCount());
        
        // Reset monitor for second interaction
        monitor.reset();
        
        // Second interaction - testing context
        CompletionResponse response2 = llmService.chatCompletionStream(
            monitor, 
            chat, 
            "What about 10 minus that result?", 
            params
        );
        
        assertTrue(monitor.waitForCompletion(30, TimeUnit.SECONDS), "Second stream should complete");
        
        assertNotNull(response2);
        String response2Text = response2.getResponse().getText();
        // Should understand "that result" refers to 12, so 10 - 12 = -2
        assertTrue(response2Text.contains("-2") || response2Text.contains("negative"), 
                  "Should handle context and give correct answer");
        assertEquals(5, chat.messageCount(), "Chat should have 5 messages after second interaction");
        
        System.out.println("2. Second interaction completed");
        System.out.println("   Response: " + response2Text);
        System.out.println("   Tokens: " + monitor.getTokenCount());
        System.out.println("   Final chat message count: " + chat.messageCount());
        System.out.println("=== Chat streaming test completed ===\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("Integration Test: Streaming performance and token analysis")
    void testStreamingPerformanceAnalysis() throws LLMException, InterruptedException {
        System.out.println("\n=== Streaming Performance Analysis Test ===");
        
        StreamingMonitor monitor = new StreamingMonitor();
        
        MapParam params = new MapParam();
        params.put("max_tokens", 100);
        params.put("temperature", 0.1);
        
        long startTime = System.currentTimeMillis();
        
        CompletionResponse response = llmService.completionStream(
            monitor, 
            "You are creative writer. Write naturally.", 
            "Write a very short story about a robot learning to dance.", 
            params
        );
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        assertTrue(monitor.waitForCompletion(45, TimeUnit.SECONDS), "Stream should complete within 45 seconds");
        
        // Performance analysis
        int tokenCount = monitor.getTokenCount();
        long avgTimePerToken = tokenCount > 0 ? totalTime / tokenCount : 0;
        
        System.out.println("1. Performance Metrics:");
        System.out.println("   - Total time: " + totalTime + "ms");
        System.out.println("   - Token count: " + tokenCount);
        System.out.println("   - Avg time per token: " + avgTimePerToken + "ms");
        System.out.println("   - Tokens per second: " + (tokenCount * 1000.0 / totalTime));
        
        System.out.println("2. Response quality:");
        String finalText = response.getResponse().getText();
        System.out.println("   - Final length: " + finalText.length() + " characters");
        System.out.println("   - Contains story elements: " + 
                          (finalText.toLowerCase().contains("robot") && finalText.toLowerCase().contains("dance")));
        
        System.out.println("3. Token streaming pattern:");
        List<String> tokens = monitor.getTokens();
        System.out.println("   - First token: '" + (tokens.isEmpty() ? "none" : tokens.get(0)) + "'");
        System.out.println("   - Last token: '" + (tokens.isEmpty() ? "none" : tokens.get(tokens.size() - 1)) + "'");
        
        // Verify reasonable performance
        assertTrue(tokenCount > 5, "Should generate meaningful content with multiple tokens");
        assertTrue(totalTime < 60000, "Should complete within reasonable time");
        
        System.out.println("=== Performance analysis completed ===\n");
    }
    
    /**
     * Enhanced monitoring stream for integration testing
     */
    private static class StreamingMonitor implements ResponseStream {
        private final List<String> tokens = new ArrayList<>();
        private final AtomicInteger tokenCount = new AtomicInteger(0);
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final AtomicBoolean hasError = new AtomicBoolean(false);
        private volatile Throwable error;
        private final CountDownLatch completionLatch = new CountDownLatch(1);
        private final List<Long> tokenTimestamps = new ArrayList<>();
        private final long startTime = System.currentTimeMillis();
        
        @Override
        public synchronized void onToken(String token) {
            tokens.add(token);
            tokenCount.incrementAndGet();
            tokenTimestamps.add(System.currentTimeMillis() - startTime);
        }
        
        @Override
        public void onComplete() {
            completed.set(true);
            completionLatch.countDown();
        }
        
        @Override
        public void onError(Throwable throwable) {
            this.error = throwable;
            hasError.set(true);
            completionLatch.countDown();
        }
        
        public List<String> getTokens() {
            synchronized (this) {
                return new ArrayList<>(tokens);
            }
        }
        
        public int getTokenCount() {
            return tokenCount.get();
        }
        
        public boolean isCompleted() {
            return completed.get();
        }
        
        public Throwable getError() {
            return error;
        }
        
        public boolean waitForCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            return completionLatch.await(timeout, unit);
        }
        
        public void reset() {
            synchronized (this) {
                tokens.clear();
                tokenCount.set(0);
                completed.set(false);
                hasError.set(false);
                error = null;
                tokenTimestamps.clear();
            }
        }
        
        public List<Long> getTokenTimestamps() {
            synchronized (this) {
                return new ArrayList<>(tokenTimestamps);
            }
        }
    }
}