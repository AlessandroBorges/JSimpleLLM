package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.ResponseStream;

/**
 * Unit tests for StreamingUtil class.
 * <p>
 * These tests verify SSE parsing and streaming utility functionality
 * without requiring actual API calls.
 * </p>
 */
class StreamingUtilTest {

	private StreamingUtil      streamingUtil;
	private OpenAIJsonMapper   jsonMapper;
	private TestResponseStream testStream;

	@BeforeEach
	void setUp() {
		jsonMapper = new OpenAIJsonMapper();
		streamingUtil = new StreamingUtil(jsonMapper);
		testStream = new TestResponseStream();
	}

	@Test
	@DisplayName("Test SSE line parsing")
	void testSSELineParsing() {
		// Test valid data line
		String dataLine = "data: {\"test\":\"content\"}";
		String parsed   = streamingUtil.parseSSELine(dataLine);
		assertEquals("{\"test\":\"content\"}", parsed);

		// Test non-data line
		String eventLine   = "event: message";
		String parsedEvent = streamingUtil.parseSSELine(eventLine);
		assertNull(parsedEvent);

		// Test null input
		assertNull(streamingUtil.parseSSELine(null));

		// Test empty line
		assertNull(streamingUtil.parseSSELine(""));
	}

	@Test
	@DisplayName("Test stream completion detection")
	void testStreamCompletionDetection() {
		assertTrue(streamingUtil.isStreamComplete("[DONE]"));
		assertFalse(streamingUtil.isStreamComplete("{\"choices\":[]}"));
		assertFalse(streamingUtil.isStreamComplete(""));
		assertFalse(streamingUtil.isStreamComplete(null));
	}

	@Test
	@DisplayName("Test streaming chunk processing via OpenAIJsonMapper")
	void testStreamingChunkProcessing() throws Exception {
		// Test valid streaming chunk
		String validChunk = """
		            {
		              "id": "chatcmpl-test",
		              "object": "chat.completion.chunk",
		              "created": 1677652288,
		              "model": "gpt-4",
		              "choices": [{
		                "delta": {
		                  "content": "Hello"
		                },
		                "index": 0,
		                "finish_reason": null
		              }]
		            }""";

		CompletionResponse response = jsonMapper.fromStreamingChunk(validChunk);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals("Hello", response.getResponse().getText());

		// Test completion chunk
		String completionChunk = """
		            {
		              "id": "chatcmpl-test",
		              "object": "chat.completion.chunk",
		              "created": 1677652288,
		              "model": "gpt-4",
		              "choices": [{
		                "delta": {},
		                "index": 0,
		                "finish_reason": "stop"
		              }]
		            }""";

		CompletionResponse completionResponse = jsonMapper.fromStreamingChunk(completionChunk);
		assertNotNull(completionResponse);
		assertEquals("stop", completionResponse.getEndReason());

		// Test DONE signal
		assertNull(jsonMapper.fromStreamingChunk("[DONE]"));

		// Test empty chunk
		assertNull(jsonMapper.fromStreamingChunk(""));
	}

	@Test
	@DisplayName("Test malformed chunk handling")
	void testMalformedChunkHandling() {
		// Should throw exception for malformed JSON (this is expected behavior)
		assertThrows(Exception.class,
		             () -> { jsonMapper.fromStreamingChunk("{invalid json}"); },
		             "Should throw exception for malformed JSON");

		// But should handle null and empty gracefully
		assertDoesNotThrow(() -> {
			assertNull(jsonMapper.fromStreamingChunk(null));
			assertNull(jsonMapper.fromStreamingChunk(""));
			assertNull(jsonMapper.fromStreamingChunk("   "));
		});
	}

	@Test
	@DisplayName("Test StreamingUtil shutdown")
	void testStreamingUtilShutdown() {
		// Should not throw exception
		assertDoesNotThrow(() -> streamingUtil.shutdown());
	}

	/**
	 * Helper class for testing ResponseStream callbacks
	 */
	public static class TestResponseStream implements ResponseStream {
		private final List<String>               tokens    = new ArrayList<>();
		private final List<String>               tokensReasoning    = new ArrayList<>();
		private final List<String>               tokensToolCalls    = new ArrayList<>();
		private final AtomicBoolean              completed = new AtomicBoolean(false);
		private final AtomicReference<Throwable> error     = new AtomicReference<>();

		@Override
		public void onToken(String token, ContentType type) {
			switch (type) {
				case TEXT -> tokens.add(token);
				case REASONING -> tokensReasoning.add(token);
				default -> tokensToolCalls.add(token); // For simplicity, add others tokensToolCalls
			}			
		}

		@Override
		public void onComplete() {
			completed.set(true);
		}

		@Override
		public void onError(Throwable throwable) {
			error.set(throwable);
		}

		public List<String> getTokens() { return new ArrayList<>(tokens); }
		
		public List<String> getTokensReasoning() { return new ArrayList<>(tokensReasoning); }
		
		public List<String> getTokensToolCalls() { return new ArrayList<>(tokensToolCalls); }

		public boolean isCompleted() { return completed.get(); }

		public Throwable getError() { return error.get(); }
	}
}
