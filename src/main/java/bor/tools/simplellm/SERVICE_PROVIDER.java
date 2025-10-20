package bor.tools.simplellm;

/**
 * LLM Service providers
 */
public enum SERVICE_PROVIDER{
	OPENAI,
	ANTHROPIC,
	LM_STUDIO,		
	OLLAMA,
	TOGETHER,
	PERPLEXITY; 
	
	public static SERVICE_PROVIDER fromString(String provider) {
		if (provider == null) {
			return null;
		}
		switch (provider.trim().toUpperCase()) {
		case "OPENAI":
			return OPENAI;
		case "ANTHROPIC":
			return ANTHROPIC;
		case "LM_STUDIO":
		case "LMSTUDIO":
			return LM_STUDIO;
		case "OLLAMA":
			return OLLAMA;
		case "TOGETHER":
			return TOGETHER;
		case "PERPLEXITY":
			return PERPLEXITY; 
		default:
			throw new IllegalArgumentException("Unsupported LLM service provider: " + provider);
		}
	}
}//enum