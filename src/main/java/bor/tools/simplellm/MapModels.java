package bor.tools.simplellm;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * A specialized map to manage Model objects, allowing retrieval by name or
 * alias.
 * <p>
 * This class extends LinkedHashMap to maintain the order of insertion and
 * provides additional methods to interact with Model objects.
 * </p>
 * 
 * @author AlessandroBorges
 * 
 * @since 1.0
 */
public class MapModels extends LinkedHashMap<String, Model> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty MapModels.
	 */
	public MapModels() {
		super();
	}

	
	private static String normalizeModelName(String modelName) {
		if( modelName == null ) {
			return null;
		}
		return modelName.toLowerCase().trim();
	}
	
	/**
	 * Adds a model to the map using its name as the key.
	 * 
	 * @param model the model to add
	 * @return true if the model was added, false if a model with the same name already exists
	 */
	public boolean add(Model model) {
		var m = put(model.getName(), model);
		
		String alias = model.getAlias();
		if( alias != null 
			&& !alias.isEmpty() 
			&& !this.containsKey(alias) == false) 
		{
			var ma = put(model.getAlias(), model);
			m = m==null ? ma : m;
		}
		return m == null;
	}
	
	/**
	 * Adds a model to the map with a specified normalized name as the key.
	 * 
	 * @param modelNameAlias the name of the model
	 * @param model               the model to add
	 * @return true if the model was added, false if a model with the same name already exists
	 */
	public boolean addModel(String modelNameAlias, Model model) {
		if( modelNameAlias == null || modelNameAlias.isEmpty() ) {
			return false;
		}
		
		if(this.containsKey(modelNameAlias)) {
			// replace existing
			put(modelNameAlias, model);
			return false;
		} else {
			put(modelNameAlias, model);
			return true;
		}		
	}
	
	/**
	 * Checks if a model with the given name exists in the map.
	 * 
	 * @param modelName the model name to check
	 * 
	 * @return true if a model with the given name exists, false otherwise
	 */ 
	public boolean contains(String modelName) {
		if (modelName == null || modelName.isEmpty()) {
			return false;
		}
		// First check direct key
		if (this.containsKey(modelName)) {
			return true;
		} else {
			// Then check normalized key	
			String normName = normalizeModelName(modelName);
			return this.containsKey(normName);
		}
	}

	/**
	 * Puts a model into the map with the specified model name as the key.
	 * If a model with the same normalized name already exists, it will not be added.
	 * 
	 * @param modelName the name of the model
	 * @param model     the model to add
	 * @return the previous model associated with the normalized name, or null if none existed
	 **/
	@Override
	public Model put(String modelName, Model model) {
		if( modelName == null || modelName.isEmpty() || model == null ) {
			return null;
		}
		String normName = normalizeModelName(modelName);
		if(this.containsKey(normName)) {
			return null;
		} else {
			return super.put(modelName, model);			
		}		
	}
	
	/**
	 * Returns a model by its alias (case insensitive).
	 * 
	 * @param alias the model alias
	 * 
	 * @return the model if found, null otherwise
	 */
	public Model getByAlias(String alias) {
		if (alias == null || alias.isEmpty()) {
			return null;
		}
		for (Model m : values()) {
			if (alias.equalsIgnoreCase(m.getAlias())) {
				return m;
			}
		}
		return null;
	}
	
	/**
	 * Returns a model whose name partially matches the given string.
	 * 
	 * @param partialName the partial model name to search for
	 * 
	 * @return the model if a match is found, null otherwise
	 **/
	public Model getByPartialMatch(String partialName) {
		if (partialName == null || partialName.isEmpty()) {
			return null;
		}
		String normPartial = normalizeModelName(partialName);
		
		Set<String> keys = this.keySet();
		SortedSet<String> sortedKeys = new java.util.TreeSet<String>(comparatorLengthDesc);
		sortedKeys.addAll(keys);
		
		// add aliases too
		for(Model m : values()) {			
			String alias = m.getAlias();
			if(alias != null  ) {
			   sortedKeys.add(alias);
			}
		}
		
		for (String key : sortedKeys) {
			String normKey = normalizeModelName(key);
			if (normKey.contains(normPartial)) {
				return getModel(key);
			}
		}
		return null;
	}
	
	/**
	 * Comparator to sort strings by length in descending order.
	 */
	private static java.util.Comparator<String> comparatorLengthDesc = new java.util.Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			return Integer.compare(s2.length(), s1.length());
		}		
	};

	/**
	 * Returns a model by its name or alias.
	 * 
	 * @param nameOrAlias the model name or alias
	 * 
	 * @return the model if found, null otherwise
	 */
	public Model getModel(String nameOrAlias) {
		Model m = super.get(nameOrAlias);
		if (m == null) {
			m = getByAlias(nameOrAlias);
		}
		return m;
	}
	
	/**
	 * Returns a model by its name.
	 * 
	 * @param modelName the model name
	 * 
	 * @return the model if found, null otherwise
	 */
	public Model get(String modelName) {
		return getModel	(modelName);
	}
	/**
	 * Checks if a model with the given name or alias exists in the map.
	 * 
	 * @param name the model name or alias to check
	 * 
	 * @return true if a model with the given name or alias exists, false otherwise
	 */

	public boolean containsName(String name) {
		boolean res = containsKey(name);
		if (res == false) {
			return getByAlias(name) != null;
		}
		return res;
	}

	/**
	 * Returns a list of models that match ALL of the given types.
	 * 
	 * @param types the model types to filter by
	 * 
	 * @return a list of models that match any of the given types
	 */
	public List<Model> getModelByTypes(Model_Type... types) {

		return values().stream().filter(m -> {
			for (Model_Type t : types) {
				if (m.getTypes().contains(t)) {
					return true;
				}
			}
			return false;
		}).toList();
	}

	/**
	 * Returns an unmodifiable list of all models in the map.
	 * @return
	 */
	public List<Model> getModels() { // TODO Auto-generated method stub
	  	return List.copyOf(values());
	}

	

}
