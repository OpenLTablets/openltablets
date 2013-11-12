package org.openl.rules.datatype.gen.bean.writers;

import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.openl.rules.datatype.gen.ByteCodeGeneratorHelper;
import org.openl.rules.datatype.gen.FieldDescription;

public abstract class MethodWriter extends DefaultBeanByteCodeWriter {
    
    public static final String VOID_CLASS_NAME = "void";
    
    /**
     * Number of fields that will take 2 stack elements(like a double and long)
     */
    private int twoStackElementFieldsCount;
    
    public MethodWriter(String beanNameWithPackage, Map<String, FieldDescription> allFields) {
        super(beanNameWithPackage, null, allFields);        
        this.twoStackElementFieldsCount = ByteCodeGeneratorHelper.getTwoStackElementFieldsCount(allFields);
    }

    protected String getBeanNameWithPackage() {
        return super.getBeanNameWithPackage();
    }

    protected Map<String, FieldDescription> getAllFields() {
        return getBeanFields();
    }

    protected int getTwoStackElementFieldsCount() {
        return twoStackElementFieldsCount;
    }
    
    protected void pushFieldToStack(MethodVisitor codeVisitor, int fieldOwnerLocalVarIndex, String fieldName) {
        codeVisitor.visitVarInsn(Opcodes.ALOAD, fieldOwnerLocalVarIndex);
        codeVisitor.visitFieldInsn(Opcodes.GETFIELD, getBeanNameWithPackage(), fieldName, ByteCodeGeneratorHelper.getJavaType(getAllFields()
                .get(fieldName)));
    }
    
    public static boolean containRestrictedSymbols(String fieldName) {
        /** regex for validating field names. Field name
         * may start from '_', any letter or '$' sign.
         * And may be followed by the described symbols and also by any number.
         */
        String regex = "^(_|[a-zA-Z]|\\$)(_|[a-zA-Z0-9]|\\$)*";
        
        return !fieldName.matches(regex);
    }
    
    /** Generate methods only for fields without restricted symbols.
    In future should be updated to use this fields too somehow*/
    protected boolean validField(String fieldName, FieldDescription fieldDescription) {
        return !fieldDescription.getCanonicalTypeName().equals(VOID_CLASS_NAME) && !containRestrictedSymbols(fieldName);
    }
    
}
