package bor.tools.simplellm.abstraction;

import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.*;

/**
 * Interface for LLM completion operations including text completions,
 * chat completions, streaming operations, and image generation.
 * 
 * @author AlessandroBorges
 * @since 1.0
 */
public interface ICompletionOperations {
    
    /**
     * Performs a simple text completion using the specified system prompt and user query.
     * @param system the system prompt that defines the AI's behavior and context
     * @param query the user's input or question
     * @param params additional parameters such as temperature, max_tokens, etc.
     * @return a CompletionResponse containing the generated text and metadata
     * @throws LLMException if there's an error during completion
     */
    CompletionResponse completion(String system, String query, MapParam params) throws LLMException;
    
    /**
     * Performs a chat completion within an existing chat session.
     * @param chat the current chat session
     * @param query the user's input or question
     * @param params additional parameters
     * @return a CompletionResponse containing the generated text and metadata
     * @throws LLMException if there's an error during completion
     */
    CompletionResponse chatCompletion(Chat chat, String query, MapParam params) throws LLMException;
    
    /**
     * Performs a streaming text completion.
     * @param stream object response stream
     * @param system the system prompt
     * @param query the user's input or question
     * @param params additional parameters
     * @return a ResponseStream for processing the completion as it's generated
     * @throws LLMException if there's an error during completion
     */
    CompletionResponse completionStream(ResponseStream stream, String system, String query, MapParam params)
            throws LLMException;
    
    /**
     * Performs a streaming chat completion within an existing chat session.
     * @param stream object response stream
     * @param chat the current chat session
     * @param query the user's input or question
     * @param params additional parameters
     * @return a ResponseStream for processing the chat response as it's generated
     * @throws LLMException if there's an error during chat completion
     */
    CompletionResponse chatCompletionStream(ResponseStream stream, Chat chat, String query, MapParam params)
            throws LLMException;
    
    /**
     * Generates one or more images from a text prompt.
     * @param prompt the text description of the desired image(s)
     * @param params additional parameters such as size, quality, number of images, etc.
     * @return CompletionResponse containing the generated image(s)
     * @throws LLMException if there's an error during image generation
     */
    CompletionResponse generateImage(String prompt, MapParam params) throws LLMException;
    
    /**
     * Edits an existing image based on a text prompt and an optional mask.
     * @param originalImage the original image data as byte array
     * @param prompt the text description of the desired edit
     * @param maskImage optional mask image as byte array
     * @param params additional parameters
     * @return CompletionResponse containing the edited image(s)
     * @throws LLMException if there's an error during image editing
     */
    CompletionResponse editImage(byte[] originalImage, String prompt, byte[] maskImage, MapParam params)
            throws LLMException;
    
    /**
     * Creates variations of an existing image.
     * @param originalImage the original image data as byte array
     * @param params additional parameters
     * @return CompletionResponse containing the image variation(s)
     * @throws LLMException if there's an error during image variation creation
     */
    CompletionResponse createImageVariation(byte[] originalImage, MapParam params) throws LLMException;
    
    /**
     * Summarizes the provided chat using the specified summary prompt.
     * @param chat the chat conversation to be summarized
     * @param summaryPrompt the prompt that guides the summarization process
     * @param params additional parameters
     * @return a compacted Chat object containing the summarized conversation
     * @throws LLMException if there's an error during chat summarization
     */
    Chat sumarizeChat(Chat chat, String summaryPrompt, MapParam params) throws LLMException;
    
    /**
     * Summarizes the provided text using the specified summary prompt.
     * @param text the text to be summarized
     * @param summaryPrompt the prompt that guides the summarization process
     * @param params additional parameters
     * @return the summarized text
     * @throws LLMException if there's an error during text summarization
     */
    String sumarizeText(String text, String summaryPrompt, MapParam params) throws LLMException;
}