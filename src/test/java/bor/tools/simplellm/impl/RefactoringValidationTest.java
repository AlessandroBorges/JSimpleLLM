package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import bor.tools.simplellm.LLMConfig;

/**
 * Test to validate the refactoring changes to the LLMService hierarchy.
 * This ensures that the helper method in OpenAILLMService works correctly
 * and that all subclasses inherit the behavior properly.
 * 
 * @author AlessandroBorges
 */
public class RefactoringValidationTest {

    @Test
    public void testOpenAIDefaultCompletionModelName() {
        OpenAILLMService service = new OpenAILLMService();
        String modelName = service.getDefaultCompletionModelName();
        
        assertNotNull(modelName, "Default completion model name should not be null");
        assertFalse(modelName.isEmpty(), "Default completion model name should not be empty");
        System.out.println("OpenAI default completion model: " + modelName);
    }

    @Test
    public void testOpenAIDefaultEmbeddingModelName() {
        OpenAILLMService service = new OpenAILLMService();
        String modelName = service.getDefaultEmbeddingModelName();
        
        assertNotNull(modelName, "Default embedding model name should not be null");
        assertFalse(modelName.isEmpty(), "Default embedding model name should not be empty");
        System.out.println("OpenAI default embedding model: " + modelName);
    }

    @Test
    public void testLMStudioDefaultCompletionModelName() {
        LMStudioLLMService service = new LMStudioLLMService();
        String modelName = service.getDefaultCompletionModelName();
        
        assertNotNull(modelName, "Default completion model name should not be null");
        assertFalse(modelName.isEmpty(), "Default completion model name should not be empty");
        System.out.println("LMStudio default completion model: " + modelName);
    }

    @Test
    public void testLMStudioSupportsInstalledModelsQuery() {
        LMStudioLLMService service = new LMStudioLLMService();
        
        assertTrue(service.supportsInstalledModelsQuery(), 
            "LMStudio should support querying installed models");
    }

    @Test
    public void testOllamaDefaultCompletionModelName() {
        OllamaLLMService service = new OllamaLLMService();
        String modelName = service.getDefaultCompletionModelName();
        
        assertNotNull(modelName, "Default completion model name should not be null");
        assertFalse(modelName.isEmpty(), "Default completion model name should not be empty");
        System.out.println("Ollama default completion model: " + modelName);
    }

    @Test
    public void testOllamaDefaultEmbeddingModelName() {
        OllamaLLMService service = new OllamaLLMService();
        String modelName = service.getDefaultEmbeddingModelName();
        
        assertNotNull(modelName, "Default embedding model name should not be null");
        assertFalse(modelName.isEmpty(), "Default embedding model name should not be empty");
        System.out.println("Ollama default embedding model: " + modelName);
    }

    @Test
    public void testOllamaSupportsInstalledModelsQuery() {
        OllamaLLMService service = new OllamaLLMService();
        
        assertTrue(service.supportsInstalledModelsQuery(), 
            "Ollama should support querying installed models");
    }

    @Test
    public void testOpenAIDoesNotSupportInstalledModelsQuery() {
        OpenAILLMService service = new OpenAILLMService();
        
        assertFalse(service.supportsInstalledModelsQuery(), 
            "OpenAI should not support querying installed models");
    }

    @Test
    public void testToStringMethods() {
        OpenAILLMService openaiService = new OpenAILLMService();
        LMStudioLLMService lmStudioService = new LMStudioLLMService();
        OllamaLLMService ollamaService = new OllamaLLMService();
        
        String openaiStr = openaiService.toString();
        String lmStudioStr = lmStudioService.toString();
        String ollamaStr = ollamaService.toString();
        
        assertNotNull(openaiStr, "OpenAI toString should not be null");
        assertNotNull(lmStudioStr, "LMStudio toString should not be null");
        assertNotNull(ollamaStr, "Ollama toString should not be null");
        
        assertTrue(openaiStr.contains("OpenAI"), "OpenAI toString should contain 'OpenAI'");
        assertTrue(lmStudioStr.contains("LMStudio"), "LMStudio toString should contain 'LMStudio'");
        assertTrue(ollamaStr.contains("Ollama"), "Ollama toString should contain 'Ollama'");
        
        System.out.println("\n=== Service toString() outputs ===");
        System.out.println(openaiStr);
        System.out.println("\n" + lmStudioStr);
        System.out.println("\n" + ollamaStr);
    }

    @Test
    public void testCustomConfigWithFallback() {
        // Create a config without default model names to test fallback logic
        LLMConfig config = LLMConfig.builder()
            .apiToken("test-token")
            .baseUrl("http://localhost:1234/v1/")
            .build();
        
        LMStudioLLMService service = new LMStudioLLMService(config);
        String modelName = service.getDefaultCompletionModelName();
        
        assertNotNull(modelName, "Should fallback to default model when config doesn't specify");
        System.out.println("Custom config fallback model: " + modelName);
    }
}
