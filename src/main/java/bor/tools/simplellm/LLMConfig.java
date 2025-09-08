package bor.tools.simplellm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration class for Large Language Model (LLM) settings and model
 * definitions.
 * <p>
 * This class provides a comprehensive configuration structure for managing LLM
 * connections
 * and model specifications. It supports various model types including
 * embedding, language,
 * reasoning, coding, vision, image, and audio models.
 * </p>
 * <p>
 * The configuration includes connection parameters such as base URL and API
 * tokens,
 * as well as a collection of model definitions with their respective
 * capabilities and
 * context lengths.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * LLMConfig config = LLMConfig.builder().baseUrl("https://api.example.com").apiToken("your-api-token").build();
 * 
 * config.getModelMap().put("gpt-4", new LLMConfig.Model("gpt-4", 8192, MODEL_TYPE.LANGUAGE, MODEL_TYPE.REASONING));
 * </pre>
 * 
 * @author AlessandroBorges
 * 
 * @version 1.0
 * 
 * @since 1.0
 */
@Data
@Builder
public class LLMConfig {

	/**
	 * Enumeration defining the various types of LLM models supported.
	 * <p>
	 * Each model type represents a specific capability or use case:
	 * </p>
	 * <ul>
	 * <li>{@code EMBEDDING} - Models for generating text embeddings</li>
	 * <li>{@code LANGUAGE} - General language understanding and generation
	 * models</li>
	 * <li>{@code FAST} - Models optimized for speed and quick responses</li>
	 * <li>{@code REASONING} - Models specialized in logical reasoning and
	 * problem-solving</li>
	 * <li>{@code CODING} - Models designed for code generation and programming
	 * tasks</li>
	 * <li>{@code TEXT} - Text processing and manipulation models</li>
	 * <li>{@code VISION} - Vision and image understanding models</li>
	 * <li>{@code IMAGE} - Image generation and manipulation models</li>
	 * <li>{@code AUDIO} - Audio processing and generation models</li>
	 * </ul>
	 */
	public enum MODEL_TYPE {
			EMBEDDING, LANGUAGE, FAST, REASONING, CODING, TEXT, VISION, IMAGE, AUDIO, RESPONSES_API, BATCH, TOOLS
	}

	/**
	 * The base URL for the LLM API endpoint.
	 * <p>
	 * This should be the root URL where the LLM service is hosted,
	 * for example: "https://api.openai.com" or "https://api.anthropic.com"
	 * </p>
	 */
	private String baseUrl;

	/**
	 * The API token used for authentication with the LLM service.
	 * <p>
	 * This token should be kept secure and not exposed in logs or version control.
	 * Consider using the {@link #apiTokenEnvironment} field to reference an
	 * environment
	 * variable instead of hardcoding the token.
	 * </p>
	 */
	private String apiToken;

	/**
	 * The name of the environment variable containing the API token.
	 * <p>
	 * When specified, the application should read the API token from this
	 * environment variable instead of using the {@link #apiToken} field directly.
	 * This provides better security practices for token management.
	 * </p>
	 */
	private String apiTokenEnvironment;

	/**
	 * Additional properties for complex API configurations.
	 * <p>
	 * This map allows for storing custom configuration parameters that may be
	 * required by specific LLM providers or for advanced API features. The map
	 * uses String keys and Object values to provide maximum flexibility.
	 * </p>
	 * <p>
	 * Default value is an empty {@link LinkedHashMap}.
	 * </p>
	 */
	@Builder.Default
	private Map<String, Object> additionalProperties = new LinkedHashMap<>();

	/**
	 * Map of available models keyed by their identifier.
	 * <p>
	 * This map contains all configured models available for use. The key should
	 * be a unique identifier for the model (typically the model name), and the
	 * value
	 * is a {@link Model} object containing the model's specifications.
	 * </p>
	 * <p>
	 * Default value is an empty {@link LinkedHashMap}.
	 * </p>
	 */
	@Builder.Default
	private Map<String, Model> modelMap = new LinkedHashMap<>();

	/**
	 * Inner class representing a specific LLM model configuration.
	 * <p>
	 * This class encapsulates the properties and capabilities of an individual
	 * model, including its name, supported types, and context length limitations.
	 * </p>
	 * <p>
	 * A model can support multiple types simultaneously, allowing for versatile
	 * usage across different use cases.
	 * </p>
	 */
	@Data
	public static class Model {

		/**
		 * The name of the model.
		 * <p>
		 * This should be the official model name as recognized by the LLM provider,
		 * for example: "gpt-4", "claude-3-sonnet", "llama-2-70b"
		 * </p>
		 */
		String name;

		/**
		 * List of model types that this model supports.
		 * <p>
		 * A model can support multiple types, allowing it to be used for different
		 * purposes. For example, a model might support both {@code LANGUAGE} and
		 * {@code REASONING} types.
		 * </p>
		 * 
		 * @see MODEL_TYPE
		 */
		List<MODEL_TYPE> types;

		/**
		 * The maximum context length supported by this model.
		 * <p>
		 * This value represents the maximum number of tokens (words, characters, or
		 * other units depending on the tokenization method) that can be processed
		 * in a single request, including both input and output tokens.
		 * </p>
		 * <p>
		 * Common values include 4096, 8192, 16384, 32768, or larger depending
		 * on the model's capabilities.
		 * </p>
		 */
		Integer contextLength;

		/**
		 * Default constructor.
		 * <p>
		 * Initializes the type list with an initial capacity of 2 elements
		 * to optimize for the common case of models supporting 1-2 types.
		 * </p>
		 */
		public Model() {
			types = new ArrayList<>(2);
		}

		/**
		 * Parameterized constructor for creating a model with specified properties.
		 * <p>
		 * This constructor initializes all model properties and populates the
		 * type list with the provided model types.
		 * </p>
		 * 
		 * @param name          the name of the model (must not be null)
		 * @param contextLength the maximum context length for this model
		 * @param types         variable arguments of model types this model supports
		 * 
		 * @see MODEL_TYPE
		 */
		public Model(String name, Integer contextLength, MODEL_TYPE... model_types) {
			this();
			this.name = name;
			this.contextLength = contextLength;
			if (types != null) {
				for (var mt : model_types) {
					types.add(mt);
				}
			}
		}

		public List<MODEL_TYPE> getTypes() { // TODO Auto-generated method stub
			return this.types;
		}
	}
}
