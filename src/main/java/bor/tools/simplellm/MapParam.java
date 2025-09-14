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

	private static final String STREAM = "stream";

	public static final String REASONING_EFFORT = "reasoning_effort";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String TEMPERATURE        = "temperature";
	public static final String MODEL              = "model";
	public static final String MAX_CONTENT_TOKENS = "max_content_tokens";
	public static final String MAX_TOKENS         = "max_tokens";
	public static final String DIMENSIONS         = "dimensions";

	public static final String TOP_P = "top_p";
	public static final String MIN_P = "min_p";
	public static final String REPEAT_PENALTY  = "repeat_penalty";
	

	/**
	 * Default constructor initializing an empty parameter map.
	 */
	public MapParam() {
		super();
	}

	/**
	 * Constructor initializing the parameter map with the provided map.
	 * 
	 * @param map the initial parameters
	 */
	public MapParam(Map<String, Object> map) {
		super(map);
	}

	/**
	 * Copy constructor creating a new MapParam from another MapParam.
	 * 
	 * @param other the MapParam to copy
	 */
	public MapParam(MapParam other) {
		super(other);
	}

	/**
	 * set temperature
	 * 
	 * @param value temperature value
	 * 
	 * @return this object
	 */
	public MapParam temperature(Float value) {
		// Temperature must be between 0.0 and 2.0
		if (value == null)
			super.remove(TEMPERATURE);
		else {
			if (value != null && (value < 0.0f || value > 2.0f))
				throw new IllegalArgumentException("Temperature must be between 0.0 and 2.0");
			put(TEMPERATURE, value);
		}
		return this;
	}

	/**
	 * set max_content_tokens
	 * 
	 * @param value the max tokens you want to set
	 * 
	 * @return this object
	 */
	public MapParam maxTokens(Integer value) {
		if (value == null)
			super.remove(MAX_TOKENS);
		else {
			if (value <= 0)
				throw new IllegalArgumentException("max_tokens must be greater than 0");
			put(MAX_TOKENS, value);
		}
		return this;
	}

	/**
	 * Set reasoning effort level.
	 * 
	 * @param value the reasoning effort level
	 * 
	 * @return this object
	 */
	public MapParam reasoningEffort(Reasoning_Effort value) {
		if (value == null)
			super.remove(REASONING_EFFORT);
		else
			put(REASONING_EFFORT, value);
		return this;
	}

	/**
	 * Sets the dimension for vector embeddings.
	 * 
	 * @param value the embedding dimension
	 * @return this object
	 */
	public MapParam dimension(Integer value) {
		if (value == null)
			super.remove(DIMENSIONS);
		else
			put(DIMENSIONS, value);
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
		if (modelName == null)
			super.remove(MODEL);
		else
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
		if (model == null)
			super.remove(MODEL);
		else
			put(MODEL, model);
		return this;
	}

	/**
	 * Returns temperature if set, otherwise null.
	 * 
	 * @return the temperature value
	 */
	public Float getTemperature() {	
		return getAsFloat(TEMPERATURE);
	}

	/**
	 * Returns reasoning effort if set, otherwise null.
	 * 
	 * @return reasoning effort
	 */
	public Reasoning_Effort getReasoningEffort() { 
		return (Reasoning_Effort) get(REASONING_EFFORT); 
	}

	/**
	 * Returns dimension if set, otherwise null.
	 * 
	 * @return dimension
	 */
	public Integer getDimension() { return getAsInteger(DIMENSIONS); }

	/**
	 * Returns max_tokens if set, otherwise max_content_tokens.
	 * 
	 * @return max_tokens or max_content_tokens
	 */
	public Integer getMaxTokens() {		
		Integer temp = getAsInteger(MAX_TOKENS);		
		temp = temp == null ?  getAsInteger	(MAX_CONTENT_TOKENS) : temp;
		return temp;
	}

	/**
	 * Returns model if set, otherwise null.
	 * 
	 * @return model
	 */
	public Object getModel() { return get(MODEL); }

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
	public Integer getAsInteger(String string) {
		Object v = get(string);
		if (v instanceof Integer) {
			return (Integer) v;
		} else if (v instanceof Number) {
			return ((Number) v).intValue();
		} else if (v instanceof String) {
			try {
				return Integer.parseInt((String) v);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Sets the top_p parameter for nucleus sampling.
	 * <p>
	 * The value must be between 0.0 and 1.0. If null is provided, the parameter
	 * is removed from the map.
	 * </p>
	 * 
	 * @param topP the top_p value to set
	 */
	public MapParam top_p(Float topP) { // TODO Auto-generated method stub
		if (topP == null)
			super.remove(TOP_P);
		else {
			if (topP != null && (topP < 0.0f || topP > 1.0f))
				throw new IllegalArgumentException("top_p must be between 0.0 and 1.0");
			super.put(TOP_P, topP);
		}	
		return this;
	}
	
	/**
	 * Returns top_p if set, otherwise null.
	 * 
	 * @return the top_p value
	 */
	public Float getTop_p() { // TODO Auto-generated method stub
		return getAsFloat(TOP_P);		
	}
	
	/**
	 * Sets the min_p parameter for minimum probability sampling.
	 * <p>
	 * The value must be between 0.0 and 1.0. If null is provided, the parameter
	 * is removed from the map.
	 * </p>
	 * 
	 * @param minP the min_p value to set
	 */
	public MapParam min_p(Float minP) { // TODO Auto-generated method stub
		if (minP == null)
			super.remove(MIN_P);
		else {
			if (minP != null && (minP < 0.0f || minP > 1.0f))
				throw new IllegalArgumentException("min_p must be between 0.0 and 1.0");
			super.put(MIN_P, minP);
		}		
		return this;
	}
	
	/**
	 * Returns min_p if set, otherwise null.
	 * 
	 * @return the min_p value
	 */
	public Float getMin_p() { // TODO Auto-generated method stub
		return getAsFloat(MIN_P);		
	}
	
	/**
	 * Returns repeat_penalty if set, otherwise null.
	 * 
	 * @return the repeat_penalty value
	 */
	public Float getRepeat_penalty() { // TODO Auto-generated method stub
		return getAsFloat(REPEAT_PENALTY);		
	}
	
	/**
	 * Sets the repeat penalty parameter to reduce repetitive responses.
	 * <p>
	 * The value must be between 0.0 and 2.0. If null is provided, the parameter
	 * is removed from the map.
	 * </p>
	 * 
	 * @param penalty the repeat_penalty value to set
	 */
	public MapParam repeat_penalty(Float penalty) { // TODO Auto-generated method stub
		if (penalty == null)
			super.remove(REPEAT_PENALTY);
		else {
			if (penalty != null && (penalty < 0.0f || penalty > 2.0f))
				throw new IllegalArgumentException("repeat_penalty must be between 0.0 and 2.0");
			super.put(REPEAT_PENALTY, penalty);
		}
		return this;
	}

	/**
	 * Sets the stream parameter to enable or disable streaming responses.
	 * <p>
	 * If true, the LLM will return partial results as they are generated.
	 * If false, the LLM will return the complete response at once.
	 * </p>
	 * 
	 * @param b true to enable streaming, false to disable
	 */
	public MapParam stream(Boolean b) {
		super.put(STREAM, b);	
		return this;
	}
	
	/**
	 * Returns the stream parameter value if set, otherwise null.
	 * 
	 * @return true if streaming is enabled, false if disabled, or null if not set
	 */
	public Boolean isStream() {
		Object v = get(STREAM);
		if (v instanceof Boolean) {
			return (Boolean) v;
		}
		return null;
	}
	
	/**
	 * Get a value as a Float.
	 * Return null if the key does not exist or cannot be converted to Float.
	 * @param key the key to look up
	 * @return the Float value, or null
	 */
	public Float getAsFloat(String key) {
		Object v = get(key);
		if (v == null)
			return null;
		if (v instanceof Float) {
			return (Float) v;
		} else if (v instanceof Double) {
			return ((Double) v).floatValue();
		} else if (v instanceof Number) {
			return ((Number) v).floatValue();
		} else if (v instanceof String) {
			try {
				return Float.parseFloat((String) v);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

}