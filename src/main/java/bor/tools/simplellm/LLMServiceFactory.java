package bor.tools.simplellm;

import bor.tools.simplellm.impl.OpenAILLMService;

//Factory para criação de implementações
public class LLMServiceFactory {
	
	/**
	 * Cria uma instância do serviço OpenAI LLM.
	 * @param config
	 * @return LLMService instância do serviço OpenAI
	 */
 public static LLMService createOpenAI(LLMConfig config) { 
	 return new OpenAILLMService(config);
	 }
 // Futuras implementações: createClaude(), createGemini(), etc.
}