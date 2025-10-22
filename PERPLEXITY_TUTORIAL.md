# Tutorial: Perplexity WebSearch no JSimpleLLM

## 📚 Índice

1. [Introdução](#introdução)
2. [Configuração Inicial](#configuração-inicial)
3. [Conceitos Básicos](#conceitos-básicos)
4. [Exemplos Práticos](#exemplos-práticos)
5. [Recursos Avançados](#recursos-avançados)
6. [Melhores Práticas](#melhores-práticas)
7. [Troubleshooting](#troubleshooting)
8. [Referência da API](#referência-da-api)

---

## Introdução

### O que é Perplexity WebSearch?

Perplexity WebSearch é uma integração que permite ao JSimpleLLM realizar buscas web em tempo real e obter respostas fundamentadas com **citações de fontes**, combinando a capacidade de um LLM com acesso à informação atualizada da internet.

### Principais Recursos

✅ **Busca Web em Tempo Real** - Acesso a informações atualizadas da internet
✅ **Citações Automáticas** - Todas as respostas incluem fontes verificáveis
✅ **Filtros Avançados** - Filtre por domínio, período de tempo, e mais
✅ **Perguntas Relacionadas** - Obtenha sugestões de follow-up questions
✅ **Streaming Support** - Receba respostas progressivamente
✅ **Pesquisa Conversacional** - Mantenha contexto entre múltiplas queries

### Modelos Disponíveis

| Modelo | Context | Velocidade | Uso Recomendado |
|--------|---------|------------|-----------------|
| `sonar` | 128k | ⚡ Rápido | Consultas gerais |
| `sonar-pro` | 200k | 🔷 Médio | Análises detalhadas |
| `sonar-deep-research` | 128k | 🐢 Lento | Pesquisas exaustivas |
| `sonar-reasoning` | 128k | 🔷 Médio | Raciocínio + busca |
| `sonar-reasoning-pro` | 128k | 🐢 Lento | Raciocínio avançado |
| `r1-1776` | 128k | ⚡ Rápido | Chat offline (sem busca) |

---

## Configuração Inicial

### 1. Obter API Key

1. Acesse [https://www.perplexity.ai/](https://www.perplexity.ai/)
2. Crie uma conta ou faça login
3. Navegue até as configurações da API
4. Gere uma nova API key
5. Copie e guarde a chave em local seguro

### 2. Configurar Variável de Ambiente

**Linux/Mac:**
```bash
export PERPLEXITY_API_KEY="pplx-xxxxxxxxxxxxxxxxxxxxxxxx"
```

**Windows (PowerShell):**
```powershell
$env:PERPLEXITY_API_KEY="pplx-xxxxxxxxxxxxxxxxxxxxxxxx"
```

**Windows (CMD):**
```cmd
set PERPLEXITY_API_KEY=pplx-xxxxxxxxxxxxxxxxxxxxxxxx
```

### 3. Adicionar Dependência ao Projeto

Se você estiver usando Maven, certifique-se de que JSimpleLLM está no seu `pom.xml`:

```xml
<dependency>
    <groupId>bor.tools</groupId>
    <artifactId>JSimpleLLM</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 4. Verificar Instalação

Teste se tudo está configurado corretamente:

```java
import bor.tools.simplellm.*;
import bor.tools.simplellm.websearch.*;

public class PerplexityTest {
    public static void main(String[] args) {
        try {
            // Opção 1: Via LLMServiceFactory (tradicional)
            LLMService service = LLMServiceFactory.createPerplexity();
            System.out.println("✅ Perplexity configurado com sucesso!");
            System.out.println("Provider: " + service.getServiceProvider());

            // Opção 2: Via WebSearchFactory (recomendado para busca)
            WebSearch search = WebSearchFactory.createPerplexity();
            System.out.println("✅ WebSearch configurado com sucesso!");
            System.out.println("Modelos disponíveis: " + ((LLMService)search).getRegisteredModels().size());
        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
        }
    }
}
```

### 5. Escolhendo a Factory Correta

JSimpleLLM oferece duas factories para criar serviços Perplexity:

#### LLMServiceFactory
```java
LLMService service = LLMServiceFactory.createPerplexity();
WebSearch search = (WebSearch) service;  // Cast necessário
```

**Use quando:**
- Precisa de funcionalidades LLM completas (completion, embeddings, etc.)
- Usa Perplexity tanto para LLM quanto para busca

#### WebSearchFactory (NOVO! 🔍)
```java
WebSearch search = WebSearchFactory.createPerplexity();  // Sem cast!
```

**Use quando:**
- Foca em busca web com citações
- Quer código mais limpo sem casts
- Constrói aplicações centradas em pesquisa

**Ambas retornam a mesma implementação** (`PerplexityLLMService`), mas `WebSearchFactory` oferece uma API mais semântica e limpa para casos de uso de busca.

---

## Conceitos Básicos

### Interface WebSearch

A interface `WebSearch` estende as capacidades do `LLMService` com métodos específicos para busca web:

```java
public interface WebSearch {
    // Busca simples
    SearchResponse webSearch(String query, MapParam params);

    // Busca conversacional
    SearchResponse webSearchChat(Chat chat, String query, MapParam params);

    // Busca com streaming
    SearchResponse webSearchStream(ResponseStream stream, String query, MapParam params);

    // Busca conversacional com streaming
    SearchResponse webSearchChatStream(ResponseStream stream, Chat chat, String query, MapParam params);
}
```

### SearchResponse

Objeto de resposta estendido que inclui informações de busca:

```java
SearchResponse response = searchService.webSearch("query", params);

// Conteúdo da resposta
String text = response.getResponse().getText();

// Citações (URLs das fontes)
List<String> citations = response.getCitations();

// Perguntas relacionadas
List<String> relatedQuestions = response.getRelatedQuestions();

// Metadados de resultados de busca
List<SearchResultMetadata> searchResults = response.getSearchResults();
```

### MapParam - Parâmetros de Busca

Configure sua busca usando `MapParam`. Parâmetros não especificados usam os **valores padrão do serviço**.

#### Parâmetros Básicos

```java
MapParam params = new MapParam()
    .model("sonar-pro")                              // Modelo a usar
    .maxTokens(1000)                                 // Limite de tokens
    .temperature(0.7f)                               // Criatividade (0.0-2.0)
    .reasoningEffort(Reasoning_Effort.MEDIUM);       // Nível de raciocínio
```

#### Parâmetros de Busca Web (Perplexity)

```java
params
    // Modo de busca
    .searchMode("web")                               // "web" ou "academic"

    // Filtros de domínio
    .searchDomainFilter(new String[]{
        "arxiv.org",                                  // Incluir arxiv.org
        "-reddit.com"                                 // Excluir reddit.com (prefixo -)
    })

    // Filtros de tempo
    .searchRecencyFilter("week")                     // "hour", "day", "week", "month", "year"
    .searchAfterDateFilter("01/01/2025")            // Apenas após data (MM/DD/YYYY)
    .searchBeforeDateFilter("12/31/2025")           // Apenas antes da data (MM/DD/YYYY)

    // Opções de resposta
    .returnImages(true)                              // Incluir imagens
    .returnRelatedQuestions(true)                    // Incluir perguntas relacionadas

    // Contexto de busca
    .searchContext("high")                           // "low", "medium", "high"

    // Localização do usuário
    .userLocation(-15.7933, -47.8827, "br");        // latitude, longitude, país
```

#### Valores Padrão do Serviço

O serviço Perplexity vem pré-configurado com valores padrão sensatos:

| Parâmetro | Valor Padrão | Descrição |
|-----------|--------------|-----------|
| `search_mode` | `"web"` | Busca geral na web |
| `return_related_questions` | `true` | Sempre retorna perguntas relacionadas |
| `temperature` | `0.7` | Equilíbrio entre precisão e criatividade |

**Esses defaults se aplicam automaticamente** a menos que você os sobrescreva explicitamente:

```java
// Usa defaults: search_mode="web", return_related_questions=true, temperature=0.7
searchService.webSearch("query", null);

// Sobrescreve apenas temperature, mantém outros defaults
searchService.webSearch("query", new MapParam().temperature(0.2f));
```

### SearchMetadata - Metadados nas Mensagens

Quando você usa `webSearchChat()` ou `chatCompletion()` com modelos Perplexity, os **metadados de busca são automaticamente anexados às mensagens do assistente** como `SearchMetadata`.

#### O que é SearchMetadata?

`SearchMetadata` é uma classe leve que contém informações da busca web associadas a uma mensagem:

```java
public class SearchMetadata {
    private List<String> citations;                    // URLs citadas
    private List<SearchResultMetadata> searchResults;   // Resultados detalhados
    private List<String> relatedQuestions;              // Perguntas relacionadas
    private List<ImageResult> images;                   // Imagens relacionadas
    private Integer searchQueriesCount;                 // Quantidade de buscas realizadas
}
```

#### Acessando SearchMetadata nas Mensagens

```java
// Executar busca conversacional
Chat chat = new Chat();
searchService.webSearchChat(chat, "What is quantum computing?", params);

// Obter última mensagem do assistente
Message assistantMessage = chat.getLastMessage();

// Verificar se tem metadados de busca
if (assistantMessage.hasSearchMetadata()) {
    SearchMetadata metadata = assistantMessage.getSearchMetadata();

    // Acessar citações
    if (metadata.hasCitations()) {
        System.out.println("Fontes:");
        for (String citation : metadata.getCitations()) {
            System.out.println("  • " + citation);
        }
    }

    // Acessar resultados de busca detalhados
    if (metadata.hasSearchResults()) {
        for (SearchMetadata.SearchResultMetadata result : metadata.getSearchResults()) {
            System.out.println(result.getTitle() + " - " + result.getUrl());
            System.out.println(result.getSnippet());
        }
    }

    // Acessar perguntas relacionadas
    if (metadata.hasRelatedQuestions()) {
        System.out.println("Perguntas relacionadas:");
        metadata.getRelatedQuestions().forEach(q -> System.out.println("  • " + q));
    }
}
```

#### Vantagens

✅ **Persistência**: Os metadados ficam vinculados à mensagem, permitindo acesso posterior
✅ **Histórico**: Cada mensagem do assistente pode ter seus próprios metadados de busca
✅ **Serialização**: SearchMetadata é automaticamente serializado com a mensagem em JSON
✅ **Leve**: Contém apenas os dados essenciais da busca (não o SearchResponse completo)

---

## Exemplos Práticos

### Exemplo 1: Busca Simples

```java
import bor.tools.simplellm.*;
import bor.tools.simplellm.websearch.*;

public class Example1_BasicSearch {
    public static void main(String[] args) throws Exception {
        // Criar serviço de busca (forma recomendada)
        WebSearch searchService = WebSearchFactory.createPerplexity();

        // OU via LLMServiceFactory (forma tradicional)
        // LLMService service = LLMServiceFactory.createPerplexity();
        // WebSearch searchService = (WebSearch) service;

        // Configurar parâmetros
        MapParam params = new MapParam()
            .model("sonar")
            .maxTokens(500);

        // Executar busca
        SearchResponse response = searchService.webSearch(
            "What are the latest developments in quantum computing?",
            params
        );

        // Exibir resultados
        System.out.println("Resposta:");
        System.out.println(response.getResponse().getText());

        System.out.println("\nFontes:");
        for (String citation : response.getCitations()) {
            System.out.println("• " + citation);
        }
    }
}
```

**Saída Esperada:**
```
Resposta:
As of January 2025, quantum computing has seen several significant developments...
[resposta detalhada com informações atualizadas]

Fontes:
• https://www.nature.com/articles/quantum-2025-breakthrough
• https://arxiv.org/abs/2501.12345
• https://www.technologyreview.com/quantum-computing-latest
```

### Exemplo 2: Busca Acadêmica com Filtros

```java
import bor.tools.simplellm.*;

public class Example2_AcademicSearch {
    public static void main(String[] args) throws Exception {
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        // Configurar busca acadêmica
        MapParam params = new MapParam()
            .model("sonar-pro")
            .searchDomainFilter(new String[]{
                "arxiv.org",           // Incluir ArXiv
                "nature.com",          // Incluir Nature
                "sciencedirect.com",   // Incluir ScienceDirect
                "-wikipedia.org"       // Excluir Wikipedia
            })
            .searchRecencyFilter("month")  // Apenas último mês
            .maxTokens(1000);

        // Executar busca
        SearchResponse response = searchService.webSearch(
            "Recent breakthroughs in CRISPR gene editing technology",
            params
        );

        // Exibir resultados detalhados
        System.out.println("=== ANÁLISE ACADÊMICA ===\n");
        System.out.println(response.getResponse().getText());

        System.out.println("\n=== PAPERS E FONTES ===");
        if (response.hasSearchResults()) {
            for (var result : response.getSearchResults()) {
                System.out.println("\n📄 " + result.getTitle());
                System.out.println("   🔗 " + result.getUrl());
                System.out.println("   📅 " + result.getDate());
                if (result.getSnippet() != null) {
                    System.out.println("   📝 " + result.getSnippet());
                }
            }
        }
    }
}
```

### Exemplo 3: Busca Conversacional

```java
import bor.tools.simplellm.*;
import bor.tools.simplellm.chat.Chat;

public class Example3_ConversationalSearch {
    public static void main(String[] args) throws Exception {
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        // Criar sessão de chat
        Chat chat = new Chat();
        chat.setModel("sonar-pro");

        MapParam params = new MapParam()
            .maxTokens(500)
            .returnRelatedQuestions(true);

        // Primeira pergunta
        System.out.println("👤 Você: Qual é a capital do Brasil?");
        SearchResponse r1 = searchService.webSearchChat(
            chat,
            "Qual é a capital do Brasil?",
            params
        );
        System.out.println("🤖 Assistente: " + r1.getResponse().getText());

        // Pergunta de follow-up (usa contexto!)
        System.out.println("\n👤 Você: E sua população?");
        SearchResponse r2 = searchService.webSearchChat(
            chat,
            "E sua população?",
            params
        );
        System.out.println("🤖 Assistente: " + r2.getResponse().getText());

        // Perguntas relacionadas sugeridas
        if (r2.hasRelatedQuestions()) {
            System.out.println("\n❓ Perguntas relacionadas:");
            for (String q : r2.getRelatedQuestions()) {
                System.out.println("   • " + q);
            }
        }

        // Exibir fontes
        System.out.println("\n📚 Fontes consultadas:");
        for (String citation : r2.getCitations()) {
            System.out.println("   • " + citation);
        }
    }
}
```

### Exemplo 4: Streaming de Respostas

```java
import bor.tools.simplellm.*;

public class Example4_StreamingSearch {
    public static void main(String[] args) throws Exception {
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        // Criar handler de streaming
        ResponseStream stream = new ResponseStream() {
            @Override
            public void onToken(String token, ContentType type) {
                // Imprimir cada token conforme chega
                System.out.print(token);
                System.out.flush();
            }

            @Override
            public void onComplete() {
                System.out.println("\n\n✅ [Streaming concluído]");
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("\n❌ Erro: " + error.getMessage());
            }
        };

        MapParam params = new MapParam()
            .model("sonar")
            .maxTokens(600);

        System.out.println("🔄 Iniciando streaming...\n");

        // Executar busca com streaming
        SearchResponse response = searchService.webSearchStream(
            stream,
            "Explain quantum entanglement in simple terms",
            params
        );

        // Após completar, exibir citações
        System.out.println("\n📚 Fontes:");
        for (String citation : response.getCitations()) {
            System.out.println("   • " + citation);
        }
    }
}
```

### Exemplo 5: Pesquisa com Filtro de Recência

```java
import bor.tools.simplellm.*;

public class Example5_RecentNews {
    public static void main(String[] args) throws Exception {
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        MapParam params = new MapParam()
            .model("sonar-pro")
            .searchRecencyFilter("day")        // Apenas últimas 24 horas
            .returnRelatedQuestions(true)
            .maxTokens(800);

        SearchResponse response = searchService.webSearch(
            "Latest AI model releases and benchmarks",
            params
        );

        System.out.println("=== NOTÍCIAS DAS ÚLTIMAS 24H ===\n");
        System.out.println(response.getResponse().getText());

        // Perguntas de acompanhamento
        if (response.hasRelatedQuestions()) {
            System.out.println("\n🔍 Explore mais:");
            for (String q : response.getRelatedQuestions()) {
                System.out.println("   • " + q);
            }
        }

        // Fontes recentes
        System.out.println("\n📰 Fontes (últimas 24h):");
        for (String citation : response.getCitations()) {
            System.out.println("   • " + citation);
        }
    }
}
```

### Exemplo 6: Deep Research

```java
import bor.tools.simplellm.*;

public class Example6_DeepResearch {
    public static void main(String[] args) throws Exception {
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        MapParam params = new MapParam()
            .model("sonar-deep-research")      // Modelo de pesquisa profunda
            .searchRecencyFilter("month")
            .returnRelatedQuestions(true)
            .maxTokens(2500);                  // Resposta mais longa

        System.out.println("🔬 Iniciando pesquisa profunda (pode levar 30-60s)...\n");

        SearchResponse response = searchService.webSearch(
            "Comprehensive analysis of renewable energy adoption trends in 2024-2025",
            params
        );

        System.out.println("=== RELATÓRIO DE PESQUISA ===\n");
        System.out.println(response.getResponse().getText());

        // Número de buscas realizadas
        if (response.getSearchQueriesCount() != null) {
            System.out.println("\n📊 Estatísticas:");
            System.out.println("   Buscas realizadas: " + response.getSearchQueriesCount());
        }

        // Lista completa de fontes
        System.out.println("\n📚 Fontes consultadas (" +
            response.getCitations().size() + " documentos):");
        for (int i = 0; i < response.getCitations().size(); i++) {
            System.out.println("   " + (i+1) + ". " + response.getCitations().get(i));
        }
    }
}
```

---

## Recursos Avançados

### 1. Filtros de Domínio Complexos

```java
// Incluir apenas domínios acadêmicos, excluir Wikipedia e blogs
MapParam params = new MapParam()
    .searchDomainFilter(new String[]{
        "arxiv.org",
        "nature.com",
        "science.org",
        "ncbi.nlm.nih.gov",
        "-wikipedia.org",
        "-medium.com",
        "-*.blog.*"
    });
```

### 2. Combinação de Filtros

```java
MapParam params = new MapParam()
    .model("sonar-pro")
    .searchDomainFilter(new String[]{"gov", "edu"})  // Apenas .gov e .edu
    .searchRecencyFilter("week")                      // Última semana
    .searchContext("high")                            // Contexto máximo
    .returnImages(true)                               // Incluir imagens
    .returnRelatedQuestions(true)                     // Perguntas relacionadas
    .maxTokens(1500);
```

### 3. Configurando Valores Padrão Personalizados

Você pode configurar valores padrão que se aplicam a **todas as requisições** do serviço:

#### Opção 1: Modificar Config Padrão

```java
// Obter configuração padrão
LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();

// Configurar defaults personalizados para pesquisa acadêmica
MapParam customDefaults = new MapParam()
    .searchMode("academic")
    .searchDomainFilter(new String[]{"arxiv.org", "scholar.google.com"})
    .returnRelatedQuestions(true)
    .temperature(0.3f)           // Mais preciso
    .maxTokens(1500);

config.setDefaultParams(customDefaults);

// Criar serviço com defaults personalizados
LLMService service = new PerplexityLLMService(config);
WebSearch searchService = (WebSearch) service;

// Agora TODAS as chamadas usam esses defaults automaticamente
searchService.webSearch("quantum physics", null);  // Usa defaults acadêmicos
```

#### Opção 2: Criar Config Completamente Customizado

```java
// Defaults para localização brasileira
Map<String, Object> location = new LinkedHashMap<>();
location.put("latitude", -15.7933);   // Brasília
location.put("longitude", -47.8827);
location.put("country", "br");

MapParam brDefaults = new MapParam()
    .userLocation(location)
    .searchMode("web")
    .returnRelatedQuestions(true)
    .temperature(0.7f);

LLMConfig brConfig = LLMConfig.builder()
    .apiTokenEnvironment("PERPLEXITY_API_KEY")
    .baseUrl("https://api.perplexity.ai")
    .defaultModelName("sonar-pro")
    .defaultParams(brDefaults)          // Defaults para Brasil
    .build();

LLMService service = new PerplexityLLMService(brConfig);
```

#### Mesclagem de Parâmetros

Os parâmetros seguem esta ordem de precedência:

1. **Parâmetros da requisição** (prioridade máxima)
2. **Parâmetros padrão do config**
3. **Valores padrão do Perplexity**

```java
// Serviço com defaults: temperature=0.3, search_mode="academic"
LLMConfig config = ...;
config.setDefaultParams(new MapParam()
    .temperature(0.3f)
    .searchMode("academic"));

LLMService service = new PerplexityLLMService(config);

// Requisição sobrescreve apenas temperature
MapParam requestParams = new MapParam().temperature(0.9f);
service.completion(null, "query", requestParams);
// Resultado: temperature=0.9 (sobrescrito), search_mode="academic" (default mantido)
```

#### Uso Recomendado

✅ **Use defaults personalizados quando:**
- Todas as buscas têm requisitos similares (ex: sempre acadêmico)
- Você tem uma localização fixa (ex: aplicação regional)
- Você quer consistência em toda a aplicação

❌ **Não use defaults personalizados quando:**
- Diferentes partes da app precisam de configs diferentes
- Os requisitos variam muito entre requisições
- Você quer controle explícito em cada chamada

### 4. Uso com Try-With-Resources

```java
try {
    LLMService service = LLMServiceFactory.createPerplexity();
    WebSearch searchService = (WebSearch) service;

    MapParam params = new MapParam().model("sonar");
    SearchResponse response = searchService.webSearch("query", params);

    // Processar resposta
    System.out.println(response.getResponse().getText());

} catch (LLMAuthenticationException e) {
    System.err.println("❌ API key inválida ou não configurada");
} catch (LLMRateLimitException e) {
    System.err.println("⚠️ Rate limit excedido. Aguarde alguns segundos.");
} catch (LLMException e) {
    System.err.println("❌ Erro: " + e.getMessage());
}
```

### 4. Processamento Assíncrono

```java
import java.util.concurrent.*;

public class AsyncSearch {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        // Executar múltiplas buscas em paralelo
        Future<SearchResponse> future1 = executor.submit(() ->
            searchService.webSearch("Query 1", new MapParam().model("sonar"))
        );

        Future<SearchResponse> future2 = executor.submit(() ->
            searchService.webSearch("Query 2", new MapParam().model("sonar"))
        );

        Future<SearchResponse> future3 = executor.submit(() ->
            searchService.webSearch("Query 3", new MapParam().model("sonar"))
        );

        try {
            // Aguardar e processar resultados
            SearchResponse r1 = future1.get();
            SearchResponse r2 = future2.get();
            SearchResponse r3 = future3.get();

            System.out.println("Resultado 1: " + r1.getResponse().getText());
            System.out.println("Resultado 2: " + r2.getResponse().getText());
            System.out.println("Resultado 3: " + r3.getResponse().getText());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
```

### 5. Integração com Spring Boot

```java
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import bor.tools.simplellm.*;

@Service
public class PerplexitySearchService {

    private final WebSearch searchService;

    public PerplexitySearchService(@Value("${perplexity.api.key}") String apiKey) {
        LLMConfig config = LLMConfig.builder()
            .apiToken(apiKey)
            .baseUrl("https://api.perplexity.ai")
            .build();

        LLMService service = LLMServiceFactory.createPerplexity(config);
        this.searchService = (WebSearch) service;
    }

    public SearchResponse search(String query) throws LLMException {
        MapParam params = new MapParam()
            .model("sonar-pro")
            .maxTokens(1000);

        return searchService.webSearch(query, params);
    }
}
```

---

## Melhores Práticas

### 1. Escolha do Modelo Adequado

```java
// ✅ BOM - Consultas rápidas e simples
params.model("sonar");

// ✅ BOM - Análises detalhadas com mais contexto
params.model("sonar-pro");

// ✅ BOM - Pesquisa exaustiva e profunda
params.model("sonar-deep-research");

// ❌ EVITAR - Usar deep-research para perguntas simples (caro e lento)
```

### 2. Controle de Tokens

```java
// ✅ BOM - Limite apropriado para a tarefa
MapParam params = new MapParam()
    .model("sonar")
    .maxTokens(500);  // Suficiente para resposta concisa

// ❌ EVITAR - Tokens excessivos desperdiçam recursos
MapParam badParams = new MapParam()
    .model("sonar")
    .maxTokens(5000);  // Muito para uma resposta simples
```

### 3. Reutilização de Conexões

```java
// ✅ BOM - Reutilizar mesma instância
public class SearchManager {
    private final WebSearch searchService;

    public SearchManager() {
        LLMService service = LLMServiceFactory.createPerplexity();
        this.searchService = (WebSearch) service;
    }

    public SearchResponse search(String query) throws LLMException {
        return searchService.webSearch(query, new MapParam().model("sonar"));
    }
}

// ❌ EVITAR - Criar nova instância a cada busca
public SearchResponse badSearch(String query) throws LLMException {
    LLMService service = LLMServiceFactory.createPerplexity();  // ❌
    WebSearch searchService = (WebSearch) service;
    return searchService.webSearch(query, new MapParam());
}
```

### 4. Tratamento de Erros Robusto

```java
public SearchResponse robustSearch(String query) {
    try {
        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        MapParam params = new MapParam()
            .model("sonar")
            .maxTokens(1000);

        return searchService.webSearch(query, params);

    } catch (LLMAuthenticationException e) {
        logger.error("API key inválida", e);
        throw new IllegalStateException("Configure PERPLEXITY_API_KEY", e);

    } catch (LLMRateLimitException e) {
        logger.warn("Rate limit excedido, aguardando...");
        try {
            Thread.sleep(5000);  // Aguardar 5 segundos
            return robustSearch(query);  // Tentar novamente
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        }

    } catch (LLMTimeoutException e) {
        logger.error("Timeout na requisição", e);
        throw new RuntimeException("Servidor não respondeu a tempo", e);

    } catch (LLMException e) {
        logger.error("Erro na busca", e);
        throw new RuntimeException("Erro ao executar busca: " + e.getMessage(), e);
    }
}
```

### 5. Cache de Resultados

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class CachedSearchService {
    private final WebSearch searchService;
    private final Map<String, SearchResponse> cache = new ConcurrentHashMap<>();

    public CachedSearchService() {
        LLMService service = LLMServiceFactory.createPerplexity();
        this.searchService = (WebSearch) service;
    }

    public SearchResponse search(String query) throws LLMException {
        // Verificar cache primeiro
        if (cache.containsKey(query)) {
            System.out.println("✅ Cache hit!");
            return cache.get(query);
        }

        // Executar busca
        System.out.println("🔍 Executando busca...");
        MapParam params = new MapParam().model("sonar");
        SearchResponse response = searchService.webSearch(query, params);

        // Armazenar em cache
        cache.put(query, response);

        return response;
    }
}
```

---

## Troubleshooting

### Problema: "API key not found"

**Erro:**
```
LLMAuthenticationException: Perplexity API key not found.
Set PERPLEXITY_API_KEY environment variable...
```

**Solução:**
```bash
# Verificar se variável está definida
echo $PERPLEXITY_API_KEY

# Se vazio, definir:
export PERPLEXITY_API_KEY="sua-chave-aqui"

# Para tornar permanente (Linux/Mac), adicionar ao ~/.bashrc ou ~/.zshrc:
echo 'export PERPLEXITY_API_KEY="sua-chave-aqui"' >> ~/.bashrc
source ~/.bashrc
```

### Problema: "Rate limit exceeded"

**Erro:**
```
LLMRateLimitException: Rate limit exceeded
```

**Solução:**
```java
// Implementar retry com backoff exponencial
public SearchResponse searchWithRetry(String query, int maxRetries) throws LLMException {
    int attempt = 0;
    while (attempt < maxRetries) {
        try {
            return searchService.webSearch(query, params);
        } catch (LLMRateLimitException e) {
            attempt++;
            if (attempt >= maxRetries) throw e;

            long waitTime = (long) Math.pow(2, attempt) * 1000; // 2s, 4s, 8s...
            System.out.println("⏳ Rate limit. Aguardando " + waitTime + "ms...");
            Thread.sleep(waitTime);
        }
    }
    throw new LLMException("Failed after " + maxRetries + " retries");
}
```

### Problema: "Timeout"

**Erro:**
```
LLMTimeoutException: Request timeout
```

**Solução:**
```java
// Usar modelo mais rápido ou reduzir maxTokens
MapParam params = new MapParam()
    .model("sonar")           // Mais rápido que sonar-pro
    .maxTokens(500);          // Menos tokens = mais rápido
```

### Problema: Citações vazias

**Situação:**
```java
SearchResponse response = searchService.webSearch(query, params);
System.out.println(response.getCitations().size());  // 0
```

**Causas possíveis:**
1. Modelo `r1-1776` não tem web search
2. Query muito específica sem resultados
3. Problema temporário da API

**Solução:**
```java
// Verificar se modelo suporta web search
if (searchService.supportsWebSearch("r1-1776")) {
    // OK
} else {
    System.out.println("❌ Modelo não suporta web search!");
}

// Usar modelo com web search garantido
params.model("sonar");  // ✅ Sempre tem citações
```

### Problema: Resposta em inglês quando esperava português

**Solução:**
```java
// Adicionar instrução no prompt
SearchResponse response = searchService.webSearch(
    "Responda em português: " + query,
    params
);

// Ou usar system prompt em chat
Chat chat = new Chat();
chat.addSystemMessage("Responda sempre em português brasileiro.");
SearchResponse response = searchService.webSearchChat(chat, query, params);
```

---

## Referência da API

### Métodos WebSearch

#### `webSearch(String query, MapParam params)`
Executa busca web simples com citações.

**Parâmetros:**
- `query`: String com a pergunta/consulta
- `params`: MapParam com configurações

**Retorna:** `SearchResponse`

**Throws:** `LLMException`, `LLMAuthenticationException`, `LLMRateLimitException`

---

#### `webSearchChat(Chat chat, String query, MapParam params)`
Executa busca web mantendo contexto de conversa.

**Parâmetros:**
- `chat`: Sessão de chat com histórico
- `query`: Nova pergunta
- `params`: MapParam com configurações

**Retorna:** `SearchResponse`

---

#### `webSearchStream(ResponseStream stream, String query, MapParam params)`
Executa busca com streaming de resultados.

**Parâmetros:**
- `stream`: Handler para receber tokens progressivamente
- `query`: Pergunta
- `params`: Configurações

**Retorna:** `SearchResponse` (completo após streaming)

---

### Métodos MapParam para Perplexity

```java
MapParam params = new MapParam()
    // Básicos
    .model(String modelName)                    // Define modelo
    .maxTokens(Integer tokens)                  // Limite de tokens
    .temperature(Float temp)                    // 0.0-2.0 (criatividade)

    // Perplexity-específicos
    .searchDomainFilter(String[] domains)       // Filtrar domínios
    .searchRecencyFilter(String period)         // "hour", "day", "week", "month", "year"
    .returnImages(Boolean enable)               // Incluir imagens
    .returnRelatedQuestions(Boolean enable)     // Perguntas relacionadas
    .searchContext(String size)                 // "low", "medium", "high"
    .searchMode(String mode);                   // "web", "academic"
```

### Campos SearchResponse

```java
SearchResponse response = ...;

// Conteúdo
ContentWrapper content = response.getResponse();
String text = response.getResponse().getText();

// Web search extras
List<String> citations = response.getCitations();
List<String> relatedQuestions = response.getRelatedQuestions();
List<SearchResultMetadata> searchResults = response.getSearchResults();
List<ImageResult> images = response.getImages();
Integer searchQueriesCount = response.getSearchQueriesCount();

// Metadados padrão
String endReason = response.getEndReason();
MapParam usage = response.getUsage();
MapParam info = response.getInfo();

// Helpers
boolean hasCitations = response.hasCitations();
boolean hasRelatedQuestions = response.hasRelatedQuestions();
boolean hasSearchResults = response.hasSearchResults();
boolean hasImages = response.hasImages();
```

---

## Conclusão

Parabéns! Você agora domina o uso do Perplexity WebSearch no JSimpleLLM. 🎉

### Próximos Passos

1. **Experimente os exemplos** fornecidos neste tutorial
2. **Adapte para seu caso de uso** específico
3. **Explore diferentes modelos** e filtros
4. **Implemente cache** para economizar créditos de API
5. **Contribua** com melhorias no projeto!

### Recursos Adicionais

- 📖 [Documentação Perplexity API](https://docs.perplexity.ai/)
- 💬 [Issues do JSimpleLLM](https://github.com/your-repo/JSimpleLLM/issues)
- 📝 [CLAUDE.md](./CLAUDE.md) - Guia para contribuidores

### Suporte

Encontrou algum problema?
1. Verifique a seção [Troubleshooting](#troubleshooting)
2. Consulte os [exemplos completos](./src/test/java/bor/tools/simplellm/impl/PerplexityExample.java)
3. Abra uma issue no GitHub com detalhes do erro

---

**Happy Coding!** 🚀

*Tutorial criado para JSimpleLLM v1.1.0 - Janeiro 2025*
