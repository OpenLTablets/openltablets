package org.openl.rules.project.xml.v5_13;

import org.openl.rules.project.model.v5_13.ModuleType_v5_13;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ModuleTypeConverter_v5_13 implements SingleValueConverter {

    @Override
    public String toString(Object obj) {
        return obj.toString().toLowerCase();
    }

    @Override
    public Object fromString(String name) {
        return ModuleType_v5_13.valueOf(name.toUpperCase());
    }

    @Override
    public boolean canConvert(Class type) {
        return type == ModuleType_v5_13.class;
    }

}