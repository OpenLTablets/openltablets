package org.openl.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.util.PassCoder;

import java.util.*;

/**
 * @author Aleh Bykhavets
 */
public class ConfigSet {
    public static String REPO_PASS_KEY = "repository.encode.decode.key";
    private final Log log = LogFactory.getLog(ConfigSet.class);

    private Map<String, Object> properties;

    public ConfigSet() {
        properties = new HashMap<String, Object>();
    }

    public void addProperties(Properties props) {
        Enumeration<?> e = props.propertyNames();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            String value = props.getProperty(key);

            addProperty(key, value);
        }
    }

    public void addProperties(Map<String, Object> props) {
        if (props != null) {
            properties.putAll(props);
        }
    }

    public void addProperty(String name, String value) {
        if (value == null) {
            return;
        }
        // extra spaces is a big problem
        value = value.trim();

        if (value.length() == 0) {
            return;
        }

        properties.put(name, value);
    }

    public void updateProperties(Collection<ConfigProperty<?>> props) {
        for (ConfigProperty<?> prop : props) {
            updateProperty(prop);
        }
    }

    public void updateProperty(ConfigProperty<?> prop) {
        Object objectValue = properties.get(prop.getName());

        if (objectValue == null) {
            return;
        }

        try {
            prop.setTextValue(objectValue.toString());
        } catch (Exception e) {
            log.error("Failed to update ConfigProperty '" + prop.getName() + "' with value '" + objectValue.toString()
                    + "'!", e);
        }
    }

    public void updatePasswordProperty(ConfigProperty<?> prop) {
        Object objectValue = properties.get(prop.getName());

        if (objectValue == null) {
            return;
        }

        String pass = objectValue.toString();
        String passKey = this.getPassKey();
        if (StringUtils.isEmpty(passKey)){
            try {
                prop.setTextValue(PassCoder.decode(pass, passKey));
            } catch (Exception e) {
                log.error("Failed to update ConfigProperty '" + prop.getName() + "' with value '" + objectValue.toString()
                        + "'!", e);
            }
        }else{
            prop.setTextValue(pass);
        }
    }

    private String getPassKey() {
        if (this.properties.containsKey(REPO_PASS_KEY)) {
            if (this.properties.get(REPO_PASS_KEY) instanceof String[]) {
                String[] stringMass = (String[]) this.properties.get(REPO_PASS_KEY);
                return stringMass[0];
            } else {
                return (String) this.properties.get(REPO_PASS_KEY);
            }
        }

        return "";
    }
}
