# OpenAI Implementation Recommendations

## Analysis Summary

Based on the study of the OpenAILLMService code, here are the findings and recommendations for implementing the service with OkHttp and JSON conversion.

### Current State Analysis

**OpenAILLMService Implementation Status:**
- All methods are TODO stubs - no actual HTTP implementation exists
- Well-defined model configurations and interfaces 
- Uses Jackson for JSON processing (already in dependencies)
- Comment mentions "All Connections are made using OkHttp3 and JSON internally" but OkHttp not yet added

**JSON Conversion Needs:**
- **Input**: Chat messages, completion parameters, embedding requests
- **Output**: CompletionResponse with ContentWrapper, usage statistics, metadata
- Complex nested structures (Chat → Messages → ContentWrapper)
- Need to handle streaming responses (Server-Sent Events)

## Implementation Recommendations

### 1. Add OkHttp Dependency
Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

### 2. JSON Strategy: **Hybrid Approach**
- **Use `Map<String,Object>` for API request/response marshalling** (simpler, flexible)
- **Keep existing POJOs for type safety** (CompletionResponse, Chat, Message)

**Benefits:**
- `Map<String,Object>` simplifies dynamic OpenAI API parameters
- Jackson handles Map ↔ JSON seamlessly
- Existing POJOs provide type safety for internal operations
- Easy to adapt to API changes without modifying POJOs

### 3. Specialized JSON Handler Class
Create `OpenAIJsonMapper` utility class:

```java
public class OpenAIJsonMapper {
    private final ObjectMapper mapper;
    
    // Convert Chat to OpenAI chat completion request
    public Map<String,Object> toChatCompletionRequest(Chat chat, String query, MapParam params)
    
    // Convert OpenAI response to CompletionResponse
    public CompletionResponse fromChatCompletionResponse(Map<String,Object> response)
    
    // Handle streaming response parsing
    public CompletionResponse fromStreamingChunk(String sseData)
}
```

### 4. HTTP Client Architecture
Structure within `OpenAILLMService`:

```java
private final OkHttpClient httpClient;
private final OpenAIJsonMapper jsonMapper;

// Core HTTP methods
private Map<String,Object> postRequest(String endpoint, Map<String,Object> payload)
private void streamRequest(String endpoint, Map<String,Object> payload, ResponseStream stream)
```

### 5. Key Implementation Points

**Chat Completion Flow:**
1. `Chat` + params → `Map<String,Object>` (request payload)
2. OkHttp POST to `/chat/completions`
3. Response `Map<String,Object>` → `CompletionResponse`

**Streaming:**
- Use OkHttp's SSE support or manual parsing
- Parse `data: {...}` chunks incrementally
- Call `ResponseStream.onToken()` for each chunk

**Error Handling:**
- Map HTTP status codes to existing `LLMException` hierarchy
- Parse error responses into exception messages

### 6. OpenAI API Endpoints

**Primary Endpoints:**
- `/chat/completions` - Chat completions (streaming and non-streaming)
- `/completions` - Legacy completions (deprecated)
- `/responses` - New responses endpoint (for certain models)
- `/embeddings` - Text embeddings generation

**Request/Response Patterns:**
- Standard JSON request/response format
- Streaming uses Server-Sent Events (SSE) with `data:` prefixed JSON chunks
- Error responses include `error` object with `type`, `message`, and optional `code`

### 7. Implementation Priority

1. **Phase 1**: Core infrastructure (OkHttpClient, JsonMapper, error handling)
2. **Phase 2**: Chat completion (non-streaming)
3. **Phase 3**: Streaming support
4. **Phase 4**: Embeddings, token counting, and other services

## Conclusion

The **Map<String,Object> + specialized mapper** approach offers the best balance of simplicity, flexibility, and type safety for implementing the OpenAI API integration with OkHttp and Jackson.

This hybrid approach allows:
- Rapid development with flexible JSON handling
- Type safety where it matters most (internal APIs)
- Easy adaptation to OpenAI API changes
- Clean separation of concerns between HTTP transport and business logic