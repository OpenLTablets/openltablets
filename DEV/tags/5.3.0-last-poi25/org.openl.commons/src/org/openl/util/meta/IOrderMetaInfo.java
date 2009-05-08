/*
 * Created on May 6, 2004
 *
 * Developed by OpenRules Inc 2003-2004
 */
package org.openl.util.meta;

import java.util.Comparator;

/**
 * @author snshor
 */
public interface IOrderMetaInfo {

    static class ComparableComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

    }

    public static class OrderedComparator implements Comparator {
        IOrderMetaInfo orderMetaInfo;

        public OrderedComparator(IOrderMetaInfo orderMetaInfo) {
            this.orderMetaInfo = orderMetaInfo;
        }

        /**
         *
         */

        public int compare(Object o1, Object o2) {
            Comparable c1 = orderMetaInfo.getOrderObject(o1);
            Comparable c2 = orderMetaInfo.getOrderObject(o2);
            return c1.compareTo(c2);
        }

    }

    public static final Comparator DEFAULT_COMPARATOR = new ComparableComparator();

    /**
     * Produces object that is used for comparison with other objects in
     * collection
     *
     * @param obj
     * @return
     */
    Comparable getOrderObject(Object obj);

}
