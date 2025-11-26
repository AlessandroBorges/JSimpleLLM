package bor.tools.simplellm.impl;

import static bor.tools.simplellm.Model_Type.CODING;
import static bor.tools.simplellm.Model_Type.EMBEDDING;
import static bor.tools.simplellm.Model_Type.EMBEDDING_DIMENSION;
import static bor.tools.simplellm.Model_Type.LANGUAGE;
import static bor.tools.simplellm.Model_Type.REASONING;
import static bor.tools.simplellm.Model_Type.REASONING_PROMPT;
import static bor.tools.simplellm.Model_Type.TOOLS;
import static bor.tools.simplellm.Model_Type.VISION;
import static bor.tools.simplellm.Reasoning_Effort.none;

import java.util.Map;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.MapModels;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.ModelEmbedding;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.Reasoning_Effort;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.SERVICE_PROVIDER;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.chat.Message;
import bor.tools.simplellm.chat.MessageRole;
import bor.tools.simplellm.exceptions.LLMException;

/**
 * Implementation of the LLMProvider interface for LM Studio's local Large
 * Language Model server.
 * <p>
 * This class extends OpenAILLMService and adapts it to work with LM Studio's
 * API endpoints.
 * LM Studio provides a local server that runs various open-source LLM models
 * with an OpenAI-compatible API.
 * LM Studio is particularly popular for running models locally with a
 * user-friendly interface.
 * </p>
 * <p>
 * Key differences from OpenAI:
 * - Uses local server (default: http://localhost:1234)
 * - Uses "lm-studio" as default API key
 * - Different model names based on what's loaded in LM Studio
 * - No responses API support (uses chat completions only)
 * - Models are dynamically loaded/unloaded by user in LM Studio UI
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 * 
 * @see OpenAILLMService
 */
public class LMStudioLLMService extends OpenAILLMService {


	protected static final String PROMPT_REASONING = 
			  "Você é um assistente de IA que usa Chain-of-Thought (CoT) para racionalizar suas respostas. \n"
			  + "Use o seguinte formato para estruturar seu raciocínio: \n"
              + "Imprima a tag <think> para iniciar o raciocínio e </think> para finalizar. \n" 						  
			  + "1. Primeiro, analise e raciocine sobre o problema sistematicamente. \n"
			  + "2. Divida questões complexas em componentes gerenciáveis. \n"
			  + "3. Considere múltiplas perspectivas/abordagens. \n"
			  + "4. Mostre explicitamente seu processo de pensamento passo a passo. \n"
			  + "5. Imprima a tag </think> para finalizar o raciocínio. \n"
			  + "5. Por fim, apresente sua conclusão de forma concisa. \n"
			  + " \n"
			  + "Formato: \n"
			  + "<think> \n"
			  + "[Classificação do Problema] \n"
			  + "- Tipo de problema (contagem, padrão, lógica, etc.) \n"
			  + "- Formato de entrada \n"
			  + "- Formato de saída necessário \n"
			  + "- Restrições conhecidas. \n"
			  + "[Estratégia de Solução] \n"
			  + "1. Detalhamento do Problema: \n"
			  + "- Divida em partes contáveis/verificáveis. \n"
			  + "- Defina o método de verificação. \n"
			  + "- Identifique possíveis armadilhas. \n"
			  + "2. Abordagem Sistemática: \n"
			  + "- Processo passo a passo. \n"
			  + "- Marcação/contagem explícita. \n"
			  + "- Acompanhe o progresso. \n"
			  + "[Verificação] \n"
			  + "1. Contagem/Solução Primária \n"
			  + "- Mostrar trabalho explícito \n"
			  + "- Marcar itens contados \n"
			  + "- Total acumulado \n"
			  + "2. Verificação Secundária \n"
			  + "- Avaliar  outras possibilidades ou métodos \n"
			  + "- Comparar resultados \n"
			  + " \n"
			 // + "[Preparação da Resposta] \n"
			 // + "- Identificar a resposta correta\n"
			//  + "- Unidades/formato especificado \n"
			//  + "- Verificação de confiança \n"
			  + "</think>\n"
			  + " \n"
			//  + "**Conclusão** \n"
			  + " {após a tag </think>, apresentar a resposta correta aqui}\n"
			  + " \n"
			  + "Exemplo: \n"
			  + "Questão: Sydney é a capital da Austrália? \n"
			  + " \n"
			  + "<think> \n"
			  + " [Classificação do Problema:] \n"
			  + " - Tipo de prooblema: Conhecimento factual \n"
			  + " - Formato de entrada: Pergunta sobre localização geográfica \n"
			  + " - Formato de saída necessário: Resposta booleana (sim/não) \n"
			  + " - Restrições conhecidas: Capitais de países são locais oficialmente designados por governos \n"
			  + " \n"
			  + " [Estratégia de Solução] \n"
			  + "   1. Detalhamento do Problema: \n"
			  + "	 - Capitais de países: A capital de um país é a cidade oficialmente designada como seu centro político, administrativo e legislativo. \n"
			  + "	 - Sidney (Sydney): Uma cidade importante na Austrália, localizada na costa leste do país, conhecida por seu porto, turismo e cultura. \n"
			  + "	 - Canberra: A cidade oficialmente designada como capital da Austrália, localizada no interior do país.	  \n\n"
			  + "  2. Abordagem Sistemática: \n"
			  + "	- Passo 1: Confirmar o nome oficial da capital da Austrália. \n"
			  + "	- Passo 2: Comparar o nome \"Sidney\" com o nome oficial da capital. \n"
			  + "	- Passo 3: Verificar se há possíveis confusões ou erros de ortografia. \n"
			  + "	 \n"
			  + "[Verificação] \n"
			  + "  1. Contagem/Solução Primária: \n"
			  + "	- Sidney é uma cidade importante, mas não é a capital. \n"
			  + "	- A capital da Austrália é Canberra, não Sidney. \n\n"
			  + "  2. Verificação Secundária: \n"
			  + "	- Outras cidades da Austrália, como Melbourne e Brisbane, são capitais de estados, mas não da nação. \n"
			  + "	- Não há outro nome oficial para a capital da Austrália. \n"
			 // + "[Preparação da Resposta:] "
			 // + " - a resposta textual deve afirmar que a Sidney não é a capital, mas Camberra.\n"
			  + "</think>\n"			 
			 // + "**Conclusão:**\n"
			  + "Não, Sidney não é a capital da Austrália. A capital é Canberra."		
			 		
			 ;
	
	protected static final MapModels defaultModelMap;
	protected static final LLMConfig defaultLLMConfig;
	
	protected static final String[] REASONING_MODELS = {"qwen3", "gpt-oss", "gemma"};

	protected static final String DEFAULT_COMPLETION_NAME = "qwen3-1.7b";
	protected static final String DEFAULT_EMBEDDING_NAME = "snowflake";

	static {
		MapModels map = new MapModels();

		// Common LM Studio model definitions - these are examples of models typically
		// used
		// Note: Actual available models depend on what user has loaded in LM Studio
		Model qwen3_1_7b = new Model("qwen/qwen3-1.7b", "qwen3-1.7b", 32000,
		                             LANGUAGE, REASONING, TOOLS, CODING);
		Model qwen3_4b    = new Model("qwen/qwen3-4b", "qwen3-4b", 32000, 
		                              LANGUAGE, REASONING, TOOLS, CODING);
		// models PHI
		Model phi4_mini   = new Model("phi-4-mini-instruct", "phi4-mini", 32768, 
		                              LANGUAGE, REASONING, REASONING_PROMPT, CODING);
		Model phi3_5_mini = new Model("lmstudio-community/phi-3.5-mini-instruct", "phi3.5-mini", 128000,
		                              LANGUAGE);		
		// models GTP-OSS
		Model gtp_oss_20b = new Model("openai/gpt-oss-20b", "gpt-oss", 32768, 
		                              LANGUAGE, REASONING, TOOLS, CODING);


		// Vision models that might be available
		Model llava_7b = new Model("llava-1.5-7b", "llava-7b", 8096, LANGUAGE, VISION);

		// Embedding models (if user has loaded embedding models		
		ModelEmbedding snowflake   = new ModelEmbedding("text-embedding-snowflake-arctic-embed-l-v2.0",
		            "snowflake",
		            8192,
		            1024,
		            EMBEDDING,
		            EMBEDDING_DIMENSION);
		
		ModelEmbedding nomic     =
		            new ModelEmbedding("text-embedding-nomic-embed-text-v1.5@q8_0", "nomic",
		                               8192,
		                               768,
		                               EMBEDDING, EMBEDDING_DIMENSION);

		// Add models to map
		map.add(qwen3_1_7b);
		map.add(phi4_mini);
		map.add(qwen3_4b);
		map.add(gtp_oss_20b);

		map.add(phi3_5_mini);
		map.add(llava_7b);
		
		map.add(snowflake);
		map.add(nomic);

		// Make the defaultModelMap unmodifiable
		defaultModelMap = map;

		defaultLLMConfig = LLMConfig.builder()
		            .apiTokenEnvironment("LMSTUDIO_API_KEY")
		            .apiToken("lm-studio") // Default API key for LM Studio
		            .baseUrl("http://localhost:1234/v1/")
		            .registeredModelMap (defaultModelMap)
		            .defaultEmbeddingModelName(DEFAULT_EMBEDDING_NAME)
		            .defaultCompletionModelName(DEFAULT_COMPLETION_NAME)
		            .build();
	}

	
	/**
	 * Retrieves the default LLM configuration for LM Studio services.
	 * <p>
	 * This configuration includes default local server endpoints, model
	 * definitions,
	 * and settings optimized for LM Studio's local LLM server.
	 * </p>
	 *
	 * @return a clone of default LLMConfig instance for LM Studio
	 */
	public static LLMConfig getDefaultLLMConfig() { 
		return defaultLLMConfig.clone(); 
	}

	/**
	 * Default constructor for LMStudioLLMService.
	 * <p>
	 * Creates a new instance with default LM Studio configuration settings.
	 * </p>
	 */
	public LMStudioLLMService() {
		this(getDefaultLLMConfig());
	}

	/**
	 * Constructor for LMStudioLLMService with custom configuration.
	 * <p>
	 * Creates a new instance with the specified LLM configuration,
	 * including API settings and model definitions for LM Studio.
	 * </p>
	 *
	 * @param config the LLM configuration containing LM Studio API settings and
	 *               parameters
	 */
	public LMStudioLLMService(LLMConfig config) {	
		super(null);
		this.config = LLMConfig.mergeConfigs(defaultLLMConfig, config);
		// LM Studio doesn't support responses API, so disable it
		this.useResponsesAPI = false;		
	}

	/**
	 * Override to check if endpoint is LM Studio (not OpenAI).
	 * This affects parameter mapping and API behavior.
	 */
	@Override
	protected boolean isOpenAIEndpoint() {
		return false; // LM Studio uses OpenAI-compatible API but isn't OpenAI
	}

	/**
	 * LM Studio doesn't support the responses API, so this always returns false.
	 */
	@Override
	protected boolean isResponsesAPIModel(String model) {
		return false; // LM Studio doesn't support responses API
	}

	/**
	 * Gets the API token from configuration, with LM Studio-specific defaults.
	 */
	@Override
	protected String getApiToken() throws bor.tools.simplellm.exceptions.LLMException {
		String token = config.getApiToken();
		if (token == null || token.trim().isEmpty()) {
			// Try environment variable
			String envVar = config.getApiTokenEnvironment();
			if (envVar != null) {
				token = System.getenv(envVar);
			}
		}

		// If still no token, use default "lm-studio" for local server
		if (token == null || token.trim().isEmpty()) {
			token = "lm-studio";
			config.setApiToken(token);
		}
		return token.trim();
	}

	/**
	 * Override parameter conversion for LM Studio-specific requirements.
	 * LM Studio has some parameter differences from standard OpenAI.
	 */
	@Override
	protected bor.tools.simplellm.MapParam convert2ResponseAPI(bor.tools.simplellm.MapParam params) {
		// LM Studio doesn't use responses API, so return original params
		return params;
	}
	
	/**
	 * Check if a model supports a specific capability.
	 * This is useful for LM Studio where model capabilities depend on what's
	 * loaded.
	 * 
	 * @param model the Model object to check
	 * @param type the Model_Type to check for
	 * @return true if the model supports the specified type, false otherwise
	 */
	@Override
	public boolean isModelType(Model model, Model_Type type) {
		if (model == null || type == null) {
			return false;
		}

		// Check if already defined in the model
		if (model.isType(type)) {
			return true;
		}

		// Special handling for reasoning models with known list
		if (type == REASONING || type == Model_Type.REASONING_PROMPT) {
			if (model.isType(REASONING) || model.isType(Model_Type.REASONING_PROMPT)) {
				return true;
			}
			String modelName = model.getName();
			for (String mname : REASONING_MODELS) {
				if (modelName.equalsIgnoreCase(mname)) {
					return true;
				}
			}
		}

		// Use enhanced detection from ModelFeatureDetector
		return ModelFeatureDetector.isModelType(model, type);
	}


	/**
	 * Override getDefaultModelName to provide a sensible default for LM Studio.
	 * Since models are user-loaded, we pick a commonly available one.
	 */
	@Override
	public String getDefaultCompletionModelName() {		
		return DEFAULT_COMPLETION_NAME; // Fallback to common model name
	}
	
		
	/**
	 * Override chatCompletion to handle LM Studio-specific reasoning prompts.
	 * LM Studio models may require specific prompt structures for reasoning tasks.
	 */
	@Override
	public CompletionResponse chatCompletion(Chat chat, String query, MapParam params) 
				throws LLMException {	
		
		params = fixParams(params, chat);
		Model model = resolveModel(params);		
		
		// if model doesn't support any reasoning 
		if(model.isTypeReasoning()==false) {			
			params.reasoningEffort(null);			
			return super.chatCompletion(chat, query, params);
		}	
		// Now apply reasoning prompt if needed
		Message system = chat.getLastMessage(MessageRole.SYSTEM);
		String prompt = system != null? system.getText() : "";
		boolean hasThinkPrompt = isThinkingPrompt(prompt);
		boolean isReasoningCompletion = params.getReasoningEffort() != null				                        				                        
				                        && model.isTypeReasoning()
				                        && hasThinkPrompt==false;	                        	
		if(isReasoningCompletion) {
			Reasoning_Effort effort = params.getReasoningEffort();
			if (effort == none) {
				// disable thinking process
				prompt = "/no-think\n"
						+ "enable_thinking=False\n " 
						+ prompt;									
			} else {
				if (model.isType(REASONING_PROMPT) && hasThinkPrompt == false) {
					// add reasoning prompt only if not already present
					prompt = PROMPT_REASONING + "\n" + prompt;
				} else {
					prompt ="reasoning_effort:" + effort.getValue() + "\n"
							+ "/think\n"
							+ "enable_thinking=True\n"
				            + prompt;
				}				
			}			
			chat.addSystemMessage(prompt);		
		} 
				                        
		return super.chatCompletion(chat, query, params);
	}
	
	/**
	 * Override chatCompletionStream to handle LM Studio-specific reasoning prompts.
	 * 
	 */
	@Override
	public CompletionResponse chatCompletionStream(ResponseStream stream, Chat chat, String query, MapParam params)
	            throws LLMException {
		
		params = fixParams(params, chat);
		Model model = resolveModel(params);		
		
		// if model doesn't support any reasoning 
		if(model.isTypeReasoning()==false) {			
			params.reasoningEffort(null);			
			return super.chatCompletionStream(stream, chat, query, params);
		}	
		// Now apply reasoning prompt if needed
		Message system = chat.getLastMessage(MessageRole.SYSTEM);
		String prompt = system != null? system.getText() : "";
		boolean hasThinkPrompt = isThinkingPrompt(prompt);
		boolean isReasoningCompletion = params.getReasoningEffort() != null				                        				                        
				                        && model.isTypeReasoning()
				                        && hasThinkPrompt==false;	                        	
		if(isReasoningCompletion) {
			Reasoning_Effort effort = params.getReasoningEffort();
			if (effort == none) {
				// disable thinking process
				prompt = "/no-think\n"
						+ "enable_thinking=False\n " 
						+ prompt;									
			} else {
				if (model.isType(REASONING_PROMPT) && hasThinkPrompt == false) {
					// add reasoning prompt only if not already present
					prompt = PROMPT_REASONING + "\n" + prompt;
				} else {
					prompt ="reasoning_effort:" + effort.getValue() + "\n"
							+ "/think\n"
							+ "enable_thinking=True\n"
				            + prompt;
				}				
			}			
			chat.addSystemMessage(prompt);		
		} 				                        
		return super.chatCompletionStream(stream, chat, query, params);
	}
	
	/**
	 * Override fixParams to set LM Studio-specific defaults.
	 * LM Studio models often support larger contexts and have different default
	 * parameters.
	 */
	@Override
	public MapParam fixParams(MapParam params, Chat chat) throws LLMException {
		if (params == null) {
			params = new MapParam();
		}
		// Ensure model is set
		if (params.getModel() == null) {
			params.model(getDefaultCompletionModelName());
		}
		
		Model model = resolveModel(params);
		if(model==null) {
			throw new LLMException("No model specified and no default model available");
		}
		if(isModelOnline(model)==false) { // @TODO - fazer cache de status dos modelos
			throw new LLMException("LM Studio service only supports installed models. "
						+ "Model " + model.getName() + " is offline.");
		}
		
		/*
		if(params.get("seed") == null) {
			params.put("seed", 42); // set a default seed for reproducibility
		}
		*/
		// Ensure max tokens is set
		if (params.getMaxTokens() == null) {			
			params.maxTokens(4000); // LM Studio models often support large contexts
		}
		
		// Ensure temperature is set
		if (params.getTemperature() == null) {
			params.temperature(0.7f); // Default temperature for LM Studio
		}
		
		// Ensure top_p is set
		if (params.getTop_p() == null) {
			params.top_p(0.95f); // Default top_p for LM Studio
		}
		
		if (params.getMin_p() == null) {
			params.min_p(0.05f); 
		}
		
		// Ensure frequency_penalty is set
		if (params.getRepeat_penalty()== null) {
			params.repeat_penalty(1.10f); // Default frequency penalty
		}		
				
		return super.fixParams(params, chat);
	}

	
	protected boolean isThinkingPrompt(String prompt) {
		if (prompt == null || prompt.trim().isEmpty()) {
			return false;
		}
		prompt = prompt.toLowerCase();
		return (prompt.contains("<think>") && prompt.contains("</think>"))
				   || prompt.contains("reasoning_effort") || prompt.contains("no-think")
				   || prompt.contains("chain-of-thought") || prompt.contains("cot")
				   || prompt.contains("enable_thinking")|| prompt.contains("/think");
	}
	
	/**
	 * Override completion to ensure Ollama-specific behavior.
	 * This method adapts the completion call to LLM Studio's API nuances.
	 * Oposite to OpenAI, which deprecated chat completions,
	 * LM_Studio does support both classic completion and 'chatCompletion endpoints.
	 */
	@Override
	public CompletionResponse completion(String prompt, String query, MapParam params) throws LLMException {
        		
		prompt = prompt == null || prompt.isEmpty() ? DEFAULT_PROMPT : prompt.trim();
		query = query == null || query.isEmpty() ? "Introduce yourself." : query.trim();		
		params = fixParams(params);
				
		Model model = resolveModel(params);
		
		// Check if reasoning effort is set and model supports reasoning
		if(params.getReasoningEffort() != null && model.isTypeReasoning() ) {
			// better call chatCompletion for reasoning 
			Chat chat = new Chat();
			Reasoning_Effort effort = params.getReasoningEffort();
			boolean hasThinkPrompt = isThinkingPrompt(prompt);			
			if (effort == none) {
				// disable thinking process
				prompt = "/no-think\n"
						+ "enable_thinking=False\n " 
						+ prompt;									
			} else {
				if (model.isType(REASONING_PROMPT) && hasThinkPrompt == false) {
					// add reasoning prompt only if not already present
					prompt = PROMPT_REASONING + "\n" + prompt;
				} else {
					prompt ="reasoning_effort:" + effort.getValue() + "\n"
							+ "/think\n"
							+ "enable_thinking=True\n---\n"
				            + prompt;
				}				
			}
			
			chat.addSystemMessage(prompt);			
			return super.chatCompletion(chat, query, params);
		} 
		
		// if model doesn't support reasoning, clear reasoning effort
		if(!model.isTypeReasoning()) {			
			params.reasoningEffort(null);
		}
		// Ensure model name is set in params
		params.model(model.getName());
		// Create request payload
		Map<String, Object> payload = jsonMapper.toCompletionRequest(prompt, query, params);

		try {
			// Make API request
			Map<String, Object> response = postRequest("completions", payload);

			// Convert response
			CompletionResponse completionResponse = jsonMapper.fromChatCompletionResponse(response);			
			return completionResponse;
		} catch (LLMException e) {
			throw e;
		} catch (Exception e) {
			throw new LLMException("Unexpected error during chat completion: "
			            + e.getMessage(), e);
		}
	}
	
	@Override
	public SERVICE_PROVIDER getServiceProvider() {		
		return SERVICE_PROVIDER.LM_STUDIO;
	}
	
	
	/**
	 * String representation of the This service instance.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("LLMStudioService using base URL: ")
		.append(getLLMConfig().getBaseUrl())
		.append(",\n\t Default Completion Model: ")
		.append(getDefaultCompletionModelName())
		.append(",\n\t Default Embedding Model: ")
		.append(getDefaultEmbeddingModelName());		
		return sb.toString();
	}
}