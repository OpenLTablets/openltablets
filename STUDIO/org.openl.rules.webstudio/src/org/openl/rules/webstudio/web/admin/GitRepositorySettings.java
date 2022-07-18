package org.openl.rules.webstudio.web.admin;

import java.util.Optional;

import org.openl.config.PropertiesHolder;
import org.openl.spring.env.DynamicPropertySource;
import org.openl.util.StringUtils;

public class GitRepositorySettings extends RepositorySettings {
    private boolean remoteRepository;

    private String uri;
    private String login;
    private String password;
    private String localRepositoryPath;
    private String branch;
    private String newBranchTemplate;
    private String newBranchRegex;
    private String newBranchRegexError;
    private String tagPrefix;
    private Integer listenerTimerPeriod;
    private Integer connectionTimeout;
    private Integer failedAuthenticationSeconds;
    private Integer maxAuthenticationAttempts;
    private String protectedBranches;

    private final String URI;
    private final String LOGIN;
    private final String PASSWORD;
    private final String LOCAL_REPOSITORY_PATH;
    private final String BRANCH;
    private final String NEW_BRANCH_TEMPLATE;
    private final String NEW_BRANCH_REGEX;
    private final String NEW_BRANCH_REGEX_ERROR;
    private final String TAG_PREFIX;
    private final String LISTENER_TIMER_PERIOD;
    private final String CONNECTION_TIMEOUT;
    private final String CONFIG_PREFIX;
    private final String FAILED_AUTHENTICATION_SECONDS;
    private final String MAX_AUTHENTICATION_ATTEMPTS;
    private final String PROTECTED_BRANCHES;

    GitRepositorySettings(PropertiesHolder properties, String configPrefix) {
        super(properties, configPrefix);
        CONFIG_PREFIX = configPrefix;
        URI = configPrefix + ".uri";
        LOGIN = configPrefix + ".login";
        PASSWORD = configPrefix + ".password";
        LOCAL_REPOSITORY_PATH = configPrefix + ".local-repository-path";
        BRANCH = configPrefix + ".branch";
        NEW_BRANCH_TEMPLATE = configPrefix + ".new-branch.pattern";
        NEW_BRANCH_REGEX = configPrefix + ".new-branch.regex";
        NEW_BRANCH_REGEX_ERROR = configPrefix + ".new-branch.regex-error";
        TAG_PREFIX = configPrefix + ".tag-prefix";
        LISTENER_TIMER_PERIOD = configPrefix + ".listener-timer-period";
        CONNECTION_TIMEOUT = configPrefix + ".connection-timeout";
        FAILED_AUTHENTICATION_SECONDS = configPrefix + ".failed-authentication-seconds";
        MAX_AUTHENTICATION_ATTEMPTS = configPrefix + ".max-authentication-attempts";
        PROTECTED_BRANCHES = configPrefix + ".protected-branches";

        load(properties);
    }

    private void load(PropertiesHolder properties) {
        String localPath = properties.getProperty(LOCAL_REPOSITORY_PATH);
        String[] prefixParts = CONFIG_PREFIX.split("\\.");
        String id = prefixParts.length > 1 ? prefixParts[1] : "repository";
        // prefixParts.length must be always > 1
        String defaultLocalPath = localPath != null ? localPath
                                                    : properties.getProperty(
                                                        DynamicPropertySource.OPENL_HOME) + "/repositories/" + id;

        uri = properties.getProperty(URI);
        login = properties.getProperty(LOGIN);
        password = properties.getProperty(PASSWORD);
        localRepositoryPath = defaultLocalPath;
        branch = properties.getProperty(BRANCH);
        tagPrefix = properties.getProperty(TAG_PREFIX);
        listenerTimerPeriod = Optional.ofNullable(properties.getProperty(LISTENER_TIMER_PERIOD)).map(Integer::parseInt)
            .orElse(null);
        connectionTimeout = Optional.ofNullable(properties.getProperty(CONNECTION_TIMEOUT)).map(Integer::parseInt)
            .orElse(null);
        failedAuthenticationSeconds = Optional.ofNullable(properties.getProperty(FAILED_AUTHENTICATION_SECONDS))
                .map(Integer::parseInt)
                .orElse(null);
        maxAuthenticationAttempts = Optional.ofNullable(properties.getProperty(MAX_AUTHENTICATION_ATTEMPTS))
                .filter(StringUtils::isNotBlank)
                .map(Integer::parseInt)
                .orElse(null);
        newBranchTemplate = properties.getProperty(NEW_BRANCH_TEMPLATE);
        newBranchRegex = properties.getProperty(NEW_BRANCH_REGEX);
        newBranchRegexError = properties.getProperty(NEW_BRANCH_REGEX_ERROR);
        protectedBranches = properties.getProperty(PROTECTED_BRANCHES);

        remoteRepository = StringUtils.isNotBlank(uri);
    }

    public boolean isRemoteRepository() {
        return remoteRepository;
    }

    public void setRemoteRepository(boolean remoteRepository) {
        this.remoteRepository = remoteRepository;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocalRepositoryPath() {
        return localRepositoryPath;
    }

    public void setLocalRepositoryPath(String localRepositoryPath) {
        this.localRepositoryPath = localRepositoryPath;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTagPrefix() {
        return tagPrefix;
    }

    public void setTagPrefix(String tagPrefix) {
        this.tagPrefix = tagPrefix;
    }

    public Integer getListenerTimerPeriod() {
        return listenerTimerPeriod;
    }

    public void setListenerTimerPeriod(Integer listenerTimerPeriod) {
        this.listenerTimerPeriod = listenerTimerPeriod;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getFailedAuthenticationSeconds() {
        return failedAuthenticationSeconds;
    }

    public void setFailedAuthenticationSeconds(int failedAuthenticationSeconds) {
        this.failedAuthenticationSeconds = failedAuthenticationSeconds;
    }

    public Integer getMaxAuthenticationAttempts() {
        return maxAuthenticationAttempts;
    }

    public void setMaxAuthenticationAttempts(Integer maxAuthenticationAttempts) {
        this.maxAuthenticationAttempts = maxAuthenticationAttempts;
    }

    public String getNewBranchTemplate() {
        return newBranchTemplate;
    }

    public void setNewBranchTemplate(String newBranchTemplate) {
        this.newBranchTemplate = newBranchTemplate;
    }

    public String getNewBranchRegex() {
        return newBranchRegex;
    }

    public void setNewBranchRegex(String newBranchRegex) {
        this.newBranchRegex = newBranchRegex;
    }

    public String getNewBranchRegexError() {
        return newBranchRegexError;
    }

    public void setNewBranchRegexError(String newBranchRegexError) {
        this.newBranchRegexError = newBranchRegexError;
    }

    public String getProtectedBranches() {
        return protectedBranches;
    }

    public void setProtectedBranches(String protectedBranches) {
        this.protectedBranches = protectedBranches;
    }

    @Override
    protected void store(PropertiesHolder propertiesHolder) {
        super.store(propertiesHolder);

        boolean clearLogin = StringUtils.isEmpty(login);

        if (isRemoteRepository()) {
            propertiesHolder.setProperty(URI, uri);
        } else {
            propertiesHolder.setProperty(URI, "");
            clearLogin = true;
        }

        if (clearLogin) {
            propertiesHolder.setProperty(LOGIN, "");
            propertiesHolder.setProperty(PASSWORD, "");
        } else {
            propertiesHolder.setProperty(LOGIN, getLogin());
            propertiesHolder.setProperty(PASSWORD, getPassword());
        }

        propertiesHolder.setProperty(LOCAL_REPOSITORY_PATH, localRepositoryPath);
        propertiesHolder.setProperty(BRANCH, branch);
        propertiesHolder.setProperty(NEW_BRANCH_TEMPLATE, newBranchTemplate);
        propertiesHolder.setProperty(NEW_BRANCH_REGEX, newBranchRegex);
        propertiesHolder.setProperty(NEW_BRANCH_REGEX_ERROR, newBranchRegexError);
        propertiesHolder.setProperty(TAG_PREFIX, tagPrefix);
        propertiesHolder.setProperty(LISTENER_TIMER_PERIOD, listenerTimerPeriod);
        propertiesHolder.setProperty(CONNECTION_TIMEOUT, connectionTimeout);
        propertiesHolder.setProperty(FAILED_AUTHENTICATION_SECONDS, failedAuthenticationSeconds);
        propertiesHolder.setProperty(MAX_AUTHENTICATION_ATTEMPTS, maxAuthenticationAttempts);
        propertiesHolder.setProperty(PROTECTED_BRANCHES, protectedBranches);
    }

    @Override
    protected void revert(PropertiesHolder properties) {
        super.revert(properties);

        properties.revertProperties(URI,
            LOGIN,
            PASSWORD,
            LOCAL_REPOSITORY_PATH,
            BRANCH,
            NEW_BRANCH_TEMPLATE,
            NEW_BRANCH_REGEX,
            NEW_BRANCH_REGEX_ERROR,
            TAG_PREFIX,
            LISTENER_TIMER_PERIOD,
            PROTECTED_BRANCHES);
        load(properties);
    }

    @Override
    public RepositorySettingsValidators getValidators() {
        return new GitRepositorySettingsValidators();
    }
}
