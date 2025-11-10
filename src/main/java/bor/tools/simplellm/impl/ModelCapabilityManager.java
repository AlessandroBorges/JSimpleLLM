package bor.tools.simplellm.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import bor.tools.simplellm.Model;
import bor.tools.simplellm.Model_Type;

/**
 * Advanced utility class for managing model capabilities and feature detection.
 * <p>
 * This class provides comprehensive model capability management including:
 * </p>
 * <ul>
 * <li>Bulk model capability analysis</li>
 * <li>Model compatibility checking</li>
 * <li>Provider-specific capability mapping</li>
 * <li>Model recommendation based on required capabilities</li>
 * <li>Capability conflict detection and resolution</li>
 * </ul>
 * 
 * @author AlessandroBorges
 * @since 1.0
 */
public class ModelCapabilityManager {

    /**
     * Capability compatibility matrix - defines which capabilities work well together
     */
    private static final Map<Model_Type, Set<Model_Type>> COMPATIBLE_CAPABILITIES = new HashMap<>();
    
    /**
     * Capability conflicts - capabilities that typically don't coexist
     */
    private static final Map<Model_Type, Set<Model_Type>> CONFLICTING_CAPABILITIES = new HashMap<>();

    /**
     * Provider-specific capability preferences
     */
    private static final Map<String, Set<Model_Type>> PROVIDER_STRENGTHS = new HashMap<>();

    static {
        initializeCompatibilityMatrix();
        initializeConflictMatrix();
        initializeProviderStrengths();
    }

    /**
     * Initialize capability compatibility matrix
     */
    private static void initializeCompatibilityMatrix() {
        // LANGUAGE models work well with most capabilities
        COMPATIBLE_CAPABILITIES.put(Model_Type.LANGUAGE, EnumSet.of(
            Model_Type.REASONING,
            Model_Type.CODING, 
            Model_Type.TOOLS, 
            Model_Type.RESPONSES_API, 
            Model_Type.BATCH, 
            Model_Type.TEXT
        ));

        // VISION models often support reasoning and tools
        COMPATIBLE_CAPABILITIES.put(Model_Type.VISION, EnumSet.of(
            Model_Type.LANGUAGE, 
            Model_Type.REASONING, 
            Model_Type.TOOLS,
            Model_Type.RESPONSES_API
        ));

        // CODING models typically support reasoning and tools
        COMPATIBLE_CAPABILITIES.put(Model_Type.CODING, EnumSet.of(
            Model_Type.LANGUAGE,
            Model_Type.REASONING,
            Model_Type.TOOLS,
            Model_Type.TEXT, 
            Model_Type.FAST
        ));

        // REASONING models often support language and tools
        COMPATIBLE_CAPABILITIES.put(Model_Type.REASONING, EnumSet.of(
            Model_Type.LANGUAGE, 
            Model_Type.CODING, 
            Model_Type.VISION,
            Model_Type.TOOLS, 
            Model_Type.REASONING_PROMPT
        ));
    }

    /**
     * Initialize capability conflict matrix
     */
    private static void initializeConflictMatrix() {
        // EMBEDDING models typically don't support chat capabilities
        CONFLICTING_CAPABILITIES.put(Model_Type.EMBEDDING, EnumSet.of(
            Model_Type.LANGUAGE,
            Model_Type.RESPONSES_API,
            Model_Type.VISION,
            Model_Type.AUDIO//, Model_Type.IMAGE
        ));

        // FAST models may conflict with reasoning (speed vs depth)
        CONFLICTING_CAPABILITIES.put(Model_Type.FAST, EnumSet.of(
          //  Model_Type.REASONING, // qwen3 and phi4 can be FAST and REASONING
          //  Model_Type.REASONING_PROMPT,
            Model_Type.DEEP_RESEARCH
        ));

        // IMAGE generation conflicts with text processing
        CONFLICTING_CAPABILITIES.put(Model_Type.IMAGE, EnumSet.of(
            Model_Type.EMBEDDING, 
            Model_Type.CODING
        ));

        // AUDIO processing typically standalone
        CONFLICTING_CAPABILITIES.put(Model_Type.AUDIO, EnumSet.of(
            Model_Type.EMBEDDING, 
            Model_Type.IMAGE,
            Model_Type.VISION
        ));
    }

    /**
     * Initialize provider-specific strengths
     */
    private static void initializeProviderStrengths() {
        PROVIDER_STRENGTHS.put("openai", EnumSet.of(
            Model_Type.LANGUAGE, 
            Model_Type.REASONING, 
            Model_Type.VISION,
            Model_Type.IMAGE, 
            Model_Type.AUDIO, 
            Model_Type.TOOLS
        ));

        PROVIDER_STRENGTHS.put("anthropic", EnumSet.of(
            Model_Type.LANGUAGE, 
            Model_Type.REASONING, 
            Model_Type.CODING,
            Model_Type.TOOLS, 
            Model_Type.TEXT
        ));

        PROVIDER_STRENGTHS.put("google", EnumSet.of(
            Model_Type.LANGUAGE, 
            Model_Type.REASONING,
            Model_Type.VISION,
            Model_Type.TOOLS,
            Model_Type.WEBSEARCH
        ));

        PROVIDER_STRENGTHS.put("meta", EnumSet.of(
            Model_Type.LANGUAGE,
            Model_Type.REASONING,
            Model_Type.CODING
        ));

        PROVIDER_STRENGTHS.put("mistral", EnumSet.of(
            Model_Type.LANGUAGE, 
            Model_Type.REASONING,
            Model_Type.CODING,
            Model_Type.TOOLS
        ));

        PROVIDER_STRENGTHS.put("deepseek", EnumSet.of(
            Model_Type.CODING, 
            Model_Type.REASONING, 
            Model_Type.LANGUAGE
        ));
        
		PROVIDER_STRENGTHS.put("qwen", EnumSet.of(
          Model_Type.FAST, 
          Model_Type.REASONING, 
          Model_Type.LANGUAGE));
		
		
		PROVIDER_STRENGTHS.put("lfm", EnumSet.of(
               Model_Type.FAST, 
               Model_Type.REASONING,
               Model_Type.LANGUAGE));
		
		PROVIDER_STRENGTHS.put("phi", EnumSet.of(
              Model_Type.FAST,
              Model_Type.REASONING,
              Model_Type.LANGUAGE));
}

    /**
     * Enhanced model creation with automatic capability detection
     * 
     * @param modelName the name of the model
     * @param contextLength the context length (can be null)
     * @param metadata additional metadata for detection
     * @return Model with automatically detected capabilities
     */
    public static Model createModelWithCapabilities(String modelName, Integer contextLength, Map<String, Object> metadata) {
        List<Model_Type> capabilities = ModelFeatureDetector.detectCapabilities(modelName, metadata);
        
        // Validate and resolve capability conflicts
        capabilities = resolveCapabilityConflicts(capabilities);
        
        // Add compatible capabilities
        capabilities = enhanceWithCompatibleCapabilities(capabilities);
        
        return new Model(modelName, contextLength, capabilities.toArray(new Model_Type[0]));
    }

    /**
     * Resolve capability conflicts using predefined rules
     * 
     * @param capabilities the list of detected capabilities
     * @return resolved list of capabilities
     */
    private static List<Model_Type> resolveCapabilityConflicts(List<Model_Type> capabilities) {
        List<Model_Type> resolved = new ArrayList<>(capabilities);
        
        for (Model_Type capability : new ArrayList<>(capabilities)) {
            Set<Model_Type> conflicts = CONFLICTING_CAPABILITIES.get(capability);
            if (conflicts != null) {
                for (Model_Type conflict : conflicts) {
                    if (resolved.contains(conflict)) {
                        // Keep the more specific capability
                        if (isMoreSpecific(capability, conflict)) {
                            resolved.remove(conflict);
                        } else {
                            resolved.remove(capability);
                        }
                    }
                }
            }
        }
        
        return resolved;
    }

    /**
     * Enhance capabilities with compatible ones
     * 
     * @param capabilities the base capabilities
     * @return enhanced capabilities list
     */
    private static List<Model_Type> enhanceWithCompatibleCapabilities(List<Model_Type> capabilities) {
        List<Model_Type> enhanced = new ArrayList<>(capabilities);
        
        for (Model_Type capability : new ArrayList<>(capabilities)) {
            Set<Model_Type> compatible = COMPATIBLE_CAPABILITIES.get(capability);
            if (compatible != null) {
                for (Model_Type compat : compatible) {
                    if (!enhanced.contains(compat) && shouldAddCompatible(enhanced, compat)) {
                        enhanced.add(compat);
                    }
                }
            }
        }
        
        return enhanced;
    }

    /**
     * Determine if one capability is more specific than another
     */
    private static boolean isMoreSpecific(Model_Type a, Model_Type b) {
        // Specific capabilities are considered more important than general ones
        if (a == Model_Type.EMBEDDING) return true;
        if (a == Model_Type.IMAGE || a == Model_Type.AUDIO) return true;
        if (a == Model_Type.CODING && b == Model_Type.LANGUAGE) return true;
        if (a == Model_Type.VISION && b == Model_Type.LANGUAGE) return true;
        return false;
    }

    /**
     * Determine if a compatible capability should be added
     */
    private static boolean shouldAddCompatible(List<Model_Type> existing, Model_Type candidate) {
        // Don't add if it would create conflicts
        for (Model_Type existing_cap : existing) {
            Set<Model_Type> conflicts = CONFLICTING_CAPABILITIES.get(candidate);
            if (conflicts != null && conflicts.contains(existing_cap)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find models that support all required capabilities
     * 
     * @param models map of available models
     * @param requiredCapabilities capabilities that must be supported
     * @return list of compatible models
     */
    public static List<Model> findCompatibleModels(Map<String, Model> models, Model_Type... requiredCapabilities) {
        Set<Model_Type> required = EnumSet.copyOf(Arrays.asList(requiredCapabilities));
        
        return models.values().stream()
            .filter(model -> hasAllCapabilities(model, required))
            .collect(Collectors.toList());
    }

    /**
     * Check if a model has all required capabilities
     */
    private static boolean hasAllCapabilities(Model model, Set<Model_Type> required) {
        for (Model_Type capability : required) {
            if (!ModelFeatureDetector.isModelType(model, capability)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the best model for a specific use case
     * 
     * @param models available models
     * @param primaryCapability the main capability needed
     * @param secondaryCapabilities additional helpful capabilities
     * @return the best matching model or null if none found
     */
    public static Model getBestModelForCapability(Map<String, Model> models, 
                                                  Model_Type primaryCapability, 
                                                  Model_Type... secondaryCapabilities) {
        // First find models with the primary capability
        List<Model> candidates = findCompatibleModels(models, primaryCapability);
        if (candidates.isEmpty()) {
            return null;
        }

        // Score models based on secondary capabilities
        Model bestModel = null;
        int bestScore = -1;

        for (Model model : candidates) {
            int score = 0;
            for (Model_Type secondary : secondaryCapabilities) {
                if (ModelFeatureDetector.isModelType(model, secondary)) {
                    score++;
                }
            }
            
            // Prefer models with higher context length for complex tasks
            if (model.getContextLength() != null && model.getContextLength() > 32000) {
                score++;
            }

            if (score > bestScore) {
                bestScore = score;
                bestModel = model;
            }
        }

        return bestModel;
    }

    /**
     * Generate a capability report for a model
     * 
     * @param model the model to analyze
     * @return detailed capability report
     */
    public static CapabilityReport generateCapabilityReport(Model model) {
        return new CapabilityReport(model);
    }

    /**
     * Capability report class for detailed model analysis
     */
    public static class CapabilityReport {
        private final Model model;
        private final List<Model_Type> detectedCapabilities;
        private final List<Model_Type> inferredCapabilities;
        private final List<String> recommendations;

        public CapabilityReport(Model model) {
            this.model = model;
            this.detectedCapabilities = model.getTypes();
            this.inferredCapabilities = ModelFeatureDetector.detectCapabilities(model.getName(), null);
            this.recommendations = generateRecommendations();
        }

        private List<String> generateRecommendations() {
            List<String> recommendations = new ArrayList<>();
            
            // Check for missing obvious capabilities
            for (Model_Type inferred : inferredCapabilities) {
                if (!detectedCapabilities.contains(inferred)) {
                    recommendations.add("Consider adding " + inferred + " capability based on model name analysis");
                }
            }

            // Check for capability conflicts
            for (Model_Type capability : detectedCapabilities) {
                Set<Model_Type> conflicts = CONFLICTING_CAPABILITIES.get(capability);
                if (conflicts != null) {
                    for (Model_Type conflict : conflicts) {
                        if (detectedCapabilities.contains(conflict)) {
                            recommendations.add("Potential conflict between " + capability + " and " + conflict + " capabilities");
                        }
                    }
                }
            }

            return recommendations;
        }

        public Model getModel() { return model; }
        public List<Model_Type> getDetectedCapabilities() { return detectedCapabilities; }
        public List<Model_Type> getInferredCapabilities() { return inferredCapabilities; }
        public List<String> getRecommendations() { return recommendations; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Capability Report for: ").append(model.getName()).append("\n");
            sb.append("Current capabilities: ").append(detectedCapabilities).append("\n");
            sb.append("Inferred capabilities: ").append(inferredCapabilities).append("\n");
            if (!recommendations.isEmpty()) {
                sb.append("Recommendations:\n");
                recommendations.forEach(rec -> sb.append("  - ").append(rec).append("\n"));
            }
            return sb.toString();
        }
    }
}