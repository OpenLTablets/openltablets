/**
 * Created Oct 26, 2006
 */
package org.openl.main;

import java.util.Properties;

import org.openl.util.Log;

/**
 * @author snshor
 *
 */
public class OpenLVersion {

    public static final String PROP_FILE_NAME = "openl.version.properties";
    public static final String PROP_VERSION_NAME = "openl.version";
    public static final String PROP_BUILD_NAME = "openl.build";
    public static final String PROP_URL_NAME = "openl.url";
    public static final String PROP_YEAR_NAME = "openl.copyrightyear";

    private static Properties props = null;

    public static String getBuild() {
        return getProperties().getProperty(PROP_BUILD_NAME, "??");
    }

    public static String getCopyrightYear() {
        return getProperties().getProperty(PROP_YEAR_NAME, "??");
    }

    static synchronized Properties getProperties() {
        if (props == null) {
            props = new Properties();

            try {
                props.load(OpenLVersion.class.getResourceAsStream(PROP_FILE_NAME));
            } catch (Throwable t) {
                Log.warn(PROP_FILE_NAME + " not found", t);
            }
        }

        return props;
    }

    public static String getURL() {
        return getProperties().getProperty(PROP_URL_NAME, "??");
    }

    public static String getVersion() {
        return getProperties().getProperty(PROP_VERSION_NAME, "???");
    }

}
