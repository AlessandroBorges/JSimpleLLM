package bor.tools.simplellm.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ResponseStream;
import bor.tools.simplellm.chat.ContentType;
import bor.tools.simplellm.chat.ContentWrapper;
import bor.tools.simplellm.exceptions.LLMException;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Utility class for handling Server-Sent Events (SSE) streaming from OpenAI
 * API.
 * <p>
 * This class provides methods to parse SSE streams, extract content chunks,
 * and manage streaming responses with proper error handling and threading.
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 */
public class StreamingUtil {

	private final OpenAIJsonMapper jsonMapper;
	private final ExecutorService  executorService;

	public StreamingUtil(OpenAIJsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
		this.executorService = Executors.newCachedThreadPool(r -> {
			Thread thread = new Thread(r, "JSimpleLLM-Streaming");
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Processes an SSE streaming response in real-time.
	 * This method reads the stream line by line as data arrives from the network.
	 *
	 * @param response       the HTTP response containing SSE stream
	 * @param responseStream callback interface for handling tokens
	 *
	 * @return Future containing the complete accumulated response
	 */
	public Future<CompletionResponse> processStreamingResponse(Response response, ResponseStream responseStream) {
		return executorService.submit(() -> {
			StringBuilder      fullContent   = new StringBuilder();
			StringBuilder      fullReasoning = new StringBuilder();
			CompletionResponse finalResponse = new CompletionResponse();
			//StringBuilder      lineBuffer    = new StringBuilder();

			try (ResponseBody body = response.body()) {
				if (body == null) {
					throw new LLMException("Empty response body for streaming request");
				}

				// Use character stream for real-time line-by-line reading
				try (BufferedReader reader = new BufferedReader(body.charStream())) {
					String line;

					// Read each line as it arrives from the network
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("data: ")) {
							String sseData = line.substring(6).trim(); // Remove "data: " prefix

							if ("[DONE]".equals(sseData)) {
								// Stream completed
								break;
							}

							if (!sseData.isEmpty()) {
								processSSEChunk(sseData, fullContent, fullReasoning,  finalResponse, responseStream);
							}
						}
						// Handle other SSE fields if needed (event:, id:, retry:)
						else if (line.startsWith("event: ") || line.startsWith("id: ") || line.startsWith("retry: ")) {
							// These can be logged or processed if needed for debugging
						}
						// Empty lines separate SSE events - can be ignored
					}
				}

				responseStream.onComplete();

				// Set the accumulated content in final response
				finalResponse.setResponse(new ContentWrapper(ContentType.TEXT, fullContent.toString()));
				if(fullReasoning.length() > 0) {
					finalResponse.setReasoningContent(fullReasoning.toString());
				}
				return finalResponse;

			} catch (IOException | LLMException e) {
				responseStream.onError(e);
				throw new RuntimeException("Failed to process streaming response", e);
			}
		});
	}

	/**
	 * Processes streaming response from a ResponseBody source in real-time.
	 * This method reads the stream line by line as data arrives from the network.
	 *
	 * @param responseBody   the response body containing SSE data
	 * @param responseStream callback interface for handling tokens
	 *
	 * @return Future containing the complete accumulated response
	 */
	public Future<CompletionResponse> processStreamingFromBody(ResponseBody responseBody, ResponseStream responseStream) {
		return executorService.submit(() -> {
			StringBuilder      fullContent   = new StringBuilder();
			StringBuilder      fullReasoning = new StringBuilder();
			CompletionResponse finalResponse = new CompletionResponse();

			try {
				// Use character stream for real-time line-by-line reading
				try (BufferedReader reader = new BufferedReader(responseBody.charStream())) {
					String line;

					// Read each line as it arrives from the network
					while ((line = reader.readLine()) != null) {
						if (line.startsWith("data: ")) {
							String sseData = line.substring(6).trim();

							if ("[DONE]".equals(sseData)) {
								break;
							}

							if (!sseData.isEmpty()) {
								processSSEChunk(sseData, fullContent, fullReasoning, finalResponse, responseStream);
							}
						}
						// Handle other SSE fields if needed (event:, id:, retry:)
						else if (line.startsWith("event: ") || line.startsWith("id: ") || line.startsWith("retry: ")) {
							// These can be logged or processed if needed for debugging
						}
						// Empty lines separate SSE events - can be ignored
					}
				}

				responseStream.onComplete();
				String fullText = fullContent.toString();
				finalResponse.setResponse(new ContentWrapper(ContentType.TEXT, fullText));
				if(fullReasoning.length() > 0) {
					finalResponse.setReasoningContent(fullReasoning.toString());
				} else {
					if(fullText.contains("<think>") && fullText.contains("</think>")) {
						String reasoning = fullText.substring(fullText.indexOf("<think>") + 8, fullText.indexOf("</think>"));
						finalResponse.setReasoningContent(reasoning.trim());
						// must remove reasoning from content  						
						int start = fullText.indexOf("<think>");
						int end = fullText.indexOf("</think>") + 9;
						String cleanedContent = fullText.substring(0, start) + fullText.substring(end);
						finalResponse.setResponse(new ContentWrapper(ContentType.TEXT, cleanedContent.trim()));
					}
				}
				return finalResponse;

			} catch (Exception e) {
				responseStream.onError(e);
				throw new RuntimeException("Failed to process streaming response", e);
			}
		});
	}

	/**
	 * Processes streaming response using byte-level reading for maximum real-time performance.
	 * This method processes data as soon as bytes arrive from the network.
	 *
	 * @param responseBody   the response body containing SSE data
	 * @param responseStream callback interface for handling tokens
	 *
	 * @return Future containing the complete accumulated response
	 */
	public Future<CompletionResponse> processStreamingFromByteStream(ResponseBody responseBody, ResponseStream responseStream) {
		return executorService.submit(() -> {
			StringBuilder      fullContent   = new StringBuilder();
			StringBuilder	  fullReasoning = new StringBuilder();
			CompletionResponse finalResponse = new CompletionResponse();
			StringBuilder      lineBuffer    = new StringBuilder();

			try (java.io.InputStream inputStream = responseBody.byteStream();
			     java.io.InputStreamReader reader = new java.io.InputStreamReader(inputStream, "UTF-8")) {

				int ch;
				// Read character by character as data arrives
				while ((ch = reader.read()) != -1) {
					char character = (char) ch;

					// Build line character by character
					if (character == '\n' || character == '\r') {
						if (lineBuffer.length() > 0) {
							String line = lineBuffer.toString();
							processSSELine(line, fullContent, fullReasoning, finalResponse, responseStream);
							lineBuffer.setLength(0); // Clear buffer
						}
					} else {
						lineBuffer.append(character);
					}
				}

				// Process any remaining content in buffer
				if (lineBuffer.length() > 0) {
					String line = lineBuffer.toString();
					processSSELine(line, fullContent, fullReasoning, finalResponse, responseStream);
				}

				responseStream.onComplete();
				finalResponse.setResponse(new ContentWrapper(ContentType.TEXT, fullContent.toString()));
				return finalResponse;

			} catch (Exception e) {
				responseStream.onError(e);
				throw new RuntimeException("Failed to process streaming response", e);
			}
		});
	}

	/**
	 * Processes a single SSE line.
	 *
	 * @param line           the SSE line to process
	 * @param fullContent    accumulator for full response content
	 * @param finalResponse  the final response object being built
	 * @param responseStream callback for token events
	 */
	private void processSSELine(String line,
	                            StringBuilder fullContent,
	                            StringBuilder fullReasoning,
	                            CompletionResponse finalResponse,
	                            ResponseStream responseStream) {
		if (line.startsWith("data: ")) {
			String sseData = line.substring(6).trim();

			if ("[DONE]".equals(sseData)) {
				return; // Stream completed signal
			}

			if (!sseData.isEmpty()) {
				processSSEChunk(sseData, fullContent, fullReasoning, finalResponse, responseStream);
			}
		}
		// Handle other SSE fields if needed (event:, id:, retry:)
		else if (line.startsWith("event: ") || line.startsWith("id: ") || line.startsWith("retry: ")) {
			// These can be logged or processed if needed for debugging
		}
	}

	/**
	 * Processes a single SSE data chunk.
	 * 
	 * @param sseData        the SSE data (JSON string)
	 * @param fullContent    accumulator for full response content
	 * @param finalResponse  the final response object being built
	 * @param responseStream callback for token events
	 */
	private void processSSEChunk(String sseData,
	                             StringBuilder fullContent,
	                             StringBuilder fullReasoning,
	                             CompletionResponse finalResponse,
	                             ResponseStream responseStream) {
		try {
			CompletionResponse chunk = jsonMapper.fromStreamingChunk(sseData);
			if (chunk != null ) {
				String content = (chunk.getResponse()!=null)? chunk.getResponse().getText() : null;
				String reasoning = chunk.getReasoningContent();
				
				if (content != null) {
					fullContent.append(content);
					responseStream.onToken(content, ResponseStream.ContentType.TEXT);
				}
				if (reasoning != null) {
					fullReasoning.append(reasoning);
					responseStream.onToken(reasoning, ResponseStream.ContentType.REASONING);
				}

				// Update metadata from chunk
				if (chunk.getEndReason() != null) {
					finalResponse.setEndReason(chunk.getEndReason());
				}
				if (chunk.getInfo() != null) {
					finalResponse.setInfo(chunk.getInfo());
				}
			}
		} catch (LLMException e) {
			System.err.println("Failed to parse SSE chunk: "
			            + e.getMessage());
		}
	}

	/**
	 * Parses a single SSE line and extracts the data content.
	 * 
	 * @param line the raw SSE line
	 * 
	 * @return the data content or null if not a data line
	 */
	public String parseSSELine(String line) {
		if (line != null && line.startsWith("data: ")) {
			return line.substring(6).trim();
		}
		return null;
	}

	/**
	 * Checks if an SSE data line indicates completion.
	 * 
	 * @param sseData the SSE data content
	 * 
	 * @return true if this indicates stream completion
	 */
	public boolean isStreamComplete(String sseData) {
		return "[DONE]".equals(sseData);
	}

	/**
	 * Shuts down the executor service.
	 * Should be called when the StreamingUtil is no longer needed.
	 */
	public void shutdown() {
		executorService.shutdown();
	}
}
