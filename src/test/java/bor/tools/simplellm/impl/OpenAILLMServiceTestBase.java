package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;

/**
 * Base class for OpenAI LLM Service tests.
 * <p>
 * This class provides common setup and configuration for testing the OpenAI
 * implementation.
 * Tests will be skipped if the OPENAI_API_TOKEN environment variable is not
 * set.
 * </p>
 */
public abstract class OpenAILLMServiceTestBase {

	protected LLMService llmService;
	protected LLMConfig  config;

	@BeforeEach
	void setUp() {
		// Skip tests if API token is not available
		String apiToken = System.getenv("OPENAI_API_KEY");
		assumeTrue(apiToken != null && !apiToken.trim().isEmpty(),
		           "OPENAI_API_KEY environment variable not set - skipping OpenAI integration tests");

		// Create service with default configuration
		config = OpenAILLMService.getDefaultLLMConfig();
		llmService = new OpenAILLMService(config);
	}

	/**
	 * Helper method to create a service with custom configuration for testing.
	 */
	protected LLMService createServiceWithCustomConfig(String baseUrl, String apiToken) {
		LLMConfig customConfig = LLMConfig.builder()
		            .baseUrl(baseUrl)
		            .apiToken(apiToken)
		            .registeredModelMap(OpenAILLMService.getDefaultLLMConfig().getRegisteredModelMap())
		            .build();

		return new OpenAILLMService(customConfig);
	}

	/**
	 * Check if we're running integration tests (API token available).
	 */
	protected boolean isIntegrationTestEnabled() {
		String apiToken = System.getenv("OPENAI_API_TOKEN");
		return apiToken != null && !apiToken.trim().isEmpty();
	}
}
