# JSimpleLLM Test Suite

Este diretório contém testes abrangentes para as implementações OpenAI e Ollama do JSimpleLLM.

## Configuração dos Testes

### Pré-requisitos

#### Para Testes OpenAI
1. **Token da API OpenAI**: Os testes de integração requerem uma chave válida da API OpenAI.
2. **Java 17+**: Os testes foram desenvolvidos para Java 17.
3. **Maven**: Para executar os testes.

#### Para Testes Ollama
1. **Servidor Ollama**: Os testes requerem um servidor Ollama rodando localmente.
2. **Modelos instalados**: Pelo menos um modelo deve estar instalado (ex: phi3.5, llama3.2).
3. **Porta 11434**: O servidor Ollama deve estar acessível em localhost:11434.

### Configuração das Variáveis de Ambiente

#### Para OpenAI
Defina a variável de ambiente `OPENAI_API_KEY` com sua chave da API:

```bash
# Linux/Mac
export OPENAI_API_KEY=your_openai_api_key_here

# Windows
set OPENAI_API_KEY=your_openai_api_key_here
```

#### Para Ollama
```bash
# Linux/Mac (opcional - usa "ollama" por padrão)
export OLLAMA_API_KEY=ollama

# Windows (opcional)
set OLLAMA_API_KEY=ollama
```

#### Configuração do Servidor Ollama
```bash
# Instalar Ollama (Linux/Mac)
curl -fsSL https://ollama.ai/install.sh | sh

# Baixar um modelo (exemplo)
ollama pull phi3.5
ollama pull llama3.2

# Iniciar servidor (geralmente inicia automaticamente)
ollama serve
```

**Importante**: 
- Se `OPENAI_API_KEY` não estiver definida, os testes OpenAI serão ignorados
- Se o servidor Ollama não estiver rodando, os testes Ollama serão ignorados

## Classes de Teste

### Testes OpenAI

#### 1. `OpenAILLMServiceTestBase`
Classe base que fornece configuração comum para todos os testes OpenAI.

#### 2. `OpenAICompletionTest`
Testa a funcionalidade de completion:
- Completions básicos com system prompts
- Completions sem system prompts
- Diferentes modelos
- Tratamento de erros
- Metadados da resposta

### 3. `OpenAIChatTest`
Testa a funcionalidade de chat:
- Conversas básicas
- Conversas multi-turn
- Preservação de contexto
- Modelos personalizados
- Chat vazio

### 4. `OpenAIEmbeddingsTest`
Testa a geração de embeddings:
- Embeddings básicos
- Diferentes modelos
- Dimensões customizadas
- Similaridade semântica
- Textos de diferentes tamanhos

### 5. `OpenAIConfigTest`
Testa configuração e gerenciamento de modelos:
- Configuração padrão
- Configurações personalizadas
- Listagem de modelos
- Autenticação
- Capacidades dos modelos

#### 6. `OpenAIIntegrationTest`
Testes de integração completos:
- Fluxo completo de conversa
- Tratamento de erros
- Performance e metadados
- Comparação entre modelos
- Casos extremos

### Testes Ollama

#### 1. `OllamaLLMServiceTestBase`
Classe base que fornece configuração comum para todos os testes Ollama.

#### 2. `OllamaCompletionTest`
Testa a funcionalidade de completion com Ollama:
- Completions básicos com modelos locais
- Uso do factory pattern
- Configuração específica do Ollama
- Tratamento de erros

#### 3. `OllamaChatTest`
Testa a funcionalidade de chat com Ollama:
- Conversas multi-turn
- Preservação de contexto
- Assistência de programação
- Chat sem consultas adicionais

#### 4. `OllamaIntegrationTest`
Testes de integração completos para Ollama:
- Conectividade com servidor local
- Listagem de modelos instalados
- Compatibilidade com API OpenAI
- Performance com modelos locais

## Executando os Testes

### Executar Todos os Testes
```bash
mvn test
```

### Executar Testes Específicos

#### Testes OpenAI
```bash
# Apenas testes de completion OpenAI
mvn test -Dtest=OpenAICompletionTest

# Apenas testes de chat OpenAI
mvn test -Dtest=OpenAIChatTest

# Apenas testes de embeddings OpenAI
mvn test -Dtest=OpenAIEmbeddingsTest

# Apenas testes de configuração OpenAI
mvn test -Dtest=OpenAIConfigTest

# Apenas testes de integração OpenAI
mvn test -Dtest=OpenAIIntegrationTest

# Todos os testes OpenAI
mvn test -Dtest=OpenAI*
```

#### Testes Ollama
```bash
# Apenas testes de completion Ollama
mvn test -Dtest=OllamaCompletionTest

# Apenas testes de chat Ollama
mvn test -Dtest=OllamaChatTest

# Apenas testes de integração Ollama
mvn test -Dtest=OllamaIntegrationTest

# Todos os testes Ollama
mvn test -Dtest=Ollama*
```

### Executar com Logs Detalhados
```bash
mvn test -Dtest=OpenAIIntegrationTest -X
```

## Estrutura dos Testes

### Testes Unitários vs Integração

- **Testes de configuração**: Não fazem chamadas para a API
- **Testes de funcionalidade**: Fazem chamadas reais para a API OpenAI
- **Testes de integração**: Testam fluxos completos com múltiplas chamadas

### Tratamento de Erros

Os testes incluem verificações para:
- Autenticação inválida
- Parâmetros inválidos
- Entrada vazia/nula
- Limites de token
- Timeouts de rede

### Performance

Os testes de integração incluem verificações básicas de performance:
- Tempo de resposta
- Utilização de tokens
- Metadados da API

## Modelos Testados

Os testes utilizam principalmente:
- `gpt-4o-mini`: Para completions e chat
- `text-embedding-3-small`: Para embeddings

## Exemplos de Saída

### Sucesso
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

### Com API Token Ausente
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 12
[WARNING] Tests were skipped due to missing OPENAI_API_TOKEN
```

### Com Falhas
```
[ERROR] Tests run: 15, Failures: 2, Errors: 1, Skipped: 0
```

## Notas Importantes

1. **Custos**: Os testes fazem chamadas reais para a API OpenAI e podem gerar custos
2. **Rate Limiting**: Execute os testes com moderação para evitar limites de taxa
3. **Configuração**: Certifique-se de que sua chave da API tem permissões adequadas
4. **Rede**: Os testes requerem conexão com a internet

## Troubleshooting

### Testes Ignorados
- Verifique se `OPENAI_API_TOKEN` está definido
- Certifique-se de que o token não está vazio

### Falhas de Autenticação
- Verifique se o token da API é válido
- Confirme se o token tem as permissões necessárias

### Timeouts
- Verifique sua conexão com a internet
- Considere aumentar os timeouts se necessário

### Limites de Taxa
- Aguarde alguns minutos antes de executar novamente
- Execute testes individuais em vez de todo o conjunto