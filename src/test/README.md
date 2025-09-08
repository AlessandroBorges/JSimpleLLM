# JSimpleLLM Test Suite

Este diretório contém testes abrangentes para a implementação OpenAI do JSimpleLLM.

## Configuração dos Testes

### Pré-requisitos

1. **Token da API OpenAI**: Os testes de integração requerem uma chave válida da API OpenAI.
2. **Java 17+**: Os testes foram desenvolvidos para Java 17.
3. **Maven**: Para executar os testes.

### Configuração da Variável de Ambiente

Defina a variável de ambiente `OPENAI_API_TOKEN` com sua chave da API:

```bash
# Linux/Mac
export OPENAI_API_TOKEN=your_openai_api_key_here

# Windows
set OPENAI_API_TOKEN=your_openai_api_key_here
```

**Importante**: Se a variável `OPENAI_API_TOKEN` não estiver definida, os testes de integração serão automaticamente ignorados (skipped).

## Classes de Teste

### 1. `OpenAILLMServiceTestBase`
Classe base que fornece configuração comum para todos os testes.

### 2. `OpenAICompletionTest`
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

### 6. `OpenAIIntegrationTest`
Testes de integração completos:
- Fluxo completo de conversa
- Tratamento de erros
- Performance e metadados
- Comparação entre modelos
- Casos extremos

## Executando os Testes

### Executar Todos os Testes
```bash
mvn test
```

### Executar Testes Específicos
```bash
# Apenas testes de completion
mvn test -Dtest=OpenAICompletionTest

# Apenas testes de chat
mvn test -Dtest=OpenAIChatTest

# Apenas testes de embeddings
mvn test -Dtest=OpenAIEmbeddingsTest

# Apenas testes de configuração
mvn test -Dtest=OpenAIConfigTest

# Apenas testes de integração
mvn test -Dtest=OpenAIIntegrationTest
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