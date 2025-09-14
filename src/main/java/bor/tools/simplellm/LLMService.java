package bor.tools.simplellm;

import java.util.List;

import bor.tools.simplellm.ModelEmbedding.Emb_Operation;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.ContentWrapper;
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
	List<Model> models() throws LLMException;
	
	default List<String> modelNames() throws LLMException {
		return models().stream().map(m -> m.getName()).toList();
	}

	
	/**
	 * Generates embeddings for the given text using the specified model.
	 * <p>
	 * Embeddings are numerical representations of text that capture semantic
	 * meaning
	 * and can be used for similarity comparisons, clustering, and other NLP tasks.
	 * </p>
	 *
	 * @param texto the text to generate embeddings for
	 * @param model the embedding model to use
	 * 
	 * @return an array of floats representing the text embedding
	 * 
	 * @throws LLMException if there's an error generating the embeddings
	 */
	default float[] embeddings(String texto, Model model) throws LLMException {
		MapParam params = new MapParam();
		params.put("model", model);		
		if (model != null && model.getTypes().contains(Model_Type.EMBEDDING)) {
			return embeddings(Emb_Operation.DEFAULT, texto, params);
		} else {
			throw new LLMException("Model " + (model != null ? model.getName() : "null") + " is not an EMBEDDING model");
		}		
	}

	/**
	 * Generates embeddings for the given text using the specified model and
	 * operation.
	 * 
	 * @param op - Embedding operations 
	 * @param texto - text to embed
	 * @param params additional parameters such as model name, vector size, etc.
	 * 
	 * @return an normalized array of floats representing the text embedding
	 * 
	 * @throws LLMException
	 */
	float[] embeddings(Emb_Operation op, String texto, MapParam params) throws LLMException;

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
	CompletionResponse completion(String system, String query, MapParam params) throws LLMException;

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
	CompletionResponse chatCompletion(Chat chat, String query, MapParam params) throws LLMException;

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

	// ================== IMAGE GENERATION METHODS ==================

	/**
	 * Generates one or more images from a text prompt using an image generation
	 * model.
	 * <p>
	 * This method creates images based on descriptive text input. The generated
	 * images
	 * can be returned as URLs or base64-encoded data, depending on the response
	 * format
	 * specified in the parameters.
	 * </p>
	 * <p>
	 * Common parameters include:
	 * <ul>
	 * <li>{@code model} - Image generation model (e.g., "dall-e-3",
	 * "dall-e-2")</li>
	 * <li>{@code size} - Image dimensions (e.g., "1024x1024", "512x512")</li>
	 * <li>{@code quality} - Image quality ("standard", "hd")</li>
	 * <li>{@code n} - Number of images to generate (1-10)</li>
	 * <li>{@code response_format} - Return format ("url", "b64_json")</li>
	 * <li>{@code style} - Image style ("vivid", "natural")</li>
	 * </ul>
	 * </p>
	 *
	 * @param prompt the text description of the desired image(s)
	 * @param params additional parameters such as size, quality, number of images,
	 *               etc.
	 * 
	 * @return CompletionResponse containing the generated image(s) as
	 *         ContentWrapper.ImageContent
	 * 
	 * @throws LLMException if the model doesn't support image generation or there's
	 *                      an API error
	 * 
	 * @see ContentWrapper.ImageContent
	 * @see Model_Type#IMAGE
	 */
	CompletionResponse generateImage(String prompt, MapParam params) throws LLMException;

	/**
	 * Edits an existing image based on a text prompt and an optional mask.
	 * <p>
	 * This method modifies parts of an existing image according to the text
	 * description.
	 * A mask can be provided to specify which areas should be edited (transparent
	 * areas
	 * in the mask will be edited, opaque areas will be preserved).
	 * </p>
	 * <p>
	 * Requirements:
	 * <ul>
	 * <li>Original image must be PNG format, less than 4MB, and square</li>
	 * <li>Mask (if provided) must be PNG format with transparency</li>
	 * <li>Both images must be the same dimensions</li>
	 * </ul>
	 * </p>
	 *
	 * @param originalImage the original image data as byte array (PNG format)
	 * @param prompt        the text description of the desired edit
	 * @param maskImage     optional mask image as byte array (PNG with
	 *                      transparency)
	 * @param params        additional parameters such as size, number of images,
	 *                      etc.
	 * 
	 * @return CompletionResponse containing the edited image(s) as
	 *         ContentWrapper.ImageContent
	 * 
	 * @throws LLMException if the model doesn't support image editing or there's an
	 *                      API error
	 * 
	 * @see ContentWrapper.ImageContent
	 */
	CompletionResponse editImage(byte[] originalImage, String prompt, byte[] maskImage, MapParam params)
	            throws LLMException;

	/**
	 * Creates variations of an existing image.
	 * <p>
	 * This method generates new images that are similar to the provided original
	 * image
	 * but with variations in style, composition, or other aspects. No text prompt
	 * is
	 * required - variations are based solely on the visual content of the original.
	 * </p>
	 * <p>
	 * Requirements:
	 * <ul>
	 * <li>Image must be PNG format, less than 4MB, and square</li>
	 * <li>Supported sizes depend on the model (typically 256x256, 512x512,
	 * 1024x1024)</li>
	 * </ul>
	 * </p>
	 *
	 * @param originalImage the original image data as byte array (PNG format)
	 * @param params        additional parameters such as size, number of
	 *                      variations, etc.
	 * 
	 * @return CompletionResponse containing the image variation(s) as
	 *         ContentWrapper.ImageContent
	 * 
	 * @throws LLMException if the model doesn't support image variations or there's
	 *                      an API error
	 * 
	 * @see ContentWrapper.ImageContent
	 */
	CompletionResponse createImageVariation(byte[] originalImage, MapParam params) throws LLMException;

	// ================== TOKEN AND TEXT METHODS ==================

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
	int tokenCount(String text, String model) throws LLMException;

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
	Chat sumarizeChat(Chat chat, String summaryPrompt, MapParam params) throws LLMException;

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
	String sumarizeText(String text, String summaryPrompt, MapParam params) throws LLMException;

	/**
	 * Get the LLM configuration .
	 * 
	 * @return
	 */
	LLMConfig getLLMConfig();

	

	String getDefaultModelName();

	/**
	 * Finds the model name from the provided parameters.
	 * 
	 * @param params
	 * 
	 * @return
	 */
	default String findModel(MapParam params) {
		if (params != null && params.containsKey("model")) {
			Object modelObj = params.get("model");
			if (modelObj != null) {
				String model = modelObj.toString();
				if (model != null && !model.trim().isEmpty()) {
					return model;
				}
			}
		}
		return getDefaultModelName();
	}

	/**
	 * Finds the most suitable model that matches all specified types.
	 * <p>
	 * This method iterates through the available models and selects the one that
	 * best fits the requested capabilities, such as completion, chat, or
	 * embeddings.
	 * If multiple models match, the one with the highest number of matching types
	 * is returned.
	 * </p>
	 *
	 * @param types variable arguments of model types to match (e.g., COMPLETION,
	 *              CHAT, EMBEDDINGS)
	 * 
	 * @return the Model that best matches the specified types, or null if no model
	 *         matches
	 * 
	 * @see Model_Type
	 * @see Model
	 * @see Model_Type
	 * @see LLMConfig
	 */
	default Model findModel(Model_Type... types) {
		MapModels models   = getLLMConfig().getModelMap();
		Model     selected = null;
		int       maxNota  = 0;
		for (var model : models.values()) {
			int nota = 0;
			for (var type : types) {
				if (isModelType(model, type)) {
					nota++;
				}
			}
			if (nota > maxNota) {
				maxNota = nota;
				selected = model;
			}
		}
		return selected;
	}

	/**
	 * Checks if the specified model supports the given type of operation.
	 * <p>
	 * This method verifies whether a model is capable of performing tasks such as
	 * completion, chat, or embeddings based on its configured types.
	 * </p>
	 *
	 * @param model - Model object to check
	 * @param type  - the type of operation to verify (e.g., COMPLETION, CHAT,
	 *              EMBEDDINGS)
	 * 
	 * @return true if the model supports the specified type, false otherwise
	 * 
	 * @see Model_Type
	 * @see Model
	 * @see Model_Type
	 * @see LLMConfig
	 */
	default boolean isModelType(Model model, Model_Type type) {
		if (model != null && type != null) {
			return model.getTypes().contains(type);
		}
		return false;
	}
	
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
	 * @see Model_Type
	 */
	default boolean isModelType(String modelName, Model_Type type) {
		LLMConfig config = getLLMConfig();
		Model     model  = config.getModel(modelName);
		return isModelType(model, type);
	}
	

}
