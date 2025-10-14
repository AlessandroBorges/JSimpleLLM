package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Integration tests for LM Studio LLM Service.
 * These tests require a running LM Studio server at localhost:1234 with at
 * least one model loaded.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LMStudioIntegrationTest extends LMStudioLLMServiceTestBase {

	@Test
	@Order(1)
	@DisplayName("Integration Test: LM Studio server connectivity")
	void testLMStudioServerConnectivity() {
		System.out.println("\n=== LM Studio Server Connectivity Test ===");

		// Test that we can create the service
		assertNotNull(llmService, "LM Studio service should be created");

		// Test configuration
		var config = llmService.getLLMConfig();
		assertNotNull(config);
		assertTrue(config.getBaseUrl().contains("localhost:1234"), "Should use local LM Studio endpoint");

		System.out.println("1. LM Studio service created successfully");
		System.out.println("2. Base URL: "
		            + config.getBaseUrl());
		System.out.println("3. Available models: "
		            + config.getModelMap().keySet());
		System.out.println("=== Connectivity test completed ===\n");
	}

	@Test
	@Order(2)
	@DisplayName("Integration Test: Model listing and capabilities")
	void testModelListingAndCapabilities() throws LLMException {
		System.out.println("\n=== Model Listing and Capabilities Test ===");

		// Get available models
		List<Model> models = llmService.getRegisteredModels();
		assertNotNull(models, "Models list should not be null");
		assertFalse(models.isEmpty(), "Should have at least one model");

		System.out.println("1. Available models count: "
		            + models.size());

		for (Model model : models) {
			assertNotNull(model.getName(), "Model name should not be null");
			assertTrue(model.getContextLength() > 0, "Context size should be positive");
			assertNotNull(model.getTypes(), "Model types should be defined");

			System.out.println("   - "
			            + model.getName()
			            + " (context: "
			            + model.getContextLength()
			            + ", types: "
			            + model.getTypes().size()
			            + ")");
		}

		System.out.println("=== Model listing test completed ===\n");
	}

	public static void main(String[] args) {
		LMStudioIntegrationTest test = new LMStudioIntegrationTest();
		test.setUp();
		try {
			test.testLMStudioServerConnectivity();
			test.testModelListingAndCapabilities();
			test.testCompleteConversationFlow();
			test.testFactoryMethodUsage();
			test.testLMStudioVsOpenAICompatibility();
			test.testLMStudioSpecificFeatures();
			test.testErrorHandlingAndRecovery();
		} catch (LLMException e) {
			e.printStackTrace();
		}
	}

	@Test
	@Order(3)
	@DisplayName("Integration Test: Complete conversation flow with LM Studio")
	void testCompleteConversationFlow() throws LLMException {
		System.out.println("\n=== Complete Conversation Flow Test ===");

		// Step 1: Create a chat session
		Chat chat = new Chat("lmstudio-integration-test");
		chat.setModel(getFirstAvailableModel());
		chat.addSystemMessage("You are a helpful assistant that provides concise answers.");

		MapParam params = new MapParam();
		params.put("max_tokens", 80);
		params.put("temperature", 0.2);

		System.out.println("1. Created chat session with model: "
		            + getFirstAvailableModel());

		// Step 2: First interaction
		CompletionResponse response1 = llmService.chatCompletion(chat, "What is the capital of Japan?", params);
		System.out.println("Debug: Chat after first user message: "
		            + chat);
		assertNotNull(response1);
		assertTrue(response1.getResponse().getText().toLowerCase().contains("tokyo"));
		assertEquals(3, chat.messageCount()); // system, user, assistant

		System.out.println("2. First response: "
		            + response1.getResponse().getText());

		// Step 3: Follow-up question (testing context retention)
		CompletionResponse response2 =
		            llmService.chatCompletion(chat, "What is the population of that city approximately?", params);

		assertNotNull(response2);
		String response2Text = response2.getResponse().getText().toLowerCase();
		assertTrue(response2Text.contains("tokyo") || response2Text.contains("million")
		           || response2Text.contains("13")
		           || response2Text.contains("14"));
		assertEquals(5, chat.messageCount()); // system, user, assistant, user, assistant

		System.out.println("3. Follow-up response: "
		            + response2.getResponse().getText());

		// Step 4: Test completion method
		CompletionResponse response3 = llmService.completion("You are a math tutor.", "What is 15 × 8?", params);

		assertNotNull(response3);
		assertTrue(response3.getResponse().getText().contains("120"));

		System.out.println("4. Math completion: "
		            + response3.getResponse().getText());

		System.out.println("=== Integration test completed successfully ===\n");
	}

	@Test
	@Order(4)
	@DisplayName("Integration Test: Factory method usage")
	void testFactoryMethodUsage() throws LLMException {
		System.out.println("\n=== Factory Method Usage Test ===");

		// Test factory method without config
		var service1 = LLMServiceFactory.createLMStudio();
		assertNotNull(service1);

		// Test factory method with config
		var customConfig = LMStudioLLMService.getDefaultLLMConfig();
		var service2     = LLMServiceFactory.createLMStudio(customConfig);
		assertNotNull(service2);

		// Test both services work
		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 30);

		CompletionResponse response = service1.completion("Be helpful.", "Say hello.", params);

		assertNotNull(response);
		assertTrue(response.getResponse().getText().toLowerCase().contains("hello"));

		System.out.println("1. Factory methods work correctly");
		System.out.println("2. Response: "
		            + response.getResponse().getText());
		System.out.println("=== Factory method test completed ===\n");
	}

	@Test
	@Order(5)
	@DisplayName("Integration Test: LM Studio vs OpenAI API compatibility")
	void testLMStudioVsOpenAICompatibility() throws LLMException {
		System.out.println("\n=== LM Studio vs OpenAI API Compatibility Test ===");

		// Test that LM Studio service implements the same interface as OpenAI
		assertTrue(llmService instanceof OpenAILLMService, "LMStudioLLMService should extend OpenAILLMService");

		// Test that basic operations work the same way
		String query  = "What is machine learning?";
		String system = "Provide a very brief, technical explanation.";

		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 600);
		params.put("temperature", 0.1);

		// Test completion
		CompletionResponse response = llmService.completion(system, query, params);
		assertNotNull(response);
		assertNotNull(response.getResponse().getText());

		// Test chat
		Chat chat = new Chat();
		chat.addSystemMessage(system);
		CompletionResponse chatResponse = llmService.chatCompletion(chat, query, params);
		assertNotNull(chatResponse);
		assertNotNull(chatResponse.getResponse().getText());

		System.out.println("1. Both completion and chat work with same interface");
		System.out.println("2. Completion response: "
		            + response.getResponse()
		                        .getText()
		                        .substring(0, Math.min(100, response.getResponse().getText().length()))
		            + "...");
		System.out.println("3. Chat response: "
		            + chatResponse.getResponse()
		                        .getText()
		                        .substring(0, Math.min(100, chatResponse.getResponse().getText().length()))
		            + "...");

		System.out.println("=== Compatibility test completed ===\n");
	}

	@Test
	@Order(6)
	@DisplayName("Integration Test: LM Studio specific features")
	void testLMStudioSpecificFeatures() throws LLMException {
		System.out.println("\n=== LM Studio Specific Features Test ===");

		LMStudioLLMService lmStudioService = (LMStudioLLMService) llmService;

		// Test model type detection
		String firstModel = getFirstAvailableModel();

		System.out.println("1. Model type detection for: "
		            + firstModel);
		System.out.println("   - Language support: "
		            + lmStudioService.isModelType(firstModel, bor.tools.simplellm.Model_Type.LANGUAGE));
		System.out.println("   - Coding support: "
		            + lmStudioService.isModelType(firstModel, bor.tools.simplellm.Model_Type.CODING));
		System.out.println("   - Vision support: "
		            + lmStudioService.isModelType(firstModel, bor.tools.simplellm.Model_Type.VISION));

		// Test default model selection
		String defaultModel = lmStudioService.getDefaultModelName();
		assertNotNull(defaultModel);
		System.out.println("2. Default model: "
		            + defaultModel);

		// Test endpoint detection
		assertFalse(lmStudioService.isOpenAIEndpoint(), "Should correctly identify as non-OpenAI endpoint");
		System.out.println("3. Correctly identified as local LM Studio endpoint");

		System.out.println("=== LM Studio specific features test completed ===\n");
	}

	@Test
	@Order(7)
	@DisplayName("Integration Test: Error handling and recovery")
	void testErrorHandlingAndRecovery() {
		System.out.println("\n=== Error Handling and Recovery Test ===");

		// Test with invalid model (should handle gracefully)
		MapParam params = new MapParam();
		params.put("model", "nonexistent-model");
		params.put("max_tokens", 30);

		try {
			CompletionResponse response = llmService.completion("You are helpful.", "Say hello.", params);

			// If it succeeds, it probably used a fallback model
			System.out.println("1. Handled invalid model gracefully with fallback");
			System.out.println("   Response: "
			            + response.getResponse().getText());

		} catch (LLMException e) {
			System.out.println("1. Correctly threw exception for invalid model: "
			            + e.getMessage());
		}

		// Test recovery with valid model
		params.put("model", getFirstAvailableModel());

		try {
			CompletionResponse normalResponse = llmService.completion("You are helpful.", "Say goodbye.", params);

			assertNotNull(normalResponse);
			System.out.println("2. Successfully recovered with valid model");
			System.out.println("   Response: "
			            + normalResponse.getResponse().getText());

		} catch (LLMException e) {
			fail("Should not fail with valid model: "
			            + e.getMessage());
		}

		System.out.println("=== Error handling test completed ===\n");
	}
}
