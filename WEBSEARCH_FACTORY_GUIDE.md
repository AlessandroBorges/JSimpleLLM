# WebSearchFactory Guide

## Overview

`WebSearchFactory` is a specialized factory for creating web search service instances in JSimpleLLM. It provides a clean, semantic API for services that implement the `WebSearch` interface, offering real-time web search capabilities with citations.

## Why WebSearchFactory?

While `LLMServiceFactory` can create services with web search capabilities, `WebSearchFactory` offers several advantages for search-focused applications:

### Comparison

| Aspect | LLMServiceFactory | WebSearchFactory |
|--------|-------------------|------------------|
| **Return Type** | `LLMService` | `WebSearch` |
| **Cast Required** | Yes (`(WebSearch)`) | No |
| **Semantic Intent** | General LLM service | Web search service |
| **Use Case** | Full LLM + search | Search-focused |

### Example Comparison

```java
// Via LLMServiceFactory (traditional)
LLMService service = LLMServiceFactory.createPerplexity();
WebSearch search = (WebSearch) service;  // Cast required

// Via WebSearchFactory (cleaner)
WebSearch search = WebSearchFactory.createPerplexity();  // No cast!
```

## Supported Providers

### Currently Implemented

#### Perplexity AI ✅

Real-time web search with automatic citations.

**Features:**
- Real-time web search integration
- Automatic source citations
- Domain filtering (include/exclude domains)
- Recency filters (hour, day, week, month, year)
- Date range filtering
- Related questions
- Optional image results
- Geographic localization
- Streaming support

**Models:**
- `sonar` - Fast (128k context)
- `sonar-pro` - Advanced (200k context)
- `sonar-deep-research` - Exhaustive (128k context)
- `sonar-reasoning` - Reasoning + search (128k context)
- `sonar-reasoning-pro` - Advanced reasoning (128k context)
- `r1-1776` - Offline (no search, 128k context)

**Example:**
```java
WebSearch search = WebSearchFactory.createPerplexity();

SearchResponse response = search.webSearch(
    "Latest AI developments",
    new MapParam()
        .model("sonar-pro")
        .searchRecencyFilter("week")
        .returnRelatedQuestions(true)
);
```

### Planned Providers

#### DeepSeek 🔜

Chinese-language optimized web search.

**Planned Features:**
- Chinese language optimization
- Baidu/Bing search integration
- Advanced reasoning models
- Bilingual support (Chinese/English)

**Example (Future):**
```java
WebSearch search = WebSearchFactory.createDeepSeek();
// Currently throws UnsupportedOperationException
```

#### Google Gemini 🔜

Google Search integration via Grounding API.

**Planned Features:**
- Google Search grounding
- Grounding chunks with source attribution
- Grounding images
- Dynamic retrieval
- Web verification

**Example (Future):**
```java
WebSearch search = WebSearchFactory.createGemini();
// Currently throws UnsupportedOperationException
```

#### Wikipedia 🔜

Structured knowledge base search.

**Planned Features:**
- MediaWiki API integration
- Article full text and summaries
- Infobox extraction
- Category browsing
- Cross-references and links
- Multi-language support

**Example (Future):**
```java
WebSearch search = WebSearchFactory.createWikipedia();
// Currently throws UnsupportedOperationException
```

#### Tavily 🔜

Research-focused search API.

**Planned Features:**
- Research-optimized search
- Academic source prioritization
- Technical content extraction
- Source quality scoring
- Comprehensive result coverage

**Example (Future):**
```java
WebSearch search = WebSearchFactory.createTavily();
// Currently throws UnsupportedOperationException
```

#### Brave Search 🔜

Privacy-focused independent search.

**Planned Features:**
- Privacy-first design (no tracking)
- Independent search index
- Web, news, and image search
- Goggles (custom search rankings)
- Freshness and relevance controls

**Example (Future):**
```java
WebSearch search = WebSearchFactory.createBrave();
// Currently throws UnsupportedOperationException
```

## API Reference

### Factory Methods

#### Direct Provider Methods

```java
// Perplexity AI
WebSearch createPerplexity()
WebSearch createPerplexity(LLMConfig config)

// DeepSeek (future)
WebSearch createDeepSeek()
WebSearch createDeepSeek(LLMConfig config)

// Google Gemini (future)
WebSearch createGemini()
WebSearch createGemini(LLMConfig config)

// Wikipedia (future)
WebSearch createWikipedia()
WebSearch createWikipedia(LLMConfig config)

// Tavily (future)
WebSearch createTavily()
WebSearch createTavily(LLMConfig config)

// Brave Search (future)
WebSearch createBrave()
WebSearch createBrave(LLMConfig config)
```

#### Generic Provider Method

```java
WebSearch createWebSearch(WEBSEARCH_PROVIDER provider, LLMConfig config)
```

**Parameters:**
- `provider` - Enum value (PERPLEXITY, DEEPSEEK, GEMINI, WIKIPEDIA, TAVILY, BRAVE)
- `config` - Optional configuration (null for defaults)

**Throws:**
- `IllegalArgumentException` - if provider is null
- `UnsupportedOperationException` - if provider not yet implemented

### WEBSEARCH_PROVIDER Enum

```java
public enum WEBSEARCH_PROVIDER {
    PERPLEXITY,   // ✅ Implemented
    DEEPSEEK,     // 🔜 Planned
    GEMINI,       // 🔜 Planned
    WIKIPEDIA,    // 🔜 Planned
    TAVILY,       // 🔜 Planned
    BRAVE         // 🔜 Planned
}
```

## Usage Patterns

### Basic Usage

```java
import bor.tools.simplellm.websearch.*;

// Simple creation
WebSearch search = WebSearchFactory.createPerplexity();

// Perform search
SearchResponse response = search.webSearch(
    "What is quantum computing?",
    null  // Use defaults
);

System.out.println(response.getResponse().getText());
System.out.println("Sources: " + response.getCitations());
```

### Custom Configuration

```java
import bor.tools.simplellm.*;
import bor.tools.simplellm.websearch.*;

// Academic search configuration
MapParam academicDefaults = new MapParam()
    .searchMode("academic")
    .searchDomainFilter(new String[]{
        "arxiv.org",
        "scholar.google.com",
        "pubmed.ncbi.nlm.nih.gov"
    })
    .temperature(0.3f);

LLMConfig config = LLMConfig.builder()
    .apiTokenEnvironment("PERPLEXITY_API_KEY")
    .defaultModelName("sonar-pro")
    .defaultParams(academicDefaults)
    .build();

WebSearch search = WebSearchFactory.createPerplexity(config);
```

### Conversational Search

```java
Chat chat = new Chat();
WebSearch search = WebSearchFactory.createPerplexity();

// First question
SearchResponse r1 = search.webSearchChat(
    chat,
    "What is machine learning?",
    null
);

// Follow-up (uses context!)
SearchResponse r2 = search.webSearchChat(
    chat,
    "What are its applications?",
    null
);

// Access metadata from messages
Message lastMsg = chat.getLastMessage();
if (lastMsg.hasSearchMetadata()) {
    SearchMetadata metadata = lastMsg.getSearchMetadata();
    System.out.println("Citations: " + metadata.getCitations());
}
```

### Streaming Search

```java
WebSearch search = WebSearchFactory.createPerplexity();

ResponseStream stream = new ResponseStream() {
    @Override
    public void onToken(String token, ContentType type) {
        System.out.print(token);
    }

    @Override
    public void onComplete() {
        System.out.println("\n[Done]");
    }

    @Override
    public void onError(Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }
};

SearchResponse response = search.webSearchStream(
    stream,
    "Explain artificial intelligence",
    new MapParam().model("sonar")
);
```

### Multi-Provider Pattern (Future-Ready)

```java
public class MultiSourceSearchEngine {
    private final WebSearch perplexity;
    private final WebSearch gemini;
    private final WebSearch wikipedia;

    public MultiSourceSearchEngine() {
        this.perplexity = WebSearchFactory.createPerplexity();
        this.gemini = WebSearchFactory.createGemini();      // Future
        this.wikipedia = WebSearchFactory.createWikipedia(); // Future
    }

    public List<SearchResponse> searchAll(String query) {
        return List.of(
            perplexity.webSearch(query, null),
            gemini.webSearch(query, null),
            wikipedia.webSearch(query, null)
        );
    }

    public SearchResponse searchBest(String query) {
        if (query.contains("latest") || query.contains("recent")) {
            return perplexity.webSearch(query, null);
        } else if (isFactualQuery(query)) {
            return wikipedia.webSearch(query, null);
        } else {
            return gemini.webSearch(query, null);
        }
    }
}
```

## Environment Setup

### Perplexity AI

```bash
# Linux/Mac
export PERPLEXITY_API_KEY="pplx-xxxxxxxxxxxxx"

# Windows PowerShell
$env:PERPLEXITY_API_KEY="pplx-xxxxxxxxxxxxx"

# Windows CMD
set PERPLEXITY_API_KEY=pplx-xxxxxxxxxxxxx
```

Get your API key at: https://www.perplexity.ai/settings/api

## Error Handling

### Common Exceptions

```java
try {
    WebSearch search = WebSearchFactory.createPerplexity();
    SearchResponse response = search.webSearch("query", null);

} catch (LLMAuthenticationException e) {
    // API key missing or invalid
    System.err.println("Check PERPLEXITY_API_KEY environment variable");

} catch (LLMRateLimitException e) {
    // Rate limit exceeded
    System.err.println("Wait before retrying");

} catch (LLMNetworkException e) {
    // Network error
    System.err.println("Check internet connection");

} catch (LLMTimeoutException e) {
    // Request timeout
    System.err.println("Try reducing maxTokens or use faster model");

} catch (LLMException e) {
    // General error
    System.err.println("Error: " + e.getMessage());
}
```

### Handling Unsupported Providers

```java
try {
    WebSearch search = WebSearchFactory.createDeepSeek();
} catch (UnsupportedOperationException e) {
    System.out.println("Provider not yet implemented");
    // Fall back to Perplexity
    WebSearch search = WebSearchFactory.createPerplexity();
}
```

### Null Provider

```java
try {
    WebSearch search = WebSearchFactory.createWebSearch(null, null);
} catch (IllegalArgumentException e) {
    System.err.println("Provider cannot be null");
}
```

## Best Practices

### 1. Choose the Right Factory

```java
// ✅ GOOD - Use WebSearchFactory for search-focused apps
WebSearch search = WebSearchFactory.createPerplexity();

// ❌ AVOID - Using LLMServiceFactory when only doing search
LLMService service = LLMServiceFactory.createPerplexity();
WebSearch search = (WebSearch) service;  // Unnecessary cast
```

### 2. Reuse Service Instances

```java
// ✅ GOOD - Create once, reuse
public class SearchService {
    private final WebSearch search = WebSearchFactory.createPerplexity();

    public SearchResponse search(String query) {
        return search.webSearch(query, null);
    }
}

// ❌ AVOID - Creating new instance per request
public SearchResponse badSearch(String query) {
    WebSearch search = WebSearchFactory.createPerplexity();  // Wasteful
    return search.webSearch(query, null);
}
```

### 3. Configure Defaults Appropriately

```java
// ✅ GOOD - Set defaults for your use case
MapParam defaults = new MapParam()
    .searchMode("academic")
    .temperature(0.3f)
    .returnRelatedQuestions(true);

LLMConfig config = LLMConfig.builder()
    .defaultParams(defaults)
    .build();

WebSearch search = WebSearchFactory.createPerplexity(config);
```

### 4. Handle Future Providers Gracefully

```java
// ✅ GOOD - Graceful fallback
public WebSearch createSearchService(WEBSEARCH_PROVIDER preferred) {
    try {
        return WebSearchFactory.createWebSearch(preferred, null);
    } catch (UnsupportedOperationException e) {
        logger.warn("Provider {} not available, using Perplexity", preferred);
        return WebSearchFactory.createPerplexity();
    }
}
```

## Examples

Complete examples available in:
- [WebSearchFactoryExample.java](src/test/java/bor/tools/simplellm/impl/WebSearchFactoryExample.java)
- [PerplexityExample.java](src/test/java/bor/tools/simplellm/impl/PerplexityExample.java)
- [SearchMetadataExample.java](src/test/java/bor/tools/simplellm/impl/SearchMetadataExample.java)

## Related Documentation

- [PERPLEXITY_TUTORIAL.md](PERPLEXITY_TUTORIAL.md) - Complete Perplexity guide
- [SEARCHMETADATA_IMPLEMENTATION.md](SEARCHMETADATA_IMPLEMENTATION.md) - Search metadata in messages
- [WEBSEARCH_FACTORY_ANALYSIS.md](WEBSEARCH_FACTORY_ANALYSIS.md) - Technical analysis and design

## Contributing

To add support for a new web search provider:

1. Implement the `WebSearch` interface
2. Add the provider to `WEBSEARCH_PROVIDER` enum
3. Add factory method in `WebSearchFactory`
4. Add tests in `WebSearchFactoryTest`
5. Update documentation

See existing `PerplexityLLMService` implementation as reference.

---

**Author:** Alessandro Borges
**Version:** JSimpleLLM 1.1
**Date:** 2025-01-22
