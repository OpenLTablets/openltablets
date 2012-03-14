/**
 * Created Jul 21, 2007
 */
package org.openl.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author snshor
 *
 *
 * IntervalMap allows for semi-open interval operations. The put operations are
 * in terms of intervals and get operations in terms of single elements. The
 * algorithm is based on splitting intervals on non-intersecting segments and
 * treating them separately. Semi-open intervals are chosen to coincide with
 * TreeMap implementation
 *
 */

/**
 *
 * 0123456789 uz N p p u u u
 *	
 *	Divide the coordinate line by dots (the beginning and the end of the interval).
 *	And assign the value to the beginning(means that the left border is included, while right not).	
 *	
 *	Example: if call the method {@link #putInterval(11, 23, "AnyValue")}, the result will be:
 *
 *	_____11("AnyValue")______________23______________ - means that value "AnyValue" belongs to [11;22].
 *
 * If then call the method {@link #putInterval(11, 50, "Yo")} the result will be:
 * 	_____11("AnyValue", "Yo")______________23("Yo")____________________50___ - means that value "AnyValue" is the same, and "Yo" belongs to [11;49]
 *
 *	@author DLiauchuk
 */

public class IntervalMap<T, V> {

    private SortedMap<Comparable<T>, List<V>> map = new TreeMap<Comparable<T>, List<V>>();

    public List<V> getInInterval(Comparable<T> key) {
        List<V> res = map.get(key);
        if (res != null) {
            return res;
        }

        SortedMap<Comparable<T>, List<V>> submap = map.headMap(key);

        if (submap.isEmpty()) {
            return Collections.emptyList();
        }
        return submap.get(submap.lastKey());
    }

    public void putInterval(Comparable<T> fromKey, Comparable<T> toKey, V value) {

        SortedMap<Comparable<T>, List<V>> submap = map.subMap(fromKey, toKey);

        if (submap.isEmpty() || !submap.firstKey().equals(fromKey)) {
            SortedMap<Comparable<T>, List<V>> head = map.headMap(fromKey);
            ArrayList<V> firstList = head.isEmpty() ? new ArrayList<V>() : new ArrayList<V>(head.get(head.lastKey()));
            map.put(fromKey, firstList);

            submap = map.subMap(fromKey, toKey);
        }

        if (!map.containsKey(toKey)) {
            ArrayList<V> lastList = new ArrayList<V>(submap.get(submap.lastKey()));
            map.put(toKey, lastList);
        }

        for (Iterator<Map.Entry<Comparable<T>, List<V>>> iter = submap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Comparable<T>, List<V>> element = iter.next();

            element.getValue().add(value);
        }
    }
    
    public boolean removeInterval(Comparable<T> fromKey, Comparable<T> toKey, V value) {
        SortedMap<Comparable<T>, List<V>> submap = map.subMap(fromKey, toKey);
        
        if (submap.isEmpty())
        	throw new RuntimeException("Interval not found! " + fromKey + " - " + toKey);        

        if (!submap.firstKey().equals(fromKey))
        	throw new RuntimeException("Interval should start with " + toKey);
        
        List<Comparable<T>> removedKeys = new ArrayList<Comparable<T>>();
        
        for (Map.Entry<Comparable<T>, List<V>> e : submap.entrySet()) {
			boolean removed = e.getValue().remove(value);
			if (removed == false)
				throw new RuntimeException("Value not found: " + value + " at the Key: " + e.getKey());
			
			if (e.getValue().isEmpty())
				removedKeys.add(e.getKey());
		}
        
        return true;
    }    

    public SortedMap<Comparable<T>, List<V>> getMap() {
        return map;
    }

}
