/*
 * Created on Apr 30, 2004
 *
 * Developed by OpenRules Inc 2003-2004
 */
package org.openl.domain;

import java.util.HashMap;

/**
 * @author snshor
 */
public class Enum<T> {

    T[] allObjects;

    public HashMap<T, Integer> indexMap;

    public Enum(T[] objs) {
        this.allObjects = objs;
        indexMap = new HashMap<T, Integer>(objs.length);

        for (int i = 0; i < objs.length; i++) {
            indexMap.put(objs[i], new Integer(i));
        }
    }

    public boolean contains(Object obj) {
        return indexMap.containsKey(obj);
    }

    // public Enum(Collection<T> objc)
    // {
    // int size = objc.size();
    //
    // this.allObjects = new Object[size];
    // this.indexMap = new HashMap(size);
    //
    // int i = 0;
    //
    // for (Iterator iter = objc.iterator(); iter.hasNext(); ++i)
    // {
    // Object element = (Object) iter.next();
    // indexMap.put(element, new Integer(i));
    // }
    // }

    public int getIndex(T obj) {
        Integer idx = indexMap.get(obj);
        if (idx == null)
            throw new RuntimeException("Object " + obj + " is outside of a valid domain");
        return idx.intValue();
    }

    /**
     * 
     */
    public int size() {
        return allObjects.length;
    }

    public T[] getAllObjects() {
        return allObjects;
    }

}
