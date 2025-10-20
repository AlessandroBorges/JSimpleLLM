package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.CompletionResponse;
import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMService;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.MapParam;
import bor.tools.simplellm.Model;
import bor.tools.simplellm.Model_Type;
import bor.tools.simplellm.SERVICE_PROVIDER;
import bor.tools.simplellm.ModelEmbedding.Embeddings_Op;
import bor.tools.simplellm.chat.Chat;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.websearch.SearchResponse;
import bor.tools.simplellm.websearch.WebSearch;

/**
 * Integration tests for Perplexity AI LLM service.
 * <p>
 * These tests verify the integration with Perplexity API including:
 * <ul>
 * <li>Basic service creation and configuration</li>
 * <li>Web search functionality</li>
 * <li>Chat completion with context</li>
 * <li>Citations and search results extraction</li>
 * <li>Model type checking</li>
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> Tests that make real API calls are skipped if PERPLEXITY_API_KEY
 * environment variable is not set. This allows the test suite to run in CI/CD
 * environments without API keys while still testing service creation and configuration.
 * </p>
 *
 * @author AlessandroBorges
 * @since 1.1
 */
public class PerplexityIntegrationTest {

    private String apiKey;

    @BeforeEach
    public void setUp() {
        apiKey = System.getenv("PERPLEXITY_API_KEY");
    }

    /**
     * Test: Verify Perplexity service can be created with default config.
     */
    @Test
    public void testCreatePerplexityService() {
        LLMService service = LLMServiceFactory.createPerplexity();

        assertNotNull(service, "Service should not be null");
        assertEquals(SERVICE_PROVIDER.PERPLEXITY, service.getServiceProvider(),
                "Service provider should be PERPLEXITY");
        assertTrue(service instanceof WebSearch, "Service should implement WebSearch interface");
    }

    /**
     * Test: Verify default configuration values.
     */
    @Test
    public void testDefaultConfiguration() {
        LLMService service = LLMServiceFactory.createPerplexity();
        LLMConfig config = service.getLLMConfig();

        assertNotNull(config, "Config should not be null");
        assertEquals("https://api.perplexity.ai", config.getBaseUrl(),
                "Base URL should be Perplexity API endpoint");
        assertEquals("PERPLEXITY_API_KEY", config.getApiTokenEnvironment(),
                "API token environment variable should be PERPLEXITY_API_KEY");
    }

    /**
     * Test: Verify registered models are correctly configured.
     * @throws LLMException 
     */
    @Test
    public void testRegisteredModels() throws LLMException {
        LLMService service = LLMServiceFactory.createPerplexity();
        var models = service.getRegisteredModels();

        assertNotNull(models, "Models map should not be null");
        assertTrue(models.size() >= 6, "Should have at least 6 models registered");

        // Check specific models
        Model sonar = models.getModel("sonar");
        assertNotNull(sonar, "sonar model should be registered");
        assertTrue(sonar.isType(Model_Type.WEBSEARCH), "sonar should support web search");
        assertTrue(sonar.isType(Model_Type.CITATIONS), "sonar should support citations");

        Model sonarPro = models.getModel("sonar-pro");
        assertNotNull(sonarPro, "sonar-pro model should be registered");
        assertEquals(200000, sonarPro.getContextLength(), "sonar-pro should have 200k context");

        Model deepResearch = models.getModel("sonar-deep-research");
        assertNotNull(deepResearch, "sonar-deep-research model should be registered");
        assertTrue(deepResearch.isType(Model_Type.DEEP_RESEARCH),
                "sonar-deep-research should support deep research");

        // Check offline model
        Model r1 = models.getModel("r1-1776");
        assertNotNull(r1, "r1-1776 model should be registered");
        assertFalse(r1.isType(Model_Type.WEBSEARCH), "r1-1776 should NOT support web search");
    }

    /**
     * Test: Verify WebSearch interface methods are available.
     */
    @Test
    public void testWebSearchInterface() {
        LLMService service = LLMServiceFactory.createPerplexity();
        assertTrue(service instanceof WebSearch, "Service should implement WebSearch");

        WebSearch searchService = (WebSearch) service;

        // Test model type checking
        assertTrue(searchService.supportsWebSearch("sonar"),
                "sonar should support web search");
        assertTrue(searchService.supportsCitations("sonar-pro"),
                "sonar-pro should support citations");
        assertFalse(searchService.supportsWebSearch("r1-1776"),
                "r1-1776 should NOT support web search");
    }

    /**
     * Test: Basic completion request (requires API key).
     */
    @Test
    public void testBasicCompletion() {
        assumeTrue(apiKey != null && !apiKey.isEmpty(),
                "Skipping test: PERPLEXITY_API_KEY not set");

        LLMService service = LLMServiceFactory.createPerplexity();

        MapParam params = new MapParam()
                .model("sonar")
                .maxTokens(100);

        try {
            CompletionResponse response = service.completion(
                    null,
                    "What is 2+2?",
                    params
            );

            assertNotNull(response, "Response should not be null");
            assertNotNull(response.getResponse(), "Response content should not be null");
            assertNotNull(response.getResponse().getText(), "Response text should not be null");
            assertTrue(response.getResponse().getText().contains("4"),
                    "Response should contain the answer 4");

        } catch (Exception e) {
            fail("Basic completion failed: " + e.getMessage());
        }
    }

    /**
     * Test: Web search with citations (requires API key).
     */
    @Test
    public void testWebSearchWithCitations() {
        assumeTrue(apiKey != null && !apiKey.isEmpty(),
                "Skipping test: PERPLEXITY_API_KEY not set");

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        MapParam params = new MapParam()
                .model("sonar")
                .maxTokens(200);

        try {
            SearchResponse response = searchService.webSearch(
                    "What is the current population of Tokyo?",
                    params
            );

            assertNotNull(response, "Response should not be null");
            assertNotNull(response.getResponse(), "Response content should not be null");

            // Verify citations are present
            assertTrue(response.hasCitations(), "Response should have citations");
            assertTrue(response.getCitations().size() > 0, "Should have at least one citation");

            // Verify citation format (should be URLs)
            String firstCitation = response.getCitations().get(0);
            assertTrue(firstCitation.startsWith("http"), "Citation should be a URL");

        } catch (Exception e) {
            fail("Web search failed: " + e.getMessage());
        }
    }

    /**
     * Test: Web search with domain filtering (requires API key).
     */
    @Test
    public void testWebSearchWithDomainFilter() {
        assumeTrue(apiKey != null && !apiKey.isEmpty(),
                "Skipping test: PERPLEXITY_API_KEY not set");

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        MapParam params = new MapParam()
                .model("sonar-pro")
                .searchDomainFilter(new String[]{"wikipedia.org"})
                .maxTokens(150);

        try {
            SearchResponse response = searchService.webSearch(
                    "Who invented the telephone?",
                    params
            );

            assertNotNull(response, "Response should not be null");
            assertTrue(response.hasCitations(), "Response should have citations");

            // Verify at least one citation is from Wikipedia
            boolean hasWikipedia = response.getCitations().stream()
                    .anyMatch(citation -> citation.contains("wikipedia.org"));

            assertTrue(hasWikipedia, "At least one citation should be from Wikipedia");

        } catch (Exception e) {
            fail("Domain filtered search failed: " + e.getMessage());
        }
    }

    /**
     * Test: Conversational search with chat context (requires API key).
     */
    @Test
    public void testConversationalSearch() {
        assumeTrue(apiKey != null && !apiKey.isEmpty(),
                "Skipping test: PERPLEXITY_API_KEY not set");

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        Chat chat = new Chat();
        chat.setModel("sonar");

        MapParam params = new MapParam().maxTokens(500);

        try {
            // First query
            SearchResponse response1 = searchService.webSearchChat(
                    chat,
                    "Qual é a Capital do Brasil?",
                    params
            );

            assertNotNull(response1, "First response should not be null");
            assertTrue(response1.getResponse().getText().toLowerCase().contains("brasília"),
                    "First response should mention Brasília");

            // Follow-up query (should use context)
            SearchResponse response2 = searchService.webSearchChat(
                    chat,
                    "Qual é a sua população?",
                    params
            );

            assertNotNull(response2, "Second response should not be null");
            assertNotNull(response2.getResponse().getText(), "Second response should have content");

            // Verify chat history was maintained
            assertEquals(4, chat.getMessages().size(),
                    "Chat should have 4 messages (2 user + 2 assistant)");

        } catch (Exception e) {
            fail("Conversational search failed: " + e.getMessage());
        }
    }

    /**
     * Test: Web search with related questions (requires API key).
     */
    @Test
    public void testSearchWithRelatedQuestions() {
        assumeTrue(apiKey != null && !apiKey.isEmpty(),
                "Skipping test: PERPLEXITY_API_KEY not set");

        LLMService service = LLMServiceFactory.createPerplexity();
        WebSearch searchService = (WebSearch) service;

        MapParam params = new MapParam()
                .model("sonar-pro")
                .returnRelatedQuestions(true)
                .maxTokens(200);

        try {
            SearchResponse response = searchService.webSearch(
                    "What is artificial intelligence?",
                    params
            );

            assertNotNull(response, "Response should not be null");

            // Note: Related questions may not always be returned, so we check if present
            if (response.hasRelatedQuestions()) {
                assertTrue(response.getRelatedQuestions().size() > 0,
                        "Should have at least one related question");
            }

        } catch (Exception e) {
            fail("Search with related questions failed: " + e.getMessage());
        }
    }

    /**
     * Test: Token counting estimation.
     */
    @Test
    public void testTokenCount() {
        try {
            LLMService service = LLMServiceFactory.createPerplexity();

            String text = "The quick brown fox jumps over the lazy dog";
            int tokenCount = service.tokenCount(text, null);

            assertTrue(tokenCount > 0, "Token count should be greater than 0");
            assertTrue(tokenCount <= text.length(), "Token count should not exceed character count");

            // Test empty string
            assertEquals(0, service.tokenCount("", null), "Empty string should have 0 tokens");

        } catch (Exception e) {
            fail("Token count failed: " + e.getMessage());
        }
    }

    /**
     * Test: Unsupported operations should throw exceptions.
     */
    @Test
    public void testUnsupportedOperations() {
        LLMService service = LLMServiceFactory.createPerplexity();
        MapParam params = new MapParam();

        // Embeddings not supported
        assertThrows(UnsupportedOperationException.class,
                () -> service.embeddings(Embeddings_Op.DEFAULT, "test", params),
                "Embeddings should not be supported");

        // Image generation not supported
        assertThrows(UnsupportedOperationException.class,
                () -> service.generateImage("test prompt", params),
                "Image generation should not be supported");

        // Image editing not supported
        assertThrows(UnsupportedOperationException.class,
                () -> service.editImage(new byte[0],"image.png",new byte[0], params),
                "Image editing should not be supported");

        // Image variations not supported
        assertThrows(UnsupportedOperationException.class,
                () -> service.createImageVariation(new byte[0], params),
                "Image variations should not be supported");
    }
}
