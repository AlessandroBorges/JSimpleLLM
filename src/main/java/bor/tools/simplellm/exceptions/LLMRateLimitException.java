package bor.tools.simplellm.exceptions;

/**
 * Exception for rate limiting errors from LLM providers.
 */
public class LLMRateLimitException extends LLMException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LLMRateLimitException(String message, Throwable cause) {
		super(message, cause);
	}

	public LLMRateLimitException(String message) {
		super(message);
	}
}
