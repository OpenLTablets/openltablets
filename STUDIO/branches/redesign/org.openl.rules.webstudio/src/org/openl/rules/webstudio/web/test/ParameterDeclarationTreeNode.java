package org.openl.rules.webstudio.web.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.openl.rules.testmethod.ParameterWithValueDeclaration;
import org.openl.types.IOpenClass;
import org.openl.types.IParameterDeclaration;
import org.richfaces.model.TreeNode;

public abstract class ParameterDeclarationTreeNode extends ParameterWithValueDeclaration implements TreeNode {

    private ParameterDeclarationTreeNode parent;
    private LinkedHashMap<Object, ParameterDeclarationTreeNode> children;

    public ParameterDeclarationTreeNode(String fieldName,
            Object value,
            IOpenClass fieldType,
            ParameterDeclarationTreeNode parent) {
        super(fieldName, value, fieldType, IParameterDeclaration.IN);
        this.parent = parent;
    }

    public ParameterDeclarationTreeNode(ParameterWithValueDeclaration paramDescription, ParameterDeclarationTreeNode parent) {
        this(paramDescription.getName(), paramDescription.getValue(), paramDescription.getType(), parent);
    }

    public ParameterDeclarationTreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return getChildernMap().isEmpty();
    }

    public abstract String getDisplayedValue();

    public boolean isValueNull() {
        return getValue() == null;
    }

    public boolean isElementOfCollection() {
        return parent instanceof CollectionParameterTreeNode;
    }

    public void setValueForced(Object value) {
        setValue(value);
        reset();
    }
    
    public Object getValueForced(){
        if(isValueNull()){
            return null;
        }else{
            return constructValueInternal();
        }
    }
    
    protected abstract Object constructValueInternal();

    public abstract String getNodeType();

    public String getTreeText() {
        StringBuilder buff = new StringBuilder();
        if (getName() != null) {
            buff.append(getName());
            buff.append(" = ");
        }
        if (isValueNull()) {
            buff.append("null");
        } else {
            buff.append(getDisplayedValue());
        }
        return buff.toString();
    }
    
    public void reset(){
        children = null;
    }

    protected LinkedHashMap<Object, ParameterDeclarationTreeNode> getChildernMap(){
        if(children == null){
            children = initChildernMap();
        }
        return children;
    }

    protected abstract LinkedHashMap<Object, ParameterDeclarationTreeNode> initChildernMap();
    
    @Override
    public void addChild(Object key, TreeNode node) {
        if(node instanceof ParameterDeclarationTreeNode){
            getChildernMap().put(key, (ParameterDeclarationTreeNode)node);
        }
    }
    
    @Override
    public ParameterDeclarationTreeNode getChild(Object key) {
        return getChildernMap().get(key);
    }
    
    public Collection<ParameterDeclarationTreeNode> getChildren(){
        return getChildernMap().values();
    }
    
    @Override
    public Iterator<Object> getChildrenKeysIterator() {
        return getChildernMap().keySet().iterator();
    }

    @Override
    public int indexOf(Object key) {
        Iterator<Object> keysIterator = getChildrenKeysIterator();
        int i = 0;
        while(keysIterator.hasNext()){
            if(keysIterator.next() == key){
                return i;
            }else{
                i++;
            }
        }
        return -1;
    }

    @Override
    public void insertChild(int index, Object key, TreeNode node) {
        addChild(key, node);
    }

    @Override
    public void removeChild(Object key) {
        getChildernMap().remove(key);
    }

}