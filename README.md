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

- ✅ OpenAI API integration
- 🔄 Complete implementation of all service methods
- 📋 Anthropic Claude API support
- 📋 Google Gemini API support
- 📋 Ollama local model support
- 📋 Comprehensive test suite
- 📋 Advanced context management
- 📋 Batch processing capabilities

## License

This project is licensed under the terms specified in the LICENSE file.

## Contributing

Contributions are welcome! Please feel free to submit pull requests, report issues, or suggest new features.

## Author

**Alessandro Borges** - Initial work and maintenance

---

*JSimpleLLM - Simplifying LLM integration for Java developers*
