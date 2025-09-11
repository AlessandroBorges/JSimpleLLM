package bor.tools.simplellm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.ContentWrapper;
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
@JsonPropertyOrder({ "chatId", "model", "response", "reasoning", "endReason", "usage", "info" })
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
	 * Optional reasoning or thinking process behind the response.
	 * <p>
	 * This field may contain additional context or explanation about how the
	 * response was generated, if provided by the model. It is not always present
	 * and may be {@code null} for many responses.
	 * </p>
	 */
	@JsonAlias({ "reasoning", "reasoning_content", "thinking", "think" })
	public String reasoning; // optional reasoning or thinking process

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
	private MapParam info;

	/**
	 * Completion usage statistics.
	 * May contain:
	 * <li>"prompt_tokens" ,
	 * <li>"completion_tokens",
	 * <li>"total_tokens"
	 */
	private MapParam usage; // for any custom data

	/**
	 * Retrieves the model name used for generating the response.
	 * <p>
	 * This method checks if the {@code info} map contains a key named "model"
	 * and returns its value as a string. If the key is not present or the
	 * {@code info} map is null, it returns {@code null}.
	 * </p>
	 * 
	 * @return the model name as a string, or null if not available
	 */
	public String getModel() {
		if (this.info != null && this.info.containsKey("model")) {
			return this.info.get("model").toString();
		}
		return null;
	}

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
	@JsonIgnore
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
	@JsonIgnore
	public Object getContent() { return this.response != null ? this.response.getContent() : null; }

	/**
	 * Returns a JSON string representation of the CompletionResponse object.
	 */
	@Override
	public String toString() {
		var mapper = Utils.createJsonMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {

		}
		return "CompletionResponse{chatId="
		            + chatId
		            + ", response="
		            + response
		            + ", endReason="
		            + endReason
		            + ", info="
		            + info
		            + "}";
	}

}
