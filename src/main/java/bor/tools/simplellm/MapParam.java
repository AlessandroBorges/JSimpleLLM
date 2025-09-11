package bor.tools.simplellm;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

/**
 * A specialized map to manage parameters for LLM requests.
 * <p>
 * This class extends LinkedHashMap to maintain the order of insertion and
 * provides typed getters and setters for common parameters like temperature,
 * model, and max tokens.
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 */
public class MapParam extends LinkedHashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static final String TEMPERATURE = "temperature";
	static final String MODEL = "model";
	static final String MAX_CONTENT_TOKENS = "max_content_tokens";
	static final String MAX_TOKENS = "max_tokens";
	static final String DIMENSIONS = "dimensions";

	public MapParam() {
		super();
	}

	public MapParam(Map<String, Object> map) {
		super(map);
	}

	public MapParam(MapParam other) {
		super(other);
	}

	// Getters/setters tipados para parâmetros comuns
	public MapParam temperature(Float value) {
		put(TEMPERATURE, value);
		return this;
	}

	public MapParam maxTokens(Integer value) {
		put(MAX_TOKENS, value);
		return this;
	}
	
	

	/**
	 * Sets the model to be used. This is a required parameter for most LLMs.
	 * 
	 * @param modelName the model name
	 * 
	 * @return this
	 */
	public MapParam model(String modelName) {
		put(MODEL, modelName);
		return this;
	}

	/**
	 * Sets the model to be used. This is a required parameter for most LLMs.
	 * 
	 * @param model the model object
	 * 
	 * @return this
	 */
	public MapParam model(Model model) {
		put(MODEL, model);
		return this;
	}
	/**
	 * Returns temperature if set, otherwise null.
	 * 
	 * @return
	 */
	public Double getTemperature() { 
		return (Double) get(TEMPERATURE); 
	}

	/**
	 * Sets the dimension for vector embeddings.
	 * 
	 * @param value the embedding dimension
	 */
	public void dimension(Integer value) {
		put(DIMENSIONS, value);
	}
	
	/**
	 * Returns dimension if set, otherwise null.
	 * 
	 * @return dimension
	 */
	public Integer getDimension() { 
		return (Integer) super.get(DIMENSIONS); 
	}
	
	/**
	 * Returns max_tokens if set, otherwise max_content_tokens.
	 * 
	 * @return max_tokens or max_content_tokens
	 */
	public Integer getMaxTokens() {
		Object v = get(MAX_TOKENS) == null ? get(MAX_CONTENT_TOKENS) : get(MAX_TOKENS);
		return (Integer) v;

	}

	/**
	 * Returns model if set, otherwise null.
	 * 
	 * @return model
	 */
	public Object getModel() { 
		return get(MODEL); 
	}

	/**
	 * Replace a key with a new key, preserving the value.
	 * 
	 * @param oldKey the key to be replaced
	 * @param newKey the new key to replace with
	 * 
	 * @return value associated with the old key, or null if old key not found
	 */
	public Object replaceKeys(String oldKey, String newKey) {
		if (containsKey(oldKey)) {
			Object v = remove(oldKey);
			return put(newKey, v);
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb    = new StringBuilder("{");
		boolean       first = true;
		for (java.util.Map.Entry<String, Object> entry : entrySet()) {
			if (!first) {
				sb.append(",\n");
			}
			first = false;
			sb.append(entry.getKey()).append(": ");
			Object value = entry.getValue();
			if (value instanceof String) {
				sb.append("\"").append(value).append("\"");
			} else {
				sb.append(value);
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Returns an Integer value for the given key, or null if the key does not
	 * exist or is not an Integer.
	 * 
	 * @param string the key to look up
	 * 
	 * @return the Integer value associated with the key, or null
	 */
	public Integer getInteger(String string) {
		Object v = get(string);
		if (v instanceof Integer) {
			return (Integer) v;
		}
		
		return null;
	}

}
