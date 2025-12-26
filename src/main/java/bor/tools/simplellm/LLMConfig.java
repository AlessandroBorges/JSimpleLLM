package bor.tools.simplellm;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	 * The service provider type for the LLM.
	 * <p>
	 * This enum indicates which LLM service provider is being used, such as
	 * OpenAI, Anthropic, etc.
	 * </p>
	 */	
	private SERVICE_PROVIDER serviceProvider;

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
	private MapModels registeredModelMap = new MapModels(); 
	
	

	/**
	 * The default completion model name to use when no specific model is requested.
	 * <p>
	 * This should correspond to one of the keys in the {@link #registeredModelMap}.
	 * If not set, the application may choose a default behavior (e.g., throw an
	 * error or use a predefined fallback model).
	 * </p>
	 */
	private String defaultCompletionModelName;
	
	/**
	 * The default embedding model name to use for generating embeddings.
	 * <p>
	 * This should correspond to one of the keys in the {@link #registeredModelMap}
	 * that is capable of generating embeddings. If not set, the application may
	 * choose a default behavior (e.g., throw an error or use a predefined fallback
	 * embedding model).
	 * </p>
	 */
	private String defaultEmbeddingModelName;

	/**
	 * Default parameters to be applied to all requests unless overridden.
	 * <p>
	 * This allows configuring provider-specific defaults at the service level,
	 * such as default temperature, search filters, reasoning effort, etc.
	 * Parameters specified in individual requests will override these defaults.
	 * </p>
	 * <p>
	 * Example for Perplexity:
	 * <pre>{@code
	 * MapParam defaults = new MapParam()
	 *     .returnRelatedQuestions(true)
	 *     .searchMode("web")
	 *     .temperature(0.7f);
	 * config.setDefaultParams(defaults);
	 * }</pre>
	 * </p>
	 */
	private MapParam defaultParams;

	/**
	 * Retrieves a registered model by its name or alias.
	 * <p>
	 * This method searches the {@link #registeredModelMap} for a model matching the given
	 * name or alias.
	 * </p>
	 * 
	 * @param modelName the model name or alias
	 * 
	 * @return the corresponding {@link Model} if found, otherwise null
	 */	
	public Model getRegisteredModel(String modelName) {
		 var model = registeredModelMap.getModel(modelName);
		 if (model == null) {
			 registeredModelMap.getBestMatchName(modelName);
		 }
		 return model;
	}
	 
	 /**
	  * Add multiple models to the model map.
	  * @param models
	  * @return MapModels instance for method chaining
	  */
	public MapModels addModels(Model... models) {
		if (models != null) {
			for (var m : models) {
				this.registeredModelMap.put(m.getName(), m);
			}
		}
		return this.registeredModelMap;
	} 
	
	/**
	 * Add models
	 * @param models - Iterable of Model instances or a Map containing Model instances as values
	 * @return MapModels instance for method chaining
	 */
	public MapModels addModels(Iterable<Model> models) {
		    if(models instanceof Map) {
		        for (var m : ((Map<?, ?>) models).values()) {
		            if (m instanceof Model) {
		                this.registeredModelMap.put(((Model) m).getName(), (Model) m);
		            }
		        }
		        return this.registeredModelMap;
		    } else
			if (models != null) {
				for (var m : models) {
					this.registeredModelMap.put(m.getName(), m);
				}
			}
			return this.registeredModelMap;
		}

	/**
	 * Merges default parameters with provided parameters.
	 * <p>
	 * If both defaultParams and params are null, returns a new empty MapParam.
	 * If params is null, returns a copy of defaultParams.
	 * If defaultParams is null, returns params unchanged.
	 * Otherwise, creates a new MapParam with defaults first, then overlays
	 * the provided params (which take precedence).
	 * </p>
	 *
	 * @param params the request-specific parameters (can be null)
	 * @return merged parameters with defaults applied
	 */
	public MapParam mergeWithDefaults(MapParam params) {
	    if (defaultParams == null) {
	        return params != null ? params : new MapParam();
	    }
	    if (params == null) {
	        return new MapParam(defaultParams);
	    }
	    // Merge: start with defaults, then overlay params
	    MapParam merged = new MapParam(defaultParams);
	    merged.putAll(params);
	    return merged;
	}

	/**
	 * Retrieves the name of the service provider.
	 * 
	 * @return the service provider name as a String, or null if not set
	 */
	@JsonIgnore
	public String getServiceProviderName() {
		return serviceProvider != null ? serviceProvider.name() : null;
	}
	
	/**
	 * Creates a deep copy of this LLMConfig instance.
	 * <p>
	 * This method constructs a new LLMConfig object with the same values as the
	 * current instance, ensuring that mutable fields are also cloned to prevent
	 * shared references.
	 * </p>
	 * 
	 * @return a new LLMConfig instance that is a deep copy of this instance
	 */
	public LLMConfig clone() {		
		MapModels clonedModelMap = new MapModels();
		for (var entry : this.registeredModelMap.entrySet()) {
			clonedModelMap.put(entry.getKey(), entry.getValue().clone());
		}
		return LLMConfig.builder()
					.serviceProvider(this.serviceProvider)
					.baseUrl(this.baseUrl)
					.apiToken(this.apiToken)
					.apiTokenEnvironment(this.apiTokenEnvironment)
					.additionalProperties(new LinkedHashMap<>(this.additionalProperties))
					.registeredModelMap(clonedModelMap)
					.defaultCompletionModelName(this.defaultCompletionModelName)
					.defaultEmbeddingModelName(this.defaultEmbeddingModelName)
					.defaultParams(this.defaultParams==null ? null : new MapParam(this.defaultParams))
					.build();
	}
	
	/**
	 * Merges two LLMConfig instances.
	 * @param base - base config
	 * @param override - override config
	 * @return
	 */
	public static LLMConfig mergeConfigs(LLMConfig base, LLMConfig override) {
		if (override == null) {
		        return base.clone();
		}
		
	    if (base == null) {
	        return override != null ? override.clone() : null;
	    }
	   
	    
	    LLMConfigBuilder builder = LLMConfig.builder()
	    	    .serviceProvider(override.getServiceProvider() != null ? override.getServiceProvider() : base.getServiceProvider())
	            .baseUrl(override.getBaseUrl() != null ? override.getBaseUrl() : base.getBaseUrl())
	            .apiToken(override.getApiToken() != null ? override.getApiToken() : base.getApiToken())
	            .apiTokenEnvironment(override.getApiTokenEnvironment() != null ? override.getApiTokenEnvironment() : base.getApiTokenEnvironment())
	            .additionalProperties(new LinkedHashMap<>(base.getAdditionalProperties()));
	    
	    // Merge additionalProperties
	    if (override.getAdditionalProperties() != null && override.getAdditionalProperties().size() > 0) {	    	
	    	 var extraProps = new LinkedHashMap<String,Object>();
	    	 if (base.getAdditionalProperties() != null) {
	    		 extraProps.putAll(base.getAdditionalProperties());
	    	 }
	    	 extraProps.putAll(override.getAdditionalProperties());	    	 
	         builder.additionalProperties(extraProps);
	    }
	    
	    // Merge registeredModelMap
	    MapModels mergedModelMap = new MapModels();
	    mergedModelMap.putAll(base.getRegisteredModelMap());
	    mergedModelMap.putAll(override.getRegisteredModelMap());
	    builder.registeredModelMap(mergedModelMap);
	    
	    builder.defaultCompletionModelName(override.getDefaultCompletionModelName() != null ? override.getDefaultCompletionModelName() : base.getDefaultCompletionModelName());
	    builder.defaultEmbeddingModelName(override.getDefaultEmbeddingModelName() != null ? override.getDefaultEmbeddingModelName() : base.getDefaultEmbeddingModelName());
	    
	    // Merge defaultParams
	    if (base.getDefaultParams() != null || override.getDefaultParams() != null) {
	        MapParam mergedParams = base.mergeWithDefaults(override.getDefaultParams());
	        builder.defaultParams(mergedParams);
	    }
	    
	    return builder.build();
	}
	
}
// LLMConfig
