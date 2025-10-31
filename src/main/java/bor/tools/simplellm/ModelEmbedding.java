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
	 * Record to hold operation and its corresponding prefix.
	 * 
	 * @param operation The embedding operation type.
	 * @param prefix    The prompt prefix associated with the operation.
	 */
	record EmbedOperation_Prefix(Embeddings_Op operation, String prefix) {}

	protected static List<EmbedOperation_Prefix> gemma_OperationPrefixs;
	protected static List<EmbedOperation_Prefix> nomic_OperationPrefixs;
	protected static List<EmbedOperation_Prefix> openai_OperationPrefixs;
	protected static List<EmbedOperation_Prefix> snowflake_OperationPrefixs;
	protected static List<EmbedOperation_Prefix> qwen3_OperationPrefixs;
	protected static List<EmbedOperation_Prefix> bge_OperationPrefixs;

	static {
		gemma_OperationPrefixs =
		            List.of(new EmbedOperation_Prefix(Embeddings_Op.QUERY, "task: search result | query: $s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.DOCUMENT, "title: none | text:  %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.QUESTION, "task: question answering | query: %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.FACT_CHECK, "task: fact checking | query:: %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.CLASSICATION, "task: classification | query: %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.CLUSTERING, "task: clustering | query: %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.SEMANTIC_SIMILARITY,
		                                "task: sentence similarity | query: %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.CODE_RETRIEVAL, "task: code retrieval: %s"),
		                    new EmbedOperation_Prefix(Embeddings_Op.DEFAULT, "title: none | text:  %s"));

		nomic_OperationPrefixs = List.of(new EmbedOperation_Prefix(Embeddings_Op.QUERY, "search_query: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.DOCUMENT, "search_document: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.QUESTION, "search_document: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.FACT_CHECK, "search_document: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.CLASSICATION, "classification: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.CLUSTERING, "clustering: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.SEMANTIC_SIMILARITY, "clustering: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.CODE_RETRIEVAL, "search_document: %s"),
		                                 new EmbedOperation_Prefix(Embeddings_Op.DEFAULT, "search_document: %s"));

		openai_OperationPrefixs = List.of(new EmbedOperation_Prefix(Embeddings_Op.QUERY, ""),
		                                  new EmbedOperation_Prefix(Embeddings_Op.DOCUMENT, ""),
		                                  new EmbedOperation_Prefix(Embeddings_Op.DEFAULT, ""));

		snowflake_OperationPrefixs = List.of(new EmbedOperation_Prefix(Embeddings_Op.QUERY, "query: %s"),
		                                     new EmbedOperation_Prefix(Embeddings_Op.DOCUMENT, ""),
		                                     new EmbedOperation_Prefix(Embeddings_Op.DEFAULT, ""));
		// Qwen3 uses QUERY as "Instruction: \n Query%s" and other Ops uses ""
		qwen3_OperationPrefixs =
		            List.of(new EmbedOperation_Prefix(Embeddings_Op.QUERY, "Instruct: \n Query: %s <|endoftext|>"),
		                    new EmbedOperation_Prefix(Embeddings_Op.DOCUMENT, "%s <|endoftext|>"),
		                    new EmbedOperation_Prefix(Embeddings_Op.DEFAULT, "%s <|endoftext|>"));

		bge_OperationPrefixs = List.of(
		                               new EmbedOperation_Prefix(Embeddings_Op.QUERY,
		                                           "Represente esta frase para buscar passagens relevantes: %s"),
		                               new EmbedOperation_Prefix(Embeddings_Op.DOCUMENT, "%s"),
		                               new EmbedOperation_Prefix(Embeddings_Op.DEFAULT, "%s"));
	}

	/**
	 * Map of operation types to their corresponding prompt prefixes.
	 */
	protected Map<Embeddings_Op, String> operationPrefixs;

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
				            .collect(java.util.stream.Collectors.toMap(EmbedOperation_Prefix::operation,
				                                                       EmbedOperation_Prefix::prefix));
			} else if (lname.contains("nomic")) {
				this.operationPrefixs = nomic_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(EmbedOperation_Prefix::operation,
				                                                       EmbedOperation_Prefix::prefix));
			} else if (lname.contains("003") || lname.contains("ada")) {
				this.operationPrefixs = openai_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(EmbedOperation_Prefix::operation,
				                                                       EmbedOperation_Prefix::prefix));
			} else if (lname.contains("qwen3") || lname.contains("qwen-3")) {
				this.operationPrefixs = qwen3_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(EmbedOperation_Prefix::operation,
				                                                       EmbedOperation_Prefix::prefix));
			} else if (lname.contains("snowflake")) {
				this.operationPrefixs = snowflake_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(EmbedOperation_Prefix::operation,
				                                                       EmbedOperation_Prefix::prefix));
			} else if (lname.contains("bge")) {
				this.operationPrefixs = bge_OperationPrefixs.stream()
				            .collect(java.util.stream.Collectors.toMap(EmbedOperation_Prefix::operation,
				                                                       EmbedOperation_Prefix::prefix));
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
	 * This method retrieves the prompt prefix for the given {@code Embeddings_Op}.
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
	public String getPrefixForOperation(Embeddings_Op op) {
		if (operationPrefixs == null) {
			return "";
		}
		if (op == null) {
			op = Embeddings_Op.DEFAULT;
		}
		return operationPrefixs.getOrDefault(op, operationPrefixs.getOrDefault(Embeddings_Op.DEFAULT, ""));
	}

	public String applyOperationPrefix(Embeddings_Op op, String text) {
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

	public ModelEmbedding clone() {
		ModelEmbedding copy = new ModelEmbedding();
		copy.name = this.name;
		copy.alias = this.alias;
		copy.contextLength = this.contextLength;
		copy.types = this.types==null ? null : List.copyOf(this.types);
		
		return copy;
	}
}
