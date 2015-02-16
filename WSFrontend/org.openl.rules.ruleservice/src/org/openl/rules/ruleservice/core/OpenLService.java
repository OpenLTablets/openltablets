package org.openl.rules.ruleservice.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openl.classloader.ClassLoaderCloserFactory;
import org.openl.classloader.SimpleBundleClassLoader;
import org.openl.rules.convertor.String2DataConvertorFactory;
import org.openl.rules.project.model.Module;
import org.openl.types.IOpenClass;
import org.openl.types.java.JavaOpenClass;

/**
 * Class designed for storing settings for service configuration and compiled
 * service bean. RuleServiceOpenLServiceInstantiationFactory is designed for
 * build OpenLService instances.
 * 
 * @author Marat Kamalov
 * 
 */
public final class OpenLService {
    /**
     * Unique for service.
     */
    private String name;
    private String url;
    private String serviceClassName;
    private Class<?> serviceClass;
    private Class<?> instanceClass;
    private Object serviceBean;
    private IOpenClass openClass;
    private boolean provideRuntimeContext = false;
    private boolean useRuleServiceRuntimeContext = false;
    private boolean provideVariations = false;
    private Collection<Module> modules;
    private Set<String> publishers;
    private ClassLoader classLoader;
    
    /**
     * Not full constructor, by default variations is not supported.
     * 
     * @param name service name
     * @param url url
     * @param serviceClassName class name for service
     * @param provideRuntimeContext define is runtime context should be used
     * @param modules a list of modules for load
     */
    OpenLService(String name, String url, String serviceClassName, boolean provideRuntimeContext, boolean useRuleServiceRuntimeContext,
            Set<String> publishers, Collection<Module> modules, ClassLoader classLoader) {
        this(name, url, serviceClassName, provideRuntimeContext, useRuleServiceRuntimeContext, false, publishers, modules, classLoader);
    }

    OpenLService(String name, String url, String serviceClassName, boolean provideRuntimeContext, boolean useRuleServiceRuntimeContext,
            Collection<Module> modules, ClassLoader classLoader) {
        this(name, url, serviceClassName, provideRuntimeContext, useRuleServiceRuntimeContext, false, null, modules, classLoader);
    }

    OpenLService(String name, String url, String serviceClassName, boolean provideRuntimeContext, boolean useRuleServiceRuntimeContext,
            boolean provideVariations, Collection<Module> modules, ClassLoader classLoader) {
        this(name, url, serviceClassName, provideRuntimeContext, useRuleServiceRuntimeContext, provideVariations, null, modules, classLoader);
    }
    
    /**
     * Returns service classloader
     * @return classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * Main constructor.
     * 
     * @param name service name
     * @param url url
     * @param serviceClassName class name for service
     * @param provideRuntimeContext define is runtime context should be used
     * @param provideVariations define is variations should be supported
     * @param modules a list of modules for load
     */
    OpenLService(String name, String url, String serviceClassName, boolean provideRuntimeContext, boolean useRuleServiceRuntimeContext,
            boolean provideVariations, Set<String> publishers, Collection<Module> modules, ClassLoader classLoader) {
        if (name == null) {
            throw new IllegalArgumentException("name arg can't be null");
        }
        this.name = name;
        this.url = url;
        if (modules != null) {
            this.modules = Collections.unmodifiableCollection(modules);
        } else {
            this.modules = Collections.emptyList();
        }
        this.serviceClassName = serviceClassName;
        this.provideRuntimeContext = provideRuntimeContext;
        this.useRuleServiceRuntimeContext = useRuleServiceRuntimeContext;
        this.provideVariations = provideVariations;
        if (publishers != null) {
            this.publishers = Collections.unmodifiableSet(publishers);
        } else {
            this.publishers = Collections.emptySet();
        }
        this.classLoader = classLoader;
    }

    private OpenLService(OpenLServiceBuilder builder) {
        this(builder.name, builder.url, builder.serviceClassName, builder.provideRuntimeContext, builder.useRuleServiceRuntimeContext,
                builder.provideVariations, builder.publishers, builder.modules, builder.classLoader);
    }

    /**
     * Returns service name.
     * 
     * @return service name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns service URL.
     * 
     * @return service URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns service publishers.
     * 
     * @return service publishers
     */
    public Collection<String> getPublishers() {
        if (publishers == null)
            return Collections.emptyList();
        return publishers;
    }

    /**
     * Returns unmodifiable collection of modules.
     * 
     * @return a collection of modules
     */
    public Collection<Module> getModules() {
        if (modules == null)
            return Collections.emptyList();
        return modules;
    }

    /**
     * Returns a class name for service.
     * 
     * @return
     */
    public String getServiceClassName() {
        return serviceClassName;
    }

    /**
     * Return provideRuntimeContext value. This value is define that service
     * methods first argument is IRulesRuntimeContext.
     * 
     * @return isProvideRuntimeContext
     */
    public boolean isProvideRuntimeContext() {
        return provideRuntimeContext;
    }

    /**
     * Returns useRuleServiceRuntimeContext
     * @return useRuleServiceRuntimeContext
     */
    public boolean isUseRuleServiceRuntimeContext() {
        return useRuleServiceRuntimeContext;
    }
    
    /**
     * This flag defines whether variations will be supported or not.
     * 
     * @return <code>true</code> if variations should be injected in service
     *         class, and <code>false</code> otherwise.
     */
    public boolean isProvideVariations() {
        return provideVariations;
    }

    /**
     * Returns service class.
     * 
     * @return
     */
    public Class<?> getServiceClass() {
        return serviceClass;
    }
    
    void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Class<?> getInstanceClass() {
        return instanceClass;
    }

    void setInstanceClass(Class<?> instanceClass) {
        this.instanceClass = instanceClass;
    }

    public Object getServiceBean() {
        return serviceBean;
    }

    void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    public void setOpenClass(IOpenClass openClass) {
        this.openClass = openClass;
    }
    
    public IOpenClass getOpenClass() {
        return openClass;
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OpenLService other = (OpenLService) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Unregister ClassLoaders of this service.
     */
    public void destroy() {
        if (getClassLoader() != null) {
            ClassLoader classLoader = getClassLoader();
            while (classLoader instanceof SimpleBundleClassLoader) {
                JavaOpenClass.resetClassloader(classLoader);
                String2DataConvertorFactory.unregisterClassLoader(classLoader);
                ClassLoaderCloserFactory.getClassLoaderCloser().close(classLoader);
                classLoader = classLoader.getParent();
            }
        }
    }

    /**
     * OpenLService builder.
     * 
     * @author Marat Kamalov
     * 
     */
    public static class OpenLServiceBuilder {
        private String name;
        private String url;
        private String serviceClassName;
        private boolean provideRuntimeContext = false;
        private boolean provideVariations = false;
        private boolean useRuleServiceRuntimeContext = false;
        private Collection<Module> modules;
        private Set<String> publishers;
        private ClassLoader classLoader;

        public OpenLServiceBuilder setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }
        
        public OpenLServiceBuilder setPublishers(Set<String> publishers) {
            if (publishers == null) {
                this.publishers = new HashSet<String>(0);
            } else {
                this.publishers = publishers;
            }
            return this;
        }

        public OpenLServiceBuilder addPublishers(Set<String> publishers) {
            if (this.publishers == null) {
                this.publishers = new HashSet<String>();
            }
            if (publishers != null) {
                this.publishers.addAll(publishers);
            }
            return this;
        }

        public OpenLServiceBuilder addPublisher(String publisher) {
            if (this.publishers == null) {
                this.publishers = new HashSet<String>();
            }
            if (publisher != null) {
                this.publishers.add(publisher);
            }
            return this;
        }

        /**
         * Sets name to the builder.
         * 
         * @param name
         * @return
         */
        public OpenLServiceBuilder setName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name arg can't be null");
            }
            this.name = name;
            return this;
        }

        /**
         * Sets class name to the builder.
         * 
         * @param serviceClassName
         * @return
         */
        public OpenLServiceBuilder setServiceClassName(String serviceClassName) {
            this.serviceClassName = serviceClassName;
            return this;
        }

        /**
         * Sets provideRuntimeContext to the builder.
         * 
         * @param provideRuntimeContext
         * @return
         */
        public OpenLServiceBuilder setProvideRuntimeContext(boolean provideRuntimeContext) {
            this.provideRuntimeContext = provideRuntimeContext;
            return this;
        }

        /**
         * Sets provideVariations flag to the builder. (Optional)
         * 
         * @param provideVariations
         * @return
         */
        public OpenLServiceBuilder setProvideVariations(boolean provideVariations) {
            this.provideVariations = provideVariations;
            return this;
        }

        /**
         * Sets a new set of modules to the builder.
         * 
         * @param modules
         * @return
         */
        public OpenLServiceBuilder setModules(Collection<Module> modules) {
            if (modules == null) {
                modules = new ArrayList<Module>(0);
            } else {
                this.modules = new ArrayList<Module>(modules);
            }
            return this;
        }

        /**
         * Add modules to the builder.
         * 
         * @param modules
         * @return
         */
        public OpenLServiceBuilder addModules(Collection<Module> modules) {
            if (this.modules == null) {
                this.modules = new ArrayList<Module>();
            }
            this.modules.addAll(modules);
            return this;
        }

        /**
         * Adds module to the builder.
         * 
         * @param module
         * @return
         */
        public OpenLServiceBuilder addModule(Module module) {
            if (this.modules == null) {
                this.modules = new ArrayList<Module>();
            }
            if (module != null) {
                this.modules.add(module);
            }
            return this;
        }

        /**
         * Sets url to the builder.
         * 
         * @param url
         * @return
         */
        public OpenLServiceBuilder setUrl(String url) {
            this.url = url;
            return this;
        }
        
        /**
         * Sets flag useRuleServiceRuntimeContext.
         */
        public OpenLServiceBuilder setUseRuleServiceRuntimeContext(boolean useRuleServiceRuntimeContext) {
            this.useRuleServiceRuntimeContext = useRuleServiceRuntimeContext;
            return this;
        }

        /**
         * Builds OpenLService.
         * 
         * @return
         */
        public OpenLService build() {
            if (name == null) {
                throw new IllegalStateException("name is required field for building ServiceDescription");
            }
            return new OpenLService(this);
        }
    }
}
