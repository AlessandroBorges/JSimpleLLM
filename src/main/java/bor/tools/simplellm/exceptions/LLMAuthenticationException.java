package bor.tools.simplellm.exceptions;

/**
 * Exception for authentication errors with LLM services.
 */
public class LLMAuthenticationException extends LLMException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LLMAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public LLMAuthenticationException(String message) {
		super(message);
	}

}
