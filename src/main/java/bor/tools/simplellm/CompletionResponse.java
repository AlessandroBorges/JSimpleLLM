package bor.tools.simplellm;

import java.util.List;

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
@JsonPropertyOrder({ "chatId", "model", "reasoning_effort", "reasoning_content", "response",  "endReason", "usage", "info" })

public class CompletionResponse {

	private static final String REASONING_END = "</reasoning>";

	private static final String REASONING = "<reasoning>";

	private static final String THINK = "<think>";

	private static final String THINK_END = "</think>";

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
	protected ContentWrapper response;

	/**
	 * Optional reasoning or thinking process behind the response.
	 * <p>
	 * This field may contain additional context or explanation about how the
	 * response was generated, if provided by the model. It is not always present
	 * and may be {@code null} for many responses.
	 * </p>
	 */
	@JsonAlias({ "reasoning_content"})
	protected String reasoningContent; // optional reasoning or thinking process
	
	/**
	 * Optional reasoning effort level used during response generation.
	 */
	protected Reasoning_Effort reasoningEffort; // optional reasoning effort level

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
	protected String endReason;

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
	protected MapParam info;

	/**
	 * Completion usage statistics.
	 * May contain:
	 * <li>"prompt_tokens" ,
	 * <li>"completion_tokens",
	 * <li>"total_tokens"
	 */
	protected MapParam usage; // for any custom data

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
	 * Sets the text content of the response.
	 * If content is already of type TEXT, it updates the content. <br>
	 * 
	 * It also extracts any reasoning content enclosed within &lt;reasoning&gt;...&lt;/reasoning&gt;
	 * or &lt;think&gt;...&lt;/think&gt; tags and stores it in the reasoningContent field.
	 * <p>
	 * This method updates the response field with a new ContentWrapper of type
	 * TEXT containing the provided text. If the response is already of type TEXT,
	 * it simply updates its content.
	 * </p>
	 * 
	 * @param text the text content to set in the response
	 */
	@JsonIgnore
	public void setText(String text) {
		var reasoning = this.extractReasoning(text);
		if (reasoning != null && !reasoning.isEmpty()) {
			this.reasoningContent = reasoning;
			text = text.replaceAll(reasoning, "").trim();
		}
		if (this.response != null && this.response.getType() == ContentType.TEXT) {	
			this.response.setContent(text);
		} else {
			this.response = new ContentWrapper(ContentType.TEXT, text);
		}		
	}
	
	/**
	 * Retrieves the reasoning content from the response if available.
	 * <p>
	 * This method first checks if the {@code reasoningContent} field is
	 * already populated. If not, it attempts to extract the reasoning content
	 * from the {@code info} map, looking for a key named "reasoning_content".
	 * If the key is found, its value is assigned to {@code reasoningContent}
	 * and returned. If the key is not present or the {@code info} map is null,
	 * it returns {@code null}.
	 * </p>
	 * 
	 * @return the reasoning content as a string, or null if not available
	 */
	public String getReasoningContent() {
		if (reasoningContent != null)
			return reasoningContent;
		else {
			// check if info has reasoning_content
			if (this.info != null) {
				if (this.info.containsKey("reasoning_content")) {
					this.reasoningContent = this.info.get("reasoning_content").toString();
				}
			}
			if (this.info.containsKey("choices")) {
				if (this.info.get("choices") instanceof List<?> == false) {
					List<?> choices = (List<?>) this.info.get("choices");
					if (choices.size() > 0) {
						Object choice0 = choices.get(0);
						if (choice0 instanceof MapParam) {
							MapParam choiceMap = (MapParam) choice0;
							if (choiceMap.containsKey("reasoning_content")) {
								this.reasoningContent = choiceMap.get("reasoning_content").toString();
							}
						}
					}
				}
			}			
			// check if text response has reasoning content inside <reasoning>...</reasoning> or <think>...</think>
			String text = getText();
			reasoningContent = extractReasoning(text);				
		}
		return reasoningContent;
	}
	
	/**
	 * Extracts reasoning content from the provided text.
	 * <p>
	 * This method searches for reasoning content enclosed within
	 * &lt;reasoning&gt;...&lt;/reasoning&gt; or &lt;think&gt;...&lt;/think&gt;
	 * tags in the given text. If such content is found and the
	 * {@code reasoningContent} field is not already populated, it extracts the
	 * reasoning content, assigns it to {@code reasoningContent}.
	 * </p>
	 * 
	 * @param text the text to extract reasoning content from
	 */
	protected String extractReasoning(String text) {
		String reasoning = null;
		 // only if not already set
		if (text != null && this.reasoningContent == null) {
			int start = text.indexOf(REASONING);
			int end = text.indexOf(REASONING_END);
			if (start >= 0 && end > start) {
				reasoning = text.substring(start, end + REASONING_END.length()).trim();
				//text = text.replaceAll("(?s)<reasoning>.*?</reasoning>", "").trim();
				//setText(text);
			} else {
				start = text.indexOf(THINK);
				end = text.indexOf(THINK_END);
				if (start >= 0 && end > start) {
					reasoning = text.substring(start, end + THINK_END.length()).trim();
					//text = text.replaceAll("(?s)<think>.*?</think>", "").trim();
					//this.setText(text);
				}
			}				
		}	
		return reasoning;
	}

	/**
	 * Retrieves the raw content from the response.
	 * 
	 * @return
	 */
	@JsonIgnore
	public Object getContent() { 
		return this.response != null ? this.response.getContent() : null; 
	}

	/**
	 * Returns a JSON string representation of the CompletionResponse object.
	 */
	@Override
	public String toString() {
		var mapper = Utils.createJsonMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "CompletionResponse{chatId="
		            + chatId
		            + ",\n response="		           
		            + getReasoningContent()
		            + getResponse()
		            + ",\n reasoningContent="
		            + ",\n endReason="		            
		            + endReason
		            + ",\n info="
		            + info
		            + "}";
	}

}
