package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.ModelEmbedding.Embeddings_Op;
import bor.tools.simplellm.chat.ContentType;
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
		assertTrue(ollamaConfig.getBaseUrl().contains("localhost:1143"), "Should use local Ollama server");
		assertEquals("OLLAMA_API_KEY", ollamaConfig.getApiTokenEnvironment(), "Should use OLLAMA_API_KEY env var");

		// Should have some models configured
		assertFalse(ollamaConfig.getRegisteredModelMap().isEmpty(), "Should have default models configured");

		System.out.println("Ollama Base URL: "
		            + ollamaConfig.getBaseUrl());
		System.out.println("Available Ollama models: "
		            + ollamaConfig.getRegisteredModelMap().keySet());
	}

	@Test
	@DisplayName("Test Ollama coding model detection")
	void testCodingModelDetection() {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		
		// Test various model name patterns
		assertTrue(service.isModelType("codellama:7b", bor.tools.simplellm.Model_Type.CODING),
				  "Should detect CodeLlama as coding model");
		assertTrue(service.isModelType("starcoder:3b", bor.tools.simplellm.Model_Type.CODING),
				  "Should detect StarCoder as coding model");
		assertTrue(service.isModelType("codestral:22b", bor.tools.simplellm.Model_Type.CODING),
				  "Should detect Codestral as coding model");
		
		System.out.println("Coding model detection works correctly");
	}

	@Test
	@DisplayName("Test Ollama vision model detection")
	void testVisionModelDetection() {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		
		// Test vision model detection
		assertTrue(service.isModelType("llava:7b", bor.tools.simplellm.Model_Type.VISION),
				  "Should detect LLaVA as vision model");
		assertTrue(service.isModelType("llava-llama3:8b", bor.tools.simplellm.Model_Type.VISION),
				  "Should detect LLaVA variant as vision model");
		assertFalse(service.isModelType("phi4-mini", bor.tools.simplellm.Model_Type.VISION),
				   "Should not detect text model as vision");
		
		System.out.println("Vision model detection works correctly");
	}

	@Test
	@DisplayName("Test Ollama embedding model detection")
	void testEmbeddingModelDetection() {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		
		// Test embedding model detection
		assertTrue(service.isModelType("nomic-embed-text", bor.tools.simplellm.Model_Type.EMBEDDING),
				  "Should detect nomic-embed-text as embedding model");
		assertTrue(service.isModelType("bge-large", bor.tools.simplellm.Model_Type.EMBEDDING),
				  "Should detect BGE as embedding model");
		assertTrue(service.isModelType("snowflake-arctic-embed", bor.tools.simplellm.Model_Type.EMBEDDING),
				  "Should detect snowflake embedding model");
		
		System.out.println("Embedding model detection works correctly");
	}
	
	@Test
	@DisplayName("Test identify all embedding models")
	void testIdentifyAllEmbeddingModels() throws LLMException {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		List<String> embeddingModels = new ArrayList<>();

		// When
		for (String modelName : service.getRegisteredModelNames()) {
			if (service.isModelType(modelName, Model_Type.EMBEDDING)) {
				embeddingModels.add(modelName);
			}
		}

		// Then
		assertFalse(embeddingModels.isEmpty(), "Should find at least one embedding model.");
		System.out.println("Found embedding models: " + embeddingModels);
	}

	@Test
	@DisplayName("Test create embeddings with all available embedding models")
	void testCreateEmbeddingsFromSentence() throws LLMException {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		String textToEmbed = "O Brasil foi campeão da Copa do Mundo de 2002.";
		List<String> embeddingModels = service.getRegisteredModelNames().stream()
				.filter(model -> service.isModelType(model, Model_Type.EMBEDDING)).collect(Collectors.toList());

		assertTrue(embeddingModels.size() > 0, "No embedding models found to test.");

		// When & Then
		for (String modelName : embeddingModels) {
			try {
				MapParam params = new MapParam();
				params.put("model", modelName);

				float[] response = service.embeddings(Embeddings_Op.DOCUMENT, textToEmbed, params);
				assertNotNull(response,
				              "Embedding response should not be null for model: "
				                          + modelName);
				assertTrue(response.length > 5, "Embedding vector should have more than 5 elements.");

				float[] firstFive = Arrays.copyOfRange(response, 0, 5);
				System.out.println("First 5 embedding values for model '"
				            + modelName
				            + "': "
				            + Arrays.toString(firstFive));
			} catch (LLMException e) {
				System.out.println("Model '"
				            + modelName
				            + "' does not support embeddings or failed: "
				            + e.getMessage());
			}
		}
	}

	@Test
	@DisplayName("Test identify models with embedding dimension capability")
	void testIdentifyModelsWithDimensionCapability() throws LLMException {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		List<String> embeddingModels = service.getRegisteredModelNames().stream()
				.filter(model -> service.isModelType(model, Model_Type.EMBEDDING)).collect(Collectors.toList());

		// When & Then
		System.out.println("Checking models for embedding dimension capability:");
		for (String modelName : embeddingModels) {
			// Models like nomic-embed-text support the 'dimensions' parameter.
			// We check by attempting a call with a valid dimension.
			MapParam params = new MapParam();
			params.put("model", modelName);
			params.put("dimensions", 128); // A common dimension to test with

			try {
				float[] response = service.embeddings(Embeddings_Op.DOCUMENT,
				                                      "Test sentence for dimension check.",
				                                      params);
				
				if (response != null && response != null) {
					System.out.println("- Model '" + modelName + "' supports custom dimensions. Default dimension: "
							+ response.length);
				}
			} catch (LLMException e) {
				// Assuming an exception means the model does not support the parameter.
				System.out.println("- Model '" + modelName + "' does not appear to support custom dimensions.");
			}
		}
	}
	
	@Test
	@DisplayName("Test create embeddings with reduced dimensions")
	void testCreateEmbeddingsWithReducedDimensions() throws LLMException {
		// Given
		OllamaLLMService service = new OllamaLLMService();
		String textToEmbed = "This is a test sentence for reduced embeddings.";
		int vecSize = 64;
		List<String> embeddingModels = service.getRegisteredModelNames().stream()
				.filter(model -> service.isModelType(model, Model_Type.EMBEDDING_DIMENSION)).collect(Collectors.toList());

		System.out.println("Creating reduced dimension embeddings (size=" + vecSize + "):");

		// When & Then
		for (String modelName : embeddingModels) {
			MapParam params = new MapParam();
			params.put("model", modelName);
			params.put("dimensions", vecSize);

			try {
				float[] response = service.embeddings(Embeddings_Op.DOCUMENT, textToEmbed, params);
				assertNotNull(response, "Response should not be null");
				assertNotNull(response.length, "Vector should not be null");
				assertEquals(vecSize, response.length,
						"Vector dimension should be reduced to " + vecSize);

				float[] firstFive = Arrays.copyOfRange(response, 0, 5);
				System.out.println(
						"- Model '" + modelName + "': First 5 values of reduced vector: " + Arrays.toString(firstFive));

			} catch (LLMException e) {
				System.out.println("- Model '" + modelName + "' does not support custom dimensions, skipping.");
			}
		}
	}


	
}
