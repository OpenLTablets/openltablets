package org.openl.meta;

import org.openl.source.IOpenSourceCodeModule;
import org.openl.source.impl.StringSourceCodeModule;
import org.openl.source.impl.URLSourceCodeModule;
import org.openl.util.ArrayTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;

public class StringValue implements IMetaHolder, CharSequence, Comparable<StringValue> {
    private final Logger log = LoggerFactory.getLogger(StringValue.class);
    private ValueMetaInfo metaInfo;
    private String value;

    public StringValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Error initializing StringValue class. Parameter \"value\" can't be null.");
        }
        this.value = value;
        metaInfo = new ValueMetaInfo();
    }

    public StringValue(String value, String shortName, String fullName, IOpenSourceCodeModule source) {
        if (value == null) {
            throw new IllegalArgumentException("Error initializing StringValue class. Parameter \"value\" can't be null.");
        }
        this.value = value;
        metaInfo = new ValueMetaInfo(shortName, fullName, source);
    }

    public IOpenSourceCodeModule asSourceCodeModule() {
        return new StringSourceCodeModule(value, getMetaInfo().getSourceUrl());
    }

    /**
     * Returns a character at position 'index' of current StringValue variable
     */
    public char charAt(int index) {
        return value.charAt(index);
    }

    /**
     * Compares StringValue v with current StringValue variable
     */
    public int compareTo(StringValue v) {
        return value.compareTo(v.value);
    }

    @Override
    /**
     * Indicates whether some other object is "equal to" this org.openl.meta.IntValue variable. 
     */
    public boolean equals(Object obj) {

        if (obj instanceof StringValue) {
            StringValue v = (StringValue) obj;
            return value.equals(v.value);
        }
        if (obj instanceof String) {
            String s = (String) obj;
            return value.equals(s);
        }

        return false;
    }

    /**
     * Returns the metainfo of current StringValue variable
     */
    public ValueMetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
     * @return the value of current StringValue variable
     */
    public String getValue() {
        return value;
    }

    @Override
    /**
     * Returns the hash code of the value
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * @return true if value is empty, and false if not
     */
    public boolean isEmpty() {
        return value.trim().length() == 0;
    }

    /**
     * Return the length of the value
     */
    public int length() {
        return value.length();
    }

    /**
     * Sets the metainfo for the value
     */
    public void setMetaInfo(IMetaInfo metaInfo) {
        if (metaInfo instanceof ValueMetaInfo) {
            setMetaInfo((ValueMetaInfo) metaInfo);
        } else {
            try {
                ValueMetaInfo valueMetaInfo = new ValueMetaInfo(metaInfo.getDisplayName(IMetaInfo.SHORT),
                        metaInfo.getDisplayName(IMetaInfo.LONG),
                        new URLSourceCodeModule(new URL(metaInfo.getSourceUrl())));
                setMetaInfo(valueMetaInfo);
            } catch (Exception e) {
                log.debug("Failed to set meta info for StringValue \"{}\"", value, e);
                setMetaInfo((ValueMetaInfo) null);
            }
        }
    }

    /**
     * Sets the metainfo for the value
     */
    public void setMetaInfo(ValueMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Sets a value for the current StringValue variable
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     */
    public CharSequence subSequence(int start, int end) {
        return value.subSequence(start, end);
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Sorts the StringValue array
     *
     * @param values array for sorting
     * @return the sorted array
     */
    public static StringValue[] sort(StringValue[] values) {
        StringValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new StringValue[values.length];
            StringValue[] notNullArray = ArrayTool.removeNulls(values);

            Arrays.sort(notNullArray);

            /* Filling sortedArray by sorted and null values */
            for (int i = 0; i < notNullArray.length; i++) {
                sortedArray[i] = notNullArray[i];
            }
        }
        return sortedArray;
    }

}
