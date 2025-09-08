package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for OpenAI embeddings functionality.
 */
class OpenAIEmbeddingsTest extends OpenAILLMServiceTestBase {

    @Test
    @DisplayName("Test basic embeddings generation")
    void testBasicEmbeddings() throws LLMException {
        // Given
        String text = "Hello, this is a test sentence for embedding generation.";
        String model = "text-embedding-3-small";
        
        // When
        float[] embeddings = llmService.embeddings(text, model, null);
        
        // Then
        assertNotNull(embeddings, "Embeddings should not be null");
        assertTrue(embeddings.length > 0, "Embeddings array should not be empty");
        
        // text-embedding-3-small typically returns 1536 dimensions
        assertEquals(1536, embeddings.length, "Expected 1536 dimensions for text-embedding-3-small");
        
        // Check that embeddings contain meaningful values
        boolean hasNonZeroValues = false;
        for (float value : embeddings) {
            if (value != 0.0f) {
                hasNonZeroValues = true;
                break;
            }
        }
        assertTrue(hasNonZeroValues, "Embeddings should contain non-zero values");
        
        System.out.println("Generated embeddings with " + embeddings.length + " dimensions");
        System.out.println("First few values: " + embeddings[0] + ", " + embeddings[1] + ", " + embeddings[2]);
    }

    @Test
    @DisplayName("Test embeddings with default model")
    void testEmbeddingsWithDefaultModel() throws LLMException {
        // Given
        String text = "This is another test sentence.";
        
        // When - using null model should use default
        float[] embeddings = llmService.embeddings(text, null, null);
        
        // Then
        assertNotNull(embeddings, "Embeddings should not be null");
        assertTrue(embeddings.length > 0, "Embeddings array should not be empty");
        
        System.out.println("Default model embeddings: " + embeddings.length + " dimensions");
    }

    @Test
    @DisplayName("Test embeddings with custom dimensions")
    void testEmbeddingsWithCustomDimensions() throws LLMException {
        // Given
        String text = "Test text for custom dimensions.";
        String model = "text-embedding-3-small";
        Integer customDimensions = 512;  // Custom dimension size
        
        // When
        float[] embeddings = llmService.embeddings(text, model, customDimensions);
        
        // Then
        assertNotNull(embeddings, "Embeddings should not be null");
        assertEquals(customDimensions.intValue(), embeddings.length, 
                    "Embeddings should have custom dimension size");
        
        System.out.println("Custom dimensions embeddings: " + embeddings.length + " dimensions");
    }

    @Test
    @DisplayName("Test embeddings similarity")
    void testEmbeddingsSimilarity() throws LLMException {
        // Given
        String text1 = "The cat is sleeping on the mat.";
        String text2 = "A cat is resting on a rug.";
        String text3 = "The weather is sunny today.";
        
        // When
        float[] embeddings1 = llmService.embeddings(text1, "text-embedding-3-small", null);
        float[] embeddings2 = llmService.embeddings(text2, "text-embedding-3-small", null);
        float[] embeddings3 = llmService.embeddings(text3, "text-embedding-3-small", null);
        
        // Then
        double similarity12 = cosineSimilarity(embeddings1, embeddings2);
        double similarity13 = cosineSimilarity(embeddings1, embeddings3);
        
        // Similar sentences should have higher similarity than different ones
        assertTrue(similarity12 > similarity13, 
                  "Similar sentences should have higher similarity. " +
                  "Similarity 1-2: " + similarity12 + ", Similarity 1-3: " + similarity13);
        
        System.out.println("Similarity (cat sentences): " + similarity12);
        System.out.println("Similarity (cat vs weather): " + similarity13);
    }

    @Test
    @DisplayName("Test embeddings with different text lengths")
    void testEmbeddingsWithDifferentTextLengths() throws LLMException {
        // Given
        String shortText = "Hello.";
        String mediumText = "This is a medium length sentence for testing embeddings.";
        String longText = "This is a much longer text that contains multiple sentences. " +
                         "It should still generate meaningful embeddings despite its length. " +
                         "The embedding model should be able to handle various text lengths effectively.";
        
        // When
        float[] shortEmbeddings = llmService.embeddings(shortText, "text-embedding-3-small", null);
        float[] mediumEmbeddings = llmService.embeddings(mediumText, "text-embedding-3-small", null);
        float[] longEmbeddings = llmService.embeddings(longText, "text-embedding-3-small", null);
        
        // Then
        assertEquals(shortEmbeddings.length, mediumEmbeddings.length, 
                    "All embeddings should have same dimensions");
        assertEquals(mediumEmbeddings.length, longEmbeddings.length, 
                    "All embeddings should have same dimensions");
        
        System.out.println("Short text embeddings generated successfully");
        System.out.println("Medium text embeddings generated successfully");
        System.out.println("Long text embeddings generated successfully");
    }

    @Test
    @DisplayName("Test embeddings error handling with empty text")
    void testEmbeddingsWithEmptyText() {
        // Given
        String emptyText = "";
        
        // When & Then
        assertThrows(LLMException.class, () -> {
            llmService.embeddings(emptyText, "text-embedding-3-small", null);
        }, "Should throw exception for empty text");
    }

    @Test
    @DisplayName("Test embeddings error handling with null text")
    void testEmbeddingsWithNullText() {
        // When & Then
        assertThrows(LLMException.class, () -> {
            llmService.embeddings(null, "text-embedding-3-small", null);
        }, "Should throw exception for null text");
    }

    @Test
    @DisplayName("Test embeddings with special characters")
    void testEmbeddingsWithSpecialCharacters() throws LLMException {
        // Given
        String textWithSpecialChars = "Hello! This text contains special characters: @#$%^&*()_+ " +
                                    "and unicode: αβγδε 中文 🚀🔥💡";
        
        // When
        float[] embeddings = llmService.embeddings(textWithSpecialChars, "text-embedding-3-small", null);
        
        // Then
        assertNotNull(embeddings, "Embeddings should be generated for text with special characters");
        assertTrue(embeddings.length > 0, "Embeddings should have positive length");
        
        System.out.println("Special characters embeddings generated: " + embeddings.length + " dimensions");
    }

    /**
     * Helper method to calculate cosine similarity between two embedding vectors.
     */
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}