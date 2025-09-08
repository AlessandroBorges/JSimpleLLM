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
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for OpenAI completion functionality.
 */
class OpenAICompletionTest extends OpenAILLMServiceTestBase {

	@Test
	@DisplayName("Test basic text completion")
	void testBasicCompletion()
	            throws LLMException {
		// Given
		String system = "You are a helpful assistant that provides concise answers.";
		String query  = "What is 2 + 2?";

		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
		params.put("max_tokens", 50);
		params.put("temperature", 0.20);

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

		System.out.println("Completion Response: "
		            + responseText);
	}

	@Test
	@DisplayName("Test completion without system message")
	void testCompletionWithoutSystem()
	            throws LLMException {
		// Given
		String query = "Tell me a very short joke.";

		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
		params.put("max_tokens", 100);

		// When
		CompletionResponse response = llmService.completion(null, query, params);

		// Then
		assertNotNull(response, "Response should not be null");
		assertNotNull(response.getResponse(), "Response content should not be null");

		String responseText = response.getResponse().getText();
		assertNotNull(responseText, "Response text should not be null");
		assertFalse(responseText.trim().isEmpty(), "Response text should not be empty");

		System.out.println("Joke Response: "
		            + responseText);
	}

	@Test
	@DisplayName("Test completion with different models")
	void testCompletionWithDifferentModels()
	            throws LLMException {
		// Test with gpt-4o-mini
		MapParam params = new MapParam();
		params.put("model", "gpt-5-mini");
		params.put("max_tokens", 30);
		params.put("temperature", 0.0);

		CompletionResponse response = llmService.completion("You are a math tutor.", "What is 5 × 3?", params);

		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertTrue(response.getResponse().getText().contains("15"));

		System.out.println("Math Response: "
		            + response.getResponse().getText());
	}

	@Test
	@DisplayName("Test completion with invalid model should use fallback")
	void testCompletionWithInvalidModel()
	            throws LLMException {
		// Given
		MapParam params = new MapParam();
		params.put("model", "nonexistent-model");
		params.put("max_tokens", 30);

		// When - this should not fail but use a fallback model
		CompletionResponse response = llmService.completion("You are helpful.", "Say 'Hello'", params);

		// Then
		assertNotNull(response);
		assertNotNull(response.getResponse());

		System.out.println("Fallback Model Response: "
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
	void testCompletionResponseMetadata()
	            throws LLMException {
		// Given
		MapParam params = new MapParam();
		params.put("model", "gpt-4o-mini");
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
}
