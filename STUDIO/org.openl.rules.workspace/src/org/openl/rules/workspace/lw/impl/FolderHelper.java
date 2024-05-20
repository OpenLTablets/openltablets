package org.openl.rules.workspace.lw.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.project.model.Module;

/**
 * Folder (File System) Helper for Local Workspace.
 *
 * @author Aleh Bykhavets
 */
public final class FolderHelper {

    public static final String PROPERTIES_FOLDER = ".studioProps";
    public static final String HISTORY_FOLDER = ".history";

    private FolderHelper() {
    }

    /**
     * Clears the folder's content. The folder itself will not be deleted. If you want to delete a folder itself, use
     * {@link #deleteFolder(File)} instead
     *
     * @param folder folder, which content will be deleted
     * @return true if all content is deleted and false if at least one file or sub-folder cannot be deleted.
     */
    public static boolean clearFolder(File folder) {
        final Logger log = LoggerFactory.getLogger(FolderHelper.class);
        log.debug("Clearing folder ''{}''", folder);

        File[] files = folder.listFiles();

        if (files == null) {
            return true;
        }

        boolean failures = false;

        // delete sub elements one by one
        for (File file : files) {
            if (file.isFile()) {
                if (!file.delete()) {
                    failures = true;
                    log.debug("Failed to delete file ''{}''.", file.getAbsolutePath());
                }
            } else {
                if (!deleteFolder(file)) {
                    failures = true;
                }
            }
        }

        return !failures;
    }

    /**
     * Deletes the folder and it's content recursively. If you do not want to delete a folder itself, use
     * {@link #clearFolder(File)} instead
     *
     * @param folder the folder, that will be deleted
     * @return true if the folder is deleted and false if the folder cannot be deleted.
     */
    private static boolean deleteFolder(File folder) {
        final Logger log = LoggerFactory.getLogger(FolderHelper.class);
        log.debug("Deleting folder ''{}''", folder);

        boolean failures = false;
        if (folder.isDirectory()) {
            // list sub elements
            File[] files = folder.listFiles();

            if (files == null) {
                // Some I/O error occurs
                return false;
            }

            // delete one by one
            for (File f : files) {
                if (f.isDirectory()) {
                    // recursive
                    if (!deleteFolder(f)) {
                        failures = true;
                    }
                } else {
                    // delete file
                    if (!f.delete()) {
                        failures = true;
                        log.debug("Failed to delete file ''{}''.", f.getAbsolutePath());
                    }
                }
            }

            if (!folder.delete()) {
                failures = true;
                log.debug("Failed to delete folder ''{}''.", folder.getAbsolutePath());
            }
        }

        return !failures;
    }

    public static String resolveHistoryFolder(RulesProject project, Module module) {
        return project != null ? normalizePath(Paths.get(project.getFolderPath(), resolveHistoryFolder(module))): null;
    }

    public static String resolveHistoryFolder(Module module) {
        return normalizePath(Paths.get(HISTORY_FOLDER, module.getRulesRootPath().getPath()));
    }

    private static String normalizePath(Path p) {
        return p.toString().replace('\\', '/');
    }
}
