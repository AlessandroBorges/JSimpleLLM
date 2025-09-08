package bor.tools.simplellm;

import java.util.LinkedHashMap;

import lombok.Data;

@Data
public class MapParam extends LinkedHashMap<String, Object> {

	public MapParam() {
		super();
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

	public MapParam model(String value) {
		put("model", value);
		return this;
	}

	public Double getTemperature() { return (Double) get("temperature"); }

	public Integer getMaxTokens() { return (Integer) get("max_tokens"); }

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
