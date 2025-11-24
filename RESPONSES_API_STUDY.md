# Estudo: Suporte à API Responses da OpenAI (/v1/responses)

## Data: 2025-01-19
## Status: Análise Preliminar

---

## ⚠️ DESCOBERTA IMPORTANTE: Web Search Já Implementado

Durante este estudo, foi identificado que **o JSimpleLLM já possui uma implementação completa de Web Search** via interface `WebSearch` no package `bor.tools.simplellm.websearch`:

- ✅ **Interface `WebSearch`** com métodos para busca, chat e streaming
- ✅ **`WebSearchFactory`** para criar instâncias de providers
- ✅ **`SearchResponse`** extendendo `CompletionResponse` com citations, related questions, images
- ✅ **Implementação via Perplexity AI** (sonar models) totalmente funcional
- ✅ **Recursos avançados**: domain filters, recency filters, date ranges, search context

**Implicação para este estudo**: A Fase 3 (Ferramentas Nativas) deve ser revisada. O web search tool da Responses API seria **complementar** ao `WebSearch` existente, não um substituto. São abordagens diferentes:
- **`WebSearch` (Perplexity)**: API dedicada, controle fino, citations detalhadas
- **Responses API tools**: Integrado ao LLM OpenAI, mais simples, menos controle

Ver seção [10.4 Web Search: Diferenças entre Implementações](#104-web-search-diferenças-entre-implementações) para detalhes completos.

---

## 1. Visão Geral da API Responses

### 1.1 O que é a API Responses?

A API Responses é a mais nova API da OpenAI, lançada em março de 2025. Ela combina os pontos fortes das APIs Chat Completions e Assistants em uma interface unificada e simplificada.

### 1.2 Principais Características

1. **Interface Simplificada**: Substitui o array complexo `messages[]` por um único campo `input`
2. **Stateful por Design**: Suporta `previous_response_id` para continuar conversações sem gerenciar histórico manualmente
3. **Ferramentas Nativas**: Integração direta com web search, file search e computer use
4. **Suporte MCP**: Model Context Protocol integrado
5. **Streaming**: Suporta Server-Sent Events (SSE) como Chat Completions
6. **Background Mode**: Permite execução assíncrona de tarefas longas

### 1.3 Modelos Suportados

Segundo a documentação, os seguintes modelos suportam a API Responses:
- GPT-5 series (gpt-5, gpt-5-mini, gpt-5-nano)
- GPT-4.1 series (gpt-4.1, gpt-4.1-mini)
- GPT-4o series (gpt-4o, gpt-4o-mini)
- O3 series (o3, o3-mini)
- Modelos de reasoning

---

## 2. Diferenças entre Chat Completions e Responses API

### 2.1 Formato de Requisição

#### Chat Completions API (`/v1/chat/completions`):

```json
{
  "model": "gpt-4.1",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant"},
    {"role": "user", "content": "Hello"}
  ],
  "temperature": 0.7,
  "max_tokens": 1000
}
```

#### Responses API (`/v1/responses`):

```json
{
  "model": "gpt-4.1",
  "input": "Hello",
  "temperature": 0.7,
  "max_output_tokens": 1000,
  "previous_response_id": "resp_abc123"
}
```

### 2.2 Principais Diferenças nos Parâmetros

| Chat Completions | Responses API | Observações |
|-----------------|---------------|-------------|
| `messages[]` | `input` | String simples ao invés de array |
| `max_tokens` | `max_output_tokens` | Renomeado para clareza |
| N/A | `previous_response_id` | Novo campo para continuação |
| N/A | `max_prompt_tokens` | Controle granular de tokens |
| N/A | `truncation_strategy` | Como truncar quando exceder limite |
| `temperature` | Não suportado (?) | Relatado como "Unknown parameter" por alguns usuários |

### 2.3 Formato de Resposta

#### Chat Completions:

```json
{
  "id": "chatcmpl-abc123",
  "choices": [{
    "message": {
      "role": "assistant",
      "content": "Hello! How can I help?"
    },
    "finish_reason": "stop"
  }],
  "usage": {...}
}
```

#### Responses API:

```json
{
  "id": "resp_abc123",
  "output_text": "Hello! How can I help?",
  "finish_reason": "stop",
  "usage": {...}
}
```

---

## 3. Estado Atual do JSimpleLLM

### 3.1 Infraestrutura Existente

Ao examinar o código, encontramos que **já existe uma base** para suporte à API Responses:

#### 3.1.1 Model_Type.RESPONSES_API
- **Localização**: `src/main/java/bor/tools/simplellm/Model_Type.java:86-88`
- **Status**: Enum definido e documentado
- **Uso**: Já atribuído a vários modelos

#### 3.1.2 Modelos Marcados com RESPONSES_API
Em `OpenAILLMService.java`, os seguintes modelos já têm o tipo `RESPONSES_API`:

```java
// Linhas 129-159
Model gpt_5_nano  = new Model("gpt-5-nano", ..., RESPONSES_API);
Model gpt_5_mini  = new Model("gpt-5-mini", ..., RESPONSES_API);
Model gpt_5       = new Model("gpt-5", ..., RESPONSES_API);
Model gpt_4_1     = new Model("gpt-4.1", ..., RESPONSES_API);
Model gpt_4_mini  = new Model("gpt-4.1-mini", ..., RESPONSES_API);
Model gpt_4o      = new Model("gpt-4o", ..., RESPONSES_API);
Model gpt_4o_mini = new Model("gpt-4o-mini", ..., RESPONSES_API);
Model gpt_o3_mini = new Model("o3-mini", ..., RESPONSES_API);
```

#### 3.1.3 Métodos Parcialmente Implementados

**Em `OpenAILLMService.java`:**

1. **`isResponsesAPIModel(String model)`** (linha 670)
   - Verifica se um modelo suporta a API Responses
   - Status: ✅ Implementado e funcional
   - Verifica tanto o tipo `RESPONSES_API` quanto `GPT5_CLASS`

2. **`convert2ResponseAPI(MapParam params)`** (linha 692)
   - Converte parâmetros do formato Chat Completions para Responses
   - Status: ⚠️ Parcialmente implementado
   - Atualmente converte:
     - `max_tokens` → `max_output_tokens`
     - `prompt` → `input`
     - Remove `temperature` (comentado)

3. **`completionWithResponsesAPI(...)`** (linha 732)
   - Executa completion usando o endpoint `/responses`
   - Status: ⚠️ Parcialmente implementado
   - Combina system + query em um único `input`
   - Faz POST para `/responses`

4. **`parseResponsesAPIResponse(...)`** (linha 790)
   - Parseia resposta da API Responses
   - Status: ⚠️ Implementação básica
   - Tenta vários nomes de campos: `response`, `output`, `text`

#### 3.1.4 Campo useResponsesAPI
- **Localização**: `OpenAILLMService.java:216`
- **Tipo**: `protected boolean useResponsesAPI = false;`
- **Status**: ❌ Declarado mas nunca usado no código

### 3.2 Gaps de Implementação

#### 3.2.1 Integração com Fluxo Existente
- Os métodos de Responses API existem, mas **não são chamados** pelos métodos públicos
- `completion()` e `chatCompletion()` sempre usam `/chat/completions`
- Não há lógica para escolher automaticamente entre os dois endpoints

#### 3.2.2 Parâmetros Faltantes
- `previous_response_id` - não existe em MapParam
- `max_prompt_tokens` - não existe em MapParam
- `truncation_strategy` - não existe em MapParam
- `background` - não existe em MapParam

#### 3.2.3 Formato de Resposta
- `CompletionResponse` não tem campo para `response_id`
- Não há suporte para retornar o ID da resposta para uso em `previous_response_id`

#### 3.2.4 Ferramentas e MCP
- Não há classes para representar tools da Responses API
- Não há suporte para web_search_preview, file_search, computer_use
- Não há infraestrutura para MCP servers

---

## 4. Proposta de Arquitetura

### 4.1 Estratégia de Implementação

Propõe-se uma abordagem **gradual e compatível com o código existente**:

#### Fase 1: Suporte Básico (Mínimo Viável)
1. Completar a implementação de `completionWithResponsesAPI()`
2. Adicionar lógica de seleção automática de endpoint baseada no modelo
3. Adicionar campos necessários em `MapParam`
4. Adicionar campos necessários em `CompletionResponse`
5. Implementar parsing completo de resposta

#### Fase 2: Continuidade de Conversação
1. Adicionar suporte a `previous_response_id`
2. Implementar gerenciamento de IDs de resposta no Chat
3. Adicionar métodos de conveniência para continuar conversas

#### Fase 3: Ferramentas Nativas (Tools da Responses API)
**NOTA**: Web search já existe via `WebSearch` interface (Perplexity). Esta fase adiciona tools nativos da OpenAI como alternativa/complemento.

1. Criar classes para representar tools da OpenAI
2. Implementar suporte a `web_search_preview` (tool nativo OpenAI)
3. Implementar suporte a `file_search` (para documentos/PDFs)
4. Implementar suporte a `computer_use` (experimental)

#### Fase 4: MCP (Opcional)
1. Adicionar infraestrutura MCP
2. Suporte a remote MCP servers
3. Configuração de approval requirements

### 4.2 Decisões de Design

#### 4.2.1 Seleção Automática de Endpoint

**Opção A: Baseada em Tipo de Modelo (Recomendada)**

```java
protected String selectEndpoint(Model model) {
    if (isResponsesAPIModel(model.getName())) {
        return "responses";
    }
    return "chat/completions";
}
```
**Vantagens:**
- Automático e transparente
- Usa infraestrutura existente (Model_Type.RESPONSES_API)
- Não quebra código existente

**Opção B: Baseada em Flag de Configuração**

```java
if (config.isUseResponsesAPI() && isResponsesAPIModel(modelName)) {
    // use responses
}
```

**Vantagens:**
- Controle explícito pelo usuário
- Permite A/B testing
- Pode desabilitar se houver problemas

**Opção C: Baseada em Parâmetro**

```java
params.useResponsesAPI(true);
```
**Vantagens:**
- Controle fino por requisição
- Útil para testes

**Recomendação**: Implementar **Opção A + Opção B**, onde:
- Por padrão, usa Opção A (automático baseado no modelo)
- Pode ser sobrescrito por config global (Opção B)
- Pode ser sobrescrito por params (Opção C) para casos especiais

#### 4.2.2 Tratamento de Histórico de Conversa

**Desafio**: A API Responses usa `input` (string) + `previous_response_id`, enquanto o código atual gerencia um array de Messages.

**Solução Proposta:**
1. Manter o gerenciamento existente de Chat/Messages
2. Ao usar Responses API:
   - Se é a primeira mensagem: usar apenas `input`
   - Se há histórico: incluir `previous_response_id` do último response
   - Armazenar `response_id` em `CompletionResponse`

```java
// Pseudo-código
String responseId = chat.getLastResponseId();
if (responseId != null) {
    payload.put("previous_response_id", responseId);
}
```

#### 4.2.3 Compatibilidade com Código Existente

**Princípio**: Mudanças devem ser **aditivas**, não destrutivas.

- ✅ Adicionar novos campos opcionais em MapParam
- ✅ Adicionar novos campos em CompletionResponse
- ✅ Criar novas classes para tools
- ❌ Não mudar assinaturas de métodos existentes
- ❌ Não quebrar comportamento padrão

---

## 5. Classes e Campos Necessários

### 5.1 Novos Campos em MapParam

```java
public class MapParam extends HashMap<String, Object> {
    // Existing fields...

    // New fields for Responses API
    public MapParam previousResponseId(String id) {
        put("previous_response_id", id);
        return this;
    }

    public String getPreviousResponseId() {
        return (String) get("previous_response_id");
    }

    public MapParam maxOutputTokens(Integer tokens) {
        put("max_output_tokens", tokens);
        return this;
    }

    public Integer getMaxOutputTokens() {
        return (Integer) get("max_output_tokens");
    }

    public MapParam maxPromptTokens(Integer tokens) {
        put("max_prompt_tokens", tokens);
        return this;
    }

    public Integer getMaxPromptTokens() {
        return (Integer) get("max_prompt_tokens");
    }

    public MapParam truncationStrategy(String strategy) {
        put("truncation_strategy", strategy);
        return this;
    }

    public String getTruncationStrategy() {
        return (String) get("truncation_strategy");
    }

    public MapParam background(Boolean enabled) {
        put("background", enabled);
        return this;
    }

    public Boolean getBackground() {
        return (Boolean) get("background");
    }
}
```

### 5.2 Novos Campos em CompletionResponse

```java
@Data
public class CompletionResponse {
    // Existing fields...

    /**
     * The ID of this response, used for continuing conversations
     * with previous_response_id in the Responses API.
     */
    private String responseId;

    /**
     * Indicates if this response was generated using the Responses API.
     */
    private boolean fromResponsesAPI;
}
```

### 5.3 Nova Classe: ResponsesTool

```java
package bor.tools.simplellm;

import lombok.Data;

/**
 * Represents a tool configuration for the OpenAI Responses API.
 */
@Data
public class ResponsesTool {

    public enum ToolType {
        WEB_SEARCH_PREVIEW("web_search_preview"),
        FILE_SEARCH("file_search"),
        COMPUTER_USE("computer_use"),
        MCP("mcp");

        private final String value;

        ToolType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private ToolType type;
    private Map<String, Object> config;

    public static ResponsesTool webSearch() {
        ResponsesTool tool = new ResponsesTool();
        tool.setType(ToolType.WEB_SEARCH_PREVIEW);
        return tool;
    }

    public static ResponsesTool fileSearch() {
        ResponsesTool tool = new ResponsesTool();
        tool.setType(ToolType.FILE_SEARCH);
        return tool;
    }

    public static ResponsesTool computerUse() {
        ResponsesTool tool = new ResponsesTool();
        tool.setType(ToolType.COMPUTER_USE);
        return tool;
    }

    public static ResponsesTool mcp(String serverUrl) {
        ResponsesTool tool = new ResponsesTool();
        tool.setType(ToolType.MCP);
        Map<String, Object> config = new HashMap<>();
        config.put("server_url", serverUrl);
        tool.setConfig(config);
        return tool;
    }
}
```

### 5.4 Extensão em Chat

```java
public class Chat {
    // Existing fields...

    /**
     * The ID of the last response received, used for continuing
     * conversations with the Responses API.
     */
    private String lastResponseId;

    /**
     * Updates the last response ID from a CompletionResponse.
     */
    public void updateLastResponseId(CompletionResponse response) {
        if (response != null && response.getResponseId() != null) {
            this.lastResponseId = response.getResponseId();
        }
    }
}
```

---

## 6. Mudanças Necessárias no OpenAIJsonMapper

### 6.1 Novo Método: toResponsesAPIRequest

```java
/**
 * Creates a request payload for the Responses API endpoint.
 *
 * @param input The user input
 * @param params Additional parameters
 * @return Map representing the JSON request body
 */
public Map<String, Object> toResponsesAPIRequest(String input, MapParam params) {
    Map<String, Object> payload = new HashMap<>();

    // Required fields
    String modelName = params.getModel();
    if (modelName == null) {
        throw new IllegalArgumentException("Model must be specified");
    }
    payload.put("model", modelName);
    payload.put("input", input);

    // Optional: previous_response_id for continuing conversations
    if (params.getPreviousResponseId() != null) {
        payload.put("previous_response_id", params.getPreviousResponseId());
    }

    // Optional: max_output_tokens (instead of max_tokens)
    if (params.getMaxOutputTokens() != null) {
        payload.put("max_output_tokens", params.getMaxOutputTokens());
    } else if (params.getMaxTokens() != null) {
        // Fallback to max_tokens if max_output_tokens not specified
        payload.put("max_output_tokens", params.getMaxTokens());
    }

    // Optional: max_prompt_tokens
    if (params.getMaxPromptTokens() != null) {
        payload.put("max_prompt_tokens", params.getMaxPromptTokens());
    }

    // Optional: truncation_strategy
    if (params.getTruncationStrategy() != null) {
        payload.put("truncation_strategy", params.getTruncationStrategy());
    }

    // Optional: background mode
    if (params.getBackground() != null) {
        payload.put("background", params.getBackground());
    }

    // Optional: streaming
    if (params.isStream() != null && params.isStream()) {
        payload.put("stream", true);
    }

    // Optional: reasoning effort for reasoning models
    if (params.getReasoningEffort() != null) {
        Map<String, String> reasoning = new HashMap<>();
        reasoning.put("effort", params.getReasoningEffort().name().toLowerCase());
        payload.put("reasoning", reasoning);
    }

    // Optional: tools
    // TODO: Add tools support when ResponsesTool class is implemented

    return payload;
}
```

### 6.2 Novo Método: fromResponsesAPIResponse

```java
/**
 * Parses a response from the Responses API endpoint.
 *
 * @param response The API response as a Map
 * @return CompletionResponse object
 * @throws LLMException if parsing fails
 */
@SuppressWarnings("unchecked")
public CompletionResponse fromResponsesAPIResponse(Map<String, Object> response)
        throws LLMException {
    CompletionResponse completionResponse = new CompletionResponse();

    try {
        // Response ID
        String responseId = (String) response.get("id");
        completionResponse.setResponseId(responseId);
        completionResponse.setFromResponsesAPI(true);

        // Output text (main content)
        String outputText = (String) response.get("output_text");
        if (outputText == null) {
            outputText = (String) response.get("output");
        }
        if (outputText != null) {
            completionResponse.setResponse(
                new ContentWrapper(ContentType.TEXT, outputText)
            );
        }

        // Reasoning content (if present)
        String reasoningContent = (String) response.get("reasoning_content");
        if (reasoningContent != null) {
            completionResponse.setReasoningContent(reasoningContent);
        }

        // Finish reason
        String finishReason = (String) response.get("finish_reason");
        if (finishReason != null) {
            completionResponse.setEndReason(finishReason);
        }

        // Usage statistics
        Map<String, Object> usage = (Map<String, Object>) response.get("usage");
        if (usage != null) {
            MapParam usageParam = new MapParam();
            usageParam.putAll(usage);
            completionResponse.setUsage(usageParam);
        }

        // Store full response in info
        MapParam info = new MapParam();
        info.putAll(response);
        completionResponse.setInfo(info);

        // Model name
        String model = (String) response.get("model");
        if (model != null) {
            info.put("model", model);
        }

        return completionResponse;

    } catch (Exception e) {
        throw new LLMException(
            "Failed to parse Responses API response: " + e.getMessage(), e
        );
    }
}
```

---

## 7. Mudanças no OpenAILLMService

### 7.1 Completar implementação de completionWithResponsesAPI

A implementação atual (linha 732) precisa ser revisada:

**Problemas na implementação atual:**
1. Concatena system + query em um único input (perde a distinção)
2. Usa lógica manual de construção de payload
3. Não usa OpenAIJsonMapper

**Implementação revisada:**

```java
protected CompletionResponse completionWithResponsesAPI(
        String system,
        String query,
        MapParam params,
        String model) throws LLMException {

    // Combine system and query into input
    StringBuilder input = new StringBuilder();
    if (system != null && !system.trim().isEmpty()) {
        input.append(system.trim()).append("\n\n");
    }
    input.append(query.trim());

    // Ensure model is set
    if (params.getModel() == null) {
        params.model(model);
    }

    try {
        // Create request payload using mapper
        Map<String, Object> payload = jsonMapper.toResponsesAPIRequest(
            input.toString(),
            params
        );

        // Make API request to responses endpoint
        Map<String, Object> response = postRequest("responses", payload);

        // Parse response using mapper
        CompletionResponse completionResponse =
            jsonMapper.fromResponsesAPIResponse(response);

        return completionResponse;

    } catch (LLMException e) {
        throw e;
    } catch (Exception e) {
        throw new LLMException(
            "Unexpected error during Responses API completion: " + e.getMessage(),
            e
        );
    }
}
```

### 7.2 Adicionar lógica de seleção de endpoint em completion()

```java
@Override
public CompletionResponse completion(String system, String query, MapParam params)
        throws LLMException {

    if (system == null && (query == null || query.trim().isEmpty())) {
        throw new LLMException("Query cannot be null or empty");
    }

    params = fixParams(params);
    Model model = params.getModelObj();
    String modelName = model != null ? model.getName() : params.getModel();

    // Decide which endpoint to use
    boolean useResponses = shouldUseResponsesAPI(model, params);

    if (useResponses) {
        // Use Responses API endpoint
        return completionWithResponsesAPI(system, query, params, modelName);
    } else {
        // Use existing Chat Completions endpoint
        Chat chat = new Chat();
        if (system != null && !system.trim().isEmpty()) {
            chat.addSystemMessage(system.trim());
        }
        return chatCompletion(chat, query, params);
    }
}
```

### 7.3 Novo método: shouldUseResponsesAPI

```java
/**
 * Determines whether to use the Responses API or Chat Completions API
 * based on model capabilities and configuration.
 *
 * @param model The model to use
 * @param params Request parameters
 * @return true if Responses API should be used
 */
protected boolean shouldUseResponsesAPI(Model model, MapParam params) {
    // Check if explicitly disabled in params
    if (params.containsKey("use_responses_api")) {
        return Boolean.TRUE.equals(params.get("use_responses_api"));
    }

    // Check global config flag
    if (!config.isUseResponsesAPI()) {
        return false;
    }

    // Check if model supports Responses API
    if (model != null) {
        return isResponsesAPIModel(model.getName());
    }

    return false;
}
```

### 7.4 Atualizar chatCompletion para armazenar response_id

```java
@Override
public CompletionResponse chatCompletion(Chat chat, String query, MapParam params)
        throws LLMException {

    // ... existing implementation ...

    // After getting response:
    CompletionResponse completionResponse = jsonMapper.fromChatCompletionResponse(response);

    // Store response ID if using Responses API
    if (completionResponse.getResponseId() != null) {
        chat.updateLastResponseId(completionResponse);
    }

    // ... rest of existing implementation ...
}
```

### 7.5 Novo método: continueConversation

Método de conveniência para continuar conversas usando `previous_response_id`:

```java
/**
 * Continues a conversation using the Responses API with previous_response_id.
 * This is more efficient than sending full message history.
 *
 * @param chat The chat session with previous response ID
 * @param query The new user query
 * @param params Additional parameters
 * @return CompletionResponse with the assistant's reply
 * @throws LLMException if the model doesn't support Responses API or if there's no previous response
 */
public CompletionResponse continueConversation(Chat chat, String query, MapParam params)
        throws LLMException {

    if (chat == null) {
        throw new LLMException("Chat session cannot be null");
    }

    String lastResponseId = chat.getLastResponseId();
    if (lastResponseId == null || lastResponseId.isEmpty()) {
        throw new LLMException(
            "Cannot continue conversation: no previous response ID found. " +
            "Use completion() or chatCompletion() for the first message."
        );
    }

    Model model = chat.getModel() != null ?
        config.getModel(chat.getModel()) :
        config.getModel(getDefaultModelName());

    if (!isResponsesAPIModel(model.getName())) {
        throw new LLMException(
            "Model " + model.getName() + " does not support Responses API. " +
            "Use a GPT-5, GPT-4.1, GPT-4o, or O3 model."
        );
    }

    // Set up parameters
    params = fixParams(params, chat);
    params.previousResponseId(lastResponseId);

    try {
        // Create request payload
        Map<String, Object> payload = jsonMapper.toResponsesAPIRequest(query, params);

        // Make API request
        Map<String, Object> response = postRequest("responses", payload);

        // Parse response
        CompletionResponse completionResponse =
            jsonMapper.fromResponsesAPIResponse(response);

        // Update chat with new messages
        chat.addUserMessage(query);
        if (completionResponse.getResponse() != null) {
            String assistantResponse = completionResponse.getResponse().getText();
            chat.addAssistantMessage(assistantResponse);
        }

        // Update response ID for next turn
        chat.updateLastResponseId(completionResponse);
        completionResponse.setChatId(chat.getId());

        return completionResponse;

    } catch (LLMException e) {
        throw e;
    } catch (Exception e) {
        throw new LLMException(
            "Failed to continue conversation: " + e.getMessage(),
            e
        );
    }
}
```

---

## 8. Atualização do LLMConfig

### 8.1 Novo campo: useResponsesAPI

```java
@Data
@Builder
public class LLMConfig {
    // Existing fields...

    /**
     * Flag to enable/disable automatic use of Responses API when available.
     * Default: true for OpenAI endpoints, false for others.
     */
    @Builder.Default
    private boolean useResponsesAPI = true;
}
```

---

## 9. Compatibilidade com Outros Providers

### 9.1 LMStudio

LM Studio anunciou suporte à Responses API em v0.3.29 (conforme documentação encontrada).

**Ações necessárias:**
- Verificar se LMStudio realmente suporta o endpoint `/v1/responses`
- Adicionar lógica similar em `LMStudioLLMService`
- Testar com modelos locais

### 9.2 Ollama

**Status**: Não encontrado suporte à Responses API.

**Ação**: Manter apenas Chat Completions para Ollama.

### 9.3 Perplexity

**Status**: Perplexity usa API própria, não OpenAI-compatible.

**Ação**: Nenhuma mudança necessária.

---

## 10. Testes Necessários

### 10.1 Testes Unitários

1. **MapParam**
   - Getters/setters para novos campos
   - Validação de valores

2. **OpenAIJsonMapper**
   - `toResponsesAPIRequest()` com vários cenários
   - `fromResponsesAPIResponse()` com respostas válidas e inválidas

3. **OpenAILLMService**
   - `shouldUseResponsesAPI()` com diferentes configs
   - `isResponsesAPIModel()` para todos os modelos

### 10.2 Testes de Integração

1. **Completion básico com Responses API**
   - Modelo GPT-5
   - Verificar que usa endpoint correto
   - Validar response_id na resposta

2. **Continuação de conversa**
   - Primeira mensagem sem previous_response_id
   - Segunda mensagem com previous_response_id
   - Verificar que contexto é mantido

3. **Fallback para Chat Completions**
   - Modelo antigo (GPT-3.5)
   - Verificar que usa endpoint antigo
   - Comportamento deve ser idêntico

4. **Streaming com Responses API**
   - Verificar formato SSE
   - Confirmar que funciona igual ao Chat Completions

5. **Parâmetros específicos**
   - max_output_tokens
   - max_prompt_tokens
   - truncation_strategy
   - background mode

### 10.3 Testes com API Real

**Requisitos:**
- Conta OpenAI com acesso a modelos GPT-5 ou GPT-4.1
- API key válida

**Cenários de teste:**

```java
@Test
public void testResponsesAPIBasicCompletion() throws LLMException {
    OpenAILLMService service = new OpenAILLMService();
    MapParam params = new MapParam();
    params.model("gpt-5-mini");
    params.maxOutputTokens(100);

    CompletionResponse response = service.completion(
        "You are a helpful assistant",
        "What is 2+2?",
        params
    );

    assertNotNull(response);
    assertNotNull(response.getResponseId());
    assertTrue(response.isFromResponsesAPI());
    assertTrue(response.getText().contains("4"));
}

@Test
public void testResponsesAPIContinuation() throws LLMException {
    OpenAILLMService service = new OpenAILLMService();
    Chat chat = new Chat();
    MapParam params = new MapParam();
    params.model("gpt-5-mini");

    // First message
    CompletionResponse response1 = service.completion(
        "You are a math tutor",
        "My name is Alice",
        params
    );
    chat.updateLastResponseId(response1);

    // Continue conversation
    CompletionResponse response2 = service.continueConversation(
        chat,
        "What is my name?",
        params
    );

    assertTrue(response2.getText().toLowerCase().contains("alice"));
}
```

---

## 11. Documentação Necessária

### 10.4 Web Search: Diferenças entre Implementações

**IMPORTANTE**: O JSimpleLLM já possui implementação de Web Search via interface `WebSearch` (package `bor.tools.simplellm.websearch`).

#### Implementação Atual (Via Perplexity)
- **Interface**: `WebSearch`
- **Provider**: Perplexity AI (sonar models)
- **Abordagem**: API dedicada de web search
- **Recursos**: Citations completas, domain filters, recency filters, related questions, images
- **Status**: ✅ **Completamente implementado e funcional**

#### Web Search via Responses API (OpenAI)
- **Interface**: Tool nativo da Responses API
- **Provider**: OpenAI (GPT-5, GPT-4.1, O3)
- **Abordagem**: Tool integrado ao LLM
- **Recursos**: Web search como capacidade nativa do modelo
- **Status**: ❌ **Não implementado**

#### Diferenças Chave

| Aspecto | WebSearch (Perplexity) | Responses API Tools (OpenAI) |
|---------|------------------------|------------------------------|
| **Como usar** | `WebSearch.webSearch()` | `params.addTool(ResponsesTool.webSearch())` |
| **Citations** | Sempre incluídas | Depende da configuração |
| **API dedicada** | Sim (Perplexity endpoint) | Não (integrado ao LLM) |
| **Controle fino** | Alto (filtros, recency, domains) | Médio (configuração de tool) |
| **Modelos** | Sonar series | GPT-5, GPT-4.1, O3 |

#### Quando usar cada um?

**Use WebSearch (Perplexity)** quando:
- Precisa de citations detalhadas
- Quer filtros avançados (domains, recency)
- Precisa de related questions
- Busca em tempo real é crítico

**Use Responses API Tools** quando:
- Já usa modelos OpenAI (GPT-5, O3)
- Quer integração mais simples
- Busca é parte de um fluxo maior
- Não precisa de controle fino sobre a busca

#### Compatibilidade

As duas abordagens **NÃO são mutuamente exclusivas**:

```java
// Opção 1: Web search via Perplexity (já funciona)
WebSearch search = WebSearchFactory.createPerplexity();
SearchResponse response = search.webSearch("query", params);

// Opção 2: Web search via Responses API (futuro - Fase 3)
OpenAILLMService service = new OpenAILLMService();
MapParam params = new MapParam()
    .model("gpt-5")
    .addTool(ResponsesTool.webSearch());
CompletionResponse response = service.completion("system", "query", params);
```

**Conclusão**: A Fase 3 do roadmap (Ferramentas Nativas) deve ser repensada. O web search via Responses API é **complementar** ao WebSearch existente, não um substituto.

---

### 11.1 README ou Wiki

Adicionar seção explicando:
- O que é a API Responses
- Quando ela é usada automaticamente
- Como desabilitar se necessário
- Como usar `previous_response_id` manualmente
- **Diferença entre WebSearch (Perplexity) e Web Search Tools (OpenAI)**

### 11.2 Javadoc

Atualizar Javadoc de:
- `LLMProvider.completion()`
- `LLMProvider.chatCompletion()`
- Novos métodos em `OpenAILLMService`

### 11.3 Exemplos

Criar exemplos de uso:
- `ResponsesAPIExample.java` - básico
- `ResponsesAPIContinuationExample.java` - com previous_response_id
- `ResponsesAPIToolsExample.java` - com web search (Fase 3)

---

## 12. Estimativa de Esforço

### 12.1 Fase 1: Suporte Básico
**Esforço estimado**: 2-3 dias de desenvolvimento

**Tarefas:**
1. Adicionar campos em MapParam (2h)
2. Adicionar campos em CompletionResponse (1h)
3. Implementar OpenAIJsonMapper.toResponsesAPIRequest() (3h)
4. Implementar OpenAIJsonMapper.fromResponsesAPIResponse() (3h)
5. Atualizar OpenAILLMService.completion() (2h)
6. Implementar shouldUseResponsesAPI() (1h)
7. Adicionar flag em LLMConfig (1h)
8. Testes unitários (4h)
9. Testes de integração (4h)
10. Documentação básica (2h)

**Total**: ~23 horas

### 12.2 Fase 2: Continuidade de Conversação
**Esforço estimado**: 1-2 dias de desenvolvimento

**Tarefas:**
1. Adicionar lastResponseId em Chat (1h)
2. Implementar continueConversation() (3h)
3. Atualizar chatCompletion() (2h)
4. Testes (4h)
5. Exemplos e documentação (2h)

**Total**: ~12 horas

### 12.3 Fase 3: Ferramentas Nativas (Revisada)
**Esforço estimado**: 2-3 dias de desenvolvimento

**NOTA**: Esta estimativa foi revisada. Web search via Perplexity já existe. Esta fase adiciona tools nativos da OpenAI como alternativa complementar.

**Tarefas:**
1. Criar classe ResponsesTool (2h)
2. Integrar no request builder (3h)
3. Parsear tool results na resposta (4h)
4. Implementar `web_search_preview` tool (3h) - **simplificado, Perplexity já existe**
5. Implementar `file_search` tool (4h)
6. Implementar `computer_use` tool (3h)
7. Testes (6h)
8. Exemplos mostrando diferença entre Perplexity e OpenAI tools (3h)
9. Documentação comparativa (2h)

**Total**: ~30 horas (reduzido de 32h devido a WebSearch existente)

---

## 13. Riscos e Considerações

### 13.1 Riscos Técnicos

1. **API ainda em evolução**
   - Risco: API Responses é nova (março 2025), pode ter mudanças
   - Mitigação: Implementar de forma modular, fácil de atualizar

2. **Diferenças sutis de comportamento**
   - Risco: Responses API pode dar respostas diferentes de Chat Completions
   - Mitigação: Testes extensivos, documentar diferenças

3. **Parâmetros não documentados**
   - Risco: Alguns parâmetros reportados como "unknown" por usuários
   - Mitigação: Testar cada parâmetro, ter fallbacks

4. **Compatibilidade com LM Studio**
   - Risco: Implementação de LM Studio pode ser incompleta
   - Mitigação: Fazer feature detection, fallback para Chat Completions

### 13.2 Riscos de Produto

1. **Confusão do usuário**
   - Risco: Usuários não entendem quando cada API é usada
   - Mitigação: Documentação clara, logs informativos

2. **Custos de API**
   - Risco: Responses API pode ter custos diferentes
   - Mitigação: Documentar, permitir opt-out

3. **Breaking changes**
   - Risco: Mudar comportamento padrão pode quebrar código existente
   - Mitigação: Adicionar flag de configuração, fase de opt-in

### 13.3 Questões em Aberto

1. **temperature em Responses API**
   - Documentação menciona, mas usuários reportam erro
   - Ação: Testar e documentar comportamento real

2. **response_format**
   - Mesmo problema que temperature
   - Ação: Testar JSON mode, structured outputs

3. **Streaming format**
   - Formato SSE pode ser diferente
   - Ação: Verificar se StreamingUtil precisa de ajustes

4. **Custos e rate limits**
   - Não está claro se há diferenças
   - Ação: Consultar documentação de pricing da OpenAI

---

## 14. Alternativas Consideradas

### 14.1 Alternativa 1: Não implementar

**Prós:**
- Menos código para manter
- Sem riscos de bugs

**Contras:**
- Perde features novas (web search nativo, MCP)
- Código fica defasado
- Usuários podem pedir depois

**Decisão**: ❌ Não recomendado

### 14.2 Alternativa 2: Implementação wrapper

Criar uma classe separada `ResponsesAPIClient` ao invés de integrar no `OpenAILLMService`.

**Prós:**
- Isolamento total
- Sem risco de quebrar código existente
- Mais fácil de deprecar se a API mudar muito

**Contras:**
- Duplicação de código
- Usuário precisa escolher qual usar
- Fragmentação da API

**Decisão**: ❌ Não recomendado

### 14.3 Alternativa 3: Implementação como extensão opcional

Criar um módulo separado `jsimplellm-responses-api` como dependência opcional.

**Prós:**
- Não aumenta tamanho base da lib
- Usuários que não precisam não pagam o custo

**Contras:**
- Complexidade de manutenção
- Dificulta descoberta do recurso

**Decisão**: ⚠️ Considerar para futuro se a lib crescer muito

---

## 15. Recomendações Finais

### 15.1 Prioridades

**Alta prioridade (fazer já):**
1. ✅ Fase 1 - Suporte básico
   - Permite usar modelos modernos (GPT-5, O3)
   - Funcionalidade essencial

2. ✅ Fase 2 - Continuidade de conversação
   - Melhora eficiência (menos tokens)
   - Feature killer da Responses API

**Média prioridade (próximos releases):**
3. ⚠️ Fase 3 - Ferramentas nativas
   - Web search é muito útil
   - Mas pode ser feito depois

**Baixa prioridade (futuro):**
4. ⏸️ Fase 4 - MCP
   - Feature avançada
   - Poucos usuários vão usar inicialmente

### 15.2 Estratégia de Rollout

**Opção A: Opt-in inicial**

```java
// Default: false para segurança
config.setUseResponsesAPI(true); // usuário deve habilitar
```

**Opção B: Opt-out**

```java
// Default: true (automático)
config.setUseResponsesAPI(false); // usuário pode desabilitar
```

**Recomendação**: Começar com **Opção A** (opt-in) durante 1-2 releases, depois mudar para **Opção B** (opt-out) quando estável.

### 15.3 Próximos Passos

1. **Validar este estudo** com mantenedores/usuários
2. **Testar API real** com conta OpenAI
   - Verificar comportamento de parâmetros duvidosos (temperature, response_format)
   - Testar com diferentes modelos
   - Documentar diferenças encontradas
3. **Implementar Fase 1** (suporte básico)
4. **Release beta** com flag opt-in
5. **Coletar feedback** da comunidade
6. **Iterar** e implementar Fase 2

---

## 16. Referências

- [OpenAI Responses API Reference](https://platform.openai.com/docs/api-reference/responses)
- [Azure OpenAI Responses API](https://learn.microsoft.com/en-us/azure/ai-foundry/openai/how-to/responses)
- [DataCamp Tutorial](https://www.datacamp.com/tutorial/openai-responses-api)
- [LM Studio Blog - Responses API Support](https://lmstudio.ai/blog/lmstudio-v0.3.29)
- [OpenAI Community - response_format issue](https://community.openai.com/t/response-format-not-available-for-the-responses-api/1147369)

---

## Conclusão

A implementação do suporte à API Responses da OpenAI é **viável e recomendada** para o JSimpleLLM. O projeto já possui uma base sólida com:
- ✅ Enum `RESPONSES_API` definido
- ✅ Modelos já marcados com suporte
- ✅ Métodos parciais implementados
- ✅ **Interface `WebSearch` completa e funcional** (via Perplexity)

### Descobertas Durante o Estudo

Durante esta análise, foi identificado que o JSimpleLLM já possui uma **implementação robusta de Web Search** via Perplexity AI. Isso significa:

1. **Web search não é prioridade na Fase 3** - já está implementado de forma superior via Perplexity
2. **Responses API tools serão complementares** - para quem já usa modelos OpenAI e quer integração mais simples
3. **Duas abordagens podem coexistir** - Perplexity para busca avançada, OpenAI tools para integração simples

### Proposta de Implementação

A implementação proposta é:
- ✅ **Compatível** com código existente (incluindo `WebSearch`)
- ✅ **Modular** e fácil de manter
- ✅ **Gradual** com fases bem definidas
- ✅ **Testável** com testes claros
- ✅ **Documentada** com exemplos práticos
- ✅ **Não duplica funcionalidades** - complementa o que já existe

### Priorização Revisada

**Fases prioritárias:**
1. ✅ **Fase 1** (Suporte Básico) - ~3 dias - **ALTA PRIORIDADE**
   - Fundamental para usar modelos modernos (GPT-5, O3)

2. ✅ **Fase 2** (Continuidade) - ~1.5 dias - **ALTA PRIORIDADE**
   - Feature killer da Responses API
   - Economiza tokens e custos

3. ⚠️ **Fase 3** (Tools) - ~3 dias - **MÉDIA PRIORIDADE (revisada)**
   - Menos prioritária devido a `WebSearch` existente
   - Focar em `file_search` e `computer_use` primeiro
   - `web_search_preview` como nice-to-have

4. ⏸️ **Fase 4** (MCP) - **BAIXA PRIORIDADE**
   - Feature avançada para casos específicos

**Recomendação final**:
1. Prosseguir com **Fase 1** (suporte básico) em modo opt-in
2. Implementar **Fase 2** imediatamente após Fase 1
3. Reavaliar necessidade da **Fase 3** após feedback dos usuários
4. **Documentar claramente** a diferença entre `WebSearch` (Perplexity) e Responses API tools
