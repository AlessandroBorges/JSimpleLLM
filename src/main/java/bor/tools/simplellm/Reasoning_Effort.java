/**
 * 
 */
package bor.tools.simplellm;

/**
 * Enumeration for reasoning effort levels.
 * Only available in Models with type Model_Type.#REASONING
 * 
 * <h3>Levels of reasoning effort:</h3>
 * <ul>
 * <li>minimal - Basic reasoning, suitable for simple tasks</li>
 * <li>low - Low-level reasoning, for moderately complex tasks</li>
 * <li>medium - Balanced reasoning, for a wide range of tasks</li>
 * <li>high - Advanced reasoning, for complex and nuanced tasks</li>
 * <li>none - No reasoning, direct responses only</li>
 * </ul>
 * 
 * 
 * @see Model_Type#
 * @see Model#isTypeReasoning()
 * @see bor.tools.simplellm.MapParam#reasoningEffort(Reasoning_Effort)
 * @see bor.tools.simplellm.MapParam#getReasoningEffort()
 */
public enum Reasoning_Effort {	
	/**
	 * minimal - Basic reasoning, suitable for simple tasks
	 */
	minimal,
	/**
	 * low - Low-level reasoning, for moderately complex tasks
	 */
	low, 
	/**
	 * medium - Balanced reasoning, for a wide range of tasks
	 */
	medium, 
	/**
	 * high - Advanced reasoning, for complex and nuanced tasks
	 */
	high, 
	/**
	 * none - No reasoning, direct responses only
	 */
	none;
	
	/**
	 * Get the string representation of the reasoning effort level.
	 * 
	 * @return the name of the reasoning effort level in lowercase
	 */	
	public String getValue() {
		return this.name().toLowerCase();
	}
	
	/**
	 * Get the default reasoning effort level.
	 * The default level is set to 'medium'.
	 * 
	 * @return the default reasoning effort level in lowercase
	 */
	public static String getDefault() {
		return medium.name().toLowerCase();
	}
	
	/**
	 * String representation of the reasoning effort level.
	 * 
	 * @return the name of the reasoning effort level in lowercase
	 */
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
	
	/**
	 * Convert a string to a Reasoning_Effort enum value.
	 * The comparison is case-insensitive and ignores leading/trailing whitespace.
	 * 
	 * @param value the string representation of the reasoning effort level
	 * @return the corresponding Reasoning_Effort enum value
	 * @throws IllegalArgumentException if the input string does not match any enum value
	 */
	public static Reasoning_Effort fromString(String value) {
		value = value.trim().toLowerCase();
		for (Reasoning_Effort effort : Reasoning_Effort.values()) {
			if (effort.name().contains(value)) {
				return effort;
			}
		}		
		throw new IllegalArgumentException("Unknown Reasoning_Effort value: " + value);
	}
	
}
