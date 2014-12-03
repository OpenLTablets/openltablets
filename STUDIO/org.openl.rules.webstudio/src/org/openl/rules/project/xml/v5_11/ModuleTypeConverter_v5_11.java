package org.openl.rules.project.xml.v5_11;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.openl.rules.project.model.v5_11.ModuleType_v5_11;

public class ModuleTypeConverter_v5_11 implements SingleValueConverter {

    public String toString(Object obj) {
        return obj.toString().toLowerCase();
    }

    public Object fromString(String name) {
        return ModuleType_v5_11.valueOf(name.toUpperCase());
    }

    public boolean canConvert(Class type) {
        return type.equals(ModuleType_v5_11.class);
    }

}