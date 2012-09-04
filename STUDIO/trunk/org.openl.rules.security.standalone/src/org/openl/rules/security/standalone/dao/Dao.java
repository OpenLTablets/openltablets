package org.openl.rules.security.standalone.dao;

import java.util.List;

import org.openl.rules.security.standalone.persistence.PersistentObject;

/**
 * Base interface to be implemented by DAO interfaces. All Dao interfaces (like
 * UserDao, etc) should extend this interface.
 *
 * @author Andrey Naumenko
 */
public interface Dao<T extends PersistentObject> {
    /**
     * Checks whether given object can be deleted. i.e. has no non-deletable
     * relations in DB.
     *
     * @param obj object to delete
     *
     * @return <code>true</code>
     */
    boolean canBeDeleted(T obj);

    /**
     * Delete given object.
     *
     * @param obj object to delete.
     */
    void delete(T obj);

    /**
     * Gets object by primary id.
     *
     * @param id primary id
     *
     * @return object with given id or <code>null</code> if it can not be
     *         found.
     */
    Object getById(Long id);

    List<T> getAll();

    /**
     * Load object by primary id.
     *
     * @param id primary id
     *
     * @return object with given id or throws runtime exception if object is not
     *         found.
     */
    Object loadById(Long id);

    /**
     * Saves object.
     *
     * @param obj object to save.
     */
    void save(T obj);

    /**
     * Saves or updates (necessary operation is determined automatically, see
     * hibernate documentation for more details).
     *
     * @param obj object to save/update
     */
    void saveOrUpdate(T obj);

    /**
     * Updates object.
     *
     * @param obj object to update.
     */
    void update(T obj);
}
