/*
 * Created on May 29, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author snshor
 *
 */
public class CategorizedMap {

    static class Category {
        Category parent = null;

        int parentDistance = 0;

        String category;

        Category(String category) {
            this.category = category;
        }

        @Override
        public boolean equals(Object obj) {
            return category.equals(((Category) obj).category);
        }

        /**
         * @return
         */
        public String getCategory() {
            return category;
        }

        /**
         * @return
         */
        public Category getParent() {
            return parent;
        }

        /**
         * @return
         */
        public int getParentDistance() {
            return parentDistance;
        }

        @Override
        public int hashCode() {
            return category.hashCode();
        }

        /**
         * @param category
         */
        public void setParent(Category category) {
            parent = category;
        }

        /**
         * @param i
         */
        public void setParentDistance(int i) {
            parentDistance = i;
        }

    }
    protected HashMap<String, Category> categories = new HashMap<String, Category>();

    protected HashMap<String, Object> all = new HashMap<String, Object>();

    Object findByCategory(String str) {
        Category c = getCategory(str);
        while ((c = c.parent) != null) {
            Object res = all.get(c.category);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return getCategorized((String) key);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.env.IResourceProvider#getResource(java.lang.Object)
     */
    public Object getCategorized(String category) {

        Object res = all.get(category);
        return res != null ? res : findByCategory(category);
    }

    public synchronized Category getCategory(String cc) {
        Category c = categories.get(cc);
        if (c == null) {
            c = new Category(cc);
            setParent(c);
            categories.put(cc, c);

            if (c.parent != null) {
                reassignParents(c.parentDistance, c.parent);
            } else {
                reassignParents(-1, null);
            }
        }

        return c;

    }

    public Object put(Object key, Object value) {
        return putCategorized((String) key, value);
    }

    public Object putCategorized(String category, Object value) {
        if (!all.containsKey(category)) {
            getCategory(category);
        }
        return all.put(category, value);
    }

    protected synchronized void reassignParents(int parentDistance, Category parent) {
        for (Iterator<Category> iter = categories.values().iterator(); iter.hasNext();) {
            Category c = iter.next();

            if (c.parent == parent && c.parentDistance > parentDistance) {
                setParent(c);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */

    protected synchronized void setParent(Category cc) {
        String search = cc.category;

        for (int i = 1;; ++i) {
            int index = search.lastIndexOf('.');
            if (index < 0) {
                break;
            }
            search = search.substring(0, index);
            Category parent = categories.get(search);
            if (parent != null) {
                cc.setParentDistance(i);
                cc.setParent(parent);
                return;
            }
        }
        cc.parent = null;
        cc.parentDistance = 0;
    }

    public Collection<Object> values() {
        return all.values();
    }

}
