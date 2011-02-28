package org.openl.rules.datatype.gen.types.writers;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.openl.rules.datatype.gen.FieldDescription;

public class DoubleTypeWriter implements TypeWriter {

    public int getConstantForVarInsn() {
        return Opcodes.DLOAD;
    }

    public int getConstantForReturn() {
        return Opcodes.DRETURN;
    }

    public int writeFieldValue(MethodVisitor methodVisitor, FieldDescription fieldType) {
        methodVisitor.visitLdcInsn(fieldType.getDefaultValue()); 
        return 3;
    }

}
