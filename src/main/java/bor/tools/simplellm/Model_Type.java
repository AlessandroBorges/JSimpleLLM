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
 * <li>{@code LANGUAGE} - General language understanding and generation
 * models</li>
 * <li>{@code FAST} - Models optimized for speed and quick responses</li>
 * <li>{@code REASONING} - Models specialized in logical reasoning and
 * problem-solving</li>
 * <li>{@code CODING} - Models designed for code generation and programming
 * tasks</li>
 * <li>{@code TEXT} - Text processing and manipulation models</li>
 * <li>{@code VISION} - Vision and image understanding models</li>
 * <li>{@code IMAGE} - Image generation and manipulation models</li>
 * <li>{@code AUDIO} - Audio processing and generation models</li>
 * </ul>
 */
public enum Model_Type {
		EMBEDDING,
		EMBEDDING_DIMENSION,
		LANGUAGE,
		FAST,
		REASONING,
		CODING,
		TEXT,
		VISION,
		IMAGE,
		AUDIO,
		RESPONSES_API,
		BATCH,
		TOOLS,
		GPT5_CLASS
}