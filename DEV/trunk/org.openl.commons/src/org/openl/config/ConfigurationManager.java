package org.openl.config;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration manager.
 * 
 * @author Andrei Astrouski
 * 
 * TODO Separate configuration sets from the manager
 */
public class ConfigurationManager {

    private final Log log = LogFactory.getLog(ConfigurationManager.class);

    private boolean useSystemProperties;
    private String propsLocation;
    private String defaultPropsLocation;
    private boolean autoSave;

    private Configuration systemConfiguration;
    private FileConfiguration configurationToSave;
    private FileConfiguration defaultConfiguration;
    private CompositeConfiguration compositeConfiguration;

    public ConfigurationManager(boolean useSystemProperties,
            String propsLocation) {
        this(useSystemProperties, propsLocation, null, false);
    }

    public ConfigurationManager(boolean useSystemProperties,
            String propsLocation, String defaultPropsLocation) {
        this(useSystemProperties, propsLocation, defaultPropsLocation, false);
    }

    public ConfigurationManager(boolean useSystemProperties,
            String propsLocation, String defaultPropsLocation, boolean autoSave) {
        this.useSystemProperties = useSystemProperties;
        this.propsLocation = propsLocation;
        this.defaultPropsLocation = defaultPropsLocation;
        this.autoSave = autoSave;

        init();
    }

    private void init() {
        compositeConfiguration = new CompositeConfiguration();

        if (useSystemProperties) {
            systemConfiguration = new SystemConfiguration();
            compositeConfiguration.addConfiguration(systemConfiguration);
        }

        configurationToSave = createFileConfiguration(propsLocation, true);
        if (configurationToSave != null) {
            compositeConfiguration.addConfiguration(configurationToSave);
            if (autoSave) {
                configurationToSave.setAutoSave(autoSave);
            }
        }

        defaultConfiguration = createFileConfiguration(defaultPropsLocation);
        if (defaultConfiguration != null) {
            compositeConfiguration.addConfiguration(defaultConfiguration);
        }
    }

    private FileConfiguration createFileConfiguration(String configLocation, boolean createIfNotExist) {
        PropertiesConfiguration configuration = null;
        if (configLocation != null) {
            try {
                if (createIfNotExist) {
                    configuration = new PropertiesConfiguration(new File(configLocation));
                } else {
                    configuration = new PropertiesConfiguration(configLocation);
                }
            } catch (Exception e) {
                log.error("Error when initializing configuration: " + configLocation, e);
            }
        }
        return configuration;
    }

    private FileConfiguration createFileConfiguration(String configLocation) {
        return createFileConfiguration(configLocation, false);
    }

    public String getStringProperty(String key) {
        return compositeConfiguration.getString(key);
    }

    public String[] getStringArrayProperty(String key) {
        return compositeConfiguration.getStringArray(key);
    }

    public boolean getBooleanProperty(String key) {
        return compositeConfiguration.getBoolean(key);
    }

    public Map<String, Object> getProperties() {
        return getProperties(false);
    }

    public Map<String, Object> getProperties(boolean cross) {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Iterator<?> iterator = compositeConfiguration.getKeys(); iterator.hasNext();) {
            String key = (String) iterator.next();

            if (!cross || (cross && configurationToSave.getProperty(key) != null)) {
                Object value = compositeConfiguration.getProperty(key);
                if (value instanceof Collection || value != null && value.getClass().isArray()) {
                    properties.put(key, getStringArrayProperty(key));
                } else {
                    properties.put(key, getStringProperty(key));
                }
            }
        }
        return properties;
    }

    public void setProperty(String key, Object value) {
        if (key != null && value != null) {
            if (!(value instanceof Collection) && !value.getClass().isArray()) {
                String defaultValue = compositeConfiguration.getString(key);
                if (defaultValue != null && !defaultValue.equals(value.toString())) {
                    getConfigurationToSave().setProperty(key, value.toString());
                }
            } else {
                String[] defaultValue = compositeConfiguration.getStringArray(key);
                if (defaultValue != null) {
                    if (value instanceof Collection) {
                        @SuppressWarnings("unchecked")
                        Collection<String> v = (Collection<String>) value; 
                        value = v.toArray(new String[v.size()]);
                    }
                    if (!defaultValue.equals(value)) {
                        getConfigurationToSave().setProperty(key, value);
                    }
                }
            }
        }
    }

    public String getPath(String key) {
        return normalizePath(getStringProperty(key));
    }

    public void setPath(String key, String path) {
        String defaultPath = normalizePath(compositeConfiguration.getString(key));
        String newPath = normalizePath(path);
        if (defaultPath != null && !defaultPath.equals(newPath)) {
            getConfigurationToSave().setProperty(key, newPath);
        }
    }

    public static String normalizePath(String path) {
        if (path == null)
            return null;

        File pathFile = new File(path);
        if (!pathFile.isAbsolute()) {
            if (!path.startsWith("/") && !path.startsWith("\\")) {
                pathFile = new File(File.separator + path);
            }
        }

        return pathFile.getAbsolutePath();
    }

    public void removeProperty(String key) {
        getConfigurationToSave().clearProperty(key);
    }

    private FileConfiguration getConfigurationToSave() {
        if (configurationToSave == null) {
            configurationToSave = createFileConfiguration(propsLocation, true);
        }
        return configurationToSave;
    }

    public boolean isSystemProperty(String name) {
        if (systemConfiguration != null && systemConfiguration.getString(name) != null) {
            return true;
        }
        return false;
    }

    public boolean save() {
        if (configurationToSave != null) {
            try {
                configurationToSave.save();
                return true;
            } catch (Exception e) {
                log.error("Error when saving configuration: " + configurationToSave.getBasePath(), e);
            }
        }
        return false;
    }

    public boolean restoreDefaults() {
        if (configurationToSave != null && !configurationToSave.isEmpty()) {
            configurationToSave.clear();
            return save();
        }

        return false;
    }
    
    public boolean delete() {
        boolean deleted = false;
        
        if (configurationToSave != null) {
            deleted = configurationToSave.getFile().delete();
            configurationToSave = null;
        }
        
        return deleted;
    }

}
