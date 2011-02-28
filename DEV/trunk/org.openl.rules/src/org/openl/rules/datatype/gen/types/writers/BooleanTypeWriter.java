package org.openl.rules.datatype.gen.types.writers;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.openl.rules.datatype.gen.FieldDescription;

public class BooleanTypeWriter extends CommonTypeWriter {
    
    @Override
    public int writeFieldValue(MethodVisitor methodVisitor, FieldDescription fieldType) {
        methodVisitor.visitInsn(getValueForBoolean((Boolean)fieldType.getDefaultValue()));
        return 2;
    }
    
    private int getValueForBoolean(Boolean defaultValue) {
        if (defaultValue.equals(Boolean.TRUE)) {
            return Opcodes.ICONST_1;
        } 
        return Opcodes.ICONST_0;
    }
}
