package bor.tools.simplellm;

/**
 * Enumeration defining the various types of LLM models supported.
 * <p>
 * Each model type represents a specific capability or use case:
 * </p>
 * <ul>
 * <li>{@code EMBEDDING} - Models for generating text embeddings</li>
 * <li>{@code EMBEDDING_DIMENSION} - Models that provide dimensioning
 * (Matrioshka)
 * for embeddings</li>
 * <li>{@code LANGUAGE} - General language understanding and generation  models</li>
 * <li>{@code FAST} - Models optimized for speed and quick responses</li>
 * <li>{@code REASONING} - Models specialized in logical reasoning and problem-solving, using {@link Reasoning_Effort} and  {@link MapParam#reasoningEffort(Reasoning_Effort)} </li>
 * <li>{@code REASONING_PROMPT} - Models that enhance reasoning through prompt
 * <li>{@code CODING} - Models designed for code generation and programming
 * tasks</li>
 * <li>{@code TEXT} - Text processing and manipulation models</li>
 * <li>{@code VISION} - Vision and image understanding models</li>
 * <li>{@code IMAGE} - Image generation and manipulation models</li>
 * <li>{@code AUDIO} - Audio processing and generation models</li>
 * <li>{@code WEBSEARCH} - Models with real-time web search capabilities</li>
 * <li>{@code CITATIONS} - Models that provide source citations</li>
 * <li>{@code DEEP_RESEARCH} - Models optimized for exhaustive research</li>
 * </ul>
 * 
 * @see bor.tools.simplellm.MapParam#reasoningEffort(Reasoning_Effort)
 * @see bor.tools.simplellm.MapParam#getReasoningEffort()
 * @see bor.tools.simplellm.Reasoning_Effort
 */
public enum Model_Type {
	/**
	 * EMBEDDING: Models for generating text embeddings
	 */
		EMBEDDING,
		/**
		 * EMBEDDING_DIMENSION: Models that provide dimensioning (Matrioshka) for
		 * embeddings
		 */
		EMBEDDING_DIMENSION,
		/**
		 * LANGUAGE: General language understanding and generation models
		 */
		LANGUAGE,
		/**
		 * FAST: Models optimized for speed and quick responses
		 */
		FAST,
		/**
		 * REASONING: Models specialized in logical reasoning and problem-solving
		 */
		REASONING,
		/**
		 * REASONING_PROMPT: Models that enhance reasoning through prompt engineering
		 * 
		 */
		REASONING_PROMPT,
		
		/**
		 * CODING: Models designed for code generation and programming tasks
		 */
		CODING,
		/**
		 * TEXT: Text processing and manipulation models
		 */
		TEXT,
		/**
		 * VISION: Vision and image understanding models
		 */
		VISION,
		/**
		 * IMAGE: Image generation and manipulation models
		 */
		IMAGE,
		/**
		 * AUDIO: Audio processing and generation models
		 */
		AUDIO,
		/**
		 * RESPONSES_API: Chat-optimized models for conversational AI
		 */	
		RESPONSES_API,
		/**
		 * BATCH: Model for batch operations
		 */
		BATCH,
		/**
		 * TOOLS: Models that can utilize external tools or plugins
		 */
		TOOLS,
		/**
		 * GPT5_CLASS: GPT-5 and o1/o3/04 class models with advanced capabilities
		 */
		GPT5_CLASS,
		/**
		 * WEBSEARCH: Models with real-time web search capabilities.
		 * <p>
		 * Models with this type can access and search the internet in real-time
		 * to provide up-to-date information. They typically return citations
		 * and source URLs along with their responses.
		 * </p>
		 * <p>
		 * Example providers: Perplexity AI (sonar models)
		 * </p>
		 *
		 * @see bor.tools.simplellm.WebSearch
		 * @see bor.tools.simplellm.SearchResponse
		 */
		WEBSEARCH,
		/**
		 * CITATIONS: Models that provide source citations with their responses.
		 * <p>
		 * Models with this type include references to their source materials,
		 * allowing users to verify information and explore topics in depth.
		 * Citations are typically URLs to web pages, papers, or documents.
		 * </p>
		 *
		 * @see bor.tools.simplellm.SearchResponse#getCitations()
		 */
		CITATIONS,
		/**
		 * DEEP_RESEARCH: Models optimized for exhaustive research tasks.
		 * <p>
		 * These models perform extensive multi-step research, often conducting
		 * dozens or hundreds of searches to provide comprehensive analysis.
		 * They are designed for in-depth exploration of complex topics.
		 * </p>
		 * <p>
		 * Example: Perplexity's sonar-deep-research model
		 * </p>
		 *
		 * @see bor.tools.simplellm.SearchResponse#getSearchQueriesCount()
		 */
		DEEP_RESEARCH
}