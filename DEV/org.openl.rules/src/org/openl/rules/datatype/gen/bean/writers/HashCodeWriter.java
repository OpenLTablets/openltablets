package org.openl.rules.datatype.gen.bean.writers;

import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openl.rules.datatype.gen.ByteCodeGeneratorHelper;
import org.openl.rules.datatype.gen.FieldDescription;

public class HashCodeWriter extends MethodWriter {
    
    /**
     * 
     * @param beanNameWithPackage name of the class being generated with package, symbol '/' is used as separator<br> 
     * (e.g. <code>my/test/TestClass</code>)
     * @param allFields collection of fields for current class and parent`s ones.
     */
    public HashCodeWriter(String beanNameWithPackage, Map<String, FieldDescription> allFields) {
        super(beanNameWithPackage, allFields);
    }
 
    public void write(ClassWriter classWriter) {
        MethodVisitor methodVisitor;
        methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "hashCode", String.format("()%s",
                ByteCodeGeneratorHelper.getJavaType(int.class)), null, null);

        // create HashCodeBuilder
        methodVisitor.visitTypeInsn(Opcodes.NEW, Type.getInternalName(HashCodeBuilder.class));
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(HashCodeBuilder.class), "<init>",
                "()V");

        // generating hash code by fields
        for (Map.Entry<String, FieldDescription> field : getAllFields().entrySet()) {
            pushFieldToStack(methodVisitor, 0, field.getKey());
            ByteCodeGeneratorHelper.invokeVirtual(methodVisitor, HashCodeBuilder.class, "append",
                    new Class<?>[] { field.getValue().getType() });
        }
        ByteCodeGeneratorHelper.invokeVirtual(methodVisitor, HashCodeBuilder.class, "toHashCode", new Class<?>[] {});
        
        methodVisitor.visitInsn(ByteCodeGeneratorHelper.getConstantForReturn(int.class));
        if (getTwoStackElementFieldsCount() > 0) {
            methodVisitor.visitMaxs(3, 1);
        } else {
            methodVisitor.visitMaxs(2, 2);
        }

    }

}
