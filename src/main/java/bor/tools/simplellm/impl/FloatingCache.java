/**
 * 
 */
package bor.tools.simplellm.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import bor.tools.simplellm.*;

/**
 * Singleton class to store some floating cache data.
 * 
 * @see MapModels
 * @see CacheEntry
 */
 class FloatingCache {

	/**
	 * Cache of LLM models per provider.
	 * Key are LLMProvider url api address.
	 */
	private Map<String, CacheEntry> llmModelsCache = new HashMap<>();
	
	/**
	 * The singleton instance.
	 */
	private static  FloatingCache instance;

	/**
	 * The singleton instance.
	 */
	private FloatingCache() {}
	
	public static FloatingCache getInstance() {
		if (instance == null) {
			instance = new FloatingCache();
		}
		return instance;
	}
	
	/**
	 * Get the cache entry for the given LLMProvider.
	 * 
	 * @param llmProvider the LLM provider
	 * 
	 * @return a CacheEntry container, with null values if not present
	 */
	public CacheEntry getCacheEntry(LLMProvider llmProvider) {
		CacheEntry entry = llmModelsCache.get(urlKey(llmProvider));
		if (entry == null) {
			entry = new CacheEntry(null, null);
			llmModelsCache.put(urlKey(llmProvider), entry);
		}
		return entry;
	}
	
	/**
	 * Add a cache entry for the given LLMProvider.
	 * @param llmProvider the LLM provider
	 * @param models the models map
	 * @param fetchTime the fetch time
	 */
	public void addCacheEntry(LLMProvider llmProvider, MapModels models, LocalDateTime fetchTime) {
		CacheEntry entry = new CacheEntry(models, fetchTime);
		llmModelsCache.put(urlKey(llmProvider), entry);
	}
	
	/**
	 * Resolve the URL key for the given LLMProvider.
	 * @param llmProvider
	 * @param fetchTime
	 */
	private String urlKey(LLMProvider llmProvider) {
		return llmProvider.getLLMConfig().getBaseUrl();
	}
	
	/**
	 * Cache entry for LLM models and fetch time.
	 */
	@lombok.Data
	public static class CacheEntry {
		/**
		 * The map of models.
		 */
		public MapModels     models;
		/**
		 * The time the models were fetched.
		 */
		public LocalDateTime fetchTime;
		
		/**
		 * Creates a new cache entry.
		 * @param models the models map
		 * @param fetchTime the fetch time
		 */	
		public CacheEntry(MapModels models, LocalDateTime fetchTime) {
			this.models    = models;
			this.fetchTime = fetchTime;
		}

		/**
		 * Update the fetch time to now.
		 */
		public void updateTimestamp() {
			fetchTime = LocalDateTime.now();			
		}

		/**
		 * Update the models and fetch time.
		 * @param installedModelsCache the models map
		 * @param lastInstalledModelsFetch the fetch time
		 */
		public void update(MapModels installedModelsCache, LocalDateTime lastInstalledModelsFetch) {
			this.models    = installedModelsCache;
			this.fetchTime = lastInstalledModelsFetch;			
		}
	}	
}
