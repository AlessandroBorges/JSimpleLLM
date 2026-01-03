package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.Embeddings_Op;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Comprehensive JUnit tests for LMStudio multi-embedding creation functionality.
 *
 * This test suite validates the multi-embedding method:
 * {@code List<float[]> embeddings(Embeddings_Op op, String[] texto, MapParam params)}
 *
 * Tests focus on:
 * - Basic multi-embedding generation with varying input sizes
 * - Identifying the threshold where the system struggles (currently > 5 sentences)
 * - Validating embedding dimensions and content
 * - Testing different operation types
 *
 * Model: text-embedding-snowflake-arctic-embed-l-v2.0
 * Base URL: http://localhost:1234 (LMStudio default)
 *
 * @author Claude Code
 * @version 1.0
 */
@lombok.extern.slf4j.Slf4j
class LMStudioMultiEmbeddingsTest extends LMStudioLLMServiceTestBase {

	private static final String MODEL = "text-embedding-snowflake-arctic-embed-l-v2.0";
	private static final String BASE_URL = "http://localhost:1234";

	@Test
	@DisplayName("Multi-embeddings: Generate embeddings for 2 sentences")
	void testMultiEmbeddingsWithTwoSentences() throws LLMException {
		// Given
		String[] texts = {
			"The quick brown fox jumps over the lazy dog.",
			"Embeddings are numerical representations of text."
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

		// Then
		assertNotNull(embeddings, "Embeddings list should not be null");
		assertEquals(2, embeddings.size(), "Should generate exactly 2 embeddings");

		for (int i = 0; i < embeddings.size(); i++) {
			float[] embedding = embeddings.get(i);
			assertNotNull(embedding, "Individual embedding at index " + i + " should not be null");
			assertTrue(embedding.length > 0, "Embedding at index " + i + " should have non-zero length");
			assertHasNonZeroValues(embedding, i);
		}

		System.out.println("✓ Successfully generated embeddings for 2 sentences");
		System.out.println("  Embedding dimension: " + embeddings.get(0).length);
	}

	@Test
	@DisplayName("Multi-embeddings: Generate embeddings for 5 sentences")
	void testMultiEmbeddingsWithFiveSentences() throws LLMException {
		// Given - 5 sentences (at the threshold)
		String[] texts = {
			"Artificial intelligence is transforming how we work.",
			"Machine learning models can process vast amounts of data.",
			"Natural language processing enables computers to understand text.",
			"Embeddings capture semantic meaning in numerical form.",
			"Transformers have revolutionized the field of NLP."
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

		// Then
		assertNotNull(embeddings, "Embeddings list should not be null");
		assertEquals(5, embeddings.size(), "Should generate exactly 5 embeddings");

		int expectedDimension = embeddings.get(0).length;

		for (int i = 0; i < embeddings.size(); i++) {
			float[] embedding = embeddings.get(i);
			assertNotNull(embedding, "Embedding at index " + i + " should not be null");
			assertEquals(expectedDimension, embedding.length,
				"All embeddings should have consistent dimensions");
			assertHasNonZeroValues(embedding, i);
		}

		System.out.println("✓ Successfully generated embeddings for 5 sentences");
		System.out.println("  Count: " + embeddings.size());
		System.out.println("  Dimension: " + expectedDimension);
	}

	@Test
	@DisplayName("Multi-embeddings: Generate embeddings for 6 sentences (just over threshold)")
	void testMultiEmbeddingsWithSixSentences() throws LLMException {
		// Given - 6 sentences (first beyond the reported threshold)
		String[] texts = {
			"Cloud computing enables scalable infrastructure.",
			"Data centers host multiple servers and storage systems.",
			"APIs provide standardized interfaces for communication.",
			"Microservices architecture breaks applications into smaller components.",
			"Docker containers package applications with their dependencies.",
			"Kubernetes orchestrates container deployment and scaling."
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		try {
			List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

			// Then
			assertNotNull(embeddings, "Embeddings list should not be null");
			assertEquals(6, embeddings.size(), "Should generate exactly 6 embeddings");

			int expectedDimension = embeddings.get(0).length;
			for (int i = 0; i < embeddings.size(); i++) {
				float[] embedding = embeddings.get(i);
				assertEquals(expectedDimension, embedding.length,
					"All embeddings should have consistent dimensions");
				assertHasNonZeroValues(embedding, i);
			}

			System.out.println("✓ Successfully generated embeddings for 6 sentences");
			System.out.println("  Count: " + embeddings.size());
			System.out.println("  Dimension: " + expectedDimension);
		} catch (LLMException e) {
			// Document the issue if it occurs
			System.err.println("✗ Failed to generate embeddings for 6 sentences");
			System.err.println("  Error: " + e.getMessage());
			throw e;
		}
	}

	@Test
	@DisplayName("Multi-embeddings: Generate embeddings for 10 sentences")
	void testMultiEmbeddingsWithTenSentences() throws LLMException {
		// Given - 10 sentences (well beyond the reported threshold)
		String[] texts = {
			"Distributed systems coordinate multiple computers.",
			"Network latency affects system performance.",
			"Load balancing distributes requests across servers.",
			"Caching reduces database queries and improves response time.",
			"Indexing optimizes database search operations.",
			"Sharding partitions data across multiple databases.",
			"Replication ensures data availability and fault tolerance.",
			"Consensus algorithms coordinate distributed agreement.",
			"Message queues decouple system components.",
			"Monitoring tracks system health and performance metrics."
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		try {
			List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

			// Then
			assertNotNull(embeddings, "Embeddings list should not be null");
			assertEquals(10, embeddings.size(), "Should generate exactly 10 embeddings");

			int expectedDimension = embeddings.get(0).length;
			for (int i = 0; i < embeddings.size(); i++) {
				float[] embedding = embeddings.get(i);
				assertEquals(expectedDimension, embedding.length,
					"All embeddings should have consistent dimensions");
				assertHasNonZeroValues(embedding, i);
			}

			System.out.println("✓ Successfully generated embeddings for 10 sentences");
			System.out.println("  Count: " + embeddings.size());
			System.out.println("  Dimension: " + expectedDimension);
		} catch (LLMException e) {
			System.err.println("✗ Failed to generate embeddings for 10 sentences");
			System.err.println("  Error: " + e.getMessage());
			fail("Multi-embedding should handle 10 sentences", e);
		}
	}

	@Test
	@DisplayName("Multi-embeddings: Generate embeddings for 15 sentences")
	void testMultiEmbeddingsWithFifteenSentences() throws LLMException {
		// Given - 15 sentences (large batch)
		String[] texts = {
			"Software development follows various methodologies.",
			"Agile practices promote iterative development.",
			"Scrum defines roles and ceremonies for team coordination.",
			"Kanban visualizes workflow and work in progress.",
			"Git enables distributed version control.",
			"Code review catches bugs and improves quality.",
			"Automated testing verifies software behavior.",
			"Continuous integration merges code changes frequently.",
			"Continuous deployment releases changes to production.",
			"Infrastructure as code manages system configuration.",
			"Terraform provisions cloud resources declaratively.",
			"Ansible automates system configuration management.",
			"Jenkins orchestrates continuous integration pipelines.",
			"Monitoring alerts teams to production issues.",
			"Logging records system events and errors for analysis."
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		try {
			List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

			// Then
			assertNotNull(embeddings, "Embeddings list should not be null");
			assertEquals(15, embeddings.size(), "Should generate exactly 15 embeddings");

			int expectedDimension = embeddings.get(0).length;
			for (int i = 0; i < embeddings.size(); i++) {
				float[] embedding = embeddings.get(i);
				assertEquals(expectedDimension, embedding.length,
					"All embeddings should have consistent dimensions");
				assertHasNonZeroValues(embedding, i);
			}

			System.out.println("✓ Successfully generated embeddings for 15 sentences");
			System.out.println("  Count: " + embeddings.size());
			System.out.println("  Dimension: " + expectedDimension);
		} catch (LLMException e) {
			System.err.println("✗ Failed to generate embeddings for 15 sentences");
			System.err.println("  Error: " + e.getMessage());
			fail("Multi-embedding should handle 15 sentences", e);
		}
	}

	@Test
	@DisplayName("Multi-embeddings: Test with QUERY operation type")
	void testMultiEmbeddingsWithQueryOperation() throws LLMException {
		// Given - Test with QUERY operation
		String[] texts = {
			"What is machine learning?",
			"How does clustering work?",
			"When should I use embeddings?",
			"Where can I find pretrained models?",
			"Why are transformers effective?"
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		List<float[]> embeddings = llmService.embeddings(Embeddings_Op.QUERY, texts, params);

		// Then
		assertNotNull(embeddings, "Embeddings list should not be null");
		assertEquals(5, embeddings.size(), "Should generate 5 embeddings for query operation");

		for (int i = 0; i < embeddings.size(); i++) {
			float[] embedding = embeddings.get(i);
			assertNotNull(embedding, "Embedding at index " + i + " should not be null");
			assertTrue(embedding.length > 0, "Embedding dimensions should be positive");
		}

		System.out.println("✓ Successfully generated embeddings with QUERY operation");
		System.out.println("  Count: " + embeddings.size());
		System.out.println("  Dimension: " + embeddings.get(0).length);
	}

	@Test
	@DisplayName("Multi-embeddings: Test with CODE_RETRIEVAL operation type")
	void testMultiEmbeddingsWithCodeRetrievalOperation() throws LLMException {
		// Given - Test with CODE_RETRIEVAL operation (if supported by model)
		String[] texts = {
			"function fibonacci(n) { if (n <= 1) return n; }",
			"class LinkedList { public void insert(int data) {} }",
			"SELECT * FROM users WHERE age > 18;",
			"docker run -d -p 8080:8080 myapp",
			"npm install express"
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		try {
			List<float[]> embeddings = llmService.embeddings(Embeddings_Op.CODE_RETRIEVAL, texts, params);

			// Then
			assertNotNull(embeddings, "Embeddings list should not be null");
			assertEquals(5, embeddings.size(), "Should generate 5 code embeddings");

			System.out.println("✓ Successfully generated embeddings with CODE_RETRIEVAL operation");
			System.out.println("  Count: " + embeddings.size());
			System.out.println("  Dimension: " + embeddings.get(0).length);
		} catch (LLMException e) {
			System.out.println("⚠ CODE_RETRIEVAL operation may not be supported: " + e.getMessage());
			// This is acceptable as not all models support CODE_RETRIEVAL
		}
	}

	@Test
	@DisplayName("Multi-embeddings: Verify consistency across calls")
	void testMultiEmbeddingsConsistency() throws LLMException {
		// Given - Same texts for two separate calls
		String[] texts = {
			"Consistency is important for embeddings.",
			"Same input should produce identical output.",
			"This enables reliable similarity comparisons."
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		List<float[]> firstCall = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);
		List<float[]> secondCall = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

		// Then
		assertEquals(firstCall.size(), secondCall.size(), "Both calls should produce same number of embeddings");

		for (int i = 0; i < firstCall.size(); i++) {
			float[] first = firstCall.get(i);
			float[] second = secondCall.get(i);

			assertEquals(first.length, second.length, "Embedding dimensions should be consistent");

			// Verify values are very close (allowing for floating point precision)
			for (int j = 0; j < first.length; j++) {
				assertEquals(first[j], second[j], 0.0001f,
					"Embedding values should be consistent across calls");
			}
		}

		System.out.println("✓ Multi-embeddings are consistent across multiple calls");
	}

	@Test
	@DisplayName("Multi-embeddings: Test with mixed content types")
	void testMultiEmbeddingsWithMixedContent() throws LLMException {
		// Given - Mixed content: short, long, with special characters
		String[] texts = {
			"Short text.",
			"This is a medium length sentence with some technical terms like API, JSON, and HTTP.",
			"A very long text that contains multiple sentences and discusses various topics. " +
			"It includes information about technology, science, and general knowledge. " +
			"The system should handle varying lengths appropriately.",
			"Numbers: 12345, 67.89, -100",
			"Special chars: @#$%^&*() and unicode: 你好 мир 🚀"
		};

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

		// Then
		assertEquals(5, embeddings.size(), "Should generate embeddings for all 5 texts");

		int expectedDimension = embeddings.get(0).length;
		for (int i = 0; i < embeddings.size(); i++) {
			float[] embedding = embeddings.get(i);
			assertEquals(expectedDimension, embedding.length,
				"All embeddings should have consistent dimensions regardless of input length");
		}

		System.out.println("✓ Successfully handled mixed content types");
		System.out.println("  Short text: " + embeddings.get(0).length + " dimensions");
		System.out.println("  Medium text: " + embeddings.get(1).length + " dimensions");
		System.out.println("  Long text: " + embeddings.get(2).length + " dimensions");
		System.out.println("  Numbers: " + embeddings.get(3).length + " dimensions");
		System.out.println("  Special chars: " + embeddings.get(4).length + " dimensions");
	}

	@Test
	@DisplayName("Multi-embeddings: Stress test with maximum batch")
	void testMultiEmbeddingsMaximumBatch() throws LLMException {
		// Given - Generate a large batch to find the actual limit
		String[] texts = new String[20];
		for (int i = 0; i < 20; i++) {
			texts[i] = "Sentence number " + (i + 1) + " for stress testing the multi-embedding system.";
		}

		MapParam params = new MapParam();
		params.model(MODEL);

		// When
		try {
			List<float[]> embeddings = llmService.embeddings(Embeddings_Op.DOCUMENT, texts, params);

			// Then
			assertEquals(20, embeddings.size(), "Should generate embeddings for all 20 texts");

			System.out.println("✓ Successfully generated 20 embeddings (stress test passed)");
			System.out.println("  Count: " + embeddings.size());
			System.out.println("  Dimension: " + embeddings.get(0).length);
		} catch (LLMException e) {
			System.err.println("✗ Failed to generate 20 embeddings");
			System.err.println("  Error type: " + e.getClass().getSimpleName());
			System.err.println("  Message: " + e.getMessage());
			fail("Multi-embedding system should handle at least 20 sentences", e);
		}
	}

	/**
	 * Helper method to verify that an embedding contains non-zero values.
	 */
	private void assertHasNonZeroValues(float[] embedding, int index) {
		boolean hasNonZeroValues = false;
		for (float value : embedding) {
			if (Math.abs(value) > 1e-6f) { // Using small epsilon for floating point comparison
				hasNonZeroValues = true;
				break;
			}
		}
		assertTrue(hasNonZeroValues,
			"Embedding at index " + index + " should contain non-zero values");
	}

	/**
	 * Helper method to calculate cosine similarity between two embedding vectors.
	 */
	protected double cosineSimilarity(float[] vectorA, float[] vectorB) {
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
