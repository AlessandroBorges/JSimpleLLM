package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ContentType;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for Ollama completion functionality.
 */
class OllamaCompletionTest extends OllamaLLMServiceTestBase {

	@Test
	@DisplayName("Test basic text completion with Ollama")
	void testBasicCompletion() throws LLMException {
		// Given
		String system = "You are a helpful assistant that provides concise answers.";
		String query  = "What is 2 + 2?";

		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 50);
		params.put("temperature", 0.1);

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
		assertTrue(responseText.contains("4"), "Response should contain the answer '4'");

		System.out.println("Ollama Completion Response: "
		            + responseText);
	}

	@Test
	@DisplayName("Test completion without system message")
	void testCompletionWithoutSystem() throws LLMException {
		// Given
		String query = "Tell me a very short joke.";

		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 100);

		// When
		CompletionResponse response = llmService.completion(null, query, params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");

		String responseText = response.getResponse().getText();
		assertNotNull(responseText, "Response text should not be null");
		assertFalse(responseText.trim().isEmpty(), "Response text should not be empty");

		System.out.println("Ollama Joke Response: "
		            + responseText);
	}

	@Test
	@DisplayName("Test factory method for creating Ollama service")
	void testFactoryMethod() throws LLMException {
		// Given
		var ollamaService = LLMServiceFactory.createOllama();

		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 30);

		// When
		CompletionResponse response = ollamaService.completion("You are helpful.", "Count from 1 to 3.", params);

		// Then
		assertNotNull(response);
		assertNotNull(response.getResponse());

		System.out.println("Factory Method Response: "
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test completion with different available models")
	void testCompletionWithDifferentModels() throws LLMException {
		// Test with the first available model
		MapParam params = new MapParam();
		params.put("model", getFirstAvailableModel());
		params.put("max_tokens", 30);
		params.put("temperature", 0.1);

		CompletionResponse response = llmService.completion("You are a math tutor.", "What is 3 × 4?", params);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertTrue(response.getResponse().getText().contains("12"));

		System.out.println("Math Response with "
		            + getFirstAvailableModel()
		            + ": "
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test completion error handling with empty query")
	void testCompletionWithEmptyQuery() {
		// Given
		String   query  = "";
		MapParam params = new MapParam();

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.completion("System", query, params); },
		             "Should throw exception for empty query");
	}

	@Test
	@DisplayName("Test completion error handling with null query")
	void testCompletionWithNullQuery() {
		// Given
		String   query  = null;
		MapParam params = new MapParam();

		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.completion("System", query, params); },
		             "Should throw exception for null query");
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
	@DisplayName("Test Ollama-specific configuration")
	void testOllamaSpecificConfig() {
		// Given
		var ollamaConfig = OllamaLLMService.getDefaultLLMConfig();

		// Then
		assertNotNull(ollamaConfig);
		assertTrue(ollamaConfig.getBaseUrl().contains("localhost:11434"), "Should use local Ollama server");
		assertEquals("OLLAMA_API_KEY", ollamaConfig.getApiTokenEnvironment(), "Should use OLLAMA_API_KEY env var");

		// Should have some models configured
		assertFalse(ollamaConfig.getModelMap().isEmpty(), "Should have default models configured");

		System.out.println("Ollama Base URL: "
		            + ollamaConfig.getBaseUrl());
		System.out.println("Available Ollama models: "
		            + ollamaConfig.getModelMap().keySet());
	}

	@Test
	@DisplayName("Test Ollama coding model detection")
	void testCodingModelDetection() {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		
		// Test various model name patterns
		assertTrue(service.isModelType("codellama:7b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING),
				  "Should detect CodeLlama as coding model");
		assertTrue(service.isModelType("starcoder:3b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING),
				  "Should detect StarCoder as coding model");
		assertTrue(service.isModelType("codestral:22b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING),
				  "Should detect Codestral as coding model");
		
		System.out.println("Coding model detection works correctly");
	}

	@Test
	@DisplayName("Test Ollama vision model detection")
	void testVisionModelDetection() {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		
		// Test vision model detection
		assertTrue(service.isModelType("llava:7b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION),
				  "Should detect LLaVA as vision model");
		assertTrue(service.isModelType("llava-llama3:8b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION),
				  "Should detect LLaVA variant as vision model");
		assertFalse(service.isModelType("phi4-mini", bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION),
				   "Should not detect text model as vision");
		
		System.out.println("Vision model detection works correctly");
	}

	@Test
	@DisplayName("Test Ollama embedding model detection")
	void testEmbeddingModelDetection() {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		
		// Test embedding model detection
		assertTrue(service.isModelType("nomic-embed-text", bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING),
				  "Should detect nomic-embed-text as embedding model");
		assertTrue(service.isModelType("bge-large", bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING),
				  "Should detect BGE as embedding model");
		assertTrue(service.isModelType("snowflake-arctic-embed", bor.tools.simplellm.LLMConfig.MODEL_TYPE.EMBEDDING),
				  "Should detect snowflake embedding model");
		
		System.out.println("Embedding model detection works correctly");
	}
}
