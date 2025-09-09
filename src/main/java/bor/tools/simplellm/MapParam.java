package bor.tools.simplellm;

import java.util.LinkedHashMap;

import lombok.Data;

@Data
public class MapParam extends LinkedHashMap<String, Object> {

	public MapParam() {
		super();
	}

	public MapParam(MapParam other) {
		super(other);
	}

	// Getters/setters tipados para parâmetros comuns
	public MapParam temperature(Double value) {
		put("temperature", value);
		return this;
	}

	public MapParam maxTokens(Integer value) {
		put("max_tokens", value);
		return this;
	}

	/**
	 * Sets the model to be used. This is a required parameter for most LLMs.
	 * 
	 * @param value the model name
	 * 
	 * @return this
	 */
	public MapParam model(String value) {
		put("model", value);
		return this;
	}

	/**
	 * Returns temperature if set, otherwise null.
	 * 
	 * @return
	 */
	public Double getTemperature() { return (Double) get("temperature"); }

	/**
	 * Returns max_tokens if set, otherwise max_content_tokens.
	 * 
	 * @return max_tokens or max_content_tokens
	 */
	public Integer getMaxTokens() {
		Object v = get("max_tokens") == null ? get("max_content_tokens") : get("max_tokens");
		return (Integer) v;

	}

	/**
	 * Returns model if set, otherwise null.
	 * 
	 * @return model
	 */
	public String getModel() { return (String) get("model"); }

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

}
