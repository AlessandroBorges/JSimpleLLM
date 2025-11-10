package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import bor.tools.simplellm.Model;
import bor.tools.simplellm.Model_Type;

/**
 * Test class for the enhanced model feature detection system.
 * 
 * @author AlessandroBorges
 */
public class ModelFeatureDetectorTest {

    @Test
    public void testEmbeddingModelDetection() {
        // Test various embedding model names
        String[] embeddingModels = {
            "text-embedding-ada-002", 
            "text-embedding-3-small", 
            "text-embedding-3-large",
            "bge-large-en", 
            "nomic-embed-text", 
            "e5-large", "gte-base", 
            "sentence-transformers"
        };
        
        for (String modelName : embeddingModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.EMBEDDING), 
                "Model " + modelName + " should be detected as EMBEDDING");
        }
    }

    @Test
    public void testVisionModelDetection() {
        String[] visionModels = {
            "gpt-4-vision-preview", "gpt-4o", "claude-3-opus", "llava-1.5-7b",
            "bakllava-1", "moondream2", "cogvlm-chat", "qwen-vl-chat", "pixtral-12b"
        };
        
        for (String modelName : visionModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.VISION), 
                "Model " + modelName + " should be detected as VISION");
        }
    }

    @Test
    public void testCodingModelDetection() {
        String[] codingModels = {
            "code-davinci-002", "codellama-7b-instruct", "deepseek-coder-6.7b",
            "starcoder-15b", "wizardcoder-15b", "codeqwen-7b", "magicoder-s-ds-6.7b"
        };
        
        for (String modelName : codingModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.CODING), 
                "Model " + modelName + " should be detected as CODING");
        }
    }

    @Test
    public void testReasoningModelDetection() {
        String[] reasoningModels = {
            "o1-preview", "o1-mini", "o3-mini", "gpt-4o-reasoning",
            "qwen-72b-chat", "llama-2-70b-chat", "gemma-7b-it"
        };
        
        for (String modelName : reasoningModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.REASONING), 
                "Model " + modelName + " should be detected as REASONING");
        }
    }

    @Test
    public void testFastModelDetection() {
        String[] fastModels = {
            "gpt-3.5-turbo", "claude-3-haiku", "gemini-1.5-flash",
            "llama-7b-mini", "qwen-1.5-nano", "phi-3-mini"
        };
        
        for (String modelName : fastModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.FAST), 
                "Model " + modelName + " should be detected as FAST");
        }
    }

    @Test
    public void testImageGenerationModelDetection() {
        String[] imageModels = {
            "dall-e-3", "dalle-2", "midjourney-v6", "stable-diffusion-xl",
            "flux-1-pro", "ideogram-v1"
        };
        
        for (String modelName : imageModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.IMAGE), 
                "Model " + modelName + " should be detected as IMAGE");
        }
    }

    @Test
    public void testAudioModelDetection() {
        String[] audioModels = {
            "whisper-1", "whisper-large-v3", "tts-1", "tts-1-hd",
            "bark-v2", "tortoise-tts"
        };
        
        for (String modelName : audioModels) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, null);
            assertTrue(capabilities.contains(Model_Type.AUDIO), 
                "Model " + modelName + " should be detected as AUDIO");
        }
    }

    @Test
    public void testMultiCapabilityDetection() {
        // Test models that should have multiple capabilities
        Map<String, Model_Type[]> multiCapabilityModels = new HashMap<>();
        multiCapabilityModels.put("gpt-4o", new Model_Type[]{Model_Type.LANGUAGE, Model_Type.VISION, Model_Type.TOOLS});
        multiCapabilityModels.put("claude-3-opus", new Model_Type[]{Model_Type.LANGUAGE, Model_Type.REASONING, Model_Type.TOOLS});
        multiCapabilityModels.put("deepseek-coder-instruct", new Model_Type[]{Model_Type.CODING, Model_Type.LANGUAGE, Model_Type.REASONING});
        
        for (Map.Entry<String, Model_Type[]> entry : multiCapabilityModels.entrySet()) {
            List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(entry.getKey(), null);
            
            for (Model_Type expectedType : entry.getValue()) {
                assertTrue(capabilities.contains(expectedType), 
                    "Model " + entry.getKey() + " should have capability " + expectedType);
            }
        }
    }

    @Test
    public void testMetadataBasedDetection() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("context_length", 128000);
        metadata.put("description", "A vision model with coding capabilities");
        
        List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities("test-model", metadata);
        
        assertTrue(capabilities.contains(Model_Type.BATCH), 
            "Model with large context should have BATCH capability");
        assertTrue(capabilities.contains(Model_Type.VISION), 
            "Model with vision in description should have VISION capability");
        assertTrue(capabilities.contains(Model_Type.CODING), 
            "Model with coding in description should have CODING capability");
    }

    @Test
    public void testIsModelTypeMethod() {
        Model codingModel = new Model("deepseek-coder-7b", 4096, Model_Type.CODING, Model_Type.LANGUAGE);
        
        assertTrue(ModelFeatureDetector.isModelType(codingModel, Model_Type.CODING));
        assertTrue(ModelFeatureDetector.isModelType(codingModel, Model_Type.LANGUAGE));
        assertFalse(ModelFeatureDetector.isModelType(codingModel, Model_Type.VISION));
        
        // Test with model that doesn't have predefined types - should use name detection
        Model unknownModel = new Model("starcoder-15b", null);
        assertTrue(ModelFeatureDetector.isModelType(unknownModel, Model_Type.CODING));
    }

    @Test
    public void testCapabilityManager() {
        // Test model creation with automatic capability detection
        Model autoModel = ModelCapabilityManager.createModelWithCapabilities(
            "gpt-4o-vision", 128000, null);
        
        assertTrue(autoModel.isType(Model_Type.VISION));
        assertTrue(autoModel.isType(Model_Type.LANGUAGE));
        
        // Test finding compatible models
        Map<String, Model> models = new HashMap<>();
        models.put("gpt-4o", new Model("gpt-4o", 128000, Model_Type.LANGUAGE, Model_Type.VISION, Model_Type.TOOLS));
        models.put("claude-3-opus", new Model("claude-3-opus", 200000, Model_Type.LANGUAGE, Model_Type.REASONING));
        models.put("deepseek-coder", new Model("deepseek-coder", 16000, Model_Type.CODING, Model_Type.LANGUAGE));
        
        List<Model> visionModels = ModelCapabilityManager.findCompatibleModels(models, Model_Type.VISION);
        assertEquals(1, visionModels.size());
        assertEquals("gpt-4o", visionModels.get(0).getName());
        
        // Test finding best model for capability
        Model bestCodingModel = ModelCapabilityManager.getBestModelForCapability(
            models, Model_Type.CODING, Model_Type.REASONING);
        assertEquals("deepseek-coder", bestCodingModel.getName());
    }

    @Test
    public void testCapabilityReport() {
        Model testModel = new Model("gpt-4o-preview", 128000, Model_Type.LANGUAGE);
        ModelCapabilityManager.CapabilityReport report = 
            ModelCapabilityManager.generateCapabilityReport(testModel);
        
        assertNotNull(report);
        assertEquals(testModel, report.getModel());
        assertFalse(report.getInferredCapabilities().isEmpty());
        
        // The report should suggest adding vision capability for gpt-4o
        List<Model_Type> inferred = report.getInferredCapabilities();
        assertTrue(inferred.contains(Model_Type.VISION) || inferred.contains(Model_Type.TOOLS),
            "Should infer additional capabilities for gpt-4o model");
    }

    @Test
    public void testEdgeCases() {
        // Test null and empty inputs
        assertTrue(ModelFeatureDetector.detectCapabilities(null, null).isEmpty());
        assertTrue(ModelFeatureDetector.detectCapabilities("", null).isEmpty());
        assertTrue(ModelFeatureDetector.detectCapabilities("   ", null).isEmpty());
        
        assertFalse(ModelFeatureDetector.isModelType(null, Model_Type.LANGUAGE));
        assertFalse(ModelFeatureDetector.isModelType(new Model("test", null), null));
        
        // Test unknown model names
        List<Model_Type> unknownCapabilities = ModelFeatureDetector.detectCapabilities("random-unknown-model-xyz", null);
        assertTrue(unknownCapabilities.contains(Model_Type.LANGUAGE), 
            "Unknown models should default to LANGUAGE capability");
    }

    @Test
    public void testSpecialModelPatterns() {
        // Test regex pattern matching
        List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities("custom-embed-v2", null);
        assertTrue(capabilities.contains(Model_Type.EMBEDDING));
        
        capabilities = ModelFeatureDetector.detectCapabilities("llava-next-34b", null);
        assertTrue(capabilities.contains(Model_Type.VISION));
        
        capabilities = ModelFeatureDetector.detectCapabilities("wizardcoder-python-34b", null);
        assertTrue(capabilities.contains(Model_Type.CODING));
    }
}