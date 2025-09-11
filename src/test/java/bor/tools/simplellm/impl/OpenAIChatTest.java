package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for OpenAI chat completion functionality.
 */
class OpenAIChatTest extends OpenAILLMServiceTestBase {

	@Test
	@DisplayName("Test basic chat completion")
	void testBasicChatCompletion()
	            throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.addSystemMessage("You are a helpful math tutor.");
		chat.addUserMessage("What is 10 divided by 2?");

		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
		params.put("max_tokens", 50);
		params.put("temperature", 0.0);

		// When
		CompletionResponse response = llmService.chatCompletion(chat, "Please explain your answer.", params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");
		assertEquals(ContentType.TEXT, response.getResponse().getType(), "Response should be text type");

		String responseText = response.getResponse().getText();
		assertNotNull(responseText, "Response text should not be null");
		assertFalse(responseText.trim().isEmpty(), "Response text should not be empty");

		// Response should contain the answer
		assertTrue(responseText.contains("5"), "Response should contain the answer '5'");

		// Check that the chat was updated with messages
		assertEquals(4, chat.messageCount(), "Chat should have 4 messages: system, user, new user, assistant");
		assertEquals(MessageRole.ASSISTANT, chat.getLastMessage().getRole(), "Last message should be from assistant");

		System.out.println("# Chat Response: "
		            + responseText);
		System.out.println("\n# Final message count: "
		            + chat.messageCount());
	}

	@Test
	@DisplayName("Test chat completion without additional query")
	void testChatCompletionWithoutQuery()
	            throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.addSystemMessage("You are a creative writer.");
		chat.addUserMessage("Write a haiku about programming.");

		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
		params.put("max_tokens", 100);

		// When - passing null/empty query
		CompletionResponse response = llmService.chatCompletion(chat, null, params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");

		String responseText = response.getResponse().getText();
		assertNotNull(responseText, "Response text should not be null");
		assertFalse(responseText.trim().isEmpty(), "Response text should not be empty");

		// Check that only the assistant response was added (no new user message)
		assertEquals(3, chat.messageCount(), "Chat should have 3 messages: system, user, assistant");

		System.out.println("Haiku Response: "
		            + responseText);
	}

	@Test
	@DisplayName("Test multi-turn conversation")
	void testMultiTurnConversation()
	            throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.setModel("gpt-4o-mini");
		chat.addSystemMessage("You are a helpful assistant. Keep responses brief.");

		MapParam params = new MapParam();
		params.put("max_tokens", 50);
		params.put("temperature", 0.3);

		// Turn 1
		CompletionResponse response1 = llmService.chatCompletion(chat, "What's the capital of France?", params);
		assertNotNull(response1);
		assertTrue(response1.getResponse().getText().toLowerCase().contains("paris"));

		// Turn 2 - follow up question
		CompletionResponse response2 = llmService.chatCompletion(chat, "What about Italy?", params);
		assertNotNull(response2);
		assertTrue(response2.getResponse().getText().toLowerCase().contains("rome"));

		// Check conversation state
		assertEquals(6, chat.messageCount(), "Should have 6 messages total");

		System.out.println("Conversation:");
		for (int i = 0; i < chat.messageCount(); i++) {
			var msg = chat.getMessage(i);
			System.out.println(msg.getRole()
			            + ": "
			            + msg.getText());
		}
	}

	@Test
	@DisplayName("Test chat with custom model")
	void testChatWithCustomModel()
	            throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.setModel("gpt-4o-mini");  // Set model in chat
		chat.addSystemMessage("You are a concise assistant.");

		MapParam params = new MapParam();
		params.put("max_tokens", 30);

		// When
		CompletionResponse response = llmService.chatCompletion(chat, "Say hello", params);

		// Then
		assertNotNull(response);
		assertTrue(response.getResponse().getText().toLowerCase().contains("hello"));
		assertEquals(chat.getId(), response.getChatId(), "Response should have chat ID");

		System.out.println("Custom model response: "
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test chat error handling with null chat")
	void testChatWithNullChat() {
		// Given
		Chat     chat   = null;
		MapParam params = new MapParam();

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.chatCompletion(chat, "Hello", params); },
		             "Should throw exception for null chat");
	}

	@Test
	@DisplayName("Test empty chat completion")
	void testEmptyChat()
	            throws LLMException {
		// Given
		Chat chat = new Chat();  // Empty chat

		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
		params.put("max_tokens", 50);

		// When
		CompletionResponse response = llmService.chatCompletion(chat, "Hello, how are you?", params);

		// Then
		assertNotNull(response);
		assertNotNull(response.getResponse().getText());

		// Chat should now have user message and assistant response
		assertEquals(2, chat.messageCount());

		System.out.println("Empty chat response: "
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test chat context preservation")
	void testChatContextPreservation()
	            throws LLMException {
		// Given
		Chat chat = new Chat();
		chat.addSystemMessage("Remember: My favorite color is blue.");
		chat.addUserMessage("What's my favorite color?");

		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
		params.put("max_tokens", 30);
		params.put("temperature", 0.0);

		// First interaction
		CompletionResponse response1 = llmService.chatCompletion(chat, null, params);
		assertTrue(response1.getResponse().getText().toLowerCase().contains("blue"));

		// Follow-up to test context retention
		CompletionResponse response2 = llmService.chatCompletion(chat, "Are you sure?", params);
		assertNotNull(response2);

		System.out.println("Context test - First: "
		            + response1.getResponse().getText());
		System.out.println("Context test - Follow-up: "
		            + response2.getResponse().getText());
	}
}
