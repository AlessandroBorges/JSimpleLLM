package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ContentType;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for LM Studio completion functionality.
 */
class LMStudioCompletionTest extends LMStudioLLMServiceTestBase {

    @Test
    @DisplayName("Test basic text completion with LM Studio")
    void testBasicCompletion() throws LLMException {
        // Given
        String system = "You are a helpful assistant that provides concise answers.";
        String query = "What is 2 + 2?";
        
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
        
        System.out.println("LM Studio Completion Response: " + responseText);
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
        
        System.out.println("LM Studio Joke Response: " + responseText);
    }

    @Test
    @DisplayName("Test factory method for creating LM Studio service")
    void testFactoryMethod() throws LLMException {
        // Given
        var lmStudioService = LLMServiceFactory.createLMStudio();
        
        MapParam params = new MapParam();
        params.put("model", getFirstAvailableModel());
        params.put("max_tokens", 30);
        
        // When
        CompletionResponse response = lmStudioService.completion(
            "You are helpful.", 
            "Count from 1 to 3.", 
            params
        );
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getResponse());
        
        System.out.println("Factory Method Response: " + response.getResponse().getText());
    }

    @Test
    @DisplayName("Test completion with different available models")
    void testCompletionWithDifferentModels() throws LLMException {
        // Test with the first available model
        MapParam params = new MapParam();
        params.put("model", getFirstAvailableModel());
        params.put("max_tokens", 30);
        params.put("temperature", 0.1);
        
        CompletionResponse response = llmService.completion(
            "You are a math tutor.", 
            "What is 3 × 4?", 
            params
        );
        
        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse().getText().contains("12"));
        
        System.out.println("Math Response with " + getFirstAvailableModel() + ": " + 
                          response.getResponse().getText());
    }

    @Test
    @DisplayName("Test completion error handling with empty query")
    void testCompletionWithEmptyQuery() {
        // Given
        String query = "";
        MapParam params = new MapParam();
        
        // When & Then
        assertThrows(LLMException.class, () -> {
            llmService.completion("System", query, params);
        }, "Should throw exception for empty query");
    }

    @Test
    @DisplayName("Test completion error handling with null query")
    void testCompletionWithNullQuery() {
        // Given
        String query = null;
        MapParam params = new MapParam();
        
        // When & Then
        assertThrows(LLMException.class, () -> {
            llmService.completion("System", query, params);
        }, "Should throw exception for null query");
    }

    @Test
    @DisplayName("Test completion response metadata")
    void testCompletionResponseMetadata() throws LLMException {
        // Given
        MapParam params = new MapParam();
        params.put("model", getFirstAvailableModel());
        params.put("max_tokens", 20);
        
        // When
        CompletionResponse response = llmService.completion(
            "Be brief.", 
            "Count to 3.", 
            params
        );
        
        // Then
        assertNotNull(response.getInfo(), "Response metadata should not be null");
        assertNotNull(response.getEndReason(), "End reason should be set");
        
        System.out.println("End reason: " + response.getEndReason());
        System.out.println("Response metadata keys: " + response.getInfo().keySet());
    }

    @Test
    @DisplayName("Test LM Studio-specific configuration")
    void testLMStudioSpecificConfig() {
        // Given
        var lmStudioConfig = LMStudioLLMService.getDefaultLLMConfig();
        
        // Then
        assertNotNull(lmStudioConfig);
        assertTrue(lmStudioConfig.getBaseUrl().contains("localhost:1234"), 
                  "Should use local LM Studio server");
        assertEquals("LMSTUDIO_API_KEY", lmStudioConfig.getApiTokenEnvironment(),
                    "Should use LMSTUDIO_API_KEY env var");
        
        // Should have some models configured
        assertFalse(lmStudioConfig.getModelMap().isEmpty(), 
                   "Should have default models configured");
        
        System.out.println("LM Studio Base URL: " + lmStudioConfig.getBaseUrl());
        System.out.println("Available LM Studio models: " + lmStudioConfig.getModelMap().keySet());
    }

    @Test
    @DisplayName("Test LM Studio coding model detection")
    void testCodingModelDetection() {
        // Given
        LMStudioLLMService service = new LMStudioLLMService();
        
        // Test various model name patterns
        assertTrue(service.isModelType("codellama-7b-instruct", bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING),
                  "Should detect CodeLlama as coding model");
        assertTrue(service.isModelType("deepseek-coder", bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING),
                  "Should detect DeepSeek Coder as coding model");
        assertFalse(service.isModelType("llama-3.1-8b-instruct", bor.tools.simplellm.LLMConfig.MODEL_TYPE.CODING),
                   "Should not detect regular Llama as coding-specific");
        
        System.out.println("Coding model detection works correctly");
    }

    @Test
    @DisplayName("Test LM Studio vision model detection")
    void testVisionModelDetection() {
        // Given
        LMStudioLLMService service = new LMStudioLLMService();
        
        // Test vision model detection
        assertTrue(service.isModelType("llava-1.5-7b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION),
                  "Should detect LLaVA as vision model");
        assertTrue(service.isModelType("bakllava-1-7b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION),
                  "Should detect BakLLaVA as vision model");
        assertFalse(service.isModelType("mistral-7b", bor.tools.simplellm.LLMConfig.MODEL_TYPE.VISION),
                   "Should not detect text model as vision");
        
        System.out.println("Vision model detection works correctly");
    }
}