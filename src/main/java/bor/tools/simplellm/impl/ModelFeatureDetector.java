package bor.tools.simplellm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import bor.tools.simplellm.Model;
import bor.tools.simplellm.Model_Type;

/**
 * Utility class for detecting model capabilities and features based on model names and metadata.
 * <p>
 * This class provides sophisticated pattern matching and feature detection for various
 * LLM model types, supporting multiple detection strategies including:
 * </p>
 * <ul>
 * <li>Name pattern matching (keywords, prefixes, suffixes)</li>
 * <li>Regular expression matching</li>
 * <li>Provider-specific detection rules</li>
 * <li>Model family classification</li>
 * </ul>
 * 
 * @author AlessandroBorges
 * @since 1.0
 */
public class ModelFeatureDetector {

    // Pattern-based detection rules
    private static final Map<Model_Type, ModelDetectionRule> DETECTION_RULES = new HashMap<>();
    
    // Known model families and their capabilities
    private static final Map<String, Set<Model_Type>> MODEL_FAMILIES = new HashMap<>();
    
    static {
        initializeDetectionRules();
        initializeModelFamilies();
    }

    /**
     * Detection rule for a specific model type
     */
    private static class ModelDetectionRule {
        private final List<String> keywords;
        private final List<String> exactMatches;
        private final List<Pattern> patterns;
        private final List<String> prefixes;
        private final List<String> suffixes;

        public ModelDetectionRule(Builder builder) {
            this.keywords = new ArrayList<>(builder.keywords);
            this.exactMatches = new ArrayList<>(builder.exactMatches);
            this.patterns = new ArrayList<>(builder.patterns);
            this.prefixes = new ArrayList<>(builder.prefixes);
            this.suffixes = new ArrayList<>(builder.suffixes);
        }

        public boolean matches(String modelName) {
            String lowerName = modelName.toLowerCase();
            
            // Exact matches have highest priority
            for (String exact : exactMatches) {
                if (lowerName.equals(exact.toLowerCase())) {
                    return true;
                }
            }
            
            // Prefix matches
            for (String prefix : prefixes) {
                if (lowerName.startsWith(prefix.toLowerCase())) {
                    return true;
                }
            }
            
            // Suffix matches
            for (String suffix : suffixes) {
                if (lowerName.endsWith(suffix.toLowerCase())) {
                    return true;
                }
            }
            
            // Keyword matches
            for (String keyword : keywords) {
                if (lowerName.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
            
            // Pattern matches
            for (Pattern pattern : patterns) {
                if (pattern.matcher(lowerName).find()) {
                    return true;
                }
            }
            
            return false;
        }

        public static class Builder {
            private final List<String> keywords = new ArrayList<>();
            private final List<String> exactMatches = new ArrayList<>();
            private final List<Pattern> patterns = new ArrayList<>();
            private final List<String> prefixes = new ArrayList<>();
            private final List<String> suffixes = new ArrayList<>();

            public Builder keywords(String... keywords) {
                this.keywords.addAll(Arrays.asList(keywords));
                return this;
            }

            public Builder exactMatches(String... matches) {
                this.exactMatches.addAll(Arrays.asList(matches));
                return this;
            }

            public Builder patterns(String... regexPatterns) {
                for (String pattern : regexPatterns) {
                    this.patterns.add(Pattern.compile(pattern));
                }
                return this;
            }

            public Builder prefixes(String... prefixes) {
                this.prefixes.addAll(Arrays.asList(prefixes));
                return this;
            }

            public Builder suffixes(String... suffixes) {
                this.suffixes.addAll(Arrays.asList(suffixes));
                return this;
            }

            public ModelDetectionRule build() {
                return new ModelDetectionRule(this);
            }
        }
    }

    /**
     * Initialize detection rules for each model type
     */
    private static void initializeDetectionRules() {
        // EMBEDDING models
        DETECTION_RULES.put(Model_Type.EMBEDDING, 
            new ModelDetectionRule.Builder()
                .keywords("embed", "embedding", "bge", "nomic", "e5", "gte", "sentence")
                .prefixes("text-embedding-", "embedding-")
                .patterns(".*-embed(ding)?-.*", ".*embed.*")
                .build());
        
        DETECTION_RULES.put(Model_Type.EMBEDDING_DIMENSION, 
                            new ModelDetectionRule.Builder()
                                .keywords("snowflake-artic-embed","qwen3-embedding", "nomicémbed-text-v1.5", "embedding-3","embeddinggemma" )   
                                .patterns("(?=.*nomic)(?=.*v1\\.5).*", "(?=.*snowflake)(?=.*v2\\.0).*")
                                .build());

        // VISION models
        DETECTION_RULES.put(Model_Type.VISION, 
            new ModelDetectionRule.Builder()
                .keywords("vision", "llava", "bakllava", "moondream", "cogvlm", "qwen-vl", 
                         "internvl", "minicpm-v", "pixtral")
                .prefixes("gpt-4-vision", "gpt-4o", "claude-3")
                .patterns(".*-vision.*", ".*vision.*", ".*llava.*", ".*vl-.*")
                .build());

        // CODING models  
        DETECTION_RULES.put(Model_Type.CODING, 
            new ModelDetectionRule.Builder()
                .keywords("code", "codellama", "deepseek", "starcoder", "wizardcoder", 
                         "codeqwen", "magicoder", "phind", "sqlcoder")
                .prefixes("code-", "deepseek-coder", "starcoder")
                .patterns(".*code.*", ".*coder.*", ".*coding.*")
                .build());

        // REASONING models
        DETECTION_RULES.put(Model_Type.REASONING, 
            new ModelDetectionRule.Builder()
                .keywords("reasoning", "reason", "think", "o1", "o3", "qwen", "gemma", 
                         "llama", "mistral", "phi","qwen3")
                .exactMatches("gpt-4o-reasoning", "o1-preview", "o1-mini", "o3-mini")
                .prefixes("o1-", "o3-", "gpt-4o-r", "qwen3", "gemini-1.5-", "llama-3-")
                .patterns(".*o[13].*", ".*reason.*", ".*thinking.*")
                .build());
        
     // REASONING models
        DETECTION_RULES.put(Model_Type.INSTRUCT, 
            new ModelDetectionRule.Builder()
                .keywords("instruct", "inst", "llama", "mistral", "phi","qwen3","obedient")
                .exactMatches("gpt-4o-reasoning", "o1-preview", "o1-mini", "o3-mini","gpt-oss-20b")
                .prefixes("o1-", "o3-", "gpt-4", "qwen3", "gemini-1.5-", "llama-3-")
                .patterns(".*o[13].*", ".*reason.*", ".*thinking.*")
                .build());

        // FAST models
        DETECTION_RULES.put(Model_Type.FAST, 
            new ModelDetectionRule.Builder()
                .keywords("fast", "mini", "nano", "lite", "small", "quick", "turbo", "speed","lfm2",
                                     "0.6b","1b", "1.7b", "2b", "3b", "3.8b", "4b","")
                .suffixes("-mini", "-nano", "-lite", "-small", "-fast","-turbo","tiny")
                .prefixes("gpt-3.5-turbo", "claude-3-haiku")
                .build());

        // IMAGE generation models
        DETECTION_RULES.put(Model_Type.IMAGE, 
            new ModelDetectionRule.Builder()
                .keywords("dalle", "dall-e", "midjourney", "imagen", "ideogram", 
                         "playground", "flux", "stable-diffusion")
                .prefixes("dall-e-", "dalle-", "sd-", "flux-")
                .patterns(".*image.*gen.*", ".*diffusion.*", ".*flux.*")
                .build());

        // AUDIO models
        DETECTION_RULES.put(Model_Type.AUDIO, 
            new ModelDetectionRule.Builder()
                .keywords("whisper", "audio", "speech", "tts", "stt", "voice", "bark", "tortoise")
                .prefixes("whisper-", "tts-", "speech-")
                .patterns(".*audio.*", ".*speech.*", ".*voice.*")
                .build());

        // WEBSEARCH models
        DETECTION_RULES.put(Model_Type.WEBSEARCH, 
            new ModelDetectionRule.Builder()
                .keywords("search", "web", "perplexity", "bing", "internet", "online")
                .prefixes("perplexity-", "web-search-")
                .patterns(".*search.*", ".*web.*")
                .build());

        // GPT5_CLASS models (next-gen)
        DETECTION_RULES.put(Model_Type.GPT5_CLASS, 
            new ModelDetectionRule.Builder()
                .keywords("gpt-5", "o1", "o3", "claude-4", "gemini-2")
                .prefixes("gpt-5", "o1-", "o3-", "claude-4", "gemini-2")
                .patterns(".*gpt-?5.*", ".*o[13].*", ".*claude-?4.*")
                .build());

        // TOOLS capability
        DETECTION_RULES.put(Model_Type.TOOLS, 
            new ModelDetectionRule.Builder()
                .keywords("tools", "function", "plugin", "agent", "assistant")
                .patterns(".*tool.*", ".*function.*", ".*agent.*")
                .build());
    }

    /**
     * Initialize known model families and their default capabilities
     */
    private static void initializeModelFamilies() {
        // GPT family
        MODEL_FAMILIES.put("gpt", Set.of(Model_Type.LANGUAGE, Model_Type.TOOLS, Model_Type.RESPONSES_API));
        MODEL_FAMILIES.put("claude", Set.of(Model_Type.LANGUAGE, Model_Type.REASONING, Model_Type.TOOLS));
        MODEL_FAMILIES.put("gemini", Set.of(Model_Type.LANGUAGE, Model_Type.REASONING, Model_Type.VISION));
        MODEL_FAMILIES.put("llama", Set.of(Model_Type.LANGUAGE, Model_Type.REASONING));
        MODEL_FAMILIES.put("mistral", Set.of(Model_Type.LANGUAGE, Model_Type.REASONING, Model_Type.CODING));
        MODEL_FAMILIES.put("qwen", Set.of(Model_Type.LANGUAGE, Model_Type.REASONING, Model_Type.CODING));
        MODEL_FAMILIES.put("deepseek", Set.of(Model_Type.LANGUAGE, Model_Type.CODING, Model_Type.REASONING));
    }

    /**
     * Detect all capabilities of a model based on its name and metadata
     * 
     * @param modelName the name of the model
     * @param metadata optional metadata about the model (can be null)
     * @return list of detected Model_Type capabilities
     */
    public static List<Model_Type> detectCapabilities(String modelName, Map<String, Object> metadata) {
        List<Model_Type> capabilities = new ArrayList<>();
        
        if (modelName == null || modelName.trim().isEmpty()) {
            return capabilities;
        }

        String lowerName = modelName.toLowerCase();
        
        // Check against detection rules
        for (Map.Entry<Model_Type, ModelDetectionRule> entry : DETECTION_RULES.entrySet()) {
            if (entry.getValue().matches(modelName)) {
                capabilities.add(entry.getKey());
            }
        }

        // Check model families for additional capabilities
        for (Map.Entry<String, Set<Model_Type>> family : MODEL_FAMILIES.entrySet()) {
            if (lowerName.contains(family.getKey())) {
                for (Model_Type type : family.getValue()) {
                    if (!capabilities.contains(type)) {
                        capabilities.add(type);
                    }
                }
            }
        }

        // Default to LANGUAGE if no specific type detected
        if (capabilities.isEmpty() || (!capabilities.contains(Model_Type.EMBEDDING) && 
                                      !capabilities.contains(Model_Type.IMAGE) && 
                                      !capabilities.contains(Model_Type.AUDIO))) {
            if (!capabilities.contains(Model_Type.LANGUAGE)) {
                capabilities.add(Model_Type.LANGUAGE);
            }
        }

        // Use metadata for additional detection if available
        if (metadata != null) {
            detectFromMetadata(capabilities, metadata);
        }

        return capabilities;
    }

    /**
     * Detect capabilities from model metadata
     */
    private static void detectFromMetadata(List<Model_Type> capabilities, Map<String, Object> metadata) {
        // Check for context length to determine if it's suitable for batch processing
        Object contextLengthObj = metadata.get("context_length");
        if (contextLengthObj != null) {
            try {
                int contextLength = Integer.parseInt(contextLengthObj.toString());
                if (contextLength > 100000) { // Large context models
                    if (!capabilities.contains(Model_Type.BATCH)) {
                        capabilities.add(Model_Type.BATCH);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid context length
            }
        }

        // Check for specific capabilities mentioned in metadata
        String description = (String) metadata.get("description");
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("vision") && !capabilities.contains(Model_Type.VISION)) {
                capabilities.add(Model_Type.VISION);
            }
            if (lowerDesc.contains("code") && !capabilities.contains(Model_Type.CODING)) {
                capabilities.add(Model_Type.CODING);
            }
        }
    }

    /**
     * Enhanced version of isModelType with improved detection
     * 
     * @param model the Model object to check
     * @param type the Model_Type to check for  
     * @return true if the model supports the specified type, false otherwise
     */
    public static boolean isModelType(Model model, Model_Type type) {
        if (model == null || type == null) {
            return false;
        }

        // Check if already defined in the model
        if (model.isType(type)) {
            return true;
        }

        // Use enhanced detection
        List<Model_Type> detectedTypes = detectCapabilities(model.getName(), null);
        return detectedTypes.contains(type);
    }

    /**
     * Get all detected capabilities for a model
     * 
     * @param model the model to analyze
     * @return array of detected Model_Type capabilities
     */
    public static Model_Type[] getAllCapabilities(Model model) {
        if (model == null) {
            return new Model_Type[0];
        }

        List<Model_Type> capabilities = detectCapabilities(model.getName(), null);
        return capabilities.toArray(new Model_Type[0]);
    }
}