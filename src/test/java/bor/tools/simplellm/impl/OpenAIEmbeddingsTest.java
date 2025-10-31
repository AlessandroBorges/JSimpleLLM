package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.Embeddings_Op;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Tests for OpenAI embeddings functionality.
 */
class OpenAIEmbeddingsTest extends OpenAILLMServiceTestBase {

	@Test
	@DisplayName("Test basic embeddings generation")
	void testBasicEmbeddings() throws LLMException {
		// Given
		String text  = "Hello, this is a test sentence for embedding generation.";
		String model = "text-embedding-3-small";
        MapParam param = new MapParam();
        param.model(model);
		// When
		float[] embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT,text, param);

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

		System.out.println("Generated embeddings with "
		            + embeddings.length
		            + " dimensions");
		System.out.println("First few values: "
		            + embeddings[0]
		            + ", "
		            + embeddings[1]
		            + ", "
		            + embeddings[2]);
	}

	@Test
	@DisplayName("Test embeddings with default model")
	void testEmbeddingsWithDefaultModel() throws LLMException {
		// Given
		String text = "This is another test sentence.";
        MapParam param = new MapParam();
		// When - using null model should use default
		float[] embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT,text,param);

		// Then
		assertNotNull(embeddings, "Embeddings should not be null");
		assertTrue(embeddings.length > 0, "Embeddings array should not be empty");

		System.out.println("Default model embeddings: "
		            + embeddings.length
		            + " dimensions");
	}

	@Test
	@DisplayName("Test embeddings with custom dimensions")
	void testEmbeddingsWithCustomDimensions() throws LLMException {
		// Given
		String  text             = "Test text for custom dimensions.";
		String  model            = "text-embedding-3-small";
		Integer customDimensions = 512;  // Custom dimension size
		
		MapParam param = new MapParam();
		param.model(model);
		param.dimension(customDimensions);
		// When
		float[] embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT,text,param);

		// Then
		assertNotNull(embeddings, "Embeddings should not be null");
		assertEquals(customDimensions.intValue(), embeddings.length, "Embeddings should have custom dimension size");

		System.out.println("Custom dimensions embeddings: "
		            + embeddings.length
		            + " dimensions");
	}

	@Test
	@DisplayName("Test embeddings similarity")
	void testEmbeddingsSimilarity() throws LLMException {
		// Given
		String text1 = "O gato está dormindo no carpete.";//
		String text2 = "Um gato esta descansando no tapete";                                                                                // rug.";
		String text3 = "O tempo está ensolarado hoje!";// "The weather is sunny today.";
		MapParam param = new MapParam();
		param.model("text-embedding-3-small");
		param.dimension(512);
		// When
		float[] embeddings1 = llmService.embeddings(Embeddings_Op.DOCUMENT,text1,param);
		float[] embeddings2 = llmService.embeddings(Embeddings_Op.DOCUMENT,text2,param);
		float[] embeddings3 = llmService.embeddings(Embeddings_Op.DOCUMENT,text3,param);

		// Then
		double similarity12 = cosineSimilarity(embeddings1, embeddings2);
		double similarity13 = cosineSimilarity(embeddings1, embeddings3);

		// Similar sentences should have higher similarity than different ones
		assertTrue(similarity12 > similarity13,
		           "Similar sentences should have higher similarity. "
		                       + "Similarity 1-2: "
		                       + similarity12
		                       + ", Similarity 1-3: "
		                       + similarity13);

		System.out.println("Similarity (cat sentences): "
		            + similarity12);
		System.out.println("Similarity (cat vs weather): "
		            + similarity13);
	}

	@Test
	@DisplayName("Test embeddings with different text lengths")
	void testEmbeddingsWithDifferentTextLengths() throws LLMException {
		// Given
		String shortText  = "Hello.";
		String mediumText = "This is a medium length sentence for testing embeddings.";
		String longText   = "This is a much longer text that contains multiple sentences. "
		            + "It should still generate meaningful embeddings despite its length. "
		            + "The embedding model should be able to handle various text lengths effectively.";
		
		MapParam param = new MapParam();
		param.model("text-embedding-3-small");
		// When
		float[] shortEmbeddings  = llmService.embeddings(Embeddings_Op.DOCUMENT,shortText, param);
		float[] mediumEmbeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, mediumText, param);
		float[] longEmbeddings   = llmService.embeddings(Embeddings_Op.DOCUMENT,longText, param);

		// Then
		assertEquals(shortEmbeddings.length, mediumEmbeddings.length, "All embeddings should have same dimensions");
		assertEquals(mediumEmbeddings.length, longEmbeddings.length, "All embeddings should have same dimensions");

		System.out.println("Short text embeddings generated successfully");
		System.out.println("Medium text embeddings generated successfully");
		System.out.println("Long text embeddings generated successfully");
	}

	@Test
	@DisplayName("Test embeddings error handling with empty text")
	void testEmbeddingsWithEmptyText() {
		// Given
		String emptyText = "";
		MapParam param = new MapParam();
		param.model("text-embedding-3-small");
		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.embeddings(Embeddings_Op.DOCUMENT,emptyText, param); },
		             "Should throw exception for empty text");
	}

	@Test
	@DisplayName("Test embeddings error handling with null text")
	void testEmbeddingsWithNullText() {
		MapParam param = new MapParam();
		param.model("text-embedding-3-small");
		// When & Then
		assertThrows(LLMException.class,
		             () -> { llmService.embeddings(Embeddings_Op.DOCUMENT,null, param); },
		             "Should throw exception for null text");
	}

	@Test
	@DisplayName("Test embeddings with special characters")
	void testEmbeddingsWithSpecialCharacters() throws LLMException {
		MapParam param = new MapParam();
		param.model("text-embedding-3-small");
		// Given
		String textWithSpecialChars = "Hello! This text contains special characters: @#$%^&*()_+ "
		            + "and unicode: αβγδε 中文 🚀🔥💡";

		// When
		float[] embeddings = llmService.embeddings(null, textWithSpecialChars, param);

		// Then
		assertNotNull(embeddings, "Embeddings should be generated for text with special characters");
		assertTrue(embeddings.length > 0, "Embeddings should have positive length");

		System.out.println("Special characters embeddings generated: "
		            + embeddings.length
		            + " dimensions");
	}

	/**
	 * Helper method to calculate cosine similarity between two embedding vectors.
	 */
	private double cosineSimilarity(float[] vectorA, float[] vectorB) {
		if (vectorA.length != vectorB.length) {
			throw new IllegalArgumentException("Vectors must have the same length");
		}

		double dotProduct = 0.0;
		double normA      = 0.0;
		double normB      = 0.0;

		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}

		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
}
