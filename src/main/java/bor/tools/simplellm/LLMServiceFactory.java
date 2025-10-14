package bor.tools.simplellm;

import bor.tools.simplellm.impl.LMStudioLLMService;
import bor.tools.simplellm.impl.OllamaLLMService;
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
 * <li>Ollama local server - OllamaLLMService</li>
 * <li>LM Studio local server - LMStudioLLMService</li>
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
	 * LLM Service providers
	 */
	public enum SERVICE_PROVIDER{
		OPENAI,
		ANTHROPIC,
		LM_STUDIO,		
		OLLAMA,
		TOGETHER;
		
		public static SERVICE_PROVIDER fromString(String provider) {
			if (provider == null) {
				return null;
			}
			switch (provider.trim().toUpperCase()) {
			case "OPENAI":
				return OPENAI;
			case "ANTHROPIC":
				return ANTHROPIC;
			case "LM_STUDIO":
			case "LMSTUDIO":
				return LM_STUDIO;
			case "OLLAMA":
				return OLLAMA;
			case "TOGETHER":
				return TOGETHER;
			default:
				throw new IllegalArgumentException("Unsupported LLM service provider: " + provider);
			}
		}
	}//enum
	
	/**
	 * Create an instance of LLM service based on the specified provider and configuration.
	 * @param provider  - the LLM service provider to use
	 * @param config - (optional) the LLM configuration containing API settings, model definitions, API authentication details, and service endpoints
     *
	 * @return a new {@link LLMService} instance configured for the specified provider
	 */
	public static LLMService createLLMService(SERVICE_PROVIDER provider, LLMConfig config) {
		if (provider == null) {
			throw new IllegalArgumentException("Provider must not be null");
		}
		
		switch (provider) {
		case OPENAI:
			return createOpenAI(config);
		case OLLAMA:
			if (config == null) {
				return createOllama();
			} else {
				return createOllama(config);
			}
		case LM_STUDIO:
			if (config == null) {
				return createLMStudio();
			} else {
				return createLMStudio(config);
			}
		case ANTHROPIC:
			return createOpenAI(config); // Anthropic API is OpenAI-compatible
			//throw new UnsupportedOperationException("Anthropic LLM service not yet implemented");
		case TOGETHER:
			return createOpenAI(config); // Together API is OpenAI-compatible
			//throw new UnsupportedOperationException("Together LLM service not yet implemented");
		default:
			return createOpenAI(config); // Default to OpenAI-compatible service
			//throw new IllegalArgumentException("Unsupported LLM service provider: " + provider);
		}
	}

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

	/**
	 * Creates an instance of Ollama local LLM service.
	 * <p>
	 * This method instantiates an Ollama LLM service implementation that can be used
	 * to interact with a local Ollama server. Ollama provides a local server that runs
	 * various open-source LLM models with an OpenAI-compatible API.
	 * </p>
	 * <p>
	 * Default configuration:
	 * <ul>
	 * <li>Base URL: http://localhost:11434/v1/</li>
	 * <li>API Key: "ollama" (or OLLAMA_API_KEY environment variable)</li>
	 * <li>Default models: phi3.5:latest, llama3.2:latest, etc.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param config the LLM configuration containing Ollama API settings, model
	 *               definitions, and service endpoints
	 * 
	 * @return a new {@link LLMService} instance configured for Ollama local server
	 * 
	 * @throws IllegalArgumentException if the provided config is null or contains
	 *                                  invalid configuration parameters
	 * 
	 * @see LLMService
	 * @see LLMConfig
	 * @see OllamaLLMService
	 */
	public static LLMService createOllama(LLMConfig config) {
		return new OllamaLLMService(config);
	}

	/**
	 * Creates an instance of Ollama local LLM service with default configuration.
	 * <p>
	 * This is a convenience method that creates an Ollama service with pre-configured
	 * settings suitable for most local Ollama installations.
	 * </p>
	 * 
	 * @return a new {@link LLMService} instance with default Ollama configuration
	 * 
	 * @see #createOllama(LLMConfig)
	 * @see OllamaLLMService#getDefaultOllamaLLMConfig()
	 */
	public static LLMService createOllama() {
		return new OllamaLLMService();
	}

	/**
	 * Creates an instance of LM Studio local LLM service.
	 * <p>
	 * This method instantiates an LM Studio LLM service implementation that can be used
	 * to interact with a local LM Studio server. LM Studio provides a user-friendly
	 * interface for running various open-source LLM models with an OpenAI-compatible API.
	 * </p>
	 * <p>
	 * Default configuration:
	 * <ul>
	 * <li>Base URL: http://localhost:1234/v1/</li>
	 * <li>API Key: "lm-studio" (or LMSTUDIO_API_KEY environment variable)</li>
	 * <li>Models depend on what user has loaded in LM Studio UI</li>
	 * </ul>
	 * </p>
	 * 
	 * @param config the LLM configuration containing LM Studio API settings, model
	 *               definitions, and service endpoints
	 * 
	 * @return a new {@link LLMService} instance configured for LM Studio local server
	 * 
	 * @throws IllegalArgumentException if the provided config is null or contains
	 *                                  invalid configuration parameters
	 * 
	 * @see LLMService
	 * @see LLMConfig
	 * @see LMStudioLLMService
	 */
	public static LLMService createLMStudio(LLMConfig config) {
		return new LMStudioLLMService(config);
	}

	/**
	 * Creates an instance of LM Studio local LLM service with default configuration.
	 * <p>
	 * This is a convenience method that creates an LM Studio service with pre-configured
	 * settings suitable for most local LM Studio installations.
	 * </p>
	 * 
	 * @return a new {@link LLMService} instance with default LM Studio configuration
	 * 
	 * @see #createLMStudio(LLMConfig)
	 * @see LMStudioLLMService#getDefaultLLMConfig()
	 */
	public static LLMService createLMStudio() {
		return new LMStudioLLMService();
	}

	// Futuras implementações: createClaude(), createGemini(), etc.
}
