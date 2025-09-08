package bor.tools.simplellm;

import java.util.List;

import bor.tools.simplellm.LLMConfig.MODEL_TYPE;
import bor.tools.simplellm.LLMConfig.Model;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Interface for implementing basic Large Language Model (LLM) services based on
 * the OpenAI API.
 * <p>
 * This interface provides a comprehensive set of methods for interacting with
 * LLM services,
 * including text completion, chat functionality, embeddings generation, and
 * text summarization.
 * All implementations should handle authentication, request formatting, and
 * response parsing
 * according to the OpenAI API specifications.
 * </p>
 *
 * @author AlessandroBorges
 * 
 * @since 1.0
 */
public interface LLMService {

	/**
	 * Retrieves the list of available models from the LLM service, as provided to
	 * LLMConfig.
	 *
	 * @return a list of model names available for use
	 * 
	 * @throws LLMException if there's an error retrieving the models
	 * 
	 * @see LLMConfig
	 * @see Model
	 */
	List<Model> models()
	            throws LLMException;

	/**
	 * Generates embeddings for the given text using the specified model.
	 * <p>
	 * Embeddings are numerical representations of text that capture semantic
	 * meaning
	 * and can be used for similarity comparisons, clustering, and other NLP tasks.
	 * </p>
	 *
	 * @param texto   the text to generate embeddings for
	 * @param model   the embedding model to use
	 * @param vecSize the desired size of the embedding vector, or null for model
	 *                default
	 * 
	 * @return an array of floats representing the text embedding
	 * 
	 * @throws LLMException if there's an error generating the embeddings
	 */
	float[] embeddings(String texto, String model, Integer vecSize)
	            throws LLMException;

	/**
	 * Performs a simple text completion using the specified system prompt and user
	 * query.
	 * <p>
	 * This method generates a single response based on the provided prompts without
	 * maintaining conversation context.
	 * </p>
	 *
	 * @param system the system prompt that defines the AI's behavior and context
	 * @param query  the user's input or question
	 * @param params additional parameters such as temperature, max_tokens, etc.
	 * 
	 * @return a CompletionResponse containing the generated text and metadata
	 * 
	 * @throws LLMException if there's an error during completion
	 */
	CompletionResponse completion(String system, String query, MapParam params)
	            throws LLMException;

	/**
	 * Performs a simple text completion using the specified system prompt and user
	 * query.
	 * <p>
	 * This method generates a single response based on the provided prompts without
	 * maintaining conversation context.
	 * </p>
	 *
	 * @param system the system prompt that defines the AI's behavior and context
	 * @param query  the user's input or question
	 * @param params additional parameters such as temperature, max_tokens, etc.
	 * 
	 * @return a CompletionResponse containing the generated text and metadata
	 * 
	 * @throws LLMException if there's an error during completion
	 */
	CompletionResponse chatCompletion(Chat chat, String query, MapParam params)
	            throws LLMException;

	/**
	 * Performs a streaming text completion using the specified system prompt and
	 * user query.
	 * <p>
	 * This method returns a stream that allows real-time processing of the response
	 * as it's being generated, useful for providing immediate feedback to users.
	 * </p>
	 * 
	 * @param stream object response stream
	 * @param system the system prompt that defines the AI's behavior and context
	 * @param query  the user's input or question
	 * @param params additional parameters such as temperature, max_tokens, etc.
	 * 
	 * @return a ResponseStream for processing the completion as it's generated
	 * 
	 * @throws LLMException if there's an error during completion
	 */
	CompletionResponse completionStream(ResponseStream stream, String system, String query, MapParam params)
	            throws LLMException;

	/**
	 * Performs a streaming chat completion within an existing chat session.
	 * <p>
	 * This method continues a conversation by adding the user's query to the
	 * existing
	 * chat context and generating a response stream.
	 * </p>
	 *
	 * @param stream object response stream
	 * @param chat   the current chat session
	 * @param query  the user's input or question
	 * @param params additional parameters such as temperature, max_tokens, etc.
	 * 
	 * @return a ResponseStream for processing the chat response as it's generated
	 * 
	 * @throws LLMException if there's an error during chat completion
	 */
	CompletionResponse chatCompletionStream(ResponseStream stream, Chat chat, String query, MapParam params)
	            throws LLMException;

	/**
	 * Counts the number of tokens in the given text using the specified
	 * tokenization model.
	 * <p>
	 * Token counting is essential for managing API costs and ensuring requests
	 * don't exceed model limits. Different models may have different tokenization
	 * schemes.
	 * </p>
	 *
	 * @param text  the text to be tokenized and counted
	 * @param model the tokenization model to use
	 * 
	 * @return the estimated number of tokens in the text
	 * 
	 * @throws LLMException if there's an error during token counting
	 */
	int tokenCount(String text, String model)
	            throws LLMException;

	/**
	 * Summarizes the provided chat using the specified summary prompt and
	 * additional parameters.
	 * <p>
	 * This method condenses a chat conversation into a more compact form while
	 * preserving the essential information and context. The summarized chat can be
	 * used to maintain conversation history within token limits.
	 * </p>
	 *
	 * @param chat          the chat conversation to be summarized
	 * @param summaryPrompt the prompt that guides the summarization process
	 * @param params        additional parameters such as max_tokens, temperature,
	 *                      reasoning, etc.
	 * 
	 * @return a compacted Chat object containing the summarized conversation
	 * 
	 * @throws LLMException if there's an error during chat summarization
	 */
	Chat sumarizeChat(Chat chat, String summaryPrompt, MapParam params)
	            throws LLMException;

	/**
	 * Summarizes the provided text using the specified summary prompt and
	 * additional parameters.
	 * <p>
	 * This method condenses lengthy text into a shorter version while maintaining
	 * the key information and main points. Useful for creating abstracts, executive
	 * summaries, or reducing content to fit within token limits.
	 * </p>
	 *
	 * @param text          the text to be summarized
	 * @param summaryPrompt the prompt that guides the summarization process
	 * @param params        additional parameters such as max_tokens, temperature,
	 *                      reasoning, etc.
	 * 
	 * @return the summarized text
	 * 
	 * @throws LLMException if there's an error during text summarization
	 */
	String sumarizeText(String text, String summaryPrompt, MapParam params)
	            throws LLMException;

	/**
	 * Get the LLM configuration .
	 * 
	 * @return
	 */
	LLMConfig getLLMConfig();

	/**
	 * Checks if the specified model supports the given type of operation.
	 * <p>
	 * This method verifies whether a model is capable of performing tasks such as
	 * completion, chat, or embeddings based on its configured types.
	 * </p>
	 *
	 * @param modelName the name of the model to check
	 * @param type      the type of operation to verify (e.g., COMPLETION, CHAT,
	 *                  EMBEDDINGS)
	 * 
	 * @return true if the model supports the specified type, false otherwise
	 * 
	 * @see LLMConfig.MODEL_TYPE
	 */
	default boolean isModelType(String modelName, MODEL_TYPE type) {
		LLMConfig config = getLLMConfig();
		Model     model  = config.getModelMap().get(modelName);
		if (model != null) {
			return model.getTypes().contains(type);
		}
		return false;
	}

}
