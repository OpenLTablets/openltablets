package org.openl.rules.webtools.exec;

public class Launcher {
    static public String LAUNCH_DIR_PROP = "org.openl.rules.webtools.scripts";

    static String LOCAL_LAUNCH_DIR = "../org.openl.rules.webstudio/scripts";

    public static String getLaunchScriptsDir() {
        String propValue = System.getProperty(LAUNCH_DIR_PROP);
        return propValue != null ? propValue : LOCAL_LAUNCH_DIR;

    }

}
