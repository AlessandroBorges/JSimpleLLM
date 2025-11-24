package bor.tools.simplellm.abstraction;

import java.util.List;
import bor.tools.simplellm.exceptions.LLMException;
import bor.tools.simplellm.*;

/**
 * Interface for managing LLM models registration, retrieval, and validation.
 * Handles both registered and installed models, including name resolution and type checking.
 * 
 * @author AlessandroBorges
 * @since 1.0
 */
public interface IModelManager {
    
    /**
     * Adiciona um novo modelo à lista de modelos registrados.
     * @param model - modelo a ser adicionado
     * @return true se o modelo foi adicionado, false se já existia
     * @throws LLMException
     */
    boolean registerModel(Model model) throws LLMException;
    
    /**
     * Retrieves the list of registered models from the LLM service.
     * @return a list of model names available for use
     * @throws LLMException if there's an error retrieving the models
     */
    MapModels getRegisteredModels() throws LLMException;
    
    /**
     * Retrieves the list of models currently available in the service provider.
     * @return Map of model names to Model objects
     * @throws LLMException
     */
    MapModels getInstalledModels() throws LLMException;
    
    /**
     * Return all models - registered + installed
     * @return all models - registered + installed
     * @throws LLMException
     */
    MapModels getAllModels() throws LLMException;
    
    /**
     * Return names of Registered models
     * @return list of registered model names
     * @throws LLMException
     */
    List<String> getRegisteredModelNames() throws LLMException;
    
    /**
     * Return names of Installed models.
     * @return list of model names
     * @throws LLMException
     */
    List<String> getInstalledModelNames() throws LLMException;
    
    /**
     * Finds the most suitable model that matches all specified types.
     * @param types variable arguments of model types to match
     * @return the Model that best matches the specified types
     */
    Model findModel(Model_Type... types);
    
    /**
     * Checks if the specified model supports the given type of operation.
     * @param model - Model object to check
     * @param type - the type of operation to verify
     * @return true if the model supports the specified type
     */
    boolean isModelType(Model model, Model_Type type);
    
    /**
     * Checks if the specified model supports the given type of operation.
     * @param modelName the name of the model to check
     * @param type the type of operation to verify
     * @return true if the model supports the specified type
     */
    boolean isModelType(String modelName, Model_Type type);
    
    /**
     * Checks if the specified model is currently online and available.
     * @param modelName - name of the model to check
     * @return true if model is online
     */
    boolean isModelOnline(String modelName);
    
    /**
     * Checks if the specified model is currently online and available.
     * @param model - model to check
     * @return true if model is online
     */
    boolean isModelOnline(Model model);
    
    /**
     * Set the default model name to be used when no specific model is provided.
     * @param modelName default model name
     */
    void setDefaultModelName(String modelName);
    
    /**
     * Returns the default model name.
     * @return default model name
     */
    String getDefaultModelName();
    
    /**
     * Finds the model name from the provided parameters.
     * @param params parameter map
     * @return model name
     */
    String findModel(MapParam params);
}