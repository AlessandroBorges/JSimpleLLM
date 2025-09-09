package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;

/**
 * Base class for Ollama LLM Service tests.
 * <p>
 * This class provides common setup and configuration for testing the Ollama
 * implementation.
 * Tests will be skipped if Ollama server is not available at localhost:11434.
 * </p>
 */
public abstract class OllamaLLMServiceTestBase {

	protected LLMService llmService;
	protected LLMConfig  config;

	@BeforeEach
	void setUp() {
		// Skip tests if Ollama server is not available
		assumeTrue(isOllamaServerAvailable(),
		           "Ollama server not available at localhost:11434 - skipping Ollama integration tests");

		// Create service with default configuration
		config = OllamaLLMService.getDefaultLLMConfig();
		llmService = new OllamaLLMService(config);
	}

	/**
	 * Helper method to create a service with custom configuration for testing.
	 */
	protected LLMService createServiceWithCustomConfig(String baseUrl, String apiKey) {
		LLMConfig customConfig = LLMConfig.builder()
		            .baseUrl(baseUrl)
		            .apiToken(apiKey)
		            .modelMap(OllamaLLMService.getDefaultLLMConfig().getModelMap())
		            .build();

		return new OllamaLLMService(customConfig);
	}

	/**
	 * Check if Ollama server is running and accessible.
	 */
	protected boolean isOllamaServerAvailable() {
		try {
			// Try to connect to Ollama server
			java.net.Socket socket = new java.net.Socket();
			socket.connect(new java.net.InetSocketAddress("localhost", 11434), 5000);
			socket.close();
			return true;
		} catch (Exception e) {
			System.out.println("Ollama server not available: "
			            + e.getMessage());
			return false;
		}
	}

	/**
	 * Get the first available model from the configuration.
	 */
	protected String getFirstAvailableModel() { return config.getModelMap().keySet().iterator().next(); }

	/**
	 * Check if integration tests should run.
	 */
	protected boolean isIntegrationTestEnabled() { return isOllamaServerAvailable(); }
}
