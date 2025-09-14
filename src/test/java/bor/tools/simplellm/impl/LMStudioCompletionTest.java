package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.Reasoning_Effort;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.exceptions.LLMException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for LM Studio completion functionality.
 */
class LMStudioCompletionTest extends LMStudioLLMServiceTestBase {

	public static void main(String[] args) throws Exception {
		LMStudioCompletionTest test = new LMStudioCompletionTest();
		test.setUp();
		test.testBasicCompletion();
		test.testCompletionWithoutSystem();
		test.testFactoryMethod();
		test.testCompletionWithReasoning();
		test.testCompletionResponseMetadata();
		test.testLMStudioSpecificConfig();
		test.testCodingModelDetection();
		test.testVisionModelDetection();
		test.testCompletionStream();
		test.testChatCompletionStream();
	}

	@Test
	@DisplayName("Test basic text completion with LM Studio")
	void testBasicCompletion() throws LLMException {
		// Given
		String system = "You are a helpful assistant that provides concise answers.";
		String query  = "A atual capital do Brasil é o Rio de Janeiro ou Brasília?";

		MapParam params = new MapParam();
		params.model(getFirstAvailableModel());
		System.err.println("Using model: " + params.get("model"));
		
		params.maxTokens( 1024);
		params.temperature(0.4f);
		params.reasoningEffort(Reasoning_Effort.high); // Use default reasoning effort

		// When
		CompletionResponse response = llmService.completion(system, query, params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");
		assertEquals(ContentType.TEXT, response.getResponse().getType(), "Response should be text type");

		String responseText = response.getResponse().getText();
		assertNotNull(responseText, "Response text should not be null");
		assertFalse(responseText.trim().isEmpty(), "Response text should not be empty");

		// Response should contain the answer
		assertTrue(responseText.contains("Brasília"), "Response should contain the answer '4'");
		
		System.out.println("Model used: "                             + response.getModel());
		System.out.println("Reasoning Content: " + response.getReasoningContent());
		System.out.println("End reason: "                            + response.getEndReason());
		System.out.println("Response metadata: "                      + response.getInfo());
		System.out.println("LM Studio Completion Response: "          + responseText);
	}

	@Test
	@DisplayName("Test completion without system message")
	void testCompletionWithoutSystem() throws LLMException {
		// Given
		String query = "Tell me a very short joke.";

		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 500);

		// When
		CompletionResponse response = llmService.completion(null, query, params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");

		String responseText = response.getResponse().getText();
		assertNotNull(responseText, "Response text should not be null");
		assertFalse(responseText.trim().isEmpty(), "Response text should not be empty");

		System.out.println("LM Studio Joke Response: "
		            + responseText);
	}

	@Test
	@DisplayName("Test factory method for creating LM Studio service")
	void testFactoryMethod() throws LLMException {
		// Given
		var lmStudioService = LLMServiceFactory.createLMStudio();

		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 300);

		// When
		CompletionResponse response = lmStudioService.completion("You are helpful.", "Count from 1 to 3.", params);

		// Then
		assertNotNull(response);
		assertNotNull(response.getResponse());

		System.out.println("Factory Method Response: "
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test completion with reasoning effort")
	void testCompletionWithReasoning() throws LLMException {
		// Test with the first available model
		MapParam params = new MapParam();
		params.put("model", "phi3.5-mini");
		params.put("max_tokens", 1024);
		params.reasoningEffort(Reasoning_Effort.medium);
		var query = "Onde nasceu o navegador Pedro Álvares Cabral?";
		CompletionResponse response = llmService.completion("Responda o questionamento abaixo "
					+ " de forma sucinta e objetiva, "
					+ " na lingua Português do Brasil (pt_br).\n ",
                        query, 
                        params);

		System.out.println("Response text:\n"  + response.getText());
		System.out.println("Reasoning : "  + response.getReasoningContent());
		System.out.println("\n\n ####### \n Response info: "  + response);
		
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertTrue(response.getResponse().getText().contains("Brasília"));

		System.out.println("Response with model:\n"
		            + response.getModel()
		            + "\n and text: \n"
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test completion error handling with empty query")
	void testCompletionWithEmptyQuery() {
		// Given
		String   query  = "";
		MapParam params = new MapParam();
		params.maxTokens(1024);

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.completion(null, query, params); },
		             "Should throw exception for empty query");
		
		System.out.println("Empty query test ended ");
	}

	@Test
	@DisplayName("Test completion error handling with null query")
	void testCompletionWithNullQuery() {
		// Given
		String   query  = null;
		MapParam params = new MapParam();
		params.maxTokens(1024);
		// When & Then
		/*
		assertThrows(LLMException.class,
		             () -> { llmService.completion("Você é um assistente expert em matematica. ", query, params); },
		             "Should throw exception for null query");
		  */
	}

	@Test
	@DisplayName("Test completion response metadata")
	void testCompletionResponseMetadata() throws LLMException {
		// Given
		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 20);

		// When
		CompletionResponse response = llmService.completion("Be brief.", "Count to 3.", params);

		// Then
		assertNotNull(response.getInfo(), "Response metadata should not be null");
		assertNotNull(response.getEndReason(), "End reason should be set");

		System.out.println("End reason: "
		            + response.getEndReason());
		System.out.println("Response metadata keys: "
		            + response.getInfo().keySet());
	}

	@Test
	@DisplayName("Test LM Studio-specific configuration")
	void testLMStudioSpecificConfig() {
		// Given
		var lmStudioConfig = LMStudioLLMService.getDefaultLLMConfig();

		// Then
		assertNotNull(lmStudioConfig);
		assertTrue(lmStudioConfig.getBaseUrl().contains("localhost:1234"), "Should use local LM Studio server");
		assertEquals("LMSTUDIO_API_KEY", lmStudioConfig.getApiTokenEnvironment(), "Should use LMSTUDIO_API_KEY env var");

		// Should have some models configured
		assertFalse(lmStudioConfig.getModelMap().isEmpty(), "Should have default models configured");

		System.out.println("LM Studio Base URL: "
		            + lmStudioConfig.getBaseUrl());
		System.out.println("Available LM Studio models: "
		            + lmStudioConfig.getModelMap().keySet());
	}

	@Test
	@DisplayName("Test LM Studio coding model detection")
	void testCodingModelDetection() {
		// Given
		LMStudioLLMService service = new LMStudioLLMService();

		// Test various model name patterns
		assertTrue(service.isModelType("codellama-7b-instruct", bor.tools.simplellm.Model_Type.CODING),
		           "Should detect CodeLlama as coding model");
		assertTrue(service.isModelType("deepseek-coder", bor.tools.simplellm.Model_Type.CODING),
		           "Should detect DeepSeek Coder as coding model");
		assertFalse(service.isModelType("llama-3.1-8b-instruct", bor.tools.simplellm.Model_Type.CODING),
		            "Should not detect regular Llama as coding-specific");

		System.out.println("Coding model detection works correctly");
	}

	@Test
	@DisplayName("Test LM Studio vision model detection")
	void testVisionModelDetection() {
		// Given
		LMStudioLLMService service = new LMStudioLLMService();

		// Test vision model detection
		assertTrue(service.isModelType("llava-1.5-7b", bor.tools.simplellm.Model_Type.VISION),
		           "Should detect LLaVA as vision model");
		assertTrue(service.isModelType("bakllava-1-7b", bor.tools.simplellm.Model_Type.VISION),
		           "Should detect BakLLaVA as vision model");
		assertFalse(service.isModelType("mistral-7b", bor.tools.simplellm.Model_Type.VISION),
		            "Should not detect text model as vision");

		System.out.println("Vision model detection works correctly");
	}

	@Test
	@DisplayName("Test LM Studio streaming completion")
	void testCompletionStream() throws LLMException, InterruptedException {
		// Given
		String system = "You are a helpful assistant.";
		String query = "Count from 1 to 3.";

		MapParam params = new MapParam();
		params.model(getFirstAvailableModel());
		params.maxTokens(100);
		params.temperature(0.3f);

		// Streaming callback implementation
		StringBuilder streamedContent = new StringBuilder();
		AtomicReference<Throwable> error = new AtomicReference<>();
		AtomicBoolean completed = new AtomicBoolean(false);
		CountDownLatch latch = new CountDownLatch(1);

		// Streaming callback with typed content handling
		StringBuilder streamedReasoning = new StringBuilder();
		ResponseStream stream = new ResponseStream() {
			@Override
			public void onToken(String token, ResponseStream.ContentType type) {
				switch (type) {
					case TEXT -> {
						streamedContent.append(token);
						System.out.print(token);
					}
					case REASONING -> {
						streamedReasoning.append(token);
						System.out.print("[REASONING]" + token);
					}
					default -> {
						System.out.print("[" + type + "]" + token);
					}
				}
			}

			@Override
			public void onComplete() {
				completed.set(true);
				latch.countDown();
				System.out.println("\n[Stream completed]");
				if (streamedReasoning.length() > 0) {
					System.out.println("[Total Reasoning Length: " + streamedReasoning.length() + "]");
				}
			}

			@Override
			public void onError(Throwable throwable) {
				error.set(throwable);
				latch.countDown();
				System.out.println("\n[Stream error: " + throwable.getMessage() + "]");
			}
		};

		// When
		System.out.println("Starting streaming completion test...");
		CompletionResponse response = llmService.completionStream(stream, system, query, params);

		// Wait for streaming to complete
		boolean finished = latch.await(30, TimeUnit.SECONDS);

		// Then
		assertTrue(finished, "Streaming should complete within timeout");
		assertNotNull(response, "Response should not be null");
		assertTrue(completed.get(), "Stream should complete successfully");
		assertNotNull(error.get() == null ? null : error.get(), "No error should occur");

		String finalContent = streamedContent.toString();
		assertNotNull(finalContent, "Streamed content should not be null");
		assertFalse(finalContent.trim().isEmpty(), "Streamed content should not be empty");
		assertTrue(finalContent.contains("1") && finalContent.contains("2") && finalContent.contains("3"),
		           "Response should contain counting sequence");

		System.out.println("\nFinal streamed content: " + finalContent);
		System.out.println("Streamed reasoning length: " + streamedReasoning.length());
		System.out.println("Response metadata: " + response.getInfo());
	}

	@Test
	@DisplayName("Test LM Studio streaming chat completion")
	void testChatCompletionStream() throws LLMException, InterruptedException {
		// Given
		Chat chat = new Chat();
		chat.setId(UUID.randomUUID().toString());
		chat.setCreatedAt(LocalDateTime.now());		
		chat.addSystemMessage("You are a helpful assistant that provides concise answers.");

		String query = "Explique porque o céu é azul.";

		MapParam params = new MapParam();
		params.model("gpt-oss");
		params.maxTokens(2048);
		params.temperature(0.5f);
		params.reasoningEffort(Reasoning_Effort.medium);

		// Streaming callback using backward compatibility (single onToken method)
		StringBuilder streamedContent = new StringBuilder();
		StringBuilder streamedReasoning = new StringBuilder();
		AtomicReference<Throwable> error = new AtomicReference<>();
		AtomicBoolean completed = new AtomicBoolean(false);
		CountDownLatch latch = new CountDownLatch(1);

		ResponseStream stream = new ResponseStream() {
			@Override
			public void onToken(String token, ResponseStream.ContentType type) {
				// This method demonstrates backward compatibility
				// All content types are treated as TEXT by default
				switch (type) {
					case TEXT -> {
						streamedContent.append(token);
						System.out.print(token);
					}
					case REASONING -> {
						streamedReasoning.append(token);
						System.out.print("[REASONING]" + token);
					}
					default -> {
						System.out.print("[" + type + "]" + token);
					}
				}
				streamedContent.append(token);
				System.out.print(token);
			}

			@Override
			public void onComplete() {
				completed.set(true);
				latch.countDown();
				System.out.println("\n[Chat stream completed]");
			}

			@Override
			public void onError(Throwable throwable) {
				error.set(throwable);
				latch.countDown();
				System.out.println("\n[Chat stream error: " + throwable.getMessage() + "]");
			}
		};

		// When
		System.out.println("Starting streaming chat completion test...");
		CompletionResponse response = llmService.chatCompletionStream(stream, chat, query, params);

		// Wait for streaming to complete
		boolean finished = latch.await(30, TimeUnit.SECONDS);

		// Then
		assertTrue(finished, "Chat streaming should complete within timeout");
		assertNotNull(response, "Response should not be null");
		assertTrue(completed.get(), "Chat stream should complete successfully");
		//assertNotNull(error.get() == null ? null : error.get(), "No error should occur");

		String finalContent = streamedContent.toString();
		assertNotNull(finalContent, "Streamed content should not be null");
		assertFalse(finalContent.trim().isEmpty(), "Streamed content should not be empty");
		//assertTrue(finalContent.toLowerCase().contains("paris"), "Response should contain the answer 'Paris'");

		// Verify chat history was updated
		assertTrue(chat.getMessages().size() >= 3, "Chat should contain system, user, and assistant messages");
		assertEquals("You are a helpful assistant that provides concise answers.",
		             chat.getMessages().get(0).getText(),
		             "System message should be preserved");
		assertEquals(query, chat.getMessages().get(1).getText(), "User message should be added to chat");

		System.out.println("\n ## Final streamed reasoning: " + response.getReasoningContent());
		System.out.println("\n ## Final streamed content: " + finalContent);
		System.out.println("Chat messages count: " + chat.getMessages().size());
		System.out.println("Response metadata: " + response.getInfo());
	}
}
