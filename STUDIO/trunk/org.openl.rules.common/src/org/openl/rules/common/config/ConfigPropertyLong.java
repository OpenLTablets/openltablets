package org.openl.rules.common.config;

/**
 * @author Aleh Bykhavets
 */
public class ConfigPropertyLong extends ConfigProperty<Long> {
    public ConfigPropertyLong(String name, Long defValue) {
        super(name, defValue);
    }

    protected void setTextValue(String s) {
        setValue(Long.parseLong(s, 10));
    }
}
