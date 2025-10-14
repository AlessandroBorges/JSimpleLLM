package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.exceptions.LLMAuthenticationException;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for OpenAI service configuration and model management.
 */
class OpenAIConfigTest extends OpenAILLMServiceTestBase {

	@Test
	@DisplayName("Test default configuration")
	void testDefaultConfiguration() {
		// Given
		LLMConfig defaultConfig = OpenAILLMService.getDefaultLLMConfig();

		// Then
		assertNotNull(defaultConfig, "Default config should not be null");
		assertNotNull(defaultConfig.getBaseUrl(), "Base URL should be set");
		assertTrue(defaultConfig.getBaseUrl().contains("openai.com"), "Should point to OpenAI endpoint");
		assertEquals("OPENAI_API_TOKEN", defaultConfig.getApiTokenEnvironment(), "Should use OPENAI_API_TOKEN env var");
		assertNotNull(defaultConfig.getModelMap(), "Model map should not be null");
		assertFalse(defaultConfig.getModelMap().isEmpty(), "Should have predefined models");

		System.out.println("Base URL: "
		            + defaultConfig.getBaseUrl());
		System.out.println("Available models: "
		            + defaultConfig.getModelMap().keySet());
	}

	@Test
	@DisplayName("Test service creation with custom config")
	void testCustomConfiguration() {
		// Given
		LLMConfig customConfig = LLMConfig.builder()
		            .baseUrl("https://api.openai.com/v1/")
		            .apiToken("test-token")
		            .modelMap(OpenAILLMService.getDefaultLLMConfig().getModelMap())
		            .build();

		// When
		LLMService service = new OpenAILLMService(customConfig);

		// Then
		assertNotNull(service, "Service should be created");
		assertEquals(customConfig, service.getLLMConfig(), "Config should match");
	}

	@Test
	@DisplayName("Test model listing")
	void testModelListing()
	            throws LLMException {
		// When
		List<Model> models = llmService.getRegisteredModels();

		// Then
		assertNotNull(models, "Models list should not be null");
		assertFalse(models.isEmpty(), "Should have at least one model");

		// Check for expected models
		boolean hasGpt4Mini  = models.stream().anyMatch(model -> model.getName().contains("gpt-4o-mini"));
		boolean hasEmbedding = models.stream().anyMatch(model -> model.getName().contains("embedding"));

		assertTrue(hasGpt4Mini, "Should include GPT-4o-mini model");
		assertTrue(hasEmbedding, "Should include embedding model");

		System.out.println("Available models:");
		for (Model model : models) {
			System.out.println("- "
			            + model.getName()
			            + " (context: "
			            + model.getContextLength()
			            + ")");
		}
	}

	@Test
	@DisplayName("Test model capabilities")
	void testModelCapabilities()
	            throws LLMException {
		// Given
		List<Model> models = llmService.getRegisteredModels();

		// When
		Model gpt4Mini = models.stream().filter(model -> model.getName().equals("gpt-4o-mini")).findFirst().orElse(null);

		// Then
		assertNotNull(gpt4Mini, "GPT-4o-mini model should be available");
		assertTrue(gpt4Mini.getContextLength() > 0, "Context size should be positive");
		assertNotNull(gpt4Mini.getTypes(), "Model types should be defined");
		assertTrue(gpt4Mini.getTypes().size() > 0, "Should have at least one capability");

		System.out.println("GPT-4o-mini capabilities: "
		            + String.join(", ", gpt4Mini.getTypes().stream().map(Object::toString).toArray(String[]::new)));
	}

	@Test
	@DisplayName("Test authentication with invalid token")
	void testInvalidAuthentication() {
		// Given
		LLMService invalidService = createServiceWithCustomConfig("https://api.openai.com/v1/", "invalid-token");

		// When & Then
		assertThrows(LLMAuthenticationException.class,
		             () -> { invalidService.completion("Test", "Hello", null); },
		             "Should throw authentication exception for invalid token");
	}

	@Test
	@DisplayName("Test service with missing token")
	void testMissingToken() {
		// Given - create service with empty token
		LLMService serviceWithoutToken = createServiceWithCustomConfig("https://api.openai.com/v1/", "");

		// When & Then
		assertThrows(LLMAuthenticationException.class,
		             () -> { serviceWithoutToken.completion("Test", "Hello", null); },
		             "Should throw authentication exception for missing token");
	}

	@Test
	@DisplayName("Test service with custom base URL")
	void testCustomBaseUrl() {
		// Given
		String     customUrl     = "https://custom-openai-endpoint.com/v1/";
		LLMService customService = createServiceWithCustomConfig(customUrl, "test-token");

		// Then
		assertEquals(customUrl, customService.getLLMConfig().getBaseUrl(), "Should use custom base URL");
	}

	@Test
	@DisplayName("Test model context sizes")
	void testModelContextSizes()
	            throws LLMException {
		// Given
		List<Model> models = llmService.getRegisteredModels();

		// Then
		for (Model model : models) {
			assertTrue(model.getContextLength() > 0,
			           "Model "
			                       + model.getName()
			                       + " should have positive context size");

			if (model.getName().contains("gpt-5")) {
				assertTrue(model.getContextLength() >= 400000, "GPT-5 models should have large context size");
			}
		}

		System.out.println("Model context sizes verified");
	}

	@Test
	@DisplayName("Test embedding model identification")
	void testEmbeddingModelIdentification()
	            throws LLMException {
		// Given
		List<Model> models = llmService.getRegisteredModels();

		// When
		Model embeddingModel =
		            models.stream().filter(model -> model.getName().contains("embedding")).findFirst().orElse(null);

		// Then
		assertNotNull(embeddingModel, "Should have at least one embedding model");

		// Check that it has EMBEDDING capability
		boolean hasEmbeddingCapability =
		            embeddingModel.getTypes().stream().anyMatch(type -> type.toString().contains("embedding"));

		assertTrue(hasEmbeddingCapability, "Embedding model should have EMBEDDING capability");

		System.out.println("Embedding model: "
		            + embeddingModel.getName()
		            + " with context size: "
		            + embeddingModel.getContextLength());
	}
}
