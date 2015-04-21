package org.openl.rules.webstudio.web.test;

import org.openl.base.INameSpacedThing;
import org.openl.rules.testmethod.ParameterWithValueDeclaration;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;
import org.openl.vm.SimpleVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class ComplexParameterTreeNode extends ParameterDeclarationTreeNode {
    private final Logger log = LoggerFactory.getLogger(ComplexParameterTreeNode.class);
    public static final String COMPLEX_TYPE = "complex";
    private String valuePreview;

    public ComplexParameterTreeNode(String fieldName, Object value, IOpenClass fieldType, ParameterDeclarationTreeNode parent, String valuePreview) {
        super(fieldName, value, fieldType, parent);
        this.valuePreview = valuePreview;
    }

    public ComplexParameterTreeNode(ParameterWithValueDeclaration paramDescription, ParameterDeclarationTreeNode parent) {
        super(paramDescription, parent);
    }

    @Override
    public String getDisplayedValue() {
        String typeName = getType().getDisplayName(INameSpacedThing.SHORT);
        return valuePreview == null ? typeName : typeName + " (" + valuePreview + ")";
    }

    @Override
    public String getNodeType() {
        return COMPLEX_TYPE;
    }

    @Override
    protected LinkedHashMap<Object, ParameterDeclarationTreeNode> initChildrenMap() {
        if (isValueNull()) {
            return new LinkedHashMap<Object, ParameterDeclarationTreeNode>();
        } else {
            LinkedHashMap<Object, ParameterDeclarationTreeNode> fields = new LinkedHashMap<Object, ParameterDeclarationTreeNode>();
            IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
            for (Entry<String, IOpenField> fieldEntry : getType().getFields().entrySet()) {
                IOpenField field = fieldEntry.getValue();
                if (!field.isConst()) {
                    String fieldName = fieldEntry.getKey();
                    Object fieldValue;
                    IOpenClass fieldType = field.getType();

                    try {
                        fieldValue = field.get(getValue(), env);
                    } catch (RuntimeException e) {
                        // Usually this can happen only in cases when TestResult is a OpenLRuntimeException.
                        // So this field usually doesn't have any useful information.
                        // For example, it can be NotSupportedOperationException in not implemented getters.
                        log.debug("Exception while trying to get a value of a field:", e);
                        fieldType = JavaOpenClass.getOpenClass(String.class);
                        fieldValue = "Exception while trying to get a value of a field: " + e;
                    }

                    String reference = getReferenceNameToParent(fieldValue, this, "this");
                    if (reference != null) {
                        // Avoid infinite loop because of cyclic references
                        fieldType = JavaOpenClass.getOpenClass(String.class);
                        fieldValue = reference;
                    }

                    fields.put(fieldName,
                            ParameterTreeBuilder.createNode(fieldType, fieldValue, fieldName, this));
                }
            }
            return fields;
        }
    }

    /**
     * Finds a reference of a field's value to any of it's parents or object
     * itself. If field value is not referenced to any of it's parents,
     * function will return null.
     *
     * @param fieldValue    field value
     * @param parentObject  object that contains a field
     * @param referenceName reference
     * @return reference name to a parent or null
     */
    private String getReferenceNameToParent(Object fieldValue, ParameterDeclarationTreeNode parentObject,
                                            String referenceName) {
        // Check reference, not value - that's why "==" instead of "equals".
        if (parentObject.getValue() == fieldValue) {
            return referenceName;
        }

        if (parentObject.getParent() == null) {
            return null;
        }

        return getReferenceNameToParent(fieldValue, parentObject.getParent(), referenceName + ".parent");
    }

    @Override
    protected Object constructValueInternal() {
        Object value = getValue();

        IRuntimeEnv env = new SimpleVM().getRuntimeEnv();
        for (Entry<Object, ParameterDeclarationTreeNode> fieldEntry : getChildernMap().entrySet()) {
            if (!(fieldEntry.getValue() instanceof UnmodifiableParameterTreeNode)) {
                String fieldName = (String) fieldEntry.getKey();
                IOpenField field = getType().getField(fieldName);
                if (field.isWritable()) {
                    field.set(value, fieldEntry.getValue().getValueForced(), env);
                }
            }
        }
        return value;
    }
}
