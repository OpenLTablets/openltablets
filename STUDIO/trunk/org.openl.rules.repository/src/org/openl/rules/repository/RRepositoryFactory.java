package org.openl.rules.repository;

import org.openl.rules.common.config.ConfigSet;
import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * Interface for concrete repository factories.
 * 
 * @author Aleh Bykhavets
 *
 */
public interface RRepositoryFactory {
    /**
     * Gets new instance of JCR Repository.
     *
     * @return new instance of JCR Repository
     * @throws RRepositoryException if failed
     */
    public RRepository getRepositoryInstance() throws RRepositoryException;

    /**
     * Initialize factory.
     *
     * @param confSet configuration data
     * @throws RRepositoryException if failed
     */
    public void initialize(ConfigSet confSet) throws RRepositoryException;

    void release() throws RRepositoryException;
}
