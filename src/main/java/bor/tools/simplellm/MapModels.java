package bor.tools.simplellm;

import java.util.LinkedHashMap;
import java.util.List;

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

	/**
	 * Adds a model to the map using its name as the key.
	 * 
	 * @param model the model to add
	 * @return true if the model was added, false if a model with the same name already exists
	 */
	public boolean add(Model model) {
		var m = put(model.getName(), model);
		return m == null;
	}

	/**
	 * Returns a model by its alias (case insensitive).
	 * 
	 * @param alias the model alias
	 * 
	 * @return the model if found, null otherwise
	 */
	public Model getByAlias(String alias) {
		for (Model m : values()) {
			if (m.getAlias().equalsIgnoreCase(alias)) {
				return m;
			}
		}
		return null;
	}

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
