package org.openl.rules.repository;

import java.util.List;

import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * OpenL Rules Folder.
 * It can have sub folders and files.
 * Sub folders and files are treated separately.
 *
 * @author Aleh Bykhavets
 *
 */
public interface RFolder extends REntity {
    /**
     * Gets list of sub folders.
     * It returns direct descendants only.
     *
     * @return list of sub folders.
     */
    public List<RFolder> getFolders() throws RRepositoryException;

    /**
     * Gets list of files from the folder.
     *
     * @return list of files
     */
    public List<RFile> getFiles() throws RRepositoryException;

    /**
     * Deletes the folder, sub folders and all files.
     * <p>
     * Root folder cannot be deleted.
     * Still, on delete it removes all its content, i.e. sub folders and all files.
     * 
     * @throws RDeleteException
     */
    public void delete() throws RRepositoryException;

    /**
     * Creates sub folder to the folder.
     *
     * @param name name of new folder
     * @return newly created folder
     * @throws RModifyException if failed
     */
    public RFolder createFolder(String name) throws RRepositoryException;

    /**
     * Creates file to the folder.
     *
     * @param name name of new file
     * @return newly created file
     * @throws RModifyException if failed
     */
    public RFile createFile(String name) throws RRepositoryException;
}
