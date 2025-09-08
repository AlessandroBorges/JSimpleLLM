package bor.tools.simplellm.exceptions;

public class LLMAuthenticationException extends LLMException {
	public LLMAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public LLMAuthenticationException(String message) {
		super(message);
	}

}
