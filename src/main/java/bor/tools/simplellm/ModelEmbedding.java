/**
 * 
 */
package bor.tools.simplellm;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Spcialized model for embeddings
 */
@Getter
@Setter
public class ModelEmbedding extends Model {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

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
	public enum Emb_Operation {
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

		public Emb_Operation fromString(String value) {
			for (Emb_Operation op : Emb_Operation.values()) {
				if (op.name().equalsIgnoreCase(value)) {
					return op;
				}
			}
			return null;
		}
	}

	/**
	 * Record to hold operation and its corresponding prefix.
	 * 
	 * @param operation The embedding operation type.
	 * @param prefix    The prompt prefix associated with the operation.
	 */
	record Emb_OperationPrefix(Emb_Operation operation, String prefix) {}

	protected static List<Emb_OperationPrefix> gemma_OperationPrefixs;
	protected static List<Emb_OperationPrefix> nomic_OperationPrefixs;
	protected static List<Emb_OperationPrefix> openai_OperationPrefixs;
	protected static List<Emb_OperationPrefix> snowflake_OperationPrefixs;
	protected static List<Emb_OperationPrefix> qwen3_OperationPrefixs;
	protected static List<Emb_OperationPrefix> bge_OperationPrefixs;

	static {
		gemma_OperationPrefixs =
		            List.of(new Emb_OperationPrefix(Emb_Operation.QUERY, "task: search result | query: $s"),
		                    new Emb_OperationPrefix(Emb_Operation.DOCUMENT, "title: none | text:  %s"),
		                    new Emb_OperationPrefix(Emb_Operation.QUESTION, "task: question answering | query: %s"),
		                    new Emb_OperationPrefix(Emb_Operation.FACT_CHECK, "task: fact checking | query:: %s"),
		                    new Emb_OperationPrefix(Emb_Operation.CLASSICATION, "task: classification | query: %s"),
		                    new Emb_OperationPrefix(Emb_Operation.CLUSTERING, "task: clustering | query: %s"),
		                    new Emb_OperationPrefix(Emb_Operation.SEMANTIC_SIMILARITY,
		                                "task: sentence similarity | query: %s"),
		                    new Emb_OperationPrefix(Emb_Operation.CODE_RETRIEVAL, "task: code retrieval: %s"),
		                    new Emb_OperationPrefix(Emb_Operation.DEFAULT, "title: none | text:  %s"));

		nomic_OperationPrefixs = List.of(new Emb_OperationPrefix(Emb_Operation.QUERY, "search_query: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.DOCUMENT, "search_document: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.QUESTION, "search_document: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.FACT_CHECK, "search_document: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.CLASSICATION, "classification: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.CLUSTERING, "clustering: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.SEMANTIC_SIMILARITY, "clustering: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.CODE_RETRIEVAL, "search_document: %s"),
		                                 new Emb_OperationPrefix(Emb_Operation.DEFAULT, "search_document: %s"));

		openai_OperationPrefixs = List.of(new Emb_OperationPrefix(Emb_Operation.QUERY, ""),
		                                  new Emb_OperationPrefix(Emb_Operation.DOCUMENT, ""),
		                                  new Emb_OperationPrefix(Emb_Operation.DEFAULT, ""));

		snowflake_OperationPrefixs = List.of(new Emb_OperationPrefix(Emb_Operation.QUERY, "query: %s"),
		                                     new Emb_OperationPrefix(Emb_Operation.DOCUMENT, ""),
		                                     new Emb_OperationPrefix(Emb_Operation.DEFAULT, ""));
		// Qwen3 uses QUERY as "Instruction: \n Query%s" and and ll other uses ""
		qwen3_OperationPrefixs =
		            List.of(new Emb_OperationPrefix(Emb_Operation.QUERY, "Instruct: \n Query: %s <|endoftext|>"),
		                    new Emb_OperationPrefix(Emb_Operation.DOCUMENT, "%s <|endoftext|>"),
		                    new Emb_OperationPrefix(Emb_Operation.DEFAULT, "%s <|endoftext|>"));

		bge_OperationPrefixs = List.of(
		                               new Emb_OperationPrefix(Emb_Operation.QUERY,
		                                           "Represente esta frase para buscar passagens relevantes: %s"),
		                               new Emb_OperationPrefix(Emb_Operation.DOCUMENT, "%s"),
		                               new Emb_OperationPrefix(Emb_Operation.DEFAULT, "%s"));
	}

	/**
	 * Map of operation types to their corresponding prompt prefixes.
	 */
	protected Map<Emb_Operation, String> operationPrefixs;

	/**
	 * 
	 */
	public ModelEmbedding() {
		super();
		this.checkEmbeddingSet();
	}

	/**
	 * @param name
	 * @param contextLength
	 * @param model_types
	 */
	public ModelEmbedding(String name, Integer contextLength, Model_Type... model_types) {
		super(name, contextLength, model_types);
		this.checkEmbeddingSet();
	}

	/**
	 * Ensure the model is marked as an EMBEDDING type and set operation prefixes
	 */
	private void checkEmbeddingSet() {
		if (this.isType(Model_Type.EMBEDDING) == false) {
			this.addExtraType(Model_Type.EMBEDDING);
		}
		if (this.getName() != null) {
			String lname = this.getName().toLowerCase();
			if (lname.contains("gemma")) {
				this.operationPrefixs = gemma_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(Emb_OperationPrefix::operation,
				                                                       Emb_OperationPrefix::prefix));
			} else if (lname.contains("nomic")) {
				this.operationPrefixs = nomic_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(Emb_OperationPrefix::operation,
				                                                       Emb_OperationPrefix::prefix));
			} else if (lname.contains("003") || lname.contains("ada")) {
				this.operationPrefixs = openai_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(Emb_OperationPrefix::operation,
				                                                       Emb_OperationPrefix::prefix));
			} else if (lname.contains("qwen3") || lname.contains("qwen-3")) {
				this.operationPrefixs = qwen3_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(Emb_OperationPrefix::operation,
				                                                       Emb_OperationPrefix::prefix));
			} else if (lname.contains("snowflake")) {
				this.operationPrefixs = snowflake_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(Emb_OperationPrefix::operation,
				                                                       Emb_OperationPrefix::prefix));
			} else if (lname.contains("bge")) {
				this.operationPrefixs = bge_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(Emb_OperationPrefix::operation,
				                                                       Emb_OperationPrefix::prefix));
			}
		}
	}

	/**
	 * @param name
	 * @param alias
	 * @param contextLength
	 * @param model_types
	 */
	public ModelEmbedding(String name, String alias, Integer contextLength, Model_Type... model_types) {
		super(name, alias, contextLength, model_types);
	}

	/**
	 * Get the prompt prefix associated with a specific embedding operation.
	 * <p>
	 * This method retrieves the prompt prefix for the given {@code Emb_Operation}.
	 * If no specific prefix is defined for the operation, it returns the default
	 * prefix
	 * or an empty string if no default is set.
	 * </p>
	 * 
	 * @param op the embedding operation type
	 * 
	 * @return the prompt prefix associated with the operation, or an empty string
	 *         if none is defined
	 */
	public String getPrefixForOperation(Emb_Operation op) {
		if (operationPrefixs == null) {
			return "";
		}
		if (op == null) {
			op = Emb_Operation.DEFAULT;
		}
		return operationPrefixs.getOrDefault(op, operationPrefixs.getOrDefault(Emb_Operation.DEFAULT, ""));
	}

	public String applyOperationPrefix(Emb_Operation op, String text) {
		String prefix = getPrefixForOperation(op);

		if (prefix != null && prefix.isEmpty() == false) {
			if (prefix.contains("%s")) {
				return String.format(prefix, text);
			}
			return prefix
			            + " "
			            + text;
		}
		return text;
	}

}
