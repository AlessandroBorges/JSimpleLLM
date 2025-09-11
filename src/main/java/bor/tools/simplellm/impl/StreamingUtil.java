package bor.tools.simplellm.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
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
	 * Processes an SSE streaming response in the background.
	 * 
	 * @param response       the HTTP response containing SSE stream
	 * @param responseStream callback interface for handling tokens
	 * 
	 * @return Future containing the complete accumulated response
	 */
	public Future<CompletionResponse> processStreamingResponse(Response response, ResponseStream responseStream) {
		return executorService.submit(() -> {
			StringBuilder      fullContent   = new StringBuilder();
			CompletionResponse finalResponse = new CompletionResponse();

			try (ResponseBody body = response.body()) {
				if (body == null) {
					throw new LLMException("Empty response body for streaming request");
				}

				String         responseString = body.string();
				BufferedReader reader         = new BufferedReader(new StringReader(responseString));
				String         line;

				while ((line = reader.readLine()) != null) {
					if (line.startsWith("data: ")) {
						String sseData = line.substring(6); // Remove "data: " prefix

						if ("[DONE]".equals(sseData.trim())) {
							// Stream completed
							break;
						}

						try {
							CompletionResponse chunk = jsonMapper.fromStreamingChunk(sseData);
							if (chunk != null && chunk.getResponse() != null) {
								String content = chunk.getResponse().getText();
								if (content != null) {
									fullContent.append(content);
									responseStream.onToken(content);
								}

								// Update final response metadata from last chunk
								if (chunk.getEndReason() != null) {
									finalResponse.setEndReason(chunk.getEndReason());
								}
								if (chunk.getInfo() != null) {
									finalResponse.setInfo(chunk.getInfo());
								}
							}
						} catch (LLMException e) {
							// Log parsing error but continue processing
							System.err.println("Failed to parse streaming chunk: "
							            + e.getMessage());
						}
					}
				}

				responseStream.onComplete();

				// Set the accumulated content in final response
				finalResponse.setResponse(new ContentWrapper(ContentType.TEXT, fullContent.toString()));
				return finalResponse;

			} catch (IOException | LLMException e) {
				responseStream.onError(e);
				throw new RuntimeException("Failed to process streaming response", e);
			}
		});
	}

	/**
	 * Processes streaming response from a ResponseBody source.
	 * This method reads the stream line by line and processes SSE events.
	 * 
	 * @param responseBody   the response body containing SSE data
	 * @param responseStream callback interface for handling tokens
	 * 
	 * @return Future containing the complete accumulated response
	 */
	public Future<CompletionResponse> processStreamingFromBody(ResponseBody responseBody, ResponseStream responseStream) {
		return executorService.submit(() -> {
			StringBuilder      fullContent   = new StringBuilder();
			CompletionResponse finalResponse = new CompletionResponse();

			try {
				String   responseString = responseBody.string();
				String[] lines          = responseString.split("\\r?\\n");

				for (String line : lines) {
					if (line.startsWith("data: ")) {
						String sseData = line.substring(6).trim();

						if ("[DONE]".equals(sseData)) {
							break;
						}

						if (!sseData.isEmpty()) {
							processSSEChunk(sseData, fullContent, finalResponse, responseStream);
						}
					}
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
	 * Processes a single SSE data chunk.
	 * 
	 * @param sseData        the SSE data (JSON string)
	 * @param fullContent    accumulator for full response content
	 * @param finalResponse  the final response object being built
	 * @param responseStream callback for token events
	 */
	private void processSSEChunk(String sseData,
	                             StringBuilder fullContent,
	                             CompletionResponse finalResponse,
	                             ResponseStream responseStream) {
		try {
			CompletionResponse chunk = jsonMapper.fromStreamingChunk(sseData);
			if (chunk != null && chunk.getResponse() != null) {
				String content = chunk.getResponse().getText();
				if (content != null) {
					fullContent.append(content);
					responseStream.onToken(content);
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
