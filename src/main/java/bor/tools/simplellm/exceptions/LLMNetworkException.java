package bor.tools.simplellm.exceptions;

/**
 * Exception for network-related issues when communicating with LLM services.
 */
public class LLMNetworkException extends LLMException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LLMNetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public LLMNetworkException(String message) {
		super(message);
	}
}
