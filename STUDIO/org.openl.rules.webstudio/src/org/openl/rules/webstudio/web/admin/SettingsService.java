package org.openl.rules.webstudio.web.admin;

import java.io.IOException;

public interface SettingsService {

    /**
     * Load settings
     *
     * @param settings settings to load
     */
    void load(SettingsHolder settings);

    /**
     * Save settings to the memory storage
     *
     * @param settings settings to save
     */
    void store(SettingsHolder settings);

    /**
     * Apply all saved settings to the application
     *
     * @throws IOException if an I/O error occurs
     */
    void commit() throws IOException;

}
