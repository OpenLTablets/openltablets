package org.openl.rules.ruleservice.simple;

import java.util.Collection;

import org.openl.rules.ruleservice.core.OpenLService;

/**
 * The instance of this interface is not thread safe.
 * 
 * @author Marat Kamalov
 * 
 */
public interface RulesFrontend {

    /**
     * Executes method with specified parameters.
     * 
     * @param serviceNmae Name of deployed service
     * @param ruleName Technical name of the rule to execute
     * @param inputParamsTypes Types of method input parameters to discover
     *            method
     * @param params Parameters for method execution
     * @return Result of execution
     */
    Object execute(String serviceName, String ruleName, Class<?>[] inputParamsTypes, Object[] params)
            throws MethodInvocationException;

    /**
     * Executes method with specified parameters. Method discovery is done based
     * on parameters types.
     * 
     * @param serviceNmae Name of deployed service
     * @param ruleName Technical name of the rule to execute
     * @param params Parameters for method execution
     * @return Result of execution
     */
    Object execute(String serviceName, String ruleName, Object... params) throws MethodInvocationException;

    /**
     * Gets values defined in rules.
     * 
     * @param serviceNmae Name of deployed service
     * @param fieldName Technical name of the rule to execute
     * @return Data stored in field
     */
    Object getValue(String serviceName, String fieldName) throws MethodInvocationException;

    /**
     * Registers service to use it in calculations.
     * 
     * @param service Service to register.
     */
    Collection<OpenLService> getServices();

    /**
     * Returns service by name
     * 
     * @param serviceName
     * @return
     */
    OpenLService findServiceByName(String serviceName);

    /**
     * Registers service to use it in calculations.
     * 
     * @param service Service to register.
     * @return replaced service
     * 
     */
    OpenLService registerService(OpenLService service);

    /**
     * Unregister service
     * 
     * @param serviceName
     * @return unregistered service
     */
    OpenLService unregisterService(String serviceName);
}
