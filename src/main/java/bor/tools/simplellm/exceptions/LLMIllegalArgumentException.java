/**
 * 
 */
package bor.tools.simplellm.exceptions;

/**
 * 
 */
public class LLMIllegalArgumentException extends LLMException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
