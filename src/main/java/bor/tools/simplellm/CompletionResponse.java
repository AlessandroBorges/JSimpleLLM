package bor.tools.simplellm;

import java.util.Map;

import lombok.Data;

/**
 * Represents a simplified response object for OpenAI API interactions.
 * <p>
 * This class encapsulates the response data from OpenAI API calls, supporting
 * both simple text completions and chat-based interactions. It provides a
 * unified
 * structure for handling various types of responses while maintaining
 * compatibility
 * with multimodal content through the ContentWrapper.
 * </p>
 * <p>
 * The class uses Lombok's {@code @Data} annotation to automatically generate
 * getters, setters, equals, hashCode, and toString methods.
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 */
@Data
public class CompletionResponse {

	/**
	 * End reason constant indicating natural completion of the response.
	 */
	public static final String END_REASON_STOP = "stop";

	/**
	 * End reason constant indicating that the maximum token limit was reached.
	 */
	public static final String END_REASON_LENGTH = "length";

	/**
	 * End reason constant indicating that content was filtered by content policies.
	 */
	public static final String END_REASON_CONTENT_FILTER = "content_filter";

	/**
	 * End reason constant indicating that a function call was triggered.
	 */
	public static final String END_REASON_FUNCTION_CALL = "function_call";

	/**
	 * The unique identifier for the chat session.
	 * <p>
	 * This field contains the chat session ID when the response is part of a
	 * conversational interaction. For simple, stateless completions, this field
	 * will be {@code null}.
	 * </p>
	 * 
	 * @see #response
	 */
	private String chatId;

	/**
	 * The actual response content with multimodal support.
	 * <p>
	 * This field wraps the response content in a ContentWrapper object, which
	 * allows for handling different types of content including text, images, and
	 * other media formats supported by the OpenAI API.
	 * </p>
	 * 
	 * @see ContentWrapper
	 */
	public ContentWrapper response;

	/**
	 * The reason why the response generation was terminated.
	 * <p>
	 * This field indicates how the response generation ended. Common values
	 * include:
	 * <ul>
	 * <li>"stop" - Natural completion</li>
	 * <li>"length" - Maximum token limit reached</li>
	 * <li>"content_filter" - Content was filtered</li>
	 * <li>"function_call" - Function call was triggered</li>
	 * </ul>
	 * </p>
	 */
	public String endReason;

	/**
	 * Additional metadata and information from the API response.
	 * <p>
	 * This map contains supplementary data returned by the OpenAI API, such as:
	 * <ul>
	 * <li>Token usage statistics (prompt tokens, completion tokens, total
	 * tokens)</li>
	 * <li>Model information</li>
	 * <li>Processing time</li>
	 * <li>Cost information</li>
	 * <li>Other API-specific metadata</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The keys and values in this map depend on the specific API endpoint
	 * and request parameters used.
	 * </p>
	 */
	private Map<String, Object> info;

	/**
	 * Retrieves the text content from the response if available.
	 * <p>
	 * This method checks if the response is not null and if its type is TEXT.
	 * If both conditions are met, it returns the text content; otherwise, it
	 * returns null.
	 * </p>
	 * 
	 * @return the text content of the response, or null if not applicable
	 */
	public String getText() {
		if (this.response != null && this.response.getType() == ContentType.TEXT) {
			return response.getText();
		}
		return null;
	}

	/**
	 * Retrieves the raw content from the response.
	 * 
	 * @return
	 */
	public Object getContent() { return this.response != null ? this.response.getContent() : null; }

}
