package org.openl.rules.webstudio.web.admin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The class contains system settings property names for settings edited in "Administration" page.
 * 
 * @author NSamatov
 */
public final class AdministrationSettings {
    private static final Collection<String> allSettings;

    public static final String USER_WORKSPACE_HOME = "user.workspace.home";
    public static final String PROJECT_HISTORY_HOME = "project.history.home";
    public static final String PROJECT_HISTORY_COUNT = "project.history.count";
    public static final String PROJECT_HISTORY_UNLIMITED = "project.history.unlimited";
    public static final String DATE_PATTERN = "data.format.date";
    public static final String UPDATE_SYSTEM_PROPERTIES = "update.system.properties";
    public static final String DESIGN_REPOSITORY_FACTORY = "design-repository.factory";
    public static final String DESIGN_REPOSITORY_NAME = "design-repository.name";
    public static final String DESIGN_REPOSITORY_LOGIN = "design-repository.login";
    public static final String DESIGN_REPOSITORY_PASSWORD = "design-repository.pass";
    public static final String DESIGN_REPOSITORY_CONFIG_FILE = "design-repository.config";
    public static final String PRODUCTION_REPOSITORY_CONFIGS = "production-repository-configs";
    
    static {
        List<String> settingNames = new ArrayList<String>();
        
        for (Field field : AdministrationSettings.class.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                try {
                    Object value = field.get(null);
                    if (value instanceof String) {
                        settingNames.add((String) value);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        allSettings = Collections.unmodifiableCollection(settingNames);
    }

    /**
     * Get all settings edited in "Administration" page
     * 
     * @return setting property names edited in "Administration" page
     */
    public static Collection<String> getAllSettings() {
        return allSettings;
    }

    private AdministrationSettings() {}
}