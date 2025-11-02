package bor.tools.simplellm;

import java.util.LinkedHashMap;
import java.util.Map;

import bor.tools.simplellm.websearch.SearchContextSize;
import bor.tools.simplellm.websearch.SearchMode;

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
	public static final String MODEL_OBJ              = "model_obj";
	public static final String MAX_CONTENT_TOKENS = "max_content_tokens";
	public static final String MAX_TOKENS         = "max_tokens";
	public static final String DIMENSIONS         = "dimensions";

	public static final String TOP_P = "top_p";
	public static final String MIN_P = "min_p";
	public static final String REPEAT_PENALTY  = "repeat_penalty";

	// Perplexity-specific parameters for web search
	public static final String SEARCH_DOMAIN_FILTER = "search_domain_filter";
	public static final String SEARCH_RECENCY_FILTER = "search_recency_filter";
	public static final String RETURN_IMAGES = "return_images";
	public static final String RETURN_RELATED_QUESTIONS = "return_related_questions";
	public static final String SEARCH_CONTEXT = "search_context";
	public static final String SEARCH_MODE = "search_mode";
	public static final String USER_LOCATION = "user_location";
	public static final String SEARCH_AFTER_DATE_FILTER = "search_after_date_filter";
	public static final String SEARCH_BEFORE_DATE_FILTER = "search_before_date_filter";


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
	public MapParam modelObj(Model model) {
		if (model == null) {
			super.remove(MODEL_OBJ);
		}
		else {
			put(MODEL_OBJ, model);
			put(MODEL, model.getName());
		}
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
	public Integer getDimension() { 
		return getAsInteger(DIMENSIONS); 
	}

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
	 * Returns model name if set, otherwise null.
	 * 
	 * @return model
	 */
	public String getModel() {
		// check model object first
		if(containsKey(MODEL_OBJ)) {
			Object v = get(MODEL_OBJ);
			if(v instanceof Model) {
				return ((Model)v).getName();
			} 
		}
		var v = get(MODEL);
		return v==null?null:v.toString(); 
	}

	/**
	 * Returns model object if set, otherwise null.
	 * 
	 * @return model object
	 */
	public Model getModelObj() {
		Object v = get(MODEL_OBJ);
		if(v instanceof Model) {
			return (Model)v;
		} 
		return null; 
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

	// ==================== PERPLEXITY WEB SEARCH METHODS ====================

	/**
	 * Sets the domains to filter search results (Perplexity specific).
	 * <p>
	 * You can include or exclude specific domains from search results.
	 * Prefix a domain with "-" to exclude it.
	 * </p>
	 * <p>
	 * Example: {@code new String[]{"arxiv.org", "nature.com", "-wikipedia.org"}}
	 * </p>
	 *
	 * @param domains array of domains (prefix with "-" to exclude)
	 * @return this object for method chaining
	 * @see bor.tools.simplellm.websearch.WebSearch
	 */
	public MapParam searchDomainFilter(String[] domains) {
		if (domains == null || domains.length == 0)
			super.remove(SEARCH_DOMAIN_FILTER);
		else
			put(SEARCH_DOMAIN_FILTER, domains);
		return this;
	}

	/**
	 * Sets the time period for search results (Perplexity specific).
	 * <p>
	 * Valid values: "hour", "day", "week", "month", "year"
	 * </p>
	 *
	 * @param period the recency filter
	 * @return this object for method chaining
	 * @see bor.tools.simplellm.websearch.WebSearch
	 */
	public MapParam searchRecencyFilter(String period) {
		if (period == null)
			super.remove(SEARCH_RECENCY_FILTER);
		else
			put(SEARCH_RECENCY_FILTER, period);
		return this;
	}

	/**
	 * Enable/disable returning images in search results (Perplexity specific).
	 *
	 * @param value true to include images in the response
	 * @return this object for method chaining
	 * @see bor.tools.simplellm.websearch.SearchResponse#getImages()
	 */
	public MapParam returnImages(Boolean value) {
		if (value == null)
			super.remove(RETURN_IMAGES);
		else
			put(RETURN_IMAGES, value);
		return this;
	}

	/**
	 * Enable/disable returning related questions (Perplexity specific).
	 * <p>
	 * When enabled, the model will suggest related questions that the user
	 * might want to ask based on the original query.
	 * </p>
	 *
	 * @param value true to include related questions in the response
	 * @return this object for method chaining
	 * @see bor.tools.simplellm.websearch.SearchResponse#getRelatedQuestions()
	 */
	public MapParam returnRelatedQuestions(Boolean value) {
		if (value == null)
			super.remove(RETURN_RELATED_QUESTIONS);
		else
			put(RETURN_RELATED_QUESTIONS, value);
		return this;
	}

	/**
	 * Sets search context size (Perplexity specific).
	 * <p>
	 * Controls how much context is retrieved from search results.
	 * Valid values: "low", "medium", "high"
	 * </p>
	 *
	 * @param context the context size level
	 * @return this object for method chaining
	 */
	public MapParam searchContext(String context) {
		if (context == null)
			super.remove(SEARCH_CONTEXT);
		else
			put(SEARCH_CONTEXT, context);
		return this;
	}

	/**
	 * Sets search mode (Perplexity specific).
	 * <p>
	 * Valid values: "web", "academic"
	 * </p>
	 * <ul>
	 * <li>"web" - General web search (default)</li>
	 * <li>"academic" - Focus on academic and scholarly sources</li>
	 * </ul>
	 *
	 * @param mode the search mode
	 * @return this object for method chaining
	 */
	public MapParam searchMode(String mode) {
		if (mode == null)
			super.remove(SEARCH_MODE);
		else
			put(SEARCH_MODE, mode);
		return this;
	}
	
	/**
	 * Sets search context size using the SearchContextSize enum (Perplexity specific).
	 *
	 * @param mode the search context size enum
	 * @return this object for method chaining
	 */
	public MapParam searchMode(SearchContextSize mode) {
		return searchContext(mode == null ? null : mode.name());	
	}
	
	
	/**
	 * Sets search mode using the SearchMode enum (Perplexity specific).
	 *
	 * @param mode the search mode enum
	 * @return this object for method chaining
	 */
	public MapParam searchMode(SearchMode mode) {
		return searchMode(mode == null ? null : mode.name());
	}

	/**
	 * Gets the search domain filter if set.
	 *
	 * @return array of domains or null if not set
	 */
	public String[] getSearchDomainFilter() {
		Object value = get(SEARCH_DOMAIN_FILTER);
		if (value instanceof String[]) {
			return (String[]) value;
		}
		return null;
	}

	/**
	 * Gets the search recency filter if set.
	 *
	 * @return the recency period or null if not set
	 */
	public String getSearchRecencyFilter() {
		return (String) get(SEARCH_RECENCY_FILTER);
	}

	/**
	 * Gets the return images setting if set.
	 *
	 * @return true if images should be returned, false otherwise, or null if not set
	 */
	public Boolean getReturnImages() {
		Object value = get(RETURN_IMAGES);
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return null;
	}

	/**
	 * Gets the return related questions setting if set.
	 *
	 * @return true if related questions should be returned, false otherwise, or null if not set
	 */
	public Boolean getReturnRelatedQuestions() {
		Object value = get(RETURN_RELATED_QUESTIONS);
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return null;
	}

	/**
	 * Gets the search context if set.
	 *
	 * @return the search context level or null if not set
	 */
	public String getSearchContext() {
		return (String) get(SEARCH_CONTEXT);
	}

	/**
	 * Gets the search mode if set.
	 *
	 * @return the search mode or null if not set
	 */
	public String getSearchMode() {
		return (String) get(SEARCH_MODE);
	}

	/**
	 * Sets user location for localized search results (Perplexity specific).
	 * <p>
	 * Provides geographical context to improve search relevance.
	 * The location map should contain: "latitude", "longitude", and optionally "country".
	 * </p>
	 * <p>
	 * Example:
	 * <pre>{@code
	 * Map<String, Object> location = new LinkedHashMap<>();
	 * location.put("latitude", -15.7933);
	 * location.put("longitude", -47.8827);
	 * location.put("country", "br");
	 * params.userLocation(location);
	 * }</pre>
	 * </p>
	 *
	 * @param location map containing latitude, longitude, and optionally country
	 * @return this object for method chaining
	 */
	public MapParam userLocation(Map<String, Object> location) {
		if (location == null || location.isEmpty())
			super.remove(USER_LOCATION);
		else
			put(USER_LOCATION, location);
		return this;
	}

	/**
	 * Sets user location using individual values (convenience method).
	 *
	 * @param latitude the latitude coordinate
	 * @param longitude the longitude coordinate
	 * @param country the ISO country code (e.g., "br", "us")
	 * @return this object for method chaining
	 */
	public MapParam userLocation(double latitude, double longitude, String country) {
		Map<String, Object> location = new LinkedHashMap<>();
		location.put("latitude", latitude);
		location.put("longitude", longitude);
		if (country != null && !country.isEmpty()) {
			location.put("country", country);
		}
		put(USER_LOCATION, location);
		return this;
	}

	/**
	 * Sets the after-date filter for search results (Perplexity specific).
	 * <p>
	 * Only return results published after this date.
	 * Date format: "MM/DD/YYYY"
	 * </p>
	 * <p>
	 * Example: {@code params.searchAfterDateFilter("01/01/2025")}
	 * </p>
	 *
	 * @param date the date in MM/DD/YYYY format
	 * @return this object for method chaining
	 */
	public MapParam searchAfterDateFilter(String date) {
		if (date == null)
			super.remove(SEARCH_AFTER_DATE_FILTER);
		else
			put(SEARCH_AFTER_DATE_FILTER, date);
		return this;
	}

	/**
	 * Sets the before-date filter for search results (Perplexity specific).
	 * <p>
	 * Only return results published before this date.
	 * Date format: "MM/DD/YYYY"
	 * </p>
	 * <p>
	 * Example: {@code params.searchBeforeDateFilter("12/31/2025")}
	 * </p>
	 *
	 * @param date the date in MM/DD/YYYY format
	 * @return this object for method chaining
	 */
	public MapParam searchBeforeDateFilter(String date) {
		if (date == null)
			super.remove(SEARCH_BEFORE_DATE_FILTER);
		else
			put(SEARCH_BEFORE_DATE_FILTER, date);
		return this;
	}

	/**
	 * Gets the user location if set.
	 *
	 * @return map containing location data or null if not set
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getUserLocation() {
		Object value = get(USER_LOCATION);
		if (value instanceof Map) {
			return (Map<String, Object>) value;
		}
		return null;
	}

	/**
	 * Gets the search after date filter if set.
	 *
	 * @return the date string or null if not set
	 */
	public String getSearchAfterDateFilter() {
		return (String) get(SEARCH_AFTER_DATE_FILTER);
	}

	/**
	 * Gets the search before date filter if set.
	 *
	 * @return the date string or null if not set
	 */
	public String getSearchBeforeDateFilter() {
		return (String) get(SEARCH_BEFORE_DATE_FILTER);
	}

}