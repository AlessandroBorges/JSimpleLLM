package bor.tools.simplellm;

/**
 * Interface for handling streaming responses from a chat service.
 * This interface defines methods that are called during the streaming 
 * process, allowing for real-time handling of tokens, completion events, 
 * and error notifications.
 * 
 * @author Alessandro Borges
 */
public interface ResponseStream {
    /**
     * Invoked when a new token is received from the stream.
     *
     * @param token The content of the token received, which may represent 
     *              a part of the response or a continuation of the message.
     */
    void onToken(String token);
    
    /**
     * Invoked when the streaming process is complete.
     * This method signifies that no further tokens will be sent, and 
     * any final processing can be performed.
     */
    void onComplete();
    
    /**
     * Invoked when an error occurs during the streaming process.
     *
     * @param error The throwable that represents the error that occurred, 
     *              providing details about the issue encountered.
     */
    void onError(Throwable error);
}
