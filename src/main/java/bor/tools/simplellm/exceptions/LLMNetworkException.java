package bor.tools.simplellm.exceptions;

public class LLMNetworkException extends LLMException {
	public LLMNetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public LLMNetworkException(String message) {
		super(message);
	}
}
