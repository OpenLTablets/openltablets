package org.openl.rules.webstudio.web.admin;

import java.util.Optional;

import org.openl.config.PropertiesHolder;

public class AWSS3RepositorySettings extends RepositorySettings {
    private String bucketName;
    private String regionName;
    private String accessKey;
    private String secretKey;
    private int listenerTimerPeriod;

    private final String BUCKET_NAME;
    private final String REGION_NAME;
    private final String ACCESS_KEY;
    private final String SECRET_KEY;
    private final String LISTENER_TIMER_PERIOD;

    AWSS3RepositorySettings(PropertiesHolder properties, String configPrefix) {
        super(properties, configPrefix);
        BUCKET_NAME = configPrefix + ".bucket-name";
        REGION_NAME = configPrefix + ".region-name";
        ACCESS_KEY = configPrefix + ".access-key";
        SECRET_KEY = configPrefix + ".secret-key";
        LISTENER_TIMER_PERIOD = configPrefix + ".listener-timer-period";

        load(properties);
    }

    private void load(PropertiesHolder properties) {
        bucketName = properties.getProperty(BUCKET_NAME);
        regionName = properties.getProperty(REGION_NAME);
        accessKey = properties.getProperty(ACCESS_KEY);
        secretKey = properties.getProperty(SECRET_KEY);
        listenerTimerPeriod = Integer.parseInt(Optional.ofNullable(properties.getProperty(LISTENER_TIMER_PERIOD)).orElse("0"));
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public int getListenerTimerPeriod() {
        // Convert to seconds
        return listenerTimerPeriod;
    }

    public void setListenerTimerPeriod(int listenerTimerPeriod) {
        // Convert to milliseconds
        this.listenerTimerPeriod = listenerTimerPeriod;
    }

    @Override
    protected void store(PropertiesHolder propertiesHolder) {
        super.store(propertiesHolder);

        propertiesHolder.setProperty(BUCKET_NAME, bucketName);
        propertiesHolder.setProperty(REGION_NAME, regionName);
        propertiesHolder.setProperty(ACCESS_KEY, accessKey);
        propertiesHolder.setProperty(SECRET_KEY, secretKey);
        propertiesHolder.setProperty(LISTENER_TIMER_PERIOD, listenerTimerPeriod);
    }

    @Override
    protected void revert(PropertiesHolder properties) {
        super.revert(properties);

        properties.revertProperties(BUCKET_NAME, REGION_NAME, ACCESS_KEY, SECRET_KEY, LISTENER_TIMER_PERIOD);
        load(properties);
    }

    @Override
    public void copyContent(RepositorySettings other) {
        super.copyContent(other);

        if (other instanceof AWSS3RepositorySettings) {
            AWSS3RepositorySettings otherSettings = (AWSS3RepositorySettings) other;
            setBucketName(otherSettings.getBucketName());
            setRegionName(otherSettings.getRegionName());
            setAccessKey(otherSettings.getAccessKey());
            setSecretKey(otherSettings.getSecretKey());
            setListenerTimerPeriod(otherSettings.getListenerTimerPeriod());
        }
    }
}
