/**
 * 
 */
package bor.tools.simplellm.exceptions;

/**
 * 
 */
public class LLMIllegalArgumentException extends LLMException {

	/**
	 * @param message
	 */
	public LLMIllegalArgumentException(String message) {
		super(message);		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LLMIllegalArgumentException(String message, Throwable cause) {
		super(message, cause);		
	}

}
