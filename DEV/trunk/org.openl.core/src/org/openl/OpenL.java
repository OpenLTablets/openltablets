/*
 * Created on May 30, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openl.conf.IOpenLBuilder;
import org.openl.conf.IOpenLConfiguration;
import org.openl.conf.IUserContext;
import org.openl.conf.OpenLConfigurator;
import org.openl.conf.UserContext;
import org.openl.conf.cache.CacheUtils;
import org.openl.message.OpenLMessage;
import org.openl.validation.IOpenLValidator;

/**
 * This class describes OpenL engine context abstraction that used during
 * compilation process.
 * 
 * The class OpenL implements both factory(static) methods for creating OpenL
 * instances and actual OpenL functionality. Each instance of OpenL should be
 * considered as a Language Configuration(LC). You may have as many LCs in your
 * application as you want. Current OpenL architecture allows to have different
 * OpenL configurations in separate class loaders, so they will not interfere
 * with each other. It allows, for example, to have 2 LCs using different SAX or
 * DOM parser implementation.
 * 
 * The actual work is done by class OpenLConfigurator.
 * 
 * @see OpenLConfigurator
 * @author snshor
 */
public class OpenL {

    private static final String DEFAULT_USER_HOME = ".";

    private static OpenLConfigurator config = new OpenLConfigurator();

    private static HashMap<Object, OpenL> openlCache = new HashMap<Object, OpenL>();

    private IOpenParser parser;

    private IOpenBinder binder;

    private IOpenVM vm;

    private IUserContext userContext;

    private String name;

    /**
     * OpenL messages. Used to accumulate engine messages for communication with
     * end user.
     */
    private List<OpenLMessage> messages = new ArrayList<OpenLMessage>();

    /**
     * Set of validators that will be used in validation process.
     */
    private Set<IOpenLValidator> validators = new CopyOnWriteArraySet<IOpenLValidator>();

    /**
     * Gets instance of <code>OpenL</code> with given name.
     * 
     * @param name OpenL name
     * @return instance of OpenL
     * @throws OpenConfigurationException
     */
    public static OpenL getInstance(String name) throws OpenConfigurationException {

        return getInstance(name, config.getClassLoader());
    }

    /**
     * Gets instance of <code>OpenL</code> with given name and that use the
     * given class loader.
     * 
     * @param name OpenL name
     * @param classLoader class loader that associated with OpenL instance and
     *            used for resource loading
     * @return OpenL instance
     * @throws OpenConfigurationException
     * 
     * @see IOpenLConfiguration
     * @see IUserContext
     */
    public static synchronized OpenL getInstance(String name, ClassLoader classLoader)
            throws OpenConfigurationException {

        String currentWorkDirectory = new File(DEFAULT_USER_HOME).getAbsolutePath();

        return getInstance(name, new UserContext(classLoader, currentWorkDirectory));
    }

    /**
     * Gets an instance of OpenL. Each instance is cached with name and user
     * context as it's key. To remove cached instance use #remove method
     * 
     * @see #remove
     * @see IUserContext
     * 
     * @param name IOpenL name, for example org.openl.java12.v101
     * @param userContext user context
     * @return instance of IOpenL
     * @throws OpenConfigurationException
     */
    public static synchronized OpenL getInstance(String name, IUserContext userContext)
            throws OpenConfigurationException {

        Object key = CacheUtils.makeKey(name, userContext);

        OpenL openl = openlCache.get(key);

        if (openl == null) {

            IOpenLBuilder builder = config.getBuilder(name, userContext);

            openl = createInstance(name, userContext, builder);

            openlCache.put(key, openl);
        }

        return openl;
    }

    /**
     * Gets an instance of OpenL. Each instance is cached with name and user
     * context as it's key.
     * 
     * @see IUserContext
     * 
     * @param name IOpenL name
     * @param userContext user context
     * @param builder {@link IOpenLBuilder} instance which used to build new
     *            instance of OpenL if that doesn't exist
     * @return instance of IOpenL
     * @throws OpenConfigurationException
     */
    public static OpenL getInstance(String name, IUserContext userContext, IOpenLBuilder builder) {

        Object key = CacheUtils.makeKey(name, userContext);

        OpenL openl = openlCache.get(key);

        if (openl == null) {

            openl = createInstance(name, userContext, builder);

            openlCache.put(key, openl);
        }

        return openl;
    }

    /**
     * Creates new instance of OpenL.
     * 
     * @param name name of OpenL
     * @param userContext {@link IUserContext} instance
     * @param builder {@link IOpenLBuilder} instance which used to build new
     *            instance of OpenL if that doesn't exist
     * @return new instance of OpenL
     */
    private static OpenL createInstance(String name, IUserContext userContext, IOpenLBuilder builder) {

        OpenL openl = builder.build(name);
        openl.userContext = userContext;
        openl.setName(name);

        return openl;
    }

    /**
     * Removes instance of OpenL with given name from cache.
     * 
     * @param name OpenL name
     * @return removed OpenL instance
     * @throws OpenConfigurationException
     */
    public static synchronized OpenL remove(String name) throws OpenConfigurationException {

        return remove(name, config.getClassLoader());
    }

    /**
     * Removes instance of OpenL with given name and class loader from cache.
     * 
     * @param name OpenL name
     * @param classLoader class loader that associated with OpenL instance and
     *            used for resource loading
     * @return removed OpenL instance
     * @throws OpenConfigurationException
     * 
     * @see IOpenLConfiguration
     * @see IUserContext
     */
    public static synchronized OpenL remove(String name, ClassLoader classLoader) throws OpenConfigurationException {

        return remove(name, new UserContext(classLoader, DEFAULT_USER_HOME));
    }

    /**
     * Removes instance of OpenL with given name using specified user context.
     * 
     * @param name OpenL name
     * @param userContext user context that used to find appropriate instance of
     *            OpenL
     * @return removed OpenL instance
     * 
     * @see IUserContext
     */
    public static synchronized OpenL remove(String name, IUserContext userContext) {

        Object key = CacheUtils.makeKey(name, userContext);

        OpenL openl = openlCache.get(key);

        if (openl == null) {
            return null;
        }

        openlCache.remove(key);

        return openl;
    }

    /**
     * Resets OpenL internal cache.
     */
    public static void reset() {
        openlCache = new HashMap<Object, OpenL>();
    }

    /**
     * Gets name of OpenL instance.
     * 
     * @return name string
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of OpenL instance.
     * 
     * @param name name string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets parser that configured for current OpenL instance.
     * 
     * @return {@link IOpenParser} instance
     */
    public IOpenParser getParser() {
        return parser;
    }

    /**
     * Sets parser to current OpenL instance.
     * 
     * @param parser {@link IOpenParser} instance
     */
    public void setParser(IOpenParser parser) {
        this.parser = parser;
    }

    /**
     * Gets user context which associated with current OpenL instance.
     * 
     * @return {@link IUserContext} instance
     */
    public IUserContext getUserContext() {
        return userContext;
    }

    /**
     * Gets virtual machine which used during rules execution.
     * 
     * @return {@link IOpenVm} instance
     */
    public IOpenVM getVm() {
        return vm;
    }

    /**
     * Sets virtual machine.
     * 
     * @param openVM {@link IOpenVm} instance
     */
    public void setVm(IOpenVM openVM) {
        vm = openVM;
    }

    /**
     * Gets binder that configured for current OpenL instance.
     * 
     * @return {@link IOpenBinder} instance
     */
    public IOpenBinder getBinder() {
        return binder;
    }

    /**
     * Sets binder to current OpenL instance.
     * 
     * @param binder {@link IOpenBinder} instance
     */
    public void setBinder(IOpenBinder binder) {
        this.binder = binder;
    }

    /**
     * Gets copy list of OpenL messages.
     * 
     * @return list of messages
     */
    public List<OpenLMessage> getMessages() {
        return new ArrayList<OpenLMessage>(messages);
    }

    /**
     * Removes all entries from OpenL messages.
     * 
     */
    public void removeMessages() {
        messages = new ArrayList<OpenLMessage>();
    }

    /**
     * Adds new OpenL message.
     * 
     * @param message new message
     */
    public void addMessage(OpenLMessage message) {
        messages.add(message);
    }

    /**
     * Adds OpenL messages.
     * 
     * @param messages messages to add
     */
    public void addMessages(List<OpenLMessage> messages) {

        for (OpenLMessage message : messages) {
            addMessage(message);
        }
    }

    /**
     * Adds new validator to set of validators.
     * 
     * @param validator validator instance
     */
    public void addValidator(IOpenLValidator validator) {
        validators.add(validator);
    }

    /**
     * Adds new validators to set of validators.
     * 
     * @param validators list of validator
     */
    public void addValidators(List<IOpenLValidator> validators) {

        for (IOpenLValidator validator : validators) {
            addValidator(validator);
        }
    }

    /**
     * Removes validator from set of validators.
     * 
     * @param validator validator to remove
     */
    public void removeValidator(IOpenLValidator validator) {
        validators.remove(validator);
    }

    /**
     * Removes all validators.
     */
    public void removeValidators() {
        validators = new CopyOnWriteArraySet<IOpenLValidator>();
    }

    /**
     * Gets set of registered validators.
     * 
     * @return set of validators
     */
    public Set<IOpenLValidator> getValidators() {
        return validators;
    }

}