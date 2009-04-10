package org.openl.rules.webstudio.web.repository;

import org.openl.config.ConfigPropertyString;
import org.openl.config.ConfigSet;
import org.openl.config.SysConfigManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Repository artefact properties holder.
 * Used for holding artefact properties to use and their names.
 * 
 * @author Andrei Astrouski
 */
public class RepositoryArtefactPropsHolder {

    /** Properties file name. */
    private static final String PROPS_FILE = "repository-artefact-props.properties";
    
    /** Properties holder. */
    private static Map<String, String> props = getRepositoryArtefactProps();

    /**
     * Returns collection of properties to use and their names.
     * 
     * @return map of properties.
     */
    private static Map<String, String> getRepositoryArtefactProps() {
        Map<String, String> propsMap = null;
        String prefix = "props.";
        ConfigSet propsSet = SysConfigManager.getConfigManager()
            .locate(PROPS_FILE);
        ConfigPropertyString confPropsUse = new ConfigPropertyString(
                prefix + "use", null);
        propsSet.updateProperty(confPropsUse);
        String propsUse = confPropsUse.getValue();
        if (StringUtils.isNotBlank(propsUse)) {
            propsMap = new HashMap<String, String>();
            String[] props = StringUtils.deleteWhitespace(propsUse).split(",");
            for (int i = 0; i < props.length; i++) {
                ConfigPropertyString prop = new ConfigPropertyString(
                        prefix + props[i], null);
                propsSet.updateProperty(prop);
                String propName = prop.getValue();
                if (StringUtils.isBlank(propName)) {
                    propName = props[i].trim();
                }
                propsMap.put(props[i], StringUtils.removeStart(propName, prefix));
            }
        }
        return propsMap;
    }

    public Map<String, String> getProps() {
        return props;
    }

}
