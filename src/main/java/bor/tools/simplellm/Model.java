package bor.tools.simplellm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import lombok.Builder.Default;

/**
 * Class representing a specific LLM model configuration.
 * <p>
 * This class encapsulates the properties and capabilities of an individual
 * model, including its name, supported types, and context length limitations.
 * </p>
 * <p>
 * A model can support multiple types simultaneously, allowing for versatile
 * usage across different use cases.
 * </p>
 * 
 * @see Model_Type
 * @see LLMConfig
 * @see MapModels
 * 
 * @author Alessandro Borges
 */
@Data
@AllArgsConstructor
@Builder
public class Model {

	/**
	 * The name of the model.
	 * <p>
	 * This should be the official model name as recognized by the LLM provider,
	 * for example: "gpt-4", "claude-3-sonnet", "llama-2-70b"
	 * </p>
	 */
	String name;

	/**
	 * A nickname
	 */
	String alias;

	/**
	 * List of model types that this model supports.
	 * <p>
	 * A model can support multiple types, allowing it to be used for different
	 * purposes. For example, a model might support both {@code LANGUAGE} and
	 * {@code REASONING} types.
	 * </p>
	 * 
	 * @see Model_Type
	 */
	@Default
	List<Model_Type> types = new ArrayList<>();

	/**
	 * The maximum context length supported by this model.
	 * <p>
	 * This value represents the maximum number of tokens (words, characters, or
	 * other units depending on the tokenization method) that can be processed
	 * in a single request, including both input and output tokens.
	 * </p>
	 * <p>
	 * Common values include 512, 2048, 4096, 8192, 16384, 32768, or larger depending
	 * on the model's capabilities.
	 * </p>
	 */
	@Default 
	Integer contextLength = 2048;

	public Model() {
		this.types = new ArrayList<>();
		this.contextLength = 4096;
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
	 * @see Model_Type
	 */
	public Model(String name, Integer contextLength, Model_Type... model_types) {
		this();
		this.name = name;
		this.contextLength = contextLength;
		if (model_types != null) {
			for (var mt : model_types) {
				types.add(mt);
			}
		}
	}

	/**
	 * Parameterized constructor for creating a model with specified properties, including alias.
	 * <p>
	 * This constructor initializes all model properties and populates the
	 * type list with the provided model types.
	 * </p>
	 * 
	 * @param name          the name of the model (must not be null)
	 * @param alias         a nickname or alias for the model
	 * @param contextLength the maximum context length for this model
	 * @param types         variable arguments of model types this model supports
	 * 
	 * @see Model_Type
	 */	
	public Model(String name, String alias, Integer contextLength, Model_Type... model_types) {
		this();
		this.name = name;
		this.alias = alias.toLowerCase();
		this.contextLength = contextLength;
		if (model_types != null) {
			for (var mt : model_types) {
				types.add(mt);
			}
		}
	}

	/**
	 * Get all MODEL_TYPE assigned to this model
	 * 
	 * @return
	 */
	public List<Model_Type> getTypes() { return this.types; }

	/**
	 * Add a extra Type to this model
	 * 
	 * @param newType
	 */
	@JsonIgnore
	public void addExtraType(Model_Type newType) {
		if (types.contains(newType) == false) {
			types.add(newType);
		}
	}

	/**
	 * Check if this model supports a specific type.
	 * <p>
	 * This method checks if the given {@code MODEL_TYPE} is included in the
	 * model's list of supported types.
	 * </p>
	 * 
	 * @param checkType the model type to check for support
	 * 
	 * @return true if the model supports the specified type, false otherwise
	 */
	@JsonIgnore
	public boolean isType(Model_Type checkType) {
		return types.contains(checkType);
	}

	/**
	 * Check if this model supports Reasoning tasks, such as chain-of-thought.
	 * Some models provide special reasoning capabilities, through prompts or reasoning_effort parameter.
	 * <p>
	 * This method checks if the model's types include one of:
	 * <li> {@link Model_Type#REASONING} - indicates it support reasoning through reasoning_effort or prompts 
	 * <li> {@link Model_Type#REASONING_PROMPT} - indicates it support reasoning through prompts only
	 * </p>
	 * 
	 * @see Model_Type#REASONING
	 * @see Model_Type#REASONING_PROMPT
	 * @see bor.tools.simplellm.MapParam#reasoning_effort()
	 * @see bor.tools.simplellm.MapParam#getReasoningEffort()
	 * 
	 * @return true if the model supports reasoning tasks, false otherwise
	 */	
	public boolean isTypeReasoning() {
		return types.contains(Model_Type.REASONING) || types.contains(Model_Type.REASONING_PROMPT);
	}
	
	/**
	 * Create a clone of this model instance.
	 * <p>
	 * This method creates a new {@code Model} instance with the same properties
	 * as the current instance, including name, alias, context length, and types.
	 * </p>
	 * 
	 * @return a new {@code Model} instance that is a clone of this instance
	 */
	public Model clone() {
		Model cloned = new Model();
		cloned.setName(this.name);
		cloned.setAlias(this.alias);
		cloned.setContextLength(this.contextLength);
		for(var t : this.types) {
			cloned.addExtraType(t);
		}
		return cloned;
	}

	@Override
	public String toString() {
		final int     maxLen  = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("Model [");
		if (name != null) {
			builder.append("name=");
			builder.append(name);
			builder.append(", ");
		}
		if (alias != null) {
			builder.append("alias=");
			builder.append(alias);
			builder.append(", ");
		}
		if (types != null) {
			builder.append("types=");
			builder.append(types.subList(0, Math.min(types.size(), maxLen)));
			builder.append(", ");
		}
		if (contextLength != null) {
			builder.append("contextLength=");
			builder.append(contextLength);
		}
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * String representation of the model.
	 * <p>
	 * This method returns the model's name for easy identification.
	 * </p>
	 * 
	 * @return the name of the model
	 */
	

}
