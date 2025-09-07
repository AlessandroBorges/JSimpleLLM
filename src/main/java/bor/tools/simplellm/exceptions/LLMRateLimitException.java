package bor.tools.simplellm.exceptions;

public class LLMRateLimitException extends LLMException { 
	public LLMRateLimitException(String message, Throwable cause) { 
				super(message, cause);
	}
}