package bor.tools.simplellm.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bor.tools.simplellm.LLMConfig;
import bor.tools.simplellm.LLMProvider;
import bor.tools.simplellm.LLMServiceFactory;
import bor.tools.simplellm.SERVICE_PROVIDER;
import bor.tools.simplellm.websearch.WebSearch;
import bor.tools.simplellm.websearch.WebSearchFactory;
import bor.tools.simplellm.websearch.WebSearchFactory.WEBSEARCH_PROVIDER;
import bor.tools.simplellm.websearch.impl.PerplexityLLMService;

/**
 * Unit tests for WebSearchFactory.
 * <p>
 * These tests verify:
 * <ul>
 * <li>Compatibility between LLMServiceFactory and WebSearchFactory</li>
 * <li>Factory methods return correct implementations</li>
 * <li>Future implementations throw UnsupportedOperationException</li>
 * <li>Error handling for invalid inputs</li>
 * </ul>
 * </p>
 *
 * @author AlessandroBorges
 */
@DisplayName("WebSearchFactory Tests")
class WebSearchFactoryTest {

    // ========================================================================
    // Compatibility Tests - LLMServiceFactory vs WebSearchFactory
    // ========================================================================

    @Test
    @DisplayName("Both factories return same Perplexity implementation")
    void testPerplexityCompatibility() {
        // Create via LLMServiceFactory
        LLMProvider llmService = LLMServiceFactory.createPerplexity();

        // Create via WebSearchFactory
        WebSearch webSearch = WebSearchFactory.createPerplexity();

        // Both should be PerplexityLLMService instances
        assertInstanceOf(PerplexityLLMService.class, llmService,
            "LLMServiceFactory should return PerplexityLLMService");
        assertInstanceOf(PerplexityLLMService.class, webSearch,
            "WebSearchFactory should return PerplexityLLMService");

        // LLMProvider should be castable to WebSearch
        assertInstanceOf(WebSearch.class, llmService,
            "PerplexityLLMService from LLMServiceFactory should implement WebSearch");

        // WebSearch should be castable to LLMProvider
        assertInstanceOf(LLMProvider.class, webSearch,
            "PerplexityLLMService from WebSearchFactory should implement LLMProvider");
    }

    @Test
    @DisplayName("Both factories return same implementation class")
    void testSameImplementationClass() {
        LLMProvider llmService = LLMServiceFactory.createPerplexity();
        WebSearch webSearch = WebSearchFactory.createPerplexity();

        // Should be exact same class
        assertEquals(llmService.getClass(), webSearch.getClass(),
            "Both factories should return same implementation class");
    }

    @Test
    @DisplayName("Both factories support SERVICE_PROVIDER.PERPLEXITY")
    void testServiceProvider() {
        LLMProvider llmService = LLMServiceFactory.createPerplexity();
        WebSearch webSearch = WebSearchFactory.createPerplexity();

        // Both should report PERPLEXITY as provider
        assertEquals(SERVICE_PROVIDER.PERPLEXITY, llmService.getServiceProvider(),
            "LLMProvider should report PERPLEXITY provider");
        assertEquals(SERVICE_PROVIDER.PERPLEXITY, ((LLMProvider) webSearch).getServiceProvider(),
            "WebSearch should report PERPLEXITY provider");
    }

    // ========================================================================
    // WebSearchFactory Direct Creation Tests
    // ========================================================================

    @Test
    @DisplayName("createPerplexity() returns WebSearch instance")
    void testCreatePerplexity() {
        WebSearch search = WebSearchFactory.createPerplexity();

        assertNotNull(search, "Factory should return non-null WebSearch");
        assertInstanceOf(WebSearch.class, search, "Should implement WebSearch interface");
        assertInstanceOf(PerplexityLLMService.class, search, "Should be PerplexityLLMService");
    }

    @Test
    @DisplayName("createPerplexity(config) returns WebSearch instance")
    void testCreatePerplexityWithConfig() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        WebSearch search = WebSearchFactory.createPerplexity(config);

        assertNotNull(search, "Factory should return non-null WebSearch");
        assertInstanceOf(WebSearch.class, search, "Should implement WebSearch interface");
        assertInstanceOf(PerplexityLLMService.class, search, "Should be PerplexityLLMService");
    }

    @Test
    @DisplayName("createWebSearch(PERPLEXITY, null) returns Perplexity")
    void testCreateWebSearchPerplexityNull() {
        WebSearch search = WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.PERPLEXITY, null);

        assertNotNull(search, "Factory should return non-null WebSearch");
        assertInstanceOf(PerplexityLLMService.class, search, "Should be PerplexityLLMService");
    }

    @Test
    @DisplayName("createWebSearch(PERPLEXITY, config) returns Perplexity")
    void testCreateWebSearchPerplexityWithConfig() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        WebSearch search = WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.PERPLEXITY, config);

        assertNotNull(search, "Factory should return non-null WebSearch");
        assertInstanceOf(PerplexityLLMService.class, search, "Should be PerplexityLLMService");
    }

    // ========================================================================
    // Future Implementations Tests - Should Throw UnsupportedOperationException
    // ========================================================================

    @Test
    @DisplayName("createDeepSeek() throws UnsupportedOperationException")
    void testCreateDeepSeekNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createDeepSeek(),
            "DeepSeek should not be implemented yet");
    }

    @Test
    @DisplayName("createDeepSeek(config) throws UnsupportedOperationException")
    void testCreateDeepSeekWithConfigNotImplemented() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createDeepSeek(config),
            "DeepSeek should not be implemented yet");
    }

    @Test
    @DisplayName("createGemini() throws UnsupportedOperationException")
    void testCreateGeminiNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createGemini(),
            "Gemini should not be implemented yet");
    }

    @Test
    @DisplayName("createGemini(config) throws UnsupportedOperationException")
    void testCreateGeminiWithConfigNotImplemented() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createGemini(config),
            "Gemini should not be implemented yet");
    }

    @Test
    @DisplayName("createWikipedia() throws UnsupportedOperationException")
    void testCreateWikipediaNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWikipedia(),
            "Wikipedia should not be implemented yet");
    }

    @Test
    @DisplayName("createWikipedia(config) throws UnsupportedOperationException")
    void testCreateWikipediaWithConfigNotImplemented() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWikipedia(config),
            "Wikipedia should not be implemented yet");
    }

    @Test
    @DisplayName("createTavily() throws UnsupportedOperationException")
    void testCreateTavilyNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createTavily(),
            "Tavily should not be implemented yet");
    }

    @Test
    @DisplayName("createTavily(config) throws UnsupportedOperationException")
    void testCreateTavilyWithConfigNotImplemented() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createTavily(config),
            "Tavily should not be implemented yet");
    }

    @Test
    @DisplayName("createBrave() throws UnsupportedOperationException")
    void testCreateBraveNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createBrave(),
            "Brave should not be implemented yet");
    }

    @Test
    @DisplayName("createBrave(config) throws UnsupportedOperationException")
    void testCreateBraveWithConfigNotImplemented() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createBrave(config),
            "Brave should not be implemented yet");
    }

    @Test
    @DisplayName("createWebSearch(DEEPSEEK) throws UnsupportedOperationException")
    void testCreateWebSearchDeepSeekNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.DEEPSEEK, null),
            "DeepSeek via enum should not be implemented yet");
    }

    @Test
    @DisplayName("createWebSearch(GEMINI) throws UnsupportedOperationException")
    void testCreateWebSearchGeminiNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.GEMINI, null),
            "Gemini via enum should not be implemented yet");
    }

    @Test
    @DisplayName("createWebSearch(WIKIPEDIA) throws UnsupportedOperationException")
    void testCreateWebSearchWikipediaNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.WIKIPEDIA, null),
            "Wikipedia via enum should not be implemented yet");
    }

    @Test
    @DisplayName("createWebSearch(TAVILY) throws UnsupportedOperationException")
    void testCreateWebSearchTavilyNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.TAVILY, null),
            "Tavily via enum should not be implemented yet");
    }

    @Test
    @DisplayName("createWebSearch(BRAVE) throws UnsupportedOperationException")
    void testCreateWebSearchBraveNotImplemented() {
        assertThrows(UnsupportedOperationException.class,
            () -> WebSearchFactory.createWebSearch(WEBSEARCH_PROVIDER.BRAVE, null),
            "Brave via enum should not be implemented yet");
    }

    // ========================================================================
    // Error Handling Tests
    // ========================================================================

    @Test
    @DisplayName("createWebSearch(null) throws IllegalArgumentException")
    void testCreateWebSearchNullProvider() {
        assertThrows(IllegalArgumentException.class,
            () -> WebSearchFactory.createWebSearch(null, null),
            "Null provider should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("createWebSearch(null, config) throws IllegalArgumentException")
    void testCreateWebSearchNullProviderWithConfig() {
        LLMConfig config = PerplexityLLMService.getDefaultLLMConfig();
        assertThrows(IllegalArgumentException.class,
            () -> WebSearchFactory.createWebSearch(null, config),
            "Null provider should throw IllegalArgumentException even with config");
    }

    // ========================================================================
    // WEBSEARCH_PROVIDER Enum Tests
    // ========================================================================

    @Test
    @DisplayName("WEBSEARCH_PROVIDER enum has all expected providers")
    void testWebSearchProviderEnum() {
        WEBSEARCH_PROVIDER[] providers = WEBSEARCH_PROVIDER.values();

        assertEquals(6, providers.length, "Should have 6 providers defined");

        // Verify all expected providers exist
        assertNotNull(WEBSEARCH_PROVIDER.valueOf("PERPLEXITY"), "PERPLEXITY should exist");
        assertNotNull(WEBSEARCH_PROVIDER.valueOf("DEEPSEEK"), "DEEPSEEK should exist");
        assertNotNull(WEBSEARCH_PROVIDER.valueOf("GEMINI"), "GEMINI should exist");
        assertNotNull(WEBSEARCH_PROVIDER.valueOf("WIKIPEDIA"), "WIKIPEDIA should exist");
        assertNotNull(WEBSEARCH_PROVIDER.valueOf("TAVILY"), "TAVILY should exist");
        assertNotNull(WEBSEARCH_PROVIDER.valueOf("BRAVE"), "BRAVE should exist");
    }

    // ========================================================================
    // Interface Implementation Tests
    // ========================================================================

    @Test
    @DisplayName("WebSearch returned implements all required methods")
    void testWebSearchInterfaceMethods() {
        WebSearch search = WebSearchFactory.createPerplexity();

        // Verify WebSearch interface methods exist
        assertDoesNotThrow(() -> {
            search.getClass().getMethod("webSearch", String.class, bor.tools.simplellm.MapParam.class);
            search.getClass().getMethod("webSearchChat",
                bor.tools.simplellm.chat.Chat.class,
                String.class,
                bor.tools.simplellm.MapParam.class);
            search.getClass().getMethod("webSearchStream",
                bor.tools.simplellm.ResponseStream.class,
                String.class,
                bor.tools.simplellm.MapParam.class);
            search.getClass().getMethod("webSearchChatStream",
                bor.tools.simplellm.ResponseStream.class,
                bor.tools.simplellm.chat.Chat.class,
                String.class,
                bor.tools.simplellm.MapParam.class);
        }, "WebSearch should implement all required methods");
    }

    @Test
    @DisplayName("WebSearch supports model type checking")
    void testModelTypeChecking() {
        WebSearch search = WebSearchFactory.createPerplexity();

        // Verify helper methods exist
        assertTrue(search.supportsWebSearch("sonar"),
            "sonar should support web search");
        assertTrue(search.supportsCitations("sonar-pro"),
            "sonar-pro should support citations");
    }

    // ========================================================================
    // Documentation Tests
    // ========================================================================

    @Test
    @DisplayName("Factory class has proper package")
    void testFactoryPackage() {
        assertEquals("bor.tools.simplellm.websearch",
            WebSearchFactory.class.getPackageName(),
            "Factory should be in websearch package");
    }

    @Test
    @DisplayName("Enum is nested in Factory")
    void testEnumIsNested() {
        Class<?>[] nestedClasses = WebSearchFactory.class.getDeclaredClasses();

        boolean hasEnum = false;
        for (Class<?> nested : nestedClasses) {
            if (nested.getSimpleName().equals("WEBSEARCH_PROVIDER")) {
                hasEnum = true;
                break;
            }
        }

        assertTrue(hasEnum, "WEBSEARCH_PROVIDER enum should be nested in WebSearchFactory");
    }
}
