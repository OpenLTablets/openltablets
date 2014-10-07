package org.openl.rules.webstudio.web.admin;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openl.config.ConfigurationManager;

public class RepositoryConfiguration {
    public static final Comparator<RepositoryConfiguration> COMPARATOR = new NameWithNumbersComparator();
    public static final String SECURE_CONFIG_FILE = "/secure-jackrabbit-repository.xml";

    private final ConfigurationManager configManager;

    private final String REPOSITORY_FACTORY;
    private final String REPOSITORY_NAME;

    private final String REPOSITORY_LOGIN;
    private final String REPOSITORY_PASS;

    private final String REPOSITORY_CONFIG_FILE;

    private final String REPOSITORY_CONNECTION_TYPE;

    private final RepositoryType repositoryType;

    private boolean secure = false;

    private String configName;

    private String oldName = null;

    public RepositoryConfiguration(String configName, ConfigurationManager configManager, RepositoryType repositoryType) {
        this.configName = configName.toLowerCase();
        this.configManager = configManager;
        this.repositoryType = repositoryType;

        REPOSITORY_FACTORY = repositoryType.toString() + "-repository.factory";
        REPOSITORY_NAME = repositoryType.toString() + "-repository.name";

        REPOSITORY_LOGIN = repositoryType.toString() + "-repository.login";
        REPOSITORY_PASS = repositoryType == RepositoryType.PRODUCTION ?
                          "production-repository.password" :
                          "design-repository.pass"; // For backward-compatibility

        REPOSITORY_CONFIG_FILE = repositoryType.toString() + "-repository.config";
        REPOSITORY_CONNECTION_TYPE = repositoryType.toString() + "-repository.connection.type";
    }

    public String getName() {
        return configManager.getStringProperty(REPOSITORY_NAME);
    }

    public void setName(String name) {
        oldName = getName();
        configManager.setProperty(REPOSITORY_NAME, StringUtils.trimToEmpty(name));
    }

    public String getType() {
        return getJcrType().getAccessType();
    }

    public void setType(String accessType) {
        configManager.setProperty(REPOSITORY_FACTORY,
                JcrType.findByAccessType(repositoryType, accessType).getFactoryClassName());
    }

    public String getPath() {
        JcrType jcrType = getJcrType();
        String propName = jcrType.getRepositoryPathPropertyName();

        return "local".equals(jcrType.getAccessType()) ?
               configManager.getPath(propName) : configManager.getStringProperty(propName);
    }

    public void setPath(String path) {
        JcrType jcrType = getJcrType();
        String propName = jcrType.getRepositoryPathPropertyName();
        String normalizedPath = StringUtils.trimToEmpty(path);

        if ("local".equals(jcrType.getAccessType())) {
            configManager.setPath(propName, normalizedPath);
        } else {
            configManager.setProperty(propName, normalizedPath);
        }
    }

    public String getConfigName() {
        return configName;
    }

    public boolean save() {
        return configManager.save();
    }

    public boolean restoreDefaults() {
        return configManager.restoreDefaults();
    }

    public boolean delete() {
        return configManager.delete();
    }

    public void copyContent(RepositoryConfiguration other) {
        // do not copy configName, only content
        setName(other.getName());
        setType(other.getType());
        setPath(other.getPath());
    }

    public boolean isNameChanged() {
        String name = getName();
        return name != null && !name.equals(oldName) || name == null && oldName != null;
    }

    public boolean isNameChangedIgnoreCase() {
        String name = getName();
        return name != null && !name.equalsIgnoreCase(oldName) || name == null && oldName != null;
    }

    public boolean isRepositoryPathSystem() {
        return configManager.isSystemProperty(getJcrType().getRepositoryPathPropertyName());
    }

    public String getLogin() {
        return configManager.getStringProperty(REPOSITORY_LOGIN);
    }

    public void setLogin(String login) {
        configManager.setProperty(REPOSITORY_LOGIN, login);
    }

    public String getPassword() {
        return "";
        //return configManager.getPassword(REPOSITORY_PASS);
    }

    public void setPassword(String pass) {
        if (!StringUtils.isEmpty(pass)) {
            configManager.setPassword(REPOSITORY_PASS, pass);
        }
    }

    public String getConnectionType() {
        return configManager.getStringProperty(REPOSITORY_CONNECTION_TYPE);
    }

    public void setConnectionType(String connectionType) {
        configManager.setProperty(REPOSITORY_CONNECTION_TYPE, connectionType);
    }

    public Map<String, Object> getProperties() {
        return configManager.getProperties();
    }

    public String getConfigFile() {
        return configManager.getStringProperty(REPOSITORY_CONFIG_FILE);
    }

    public void setConfigFile(String configFile) {
        configManager.setProperty(REPOSITORY_CONFIG_FILE, configFile);
    }

    public boolean isSecure() {
        return secure || !StringUtils.isEmpty(getLogin());
    }

    public void setSecure(boolean secure) {
        if (!secure) {
            configManager.removeProperty(REPOSITORY_LOGIN);
            configManager.removeProperty(REPOSITORY_PASS);
            configManager.removeProperty(REPOSITORY_CONFIG_FILE);
        } else {
            configManager.setProperty(REPOSITORY_CONFIG_FILE, SECURE_CONFIG_FILE);
        }
        this.secure = secure;
    }

    private JcrType getJcrType() {
        return JcrType.findByFactory(repositoryType, configManager.getStringProperty(REPOSITORY_FACTORY));
    }

    protected static class NameWithNumbersComparator implements Comparator<RepositoryConfiguration> {
        private static final Pattern pattern = Pattern.compile("([^\\d]*+)(\\d*+)");

        @Override
        public int compare(RepositoryConfiguration o1, RepositoryConfiguration o2) {
            Matcher m1 = pattern.matcher(o1.getName());
            Matcher m2 = pattern.matcher(o2.getName());
            while (true) {
                boolean f1 = m1.find();
                boolean f2 = m2.find();
                if (!f1 && !f2) {
                    return 0;
                }
                if (f1 != f2) {
                    return f1 ? 1 : -1;
                }

                String s1 = m1.group(1);
                String s2 = m2.group(1);
                int compare = s1.compareToIgnoreCase(s2);
                if (compare != 0) {
                    return compare;
                }

                String n1 = m1.group(2);
                String n2 = m2.group(2);
                if (!n1.equals(n2)) {
                    if (n1.isEmpty()) {
                        return -1;
                    }
                    if (n2.isEmpty()) {
                        return 1;
                    }
                    return new BigInteger(n1).compareTo(new BigInteger(n2));
                }
            }
        }
    }
}
