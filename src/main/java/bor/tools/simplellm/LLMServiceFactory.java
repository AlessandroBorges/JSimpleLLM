package bor.tools.simplellm;

import bor.tools.simplellm.impl.OpenAILLMService;

/**
 * Factory class for creating instances of Large Language Model (LLM) service
 * implementations.
 * <p>
 * This factory provides a centralized way to instantiate different LLM service
 * providers
 * while abstracting the concrete implementation details from the client code.
 * It follows
 * the Factory design pattern to ensure consistent object creation and easy
 * extensibility
 * for future LLM service providers.
 * </p>
 * <p>
 * Currently supports:
 * <ul>
 * <li>OpenAI API-compatible services - OpenAILLMService</li>
 * </ul>
 * </p>
 * <p>
 * Future implementations may include support for Claude, Gemini, and other LLM
 * providers.
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 * 
 * @see LLMService
 * @see LLMConfig
 * @see OpenAILLMService
 */
public class LLMServiceFactory {

	/**
	 * Creates an instance of OpenAI-compatible LLM service.
	 * <p>
	 * This method instantiates an OpenAI LLM service implementation that can be
	 * used
	 * to interact with OpenAI's API or any OpenAI-compatible API endpoint. The
	 * service
	 * supports all standard LLM operations including text completion, chat
	 * completion,
	 * embeddings generation, and text summarization.
	 * </p>
	 * 
	 * @param config the LLM configuration containing API settings, model
	 *               definitions,
	 *               authentication details, and service endpoints
	 * 
	 * @return a new {@link LLMService} instance configured for OpenAI API
	 *         compatibility
	 * 
	 * @throws IllegalArgumentException if the provided config is null or contains
	 *                                  invalid configuration parameters
	 * 
	 * @see LLMService
	 * @see LLMConfig
	 * @see OpenAILLMService
	 */
	public static LLMService createOpenAI(LLMConfig config) {
		return new OpenAILLMService(config);
	}

	// Futuras implementações: createClaude(), createGemini(), etc.
}
