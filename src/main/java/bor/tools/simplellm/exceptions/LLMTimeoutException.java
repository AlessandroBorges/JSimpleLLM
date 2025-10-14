package bor.tools.simplellm.exceptions;

/**
 * Exception thrown when a timeout occurs while interacting with a Large Language Model (LLM).
 */
public class LLMTimeoutException extends LLMException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LLMTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public LLMTimeoutException(String message) {
		super(message);
	}
}
