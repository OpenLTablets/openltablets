package org.openl.util.filter;

/**
 * <code>Filter</code> that accepts all the objects.
 * 
 * @author Aliaksandr Antonik.
 */
public class AllFilter<T> extends BaseFilter<T> {
    /**
     * Returns <code>true</code> for any object, including <code>null</code>.
     * 
     * @param obj object to check
     * @return true
     */
    public boolean select(T obj) {
        return true;
    }

    /**
     * Returns <code>true</code> for any class, including <code>null</code>.
     * 
     * @param aClass a class to check
     * @return true
     */
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
