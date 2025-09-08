# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JSimpleLLM is a thin Java client library for OpenAI-compatible APIs. It provides a clean abstraction layer for LLM operations including text completion, chat functionality, embeddings generation, and text summarization.

## Build Commands

This is a Maven project. Common commands:

- **Build**: `mvn compile`
- **Package**: `mvn package` 
- **Clean**: `mvn clean`
- **Install**: `mvn install`
- **Test**: `mvn test` (no tests currently exist)

## Architecture Overview

### Core Components

- **LLMService**: Main interface defining all LLM operations (completion, chat, embeddings, streaming)
- **LLMServiceFactory**: Factory class for creating service implementations (currently OpenAI only)
- **OpenAILLMService**: Primary implementation of LLMService for OpenAI API
- **Chat System**: Complete chat management with messages, persistence, and context handling
- **Configuration**: LLMConfig with model definitions and API settings

### Key Classes

- `LLMService` (src/main/java/bor/tools/simplellm/LLMService.java): Core service interface
- `OpenAILLMService` (src/main/java/bor/tools/simplellm/impl/OpenAILLMService.java): OpenAI implementation
- `Chat` (src/main/java/bor/tools/simplellm/chat/Chat.java): Chat session management
- `LLMConfig`: Configuration and model definitions
- `CompletionResponse`: Response wrapper for completions
- `MapParam`: Parameter handling for API calls

### Package Structure

- `bor.tools.simplellm`: Core interfaces and main classes
- `bor.tools.simplellm.impl`: Service implementations
- `bor.tools.simplellm.chat`: Chat-related classes
- `bor.tools.simplellm.exceptions`: Custom exception hierarchy

### Dependencies

- **Lombok**: For reducing boilerplate code (@Data, @Builder annotations)
- **Jackson**: JSON processing (jackson-core, jackson-databind)
- Uses provided scope for Jackson and Lombok (expect consuming projects to provide)

### Implementation Status

Most methods in OpenAILLMService contain TODO stubs - the interface is defined but implementations are not complete. The project appears to be in early development phase with comprehensive documentation but minimal working functionality.

### Future Extensions

The factory pattern suggests planned support for additional LLM providers (Claude, Gemini) as indicated by comments in LLMServiceFactory.