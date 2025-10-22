# JSimpleLLM

A lightweight Java client library for OpenAI-compatible Large Language Model (LLM) APIs. 

JSimpleLLM provides a clean abstraction layer for integrating LLM capabilities into Java applications with support for text completion, chat functionality, embeddings, and streaming responses.

## Features

### Core Capabilities
- **Text Completion**: Single-shot text completions with system prompts
- **Chat Conversations**: Persistent chat sessions with message history
- **Embeddings**: Generate text embeddings for semantic analysis
- **Streaming Responses**: Real-time response streaming for interactive applications
- **Token Management**: Token counting and cost estimation
- **Text Summarization**: Intelligent content summarization

### Multimodal Support
- **Text Processing**: Rich text handling with ContentWrapper
- **Vision Support**: Image analysis capabilities (model-dependent)
- **Content Types**: Extensible content type system

### Advanced Features
- **Context Management**: Automatic conversation context handling
- **Model Configuration**: Flexible model definitions with capability tags
- **Error Handling**: Comprehensive exception hierarchy
- **Tools Integration**: Support for function calling and external tools
- **Session Persistence**: Chat session management and storage

### Web Search Integration (NEW! 🔍)
- **Real-time Web Search**: Access up-to-date information from the internet
- **Source Citations**: Automatic citation of sources for verifiable information
- **Search Providers**: Perplexity AI (implemented), DeepSeek, Gemini, Wikipedia (planned)
- **Advanced Filtering**: Domain filters, recency controls, date ranges
- **Related Questions**: AI-generated follow-up question suggestions
- **Search Metadata**: Persistent search results attached to chat messages
- **WebSearchFactory**: Dedicated factory for creating web search services

## Architecture

JSimpleLLM follows a clean, extensible architecture:

- **`LLMService`**: Core interface defining all LLM operations
- **`LLMServiceFactory`**: Factory pattern for creating service instances
- **`OpenAILLMService`**: Primary OpenAI API implementation
- **Chat System**: Complete conversation management (`Chat`, `Message`, `ChatManager`)
- **Configuration**: Flexible model and API configuration (`LLMConfig`)

### Model Types

The library supports various model capabilities:
- `EMBEDDING` - Text embeddings generation
- `LANGUAGE` - General language understanding
- `REASONING` - Advanced logical reasoning
- `CODING` - Code generation and analysis
- `VISION` - Image understanding
- `TOOLS` - Function calling capabilities
- `BATCH` - Batch processing support

## Quick Start

### Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>bor.tools</groupId>
    <artifactId>JSimpleLLM</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Basic Usage

```java
// Create service instance
LLMService service = LLMServiceFactory.createOpenAI(
    LLMConfig.builder()
        .apiTokenEnvironment("OPENAI_API_TOKEN")
        .baseUrl("https://api.openai.com/v1/")
        .build()
);

// Simple completion
CompletionResponse response = service.completion(
    "You are a helpful assistant",
    "Explain quantum computing",
    new MapParam()
);

// Chat conversation
Chat chat = new Chat();
chat.addSystemMessage("You are a coding assistant");
chat.addUserMessage("Write a hello world program in Java");

CompletionResponse chatResponse = service.chatCompletion(chat, "", new MapParam());
```

### Streaming Example

```java
ResponseStream stream = new ResponseStream() {
    @Override
    public void onToken(String token) {
        System.out.print(token);
    }
    
    @Override
    public void onComplete() {
        System.out.println("\n[Complete]");
    }
};

service.completionStream(stream, "You are helpful", "Tell me a joke", new MapParam());
```

### Working with Embeddings

```java
float[] embeddings = service.embeddings(
    "This is sample text to embed",
    "text-embedding-3-small",
    null
);
```

## Configuration

### Environment Variables

Set your API token as an environment variable:
```bash
export OPENAI_API_TOKEN=your_api_key_here
```

### Custom Configuration

```java
LLMConfig config = LLMConfig.builder()
    .baseUrl("https://api.openai.com/v1/")
    .apiToken("your-token")
    .build();

// Add custom model
config.getModelMap().put("custom-model", 
    new LLMConfig.Model("custom-model", 4096, MODEL_TYPE.LANGUAGE)
);
```

## Chat Management

### Basic Chat Operations

```java
Chat chat = new Chat("unique-chat-id");

// Add messages
chat.addSystemMessage("You are a helpful assistant");
chat.addUserMessage("Hello!");
chat.addAssistantMessage("Hi there! How can I help you?");

// Manage conversation
Message lastMessage = chat.getLastMessage();
int messageCount = chat.messageCount();
chat.clearMessages(); // Clear conversation history
```

### Message Content

Messages support rich content through `ContentWrapper`:

```java
// Text message
Message textMsg = new Message(MessageRole.USER, "Hello world");

// Custom content
ContentWrapper content = new ContentWrapper(ContentType.TEXT, "Custom content");
Message customMsg = new Message(MessageRole.ASSISTANT, content);
```

## Web Search

### WebSearchFactory (NEW! 🔍)

JSimpleLLM now includes dedicated web search capabilities through the `WebSearchFactory`. This factory creates services that implement the `WebSearch` interface, providing real-time access to web information with citations.

#### Quick Start - Web Search

```java
// Create web search service
WebSearch search = WebSearchFactory.createPerplexity();

// Simple search with citations
SearchResponse response = search.webSearch(
    "Latest developments in quantum computing",
    new MapParam().model("sonar-pro")
);

// Access results
System.out.println(response.getResponse().getText());
System.out.println("Sources: " + response.getCitations());
System.out.println("Related: " + response.getRelatedQuestions());
```

#### Environment Setup

```bash
# Set Perplexity API key
export PERPLEXITY_API_KEY=pplx-xxxxxxxxxxxxx
```

#### Advanced Search Features

```java
// Academic search with filters
MapParam params = new MapParam()
    .model("sonar-pro")
    .searchDomainFilter(new String[]{
        "arxiv.org",
        "scholar.google.com",
        "-wikipedia.org"  // Exclude Wikipedia
    })
    .searchRecencyFilter("week")      // Last week only
    .searchAfterDateFilter("01/01/2025")
    .returnRelatedQuestions(true)
    .maxTokens(1500);

SearchResponse response = search.webSearch(
    "Recent breakthroughs in CRISPR gene editing",
    params
);
```

#### Conversational Web Search

```java
Chat chat = new Chat();
WebSearch search = WebSearchFactory.createPerplexity();

// First question
SearchResponse r1 = search.webSearchChat(
    chat,
    "What is quantum computing?",
    null
);

// Follow-up (uses context!)
SearchResponse r2 = search.webSearchChat(
    chat,
    "What are its main applications?",
    null
);

// Access search metadata from messages
Message lastMsg = chat.getLastMessage();
if (lastMsg.hasSearchMetadata()) {
    SearchMetadata metadata = lastMsg.getSearchMetadata();
    System.out.println("Citations: " + metadata.getCitations());
}
```

#### Supported Search Providers

| Provider | Status | Features |
|----------|--------|----------|
| **Perplexity AI** | ✅ Implemented | Real-time search, citations, domain/recency filters |
| **DeepSeek** | 🔜 Planned | Chinese language optimization, reasoning |
| **Google Gemini** | 🔜 Planned | Google Search grounding, grounding chunks |
| **Wikipedia** | 🔜 Planned | Structured knowledge base search |
| **Tavily** | 🔜 Planned | Research-focused academic search |
| **Brave Search** | 🔜 Planned | Privacy-focused independent search |

#### WebSearchFactory vs LLMServiceFactory

Both factories can create Perplexity services, but with different semantics:

```java
// LLMServiceFactory - Returns LLMService (requires cast for web search)
LLMService service = LLMServiceFactory.createPerplexity();
WebSearch search = (WebSearch) service;  // Cast required

// WebSearchFactory - Returns WebSearch directly (cleaner)
WebSearch search = WebSearchFactory.createPerplexity();  // No cast!
```

**Use WebSearchFactory when:**
- You only need web search capabilities
- You want cleaner code without casts
- You're building search-focused applications

**Use LLMServiceFactory when:**
- You need full LLM service capabilities
- You're using Perplexity for both LLM and search

#### Complete Example

See [WebSearchFactoryExample.java](src/test/java/bor/tools/simplellm/impl/WebSearchFactoryExample.java) for comprehensive examples including:
- Basic usage
- Custom configuration
- Conversational search
- Streaming search
- Research assistant pattern

For detailed documentation, see [PERPLEXITY_TUTORIAL.md](PERPLEXITY_TUTORIAL.md).

## Error Handling

JSimpleLLM provides specific exception types:

```java
try {
    CompletionResponse response = service.completion(system, query, params);
} catch (LLMAuthenticationException e) {
    // Handle authentication errors
} catch (LLMRateLimitException e) {
    // Handle rate limiting
} catch (LLMNetworkException e) {
    // Handle network issues
} catch (LLMTimeoutException e) {
    // Handle timeouts
} catch (LLMException e) {
    // Handle general LLM errors
}
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/JSimpleLLM.git
cd JSimpleLLM

# Build with Maven
mvn clean compile
mvn package

# Install to local repository
mvn install
```

## Requirements

- Java 8 or higher
- Maven 3.6+
- Dependencies: Jackson (JSON processing), Lombok (code generation)

## Roadmap

### Core LLM Services
- ✅ OpenAI API integration
- ✅ Ollama local model support
- ✅ LM Studio support
- 🔄 Complete implementation of all service methods
- 📋 Anthropic Claude API support (OpenAI-compatible mode)
- 📋 Together AI support (OpenAI-compatible mode)
- 📋 Comprehensive test suite
- 📋 Advanced context management
- 📋 Batch processing capabilities

### Web Search Integration (NEW!)
- ✅ WebSearch interface and SearchResponse
- ✅ WebSearchFactory with provider enum
- ✅ Perplexity AI integration with full feature support
- ✅ Search metadata attached to messages
- ✅ Domain filtering and recency controls
- ✅ Conversational search with context
- ✅ Streaming search support
- 📋 DeepSeek web search integration
- 📋 Google Gemini grounding API
- 📋 Wikipedia structured search
- 📋 Tavily research API
- 📋 Brave Search API

## License

This project is licensed under the terms specified in the LICENSE file.

## Contributing

Contributions are welcome! Please feel free to submit pull requests, report issues, or suggest new features.

## Author

**Alessandro Borges** - Initial work and maintenance

---

*JSimpleLLM - Simplifying LLM integration for Java developers*
