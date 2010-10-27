/*
 * Created on May 28, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.util;

/**
 * @author snshor
 * 
 */
public abstract class ASelector<T> implements ISelector<T> {

    static class AllSelector<T> extends ASelector<T> {

        public boolean select(T obj) {
            return true;
        }

    }

    static class ANDSelector<T> extends BoolBinSelector<T> {
        public ANDSelector(ISelector<T> sel1, ISelector<T> sel2) {
            super(sel1, sel2);
        }

        public boolean select(T obj) {
            if (sel1.select(obj)) {
                return sel2.select(obj);
            }
            return false;
        }
    }

    /**
     * 
     * @author snshor Base class for binary boolean operators
     */
    static abstract class BoolBinSelector<T> extends ASelector<T> {
        ISelector<T> sel1;
        ISelector<T> sel2;

        protected BoolBinSelector(ISelector<T> sel1, ISelector<T> sel2) {
            this.sel1 = sel1;
            this.sel2 = sel2;
        }

        @Override
        protected boolean equalsSelector(ASelector<T> sel) {
            BoolBinSelector<?> x = null;
            if (sel != null && sel instanceof BoolBinSelector<?>) {
                x = (BoolBinSelector<?>) sel;

            } else
                return false;
            return sel1.equals(x.sel1) && sel2.equals(x.sel2);
        }

        @Override
        protected int redefinedHashCode() {
            return sel1.hashCode() + sel2.hashCode();
        }

    }

    public static class ClassSelector extends ASelector<Object> {
        Class<?> c;

        public ClassSelector(Class<?> c) {
            this.c = c;
        }

        @Override
        protected boolean equalsSelector(ASelector<Object> sel) {
            return c == ((ClassSelector) (ASelector<Object>) sel).c;
        }

        @Override
        protected int redefinedHashCode() {
            return c.hashCode();
        }

        public boolean select(Object obj) {
            return c.isInstance(obj);
        }

    }

    public static abstract class IntValueSelector<T> extends ASelector<T> {
        int value;

        public IntValueSelector(int value) {
            this.value = value;
        }

        @Override
        protected boolean equalsSelector(ASelector<T> sel) {
            return ((IntValueSelector<?>) sel).value == value;
        }

        protected abstract int getIntValue(T test);

        @Override
        protected int redefinedHashCode() {
            return value;
        }

        public boolean select(T obj) {
            return value == getIntValue(obj);
        }

    }

    static class NoneSelector<T> extends ASelector<T> {

        public boolean select(T obj) {
            return false;
        }

    }

    static class NOTSelector<T> extends ASelector<T> {
        ISelector<T> is;

        public NOTSelector(ISelector<T> is) {
            this.is = is;
        }

        @Override
        protected boolean equalsSelector(ASelector<T> sel) {
            return is.equals(((NOTSelector<?>) sel).is);
        }

        @Override
        protected int redefinedHashCode() {
            return is.hashCode();
        }

        public boolean select(T obj) {
            return !is.select(obj);
        }

    }

    public static class ObjectSelector<T> extends ASelector<T> {
        T myobj;

        public ObjectSelector(T obj) {
            this.myobj = obj;
        }

        @Override
        protected boolean equalsSelector(ASelector<T> sel) {
            return select(((ObjectSelector<T>) sel).myobj);
        }

        @Override
        protected int redefinedHashCode() {
            return myobj == null ? 0 : myobj.hashCode();
        }

        public boolean select(T obj) {
            if (myobj == obj) {
                return true;
            }
            if (myobj == null) {
                return false;
            }
            return myobj.equals(obj);
        }

    }

    static class ORSelector<T> extends BoolBinSelector<T> {
        public ORSelector(ISelector<T> sel1, ISelector<T> sel2) {
            super(sel1, sel2);
        }

        public boolean select(T obj) {
            if (sel1.select(obj)) {
                return true;
            }
            return sel2.select(obj);
        }
    }

    public static class StringValueSelector<T> extends ASelector<T> {
        String value;
        AStringConvertor<T> convertor;

        public StringValueSelector(String value, AStringConvertor<T> convertor) {
            this.value = value;
            this.convertor = convertor;
        }

        @Override
        protected boolean equalsSelector(ASelector<T> sel) {
            StringValueSelector<?> svs = (StringValueSelector<?>) sel;
            return value.equals(svs.value) && convertor.equals(svs.convertor);
        }

        @Override
        protected int redefinedHashCode() {
            return value.hashCode() * 37 + convertor.hashCode();
        }

        public boolean select(T obj) {
            return value.equals(convertor.getStringValue(obj));
        }
    }

    static class XORSelector<T> extends BoolBinSelector<T> {
        public XORSelector(ISelector<T> sel1, ISelector<T> sel2) {
            super(sel1, sel2);
        }

        public boolean select(T obj) {
            return sel1.select(obj) ^ sel2.select(obj);
        }
    }

    public static <T> ISelector<T> selectAll(@SuppressWarnings("unused")
    T obj) {
        return new AllSelector<T>();
    }

    public static ISelector<Object> selectClass(Class<?> c) {
        return new ClassSelector(c);
    }

    public static <T> ISelector<T> selectNone(@SuppressWarnings("unused")
    T obj) {
        return new NoneSelector<T>();
    }

    public static <T> ISelector<T> selectObject(T obj) {
        return new ObjectSelector<T>(obj);
    }

    public ISelector<T> and(ISelector<T> isel) {
        return new ANDSelector<T>(this, isel);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return equalsSelector((ASelector<T>) obj);
    }

    protected boolean equalsSelector(ASelector<T> sel) {
        return sel == this;
    }

    @Override
    public int hashCode() {
        return redefinedHashCode();
    }

    public ISelector<T> not() {
        return new NOTSelector<T>(this);
    }

    public ISelector<T> or(ISelector<T> isel) {
        return new ORSelector<T>(this, isel);
    }

    protected int redefinedHashCode() {
        return System.identityHashCode(this);
    }

    public ISelector<T> xor(ISelector<T> isel) {
        return new XORSelector<T>(this, isel);
    }

}
