package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.ContentWrapper;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for OpenAI vision and image generation functionality.
 */
class OpenAIVisionAndImageTest extends OpenAILLMServiceTestBase {

    private OpenAIJsonMapper jsonMapper;

    @BeforeEach
    void setUpVisionTests() {
        jsonMapper = new OpenAIJsonMapper();
    }

    // ================== VISION TESTS (Multimodal Input) ==================

    @Test
    @DisplayName("Test ImageContent URL conversion to multimodal format")
    void testImageContentUrlConversion() throws LLMException {
        // Given - Create a message with image URL
        String imageUrl = "https://example.com/test-image.jpg";
        ContentWrapper.ImageContent imageContent = new ContentWrapper.ImageContent(imageUrl);
        
        Message message = new Message(MessageRole.USER, imageContent);
        
        Chat chat = new Chat("vision-test");
        chat.addMessage(message);
        
        MapParam params = new MapParam();
        params.put("model", "gpt-4-vision-preview");
        
        // When - Convert to chat completion request
        Map<String, Object> request = jsonMapper.toChatCompletionRequest(chat, null, params);
        
        // Then - Verify multimodal structure
        assertNotNull(request);
        assertEquals("gpt-4-vision-preview", request.get("model"));
        
        @SuppressWarnings("unchecked")
        var messages = (java.util.List<Map<String, Object>>) request.get("messages");
        assertNotNull(messages);
        assertEquals(1, messages.size());
        
        Map<String, Object> userMessage = messages.get(0);
        assertEquals("user", userMessage.get("role"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) userMessage.get("content");
        assertEquals("image_url", content.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> imageUrlObj = (Map<String, Object>) content.get("image_url");
        assertEquals(imageUrl, imageUrlObj.get("url"));
        
        System.out.println("Vision URL request: " + jsonMapper.toJson(request));
    }

    @Test
    @DisplayName("Test ImageContent base64 conversion to multimodal format")
    void testImageContentBase64Conversion() throws LLMException {
        // Given - Create a message with image data
        byte[] imageData = "fake-image-data".getBytes(); // In reality, this would be PNG/JPEG bytes
        ContentWrapper.ImageContent imageContent = new ContentWrapper.ImageContent(imageData);
        
        // Add metadata for MIME type
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("mimeType", "image/png");
        imageContent.setMetadata(metadata);
        
        Message message = new Message(MessageRole.USER, imageContent);
        
        Chat chat = new Chat("vision-test-base64");
        chat.addMessage(message);
        
        MapParam params = new MapParam();
        params.put("model", "gpt-4-vision-preview");
        
        // When - Convert to chat completion request
        Map<String, Object> request = jsonMapper.toChatCompletionRequest(chat, null, params);
        
        // Then - Verify base64 structure
        assertNotNull(request);
        
        @SuppressWarnings("unchecked")
        var messages = (java.util.List<Map<String, Object>>) request.get("messages");
        Map<String, Object> userMessage = messages.get(0);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) userMessage.get("content");
        assertEquals("image_url", content.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> imageUrlObj = (Map<String, Object>) content.get("image_url");
        String dataUrl = (String) imageUrlObj.get("url");
        
        assertTrue(dataUrl.startsWith("data:image/png;base64,"));
        
        // Verify base64 encoding
        String base64Part = dataUrl.substring("data:image/png;base64,".length());
        byte[] decodedData = Base64.getDecoder().decode(base64Part);
        assertArrayEquals(imageData, decodedData);
        
        System.out.println("Vision base64 request generated successfully");
    }

    @Test
    @DisplayName("Test multimodal message creation utility")
    void testMultimodalMessageUtility() throws LLMException {
        // Given
        String textContent = "What's in this image?";
        String imageUrl = "https://example.com/test.jpg";
        ContentWrapper.ImageContent imageContent = new ContentWrapper.ImageContent(imageUrl);
        
        // When - Use utility method
        Map<String, Object> message = jsonMapper.createMultimodalMessage("user", textContent, imageContent);
        
        // Then - Verify structure
        assertNotNull(message);
        assertEquals("user", message.get("role"));
        
        @SuppressWarnings("unchecked")
        var contentArray = (java.util.List<Map<String, Object>>) message.get("content");
        assertNotNull(contentArray);
        assertEquals(2, contentArray.size());
        
        // Check text content
        Map<String, Object> textPart = contentArray.get(0);
        assertEquals("text", textPart.get("type"));
        assertEquals(textContent, textPart.get("text"));
        
        // Check image content
        Map<String, Object> imagePart = contentArray.get(1);
        assertEquals("image_url", imagePart.get("type"));
        
        System.out.println("Multimodal utility works correctly");
    }

    @Test
    @DisplayName("Test vision model type detection")
    void testVisionModelDetection() {
        // Given
        LLMConfig config = OpenAILLMService.getDefaultLLMConfig();
        OpenAILLMService service = new OpenAILLMService(config);
        
        // When & Then
        assertTrue(service.isModelType("gpt-4-vision-preview", Model_Type.VISION),
                  "Should detect GPT-4 Vision as vision model");
        assertTrue(service.isModelType("gpt-4o", Model_Type.VISION),
                  "Should detect GPT-4o as vision model");
        assertFalse(service.isModelType("gpt-3.5-turbo", Model_Type.VISION),
                   "Should not detect GPT-3.5 as vision model");
        
        System.out.println("Vision model detection works correctly");
    }

    // ================== IMAGE GENERATION TESTS ==================

    @Test
    @DisplayName("Test image generation request formatting")
    void testImageGenerationRequestFormat() {
        // Given
        String prompt = "A beautiful sunset over mountains";
        MapParam params = new MapParam();
        params.put("model", "dall-e-3");
        params.put("size", "1024x1024");
        params.put("quality", "hd");
        params.put("n", 1);
        params.put("response_format", "url");
        
        // When
        Map<String, Object> request = jsonMapper.toImageGenerationRequest(prompt, params);
        
        // Then
        assertNotNull(request);
        assertEquals(prompt, request.get("prompt"));
        assertEquals("dall-e-3", request.get("model"));
        assertEquals("1024x1024", request.get("size"));
        assertEquals("hd", request.get("quality"));
        assertEquals(1, request.get("n"));
        assertEquals("url", request.get("response_format"));
        
        System.out.println("Image generation request: " + request);
    }

    @Test
    @DisplayName("Test image generation response parsing with URL")
    void testImageGenerationResponseParsingUrl() throws LLMException {
        // Given - Mock OpenAI image generation response with URL
        Map<String, Object> mockResponse = new HashMap<>();
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("url", "https://example.com/generated-image.png");
        imageData.put("revised_prompt", "A beautiful sunset over mountains, digital art");
        data.add(imageData);
        
        mockResponse.put("data", data);
        mockResponse.put("created", 1234567890);
        
        // When
        CompletionResponse response = jsonMapper.fromImageGenerationResponse(mockResponse);
        
        // Then
        assertNotNull(response);
        assertEquals("image_generated", response.getEndReason());
        
        ContentWrapper content = response.getResponse();
        assertNotNull(content);
        assertEquals(ContentType.IMAGE, content.getType());
        assertTrue(content instanceof ContentWrapper.ImageContent);
        
        ContentWrapper.ImageContent imageContent = (ContentWrapper.ImageContent) content;
        assertEquals("https://example.com/generated-image.png", imageContent.getUrl());
        
        // Check metadata
        assertNotNull(imageContent.getMetadata());
        assertEquals("A beautiful sunset over mountains, digital art", 
                    imageContent.getMetadata().get("revised_prompt"));
        
        System.out.println("Image generation response parsing (URL) works correctly");
    }

    @Test
    @DisplayName("Test image generation response parsing with base64")
    void testImageGenerationResponseParsingBase64() throws LLMException {
        // Given - Mock OpenAI image generation response with base64
        Map<String, Object> mockResponse = new HashMap<>();
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        
        byte[] fakeImageData = "fake-png-data".getBytes();
        String base64Data = Base64.getEncoder().encodeToString(fakeImageData);
        
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("b64_json", base64Data);
        data.add(imageData);
        
        mockResponse.put("data", data);
        
        // When
        CompletionResponse response = jsonMapper.fromImageGenerationResponse(mockResponse);
        
        // Then
        assertNotNull(response);
        assertEquals("image_generated", response.getEndReason());
        
        ContentWrapper content = response.getResponse();
        assertTrue(content instanceof ContentWrapper.ImageContent);
        
        ContentWrapper.ImageContent imageContent = (ContentWrapper.ImageContent) content;
        assertArrayEquals(fakeImageData, imageContent.getImageData());
        
        // Check MIME type metadata
        assertEquals("image/png", imageContent.getMetadata().get("mimeType"));
        
        System.out.println("Image generation response parsing (base64) works correctly");
    }

    @Test
    @DisplayName("Test DALL-E model configuration")
    void testDalleModelConfiguration() {
        // Given
        LLMConfig config = OpenAILLMService.getDefaultLLMConfig();
        OpenAILLMService service = new OpenAILLMService(config);
        
        // When & Then - Check DALL-E models are configured
        assertTrue(service.isModelType("dall-e-3", Model_Type.IMAGE),
                  "Should detect DALL-E 3 as image generation model");
        assertTrue(service.isModelType("dall-e-2", Model_Type.IMAGE),
                  "Should detect DALL-E 2 as image generation model");
        
        // Check model finding
        bor.tools.simplellm.Model imageModel = service.findModel(Model_Type.IMAGE);
        assertNotNull(imageModel, "Should find an image generation model");
        assertTrue(imageModel.getName().contains("dall-e"), "Should be a DALL-E model");
        
        System.out.println("DALL-E models configured correctly: " + imageModel.getName());
    }

    @Test
    @DisplayName("Test image generation method with validation")
    void testImageGenerationMethodValidation() {
        // Given
        OpenAILLMService service = new OpenAILLMService();
        MapParam params = new MapParam();
        params.put("model", "dall-e-3");
        
        // When & Then - Test validation
        assertThrows(LLMException.class, () -> {
            service.generateImage(null, params);
        }, "Should throw exception for null prompt");
        
        assertThrows(LLMException.class, () -> {
            service.generateImage("", params);
        }, "Should throw exception for empty prompt");
        
        assertThrows(LLMException.class, () -> {
            service.generateImage("   ", params);
        }, "Should throw exception for blank prompt");
        
        System.out.println("Image generation validation works correctly");
    }

    @Test
    @DisplayName("Test image editing method validation")
    void testImageEditingMethodValidation() {
        // Given
        OpenAILLMService service = new OpenAILLMService();
        byte[] imageData = "fake-image-data".getBytes();
        MapParam params = new MapParam();
        
        // When & Then - Test validation
        assertThrows(LLMException.class, () -> {
            service.editImage(null, "Edit prompt", null, params);
        }, "Should throw exception for null image data");
        
        assertThrows(LLMException.class, () -> {
            service.editImage(new byte[0], "Edit prompt", null, params);
        }, "Should throw exception for empty image data");
        
        assertThrows(LLMException.class, () -> {
            service.editImage(imageData, null, null, params);
        }, "Should throw exception for null prompt");
        
        assertThrows(LLMException.class, () -> {
            service.editImage(imageData, "", null, params);
        }, "Should throw exception for empty prompt");
        
        System.out.println("Image editing validation works correctly");
    }

    @Test
    @DisplayName("Test image variation method validation")
    void testImageVariationMethodValidation() {
        // Given
        OpenAILLMService service = new OpenAILLMService();
        MapParam params = new MapParam();
        
        // When & Then - Test validation
        assertThrows(LLMException.class, () -> {
            service.createImageVariation(null, params);
        }, "Should throw exception for null image data");
        
        assertThrows(LLMException.class, () -> {
            service.createImageVariation(new byte[0], params);
        }, "Should throw exception for empty image data");
        
        System.out.println("Image variation validation works correctly");
    }

    @Test
    @DisplayName("Test multipart upload placeholder behavior")
    void testMultipartUploadPlaceholder() {
        // Given
        OpenAILLMService service = new OpenAILLMService();
        byte[] imageData = "fake-image-data".getBytes();
        MapParam params = new MapParam();
        
        // When & Then - Should throw informative exception for unimplemented multipart
        LLMException exception = assertThrows(LLMException.class, () -> {
            service.editImage(imageData, "Edit prompt", null, params);
        });
        
        assertTrue(exception.getMessage().contains("multipart upload support"),
                  "Should mention multipart support limitation");
        
        exception = assertThrows(LLMException.class, () -> {
            service.createImageVariation(imageData, params);
        });
        
        assertTrue(exception.getMessage().contains("multipart upload support"),
                  "Should mention multipart support limitation");
        
        System.out.println("Multipart placeholder behavior works correctly");
    }
}