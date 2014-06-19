package org.openl.rules.datatype.gen;

/**
 * Created by dl on 6/19/14.
 */
public interface FieldDescription {
    /**
     * Key word for the default value.
     * Some kind of the default value should be used when this word is found
     *
     */
    String DEFAULT_KEY_WORD = "_DEFAULT_";

    String getCanonicalTypeName();

    Class<?> getType();

    String getDefaultValueAsString();

    void setDefaultValueAsString(String defaultValueAsString);

    boolean isArray();

    Object getDefaultValue();

    boolean hasDefaultValue();

    boolean hasDefaultKeyWord();
}
