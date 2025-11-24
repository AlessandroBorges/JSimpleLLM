package bor.tools.simplellm.abstraction;

import java.util.List;

import bor.tools.simplellm.Embeddings_Op;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Interface for RAG-related operations including embeddings, text processing,
 * tokenization, and content classification.
 * 
 * @author AlessandroBorges
 * @since 1.0
 */
public interface IEmbeddingOperations {
    
    /**
     * Generates embeddings for the given text using the specified model.
     * @param texto the text to generate embeddings for
     * @param model the embedding model to use
     * @return an array of floats representing the text embedding
     * 
     * @throws LLMException if there's an error generating the embeddings
     */
    float[] embeddings(String texto, Model model) throws LLMException;
    
    /**
     * Generates embeddings for the given text using the specified model and operation.
     * @param op - Embedding operations 
     * @param texto - text to embed
     * @param params additional parameters
     * 
     * @return an normalized array of floats representing the text embedding
     * @throws LLMException
     */
    float[] embeddings(Embeddings_Op op, String texto, MapParam params) throws LLMException;
    
    /**
     * Generates embeddings for an array of texts.
     * @param op - Embedding operations 
     * @param texto - array of texts to embed
     * @param params additional parameters
     * 
     * @return a list of normalized float arrays representing the text embeddings
     * @throws LLMException
     */
    List<float[]> embeddings(Embeddings_Op op, String[] texto, MapParam params) throws LLMException;
    
    /**
     * Counts the number of tokens in the given text.
     * @param text the text to be tokenized and counted
     * @param model the tokenization model to use
     * 
     * @return the estimated number of tokens in the text
     * @throws LLMException if there's an error during token counting
     */
    int tokenCount(String text, String model) throws LLMException;
    
    /**
     * Reranks candidate texts based on their relevance to a given subject/query.
     * @param subject the reference text or query to rank candidates against
     * @param candidates array of candidate texts to be ranked
     * @param params additional parameters including model name, top_k, etc.
     * 
     * @return array of ranking scores (float values) corresponding to each candidate,
     *         where higher scores indicate better relevance to the subject
     * @throws LLMException if there's an error during reranking
     */
    double[] rerank(String subject, String[] candidates, MapParam params) throws LLMException;
    
    /** 
     * Classifies the given markdown content into one of the provided categories.
     * @param model the model to use for classification
     * @param conteudoMarkdown the markdown content to classify
     * @param allNames array of category names to choose from
     * @param allNamesAndDescriptions array of category names with descriptions
     * 
     * @return the name of the category that best fits the content
     * @throws LLMException if there's an error during classification
     */
    String classifyContent(Model model, 
                           String conteudoMarkdown, 
                           String[] allNames, 
                           String[] allNamesAndDescriptions) throws LLMException;
}