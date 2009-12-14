/*
 * Created on Apr 30, 2004
 *
 * Developed by OpenRules Inc 2003-2004
 */
package org.openl.domain;

import java.util.BitSet;
import java.util.Iterator;

import org.openl.util.AOpenIterator;

/**
 * @author snshor
 */
public class EnumDomain<T> extends FixedSizeDomain<T> {

    class EnumDomainIterator extends AOpenIterator<T> {

        BitSetIterator bsi = new BitSetIterator(bits);

        /**
         *
         */

        public boolean hasNext() {
            return bsi.hasNext();
        }

        /**
         *
         */

        public T next() {
            int idx = bsi.nextInt();
            return enumeration.allObjects[idx];
        }

    }
    BitSet bits;

    Enum<T> enumeration;

    public EnumDomain(Enum<T> enumeration, BitSet bits) {
        this.bits = bits;
        this.enumeration = enumeration;
    }

    public EnumDomain(Enum<T> enumeration, T[] objs) {
        bits = new BitSet(enumeration.size());
        this.enumeration = enumeration;

        for (int i = 0; i < objs.length; i++) {
            int idx = enumeration.getIndex(objs[i]);
            bits.set(idx);
        }
    }

    public EnumDomain(T[] elements) {
        this(new Enum<T>(elements), elements);
    }

    /**
     *
     */

    public EnumDomain<T> and(EnumDomain<T> sd) {
        checkOperand(sd);

        // if (!bits.intersects(sd.bits))
        // {
        // return enum.empty();
        // }

        if (bits.equals(sd.bits)) {
            return this;
        }

        BitSet copy = (BitSet) bits.clone();
        copy.and(sd.bits);
        return new EnumDomain<T>(enumeration, copy);

    }

    void checkOperand(EnumDomain<T> sd) {

        if (sd.getEnum() != enumeration) {
            throw new RuntimeException("Can not use subsets of different domains");
        }

    }

    /**
     *
     */

    public boolean contains(T obj) {
        int idx = enumeration.getIndex(obj);
        return bits.get(idx);
    }

    /**
     *
     */

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EnumDomain)) {
            return false;
        }

        EnumDomain<T> ed = (EnumDomain<T>) obj;

        return enumeration.equals(ed.enumeration) && bits.equals(ed.bits);

    }

    public IType getElementType() {
        return null;
    }

    /**
     * @return
     */
    public Enum<T> getEnum() {
        return enumeration;
    }

    /**
     *
     */

    @Override
    public int hashCode() {
        return enumeration.hashCode() * 37 + bits.hashCode();
    }

    /**
     *
     */

    public Iterator<T> iterator() {
        return new EnumDomainIterator();
    }

    /**
     *
     */

    public EnumDomain<T> not() {
        int size = enumeration.size();

        BitSet bs = (BitSet) bits.clone();

        bs.flip(0, size);

        return new EnumDomain<T>(enumeration, bs);
    }

    /**
     *
     */

    public EnumDomain<T> or(EnumDomain<T> sd) {
        checkOperand(sd);

        if (bits.equals(sd.bits)) {
            return this;
        }

        BitSet copy = (BitSet) bits.clone();
        copy.or(sd.bits);
        return new EnumDomain<T>(enumeration, copy);
    }

    public boolean selectObject(T obj) {
        return contains(obj);
    }

    public boolean selectType(IType type) {
        // TODO Auto-generated method stub
        return false;
    }

    public int size() {
        return bits.cardinality();
    }

    /**
     *
     */

    public EnumDomain<T> sub(EnumDomain<T> sd) {
        checkOperand(sd);

        if (bits.equals(sd.bits)) {
            return this;
        }

        BitSet copy = (BitSet) bits.clone();
        copy.andNot(sd.bits);
        return new EnumDomain<T>(enumeration, copy);
    }

}
