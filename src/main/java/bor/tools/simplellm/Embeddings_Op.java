package bor.tools.simplellm;

/**
 * Enumeration defining the various operations supported by embedding
 * models.<br>
 * Some models can generate optimized embeddings for various use cases—such as
 * document
 * retrieval,
 * question answering, and fact verification. Embeddings may also used for
 * specific input types—either a query or a
 * document—using prompts that are prepended to the input strings.
 */
public enum Embeddings_Op {
		/**
		 * Generate an embedding optimized for search queries.
		 */
		QUERY,
		/**
		 * Generate an embedding optimized for documents which will be queried later.
		 */
		DOCUMENT,
		/**
		 * Generate an embedding optimized for answering questions.
		 */
		QUESTION,
		/**
		 * Generate an embedding optimized for verifying facts.
		 */
		FACT_CHECK,
		/**
		 * Used to generate embeddings that are optimized to classify texts according to
		 * preset labels
		 */
		CLASSICATION,
		/**
		 * Used to generate embeddings that are optimized to cluster texts according to
		 * similarity
		 */
		CLUSTERING,
		/**
		 * Used to generate embeddings that are optimized to assess text similarity.
		 * <h3>NOTE: This is not intended for retrieval use cases.</h3>
		 */
		SEMANTIC_SIMILARITY,

		/**
		 * Used to retrieve a code block based on a natural language query,
		 * such as sort an array or reverse a linked list. <br>
		 * Embeddings of the code blocks are computed using retrieval_document.
		 */
		CODE_RETRIEVAL,
		/**
		 * Default operation with no specific optimization.
		 */
		DEFAULT;

	public Embeddings_Op fromString(String value) {
		for (Embeddings_Op op : Embeddings_Op.values()) {
			if (op.name().equalsIgnoreCase(value)) {
				return op;
			}
		}
		return null;
	}
}