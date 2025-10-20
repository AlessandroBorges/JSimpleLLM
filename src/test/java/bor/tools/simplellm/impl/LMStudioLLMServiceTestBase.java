package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;

/**
 * Base class for LM Studio LLM Service tests.
 * <p>
 * This class provides common setup and configuration for testing the LM Studio
 * implementation.
 * Tests will be skipped if LM Studio server is not available at localhost:1234.
 * </p>
 */
public abstract class LMStudioLLMServiceTestBase {

    protected LLMService llmService;
    protected LLMConfig config;
    
    @BeforeEach
    void setUp() {
        // Skip tests if LM Studio server is not available
        assumeTrue(isLMStudioServerAvailable(), 
                   "LM Studio server not available at localhost:1234 - skipping LM Studio integration tests");
        
        // Create service with default configuration
        config = LMStudioLLMService.getDefaultLLMConfig();
        llmService = new LMStudioLLMService(config);
    }
    
    /**
     * Helper method to create a service with custom configuration for testing.
     */
    protected LLMService createServiceWithCustomConfig(String baseUrl, String apiKey) {
        LLMConfig customConfig = LLMConfig.builder()
                .baseUrl(baseUrl)
                .apiToken(apiKey)
                .registeredModelMap(LMStudioLLMService.getDefaultLLMConfig().getRegisteredModelMap())
                .build();
        
        return new LMStudioLLMService(customConfig);
    }
    
    /**
     * Check if LM Studio server is running and accessible.
     */
    protected boolean isLMStudioServerAvailable() {
        try {
            // Try to connect to LM Studio server
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress("localhost", 1234), 5000);
            socket.close();
            return true;
        } catch (Exception e) {
            System.out.println("LM Studio server not available: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the first available model from the configuration.
     */
    protected String getFirstAvailableModel() {
        return config.getRegisteredModelMap().keySet().iterator().next();
    }
    
    /**
     * Check if integration tests should run.
     */
    protected boolean isIntegrationTestEnabled() {
        return isLMStudioServerAvailable();
    }
}