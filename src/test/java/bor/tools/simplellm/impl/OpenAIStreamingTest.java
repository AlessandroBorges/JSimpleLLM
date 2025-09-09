package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for OpenAI streaming completion functionality.
 * <p>
 * These tests require a valid OpenAI API key and make real API calls.
 * They will be skipped if no API key is available.
 * </p>
 */
class OpenAIStreamingTest extends OpenAILLMServiceTestBase {

	private CollectingResponseStream testStream;

	@BeforeEach
	void setUpStream() {
		testStream = new CollectingResponseStream();
	}

	@Test
	@DisplayName("Test basic streaming completion")
	void testBasicStreamingCompletion() throws LLMException {
		// Given
		String system = "You are a helpful assistant. Keep responses very short.";
		String query  = "Count from 1 to 30.";

		MapParam params = new MapParam();
		params.put("max_tokens", 3000);
		params.put("temperature", 0.1);

		// When
		CompletionResponse response = llmService.completionStream(testStream, system, query, params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");

		String finalText = response.getResponse().getText();
		assertNotNull(finalText, "Final response text should not be null");
		assertFalse(finalText.trim().isEmpty(), "Final response text should not be empty");

		// Verify streaming behavior
		assertTrue(testStream.isCompleted(), "Stream should be completed");
		assertFalse(testStream.getTokens().isEmpty(), "Should have received tokens");
		assertNull(testStream.getError(), "Should not have errors");

		// Verify final text matches accumulated tokens
		String accumulatedText = String.join("", testStream.getTokens());
		assertEquals(accumulatedText, finalText, "Final text should match accumulated tokens");

		System.out.println("Streaming completion response: "
		            + finalText);
		System.out.println("Number of tokens received: "
		            + testStream.getTokens().size());
	}

	@Test
	@DisplayName("Test streaming chat completion")
	void testStreamingChatCompletion() throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.addSystemMessage("You are a helpful assistant. Be very brief.");

		MapParam params = new MapParam();
		params.put("max_tokens", 30);
		params.put("temperature", 0.5);

		System.out.println("Antes");
		// When
		CompletionResponse response = llmService.chatCompletionStream(testStream, chat, "What is 2+2?", params);
		System.out.println("Depois");
		System.out.println("Chat ID: "
		            + response.getChatId());
		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");

		String finalText = response.getResponse().getText();
		assertNotNull(finalText, "Final response text should not be null");
		assertFalse(finalText.trim().isEmpty(), "Final response text should not be empty");
		assertTrue(finalText.contains("4"), "Response should contain the answer '4'");

		// Verify streaming behavior
		assertTrue(testStream.isCompleted(), "Stream should be completed");
		assertFalse(testStream.getTokens().isEmpty(), "Should have received tokens");
		assertNull(testStream.getError(), "Should not have errors");

		// Verify chat was updated
		assertEquals(3, chat.messageCount(), "Chat should have system, user, and assistant messages");
		assertEquals(response.getChatId(), chat.getId(), "Response should have correct chat ID");

		System.out.println("Streaming chat response: "
		            + finalText);
		System.out.println("Number of tokens received: "
		            + testStream.getTokens().size());
	}

	@Test
	@DisplayName("Test streaming with multi-turn conversation")
	void testStreamingMultiTurnConversation() throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.addSystemMessage("You are a helpful assistant. Keep responses brief.");
		chat.addUserMessage("What is the capital of France?");
		chat.addAssistantMessage("Paris");

		MapParam params = new MapParam();
		params.put("max_tokens", 30);
		params.put("temperature", 0.1);

		// When
		CompletionResponse response = llmService.chatCompletionStream(testStream, chat, "What about Italy?", params);

		// Then
		assertNotNull(response, "Response should not be null");
		String finalText = response.getResponse().getText();
		assertTrue(finalText.toLowerCase().contains("rome") || finalText.toLowerCase().contains("roma"),
		           "Response should mention Rome");

		// Verify conversation context
		assertEquals(5, chat.messageCount(), "Chat should have 5 messages total");

		System.out.println("Multi-turn streaming response: "
		            + finalText);
	}

	@Test
	@DisplayName("Test streaming error handling with null stream")
	void testStreamingWithNullStream() {
		// Given
		String   query  = "Test query";
		MapParam params = new MapParam();

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.completionStream(null, "System", query, params); },
		             "Should throw exception for null stream");
	}

	@Test
	@DisplayName("Test streaming error handling with null query")
	void testStreamingWithNullQuery() {
		// Given
		MapParam params = new MapParam();

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.completionStream(testStream, "System", null, params); },
		             "Should throw exception for null query");
	}

	@Test
	@DisplayName("Test streaming error handling with null chat")
	void testStreamingWithNullChat() {
		// Given
		MapParam params = new MapParam();

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.chatCompletionStream(testStream, null, "Query", params); },
		             "Should throw exception for null chat");
	}

	@Test
	@DisplayName("Test streaming with empty chat")
	void testStreamingWithEmptyChat() throws LLMException {
		// Given
		Chat emptyChat = new Chat();

		MapParam params = new MapParam();
		params.put("max_tokens", 30);

		// When
		CompletionResponse response = llmService.chatCompletionStream(testStream, emptyChat, "Hello", params);

		// Then
		assertNotNull(response);
		assertTrue(testStream.isCompleted());
		assertEquals(2, emptyChat.messageCount(), "Should have user and assistant messages");

		System.out.println("Empty chat streaming response: "
		            + response.getResponse().getText());
	}

	/**
	 * Helper class to collect streaming tokens and events for testing.
	 */
	private static class CollectingResponseStream implements ResponseStream {
		private final List<String>               tokens          = new ArrayList<>();
		private final AtomicBoolean              completed       = new AtomicBoolean(false);
		private final AtomicReference<Throwable> error           = new AtomicReference<>();
		private final CountDownLatch             completionLatch = new CountDownLatch(1);

		@Override
		public void onToken(String token) {
			tokens.add(token);
			System.err.println("Received token: "
			            + token);
		}

		@Override
		public void onComplete() {
			completed.set(true);
			completionLatch.countDown();
		}

		@Override
		public void onError(Throwable throwable) {
			error.set(throwable);
			completionLatch.countDown();
		}

		public List<String> getTokens() { return new ArrayList<>(tokens); }

		public boolean isCompleted() { return completed.get(); }

		public Throwable getError() { return error.get(); }

		public boolean waitForCompletion(long timeout, TimeUnit unit) throws InterruptedException {
			return completionLatch.await(timeout, unit);
		}
	}
}
