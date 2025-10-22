# WebSearchFactory - Análise de Viabilidade e Design

## Resumo Executivo

✅ **VIÁVEL** - A implementação atual permite criar `WebSearchFactory` sem quebrar interfaces existentes.

## Análise da Arquitetura Atual

### Hierarquia de Interfaces

```
┌─────────────┐
│ LLMService  │ (Interface base - operações LLM genéricas)
└─────────────┘

┌─────────────┐
│  WebSearch  │ (Interface específica - busca web)
└─────────────┘
```

**Importante:** `WebSearch` NÃO estende `LLMService` - são interfaces independentes.

### Implementação Atual: PerplexityLLMService

```java
public class PerplexityLLMService implements LLMService, WebSearch {
    // Implementa AMBAS as interfaces simultaneamente
}
```

**Linha 108 de PerplexityLLMService.java**

Isso significa que `PerplexityLLMService` é:
- ✅ Um `LLMService` completo (pode fazer completion, chat, embeddings, etc.)
- ✅ Um `WebSearch` completo (pode fazer webSearch, webSearchChat, etc.)
- ✅ Compatível com ambas as factories

## Proposta de Design: WebSearchFactory

### 1. Classe WebSearchFactory

```java
package bor.tools.simplellm.websearch;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.websearch.impl.PerplexityLLMService;

/**
 * Factory class for creating Web Search service implementations.
 * <p>
 * This factory provides a centralized way to instantiate different web search
 * service providers that implement the {@link WebSearch} interface. Unlike
 * {@link LLMServiceFactory} which creates general-purpose LLM services, this
 * factory focuses on services with real-time web search capabilities.
 * </p>
 * <p>
 * Currently supported:
 * <ul>
 * <li>Perplexity AI - Real-time web search with citations</li>
 * </ul>
 * </p>
 * <p>
 * Future implementations may include:
 * <ul>
 * <li>DeepSeek - Chinese web search engine</li>
 * <li>Google Gemini - Google's web search integration</li>
 * <li>Wikipedia Search - Structured knowledge base search</li>
 * <li>Tavily - Specialized research search API</li>
 * <li>Brave Search - Privacy-focused search API</li>
 * </ul>
 * </p>
 *
 * @author AlessandroBorges
 * @since 1.1
 *
 * @see WebSearch
 * @see SearchResponse
 */
public class WebSearchFactory {

    /**
     * Enum for web search service providers.
     */
    public enum WEBSEARCH_PROVIDER {
        /** Perplexity AI - Real-time web search with citations */
        PERPLEXITY,

        /** DeepSeek - Chinese web search (future) */
        DEEPSEEK,

        /** Google Gemini - Google web search integration (future) */
        GEMINI,

        /** Wikipedia - Structured knowledge base search (future) */
        WIKIPEDIA,

        /** Tavily - Research-focused search API (future) */
        TAVILY,

        /** Brave Search - Privacy-focused search (future) */
        BRAVE
    }

    /**
     * Creates a web search service based on the specified provider and configuration.
     *
     * @param provider the web search provider
     * @param config optional configuration (null for defaults)
     * @return a WebSearch implementation
     * @throws IllegalArgumentException if provider is null or unsupported
     */
    public static WebSearch createWebSearch(WEBSEARCH_PROVIDER provider, LLMConfig config) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider must not be null");
        }

        switch (provider) {
            case PERPLEXITY:
                return createPerplexity(config);

            case DEEPSEEK:
                throw new UnsupportedOperationException(
                    "DeepSeek web search not yet implemented");

            case GEMINI:
                throw new UnsupportedOperationException(
                    "Google Gemini web search not yet implemented");

            case WIKIPEDIA:
                throw new UnsupportedOperationException(
                    "Wikipedia search not yet implemented");

            case TAVILY:
                throw new UnsupportedOperationException(
                    "Tavily search not yet implemented");

            case BRAVE:
                throw new UnsupportedOperationException(
                    "Brave Search not yet implemented");

            default:
                throw new IllegalArgumentException(
                    "Unsupported web search provider: " + provider);
        }
    }

    /**
     * Creates a Perplexity AI web search service with custom configuration.
     * <p>
     * Perplexity provides real-time web search with citations, domain filtering,
     * recency controls, and related questions. All sonar models include native
     * web search capabilities.
     * </p>
     *
     * @param config the LLM configuration (API key, models, defaults, etc.)
     * @return a WebSearch instance configured for Perplexity
     * @throws IllegalArgumentException if config is invalid
     *
     * @see PerplexityLLMService
     */
    public static WebSearch createPerplexity(LLMConfig config) {
        return new PerplexityLLMService(config);
    }

    /**
     * Creates a Perplexity AI web search service with default configuration.
     * <p>
     * Uses default settings with API key from PERPLEXITY_API_KEY environment variable.
     * </p>
     *
     * @return a WebSearch instance with default Perplexity configuration
     * @see #createPerplexity(LLMConfig)
     */
    public static WebSearch createPerplexity() {
        return new PerplexityLLMService();
    }

    /**
     * Creates a DeepSeek web search service (future implementation).
     * <p>
     * DeepSeek provides Chinese-language web search with advanced reasoning.
     * </p>
     *
     * @param config the LLM configuration
     * @return a WebSearch instance for DeepSeek
     * @throws UnsupportedOperationException not yet implemented
     */
    public static WebSearch createDeepSeek(LLMConfig config) {
        throw new UnsupportedOperationException(
            "DeepSeek web search not yet implemented");
    }

    /**
     * Creates a DeepSeek web search service with default configuration (future).
     *
     * @return a WebSearch instance for DeepSeek
     * @throws UnsupportedOperationException not yet implemented
     */
    public static WebSearch createDeepSeek() {
        throw new UnsupportedOperationException(
            "DeepSeek web search not yet implemented");
    }

    /**
     * Creates a Google Gemini web search service (future implementation).
     * <p>
     * Gemini provides Google's web search integration with grounding in Search.
     * </p>
     *
     * @param config the LLM configuration
     * @return a WebSearch instance for Gemini
     * @throws UnsupportedOperationException not yet implemented
     */
    public static WebSearch createGemini(LLMConfig config) {
        throw new UnsupportedOperationException(
            "Google Gemini web search not yet implemented");
    }

    /**
     * Creates a Google Gemini web search service with default configuration (future).
     *
     * @return a WebSearch instance for Gemini
     * @throws UnsupportedOperationException not yet implemented
     */
    public static WebSearch createGemini() {
        throw new UnsupportedOperationException(
            "Google Gemini web search not yet implemented");
    }

    /**
     * Creates a Wikipedia search service (future implementation).
     * <p>
     * Provides structured search over Wikipedia knowledge base.
     * </p>
     *
     * @param config the configuration
     * @return a WebSearch instance for Wikipedia
     * @throws UnsupportedOperationException not yet implemented
     */
    public static WebSearch createWikipedia(LLMConfig config) {
        throw new UnsupportedOperationException(
            "Wikipedia search not yet implemented");
    }

    /**
     * Creates a Wikipedia search service with default configuration (future).
     *
     * @return a WebSearch instance for Wikipedia
     * @throws UnsupportedOperationException not yet implemented
     */
    public static WebSearch createWikipedia() {
        throw new UnsupportedOperationException(
            "Wikipedia search not yet implemented");
    }
}
```

### 2. Organização de Pacotes

```
bor.tools.simplellm
├── LLMService.java
├── LLMServiceFactory.java          (mantém como está)
└── websearch/
    ├── WebSearch.java               (interface)
    ├── SearchResponse.java
    ├── SearchMode.java
    ├── SearchContextSize.java
    ├── WebSearchFactory.java        (NOVA)
    └── impl/
        ├── PerplexityLLMService.java     (sem alterações)
        ├── PerplexityJsonMapper.java
        ├── DeepSeekLLMService.java       (futuro)
        ├── GeminiSearchService.java      (futuro)
        └── WikipediaSearchService.java   (futuro)
```

## Compatibilidade com Código Existente

### ✅ Código Existente NÃO Quebra

**Abordagem 1 - Via LLMServiceFactory (atual):**
```java
// Continua funcionando exatamente como antes
LLMService service = LLMServiceFactory.createPerplexity();
WebSearch searchService = (WebSearch) service;

SearchResponse response = searchService.webSearch("query", params);
```

**Abordagem 2 - Via WebSearchFactory (nova):**
```java
// Nova forma mais semântica
WebSearch searchService = WebSearchFactory.createPerplexity();

SearchResponse response = searchService.webSearch("query", params);
```

**Ambas retornam a MESMA implementação:** `PerplexityLLMService`

### Diferenças Semânticas

| Aspecto | LLMServiceFactory | WebSearchFactory |
|---------|-------------------|------------------|
| **Retorna** | `LLMService` | `WebSearch` |
| **Foco** | Serviço LLM completo | Capacidade de busca web |
| **Uso** | Quando precisa de LLM + búsca | Quando precisa apenas busca |
| **Cast necessário?** | Sim `(WebSearch)` | Não |
| **Semântica** | "Crie um serviço LLM" | "Crie um serviço de busca" |

## Benefícios da WebSearchFactory

### 1. Separação de Responsabilidades
```java
// Cliente só precisa de busca web (sem métodos LLM expostos)
WebSearch search = WebSearchFactory.createPerplexity();
// Não tem acesso a: completion(), embeddings(), etc.
// Apenas: webSearch(), webSearchChat(), etc.
```

### 2. Interface Mais Limpa
```java
// Antes (confuso)
LLMService service = LLMServiceFactory.createPerplexity();
if (service instanceof WebSearch) {
    WebSearch search = (WebSearch) service;
    search.webSearch("query", params);
}

// Depois (direto)
WebSearch search = WebSearchFactory.createPerplexity();
search.webSearch("query", params);
```

### 3. Suporte a Futuras Implementações

Algumas implementações futuras podem NÃO ser LLM services completos:

```java
// Wikipedia pode implementar apenas WebSearch
public class WikipediaSearchService implements WebSearch {
    // NÃO implementa LLMService
    // Apenas busca estruturada no Wikipedia
}

// Não seria apropriado via LLMServiceFactory
LLMService service = LLMServiceFactory.createWikipedia(); // ❌ Confuso

// Mas faz sentido via WebSearchFactory
WebSearch search = WebSearchFactory.createWikipedia(); // ✅ Claro
```

### 4. Documentação e Descoberta

Desenvolvedores procurando por busca web encontram diretamente:
```java
import bor.tools.simplellm.websearch.WebSearchFactory;

// Código autoexplicativo
WebSearch search = WebSearchFactory.createPerplexity();
```

## Futuras Implementações

### DeepSeek Web Search
```java
public class DeepSeekLLMService implements LLMService, WebSearch {
    // Modelo deepseek-reasoner com busca web
    // Otimizado para conteúdo em chinês
}
```

### Google Gemini Search
```java
public class GeminiSearchService implements LLMService, WebSearch {
    // Grounding with Google Search
    // Suporta returnGroundingChunks, returnGroundingImages
}
```

### Wikipedia Search
```java
public class WikipediaSearchService implements WebSearch {
    // NÃO implementa LLMService
    // Apenas busca estruturada via MediaWiki API
    // Retorna artigos, categorias, infoboxes
}
```

### Tavily Research API
```java
public class TavilySearchService implements WebSearch {
    // API especializada em research
    // Foco em fontes acadêmicas e confiáveis
}
```

## Implementação Recomendada

### Fase 1: Criar WebSearchFactory ✅
- Criar classe `WebSearchFactory`
- Enum `WEBSEARCH_PROVIDER`
- Métodos para Perplexity (reutilizando `PerplexityLLMService`)
- Stubs para futuras implementações

### Fase 2: Testes de Compatibilidade
- Garantir que código existente funciona
- Criar testes para ambas as factories
- Documentar diferenças semânticas

### Fase 3: Documentação
- Atualizar README.md
- Atualizar PERPLEXITY_TUTORIAL.md
- Criar exemplos com WebSearchFactory

### Fase 4: Futuras Implementações (conforme necessário)
- DeepSeek
- Gemini
- Wikipedia
- Outros

## Exemplo de Uso Completo

### Aplicação com Múltiplas Fontes de Busca

```java
public class MultiSourceSearch {
    private final WebSearch perplexity;
    private final WebSearch gemini;
    private final WebSearch wikipedia;

    public MultiSourceSearch() {
        // Criação limpa e semântica
        this.perplexity = WebSearchFactory.createPerplexity();
        this.gemini = WebSearchFactory.createGemini();
        this.wikipedia = WebSearchFactory.createWikipedia();
    }

    public List<SearchResponse> searchAll(String query) {
        return List.of(
            perplexity.webSearch(query, null),
            gemini.webSearch(query, null),
            wikipedia.webSearch(query, null)
        );
    }

    public SearchResponse searchBest(String query) {
        // Escolher melhor fonte baseado no tipo de query
        if (query.contains("latest") || query.contains("recent")) {
            return perplexity.webSearch(query, null); // Tempo real
        } else if (isFactualQuery(query)) {
            return wikipedia.webSearch(query, null);  // Fatos estabelecidos
        } else {
            return gemini.webSearch(query, null);     // Busca geral Google
        }
    }
}
```

## Conclusão

### ✅ Viabilidade Técnica
- **100% compatível** com código existente
- **Sem breaking changes** nas interfaces
- **Mesma implementação** (`PerplexityLLMService`) usada por ambas factories

### ✅ Benefícios Arquiteturais
- **Separação de responsabilidades** clara
- **Interface mais limpa** para clientes de busca
- **Extensibilidade** para serviços que só fazem busca (Wikipedia)

### ✅ Recomendação
**IMPLEMENTAR** a `WebSearchFactory` mantendo `LLMServiceFactory` como está.

### Próximos Passos
1. Criar `WebSearchFactory.java`
2. Criar testes de compatibilidade
3. Atualizar documentação
4. Criar exemplos práticos

---

**Análise realizada em:** 2025-01-22
**Autor:** Claude Code + AlessandroBorges
**Status:** ✅ Aprovado para implementação
