package bor.tools.simplellm.exceptions;

public class LLMTimeoutException extends LLMException {
	public LLMTimeoutException(String message, Throwable cause) { 
				super(message, cause);
	}
}