package bor.tools.simplellm;

public class JSimpleLLM {

	/**
	 * private constructor to prevent instantiation
	 */
	private JSimpleLLM() {	
	}
	
	public static String getVersion() {
		return "1.0.0";
	}
	

	/**
	 * Create LLMProvider based on SERVICE_PROVIDER type
	 * 
	 * @param type
	 * @return
	 */
	public static LLMProvider createProvider(SERVICE_PROVIDER type) {
		return LLMServiceFactory.createLLMService(type, null);
	}
	
	/**
	 * Create LLMProvider based on SERVICE_PROVIDER type
	 * 
	 * @param type
	 * @param config
	 * 
	 * @return
	 */
	public static LLMProvider createProvider(SERVICE_PROVIDER type, LLMConfig config) {
		return LLMServiceFactory.createLLMService(type, config);
	}
	

}
