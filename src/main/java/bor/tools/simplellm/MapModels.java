package bor.tools.simplellm;

import java.util.LinkedHashMap;

public class MapModels extends LinkedHashMap<String, Model> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MapModels() {
		super();
	}

	public void add(Model model) {
		put(model.getName(), model);
	}

}
