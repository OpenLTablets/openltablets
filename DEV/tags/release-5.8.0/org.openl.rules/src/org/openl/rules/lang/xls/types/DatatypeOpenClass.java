/*
 * Created on Jul 25, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.lang.xls.types;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openl.types.IAggregateInfo;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenSchema;
import org.openl.types.impl.ADynamicClass;
import org.openl.types.impl.DynamicArrayAggregateInfo;
import org.openl.types.impl.MethodSignature;
import org.openl.types.java.JavaOpenClass;
import org.openl.util.AOpenIterator;
import org.openl.vm.IRuntimeEnv;

/**
 * Open class for types represented as datatype table components in openl.
 * 
 * @author snshor
 *
 */
public class DatatypeOpenClass extends ADynamicClass {
    
    private static final Log LOG = LogFactory.getLog(DatatypeOpenClass.class);
    
    private IOpenClass superClass;
    
    /**
     * see {@link #getPackageName()}
     */
    private String packageName;
    
    public DatatypeOpenClass(IOpenSchema schema, String name, String packageName) {
        super(schema, name, null);
        addMethod(new EqualsMethod(this));
        addMethod(new HashCodeMethod(this));
        addMethod(new ToStringMethod(this));
        this.packageName = packageName;
    }

    @Override
    public IAggregateInfo getAggregateInfo() {
        return DynamicArrayAggregateInfo.aggregateInfo;
    }
    
    public IOpenClass getSuperClass() {
        return superClass;
    }

    public void setSuperClass(IOpenClass superClass) {
        this.superClass = superClass;
    }
    
    @Override
    public Iterator<IOpenClass> superClasses() {
        if(superClass != null){
            return AOpenIterator.single(superClass);
        }else{
            return AOpenIterator.empty();
        }
    }
    
    /**
     * User has a possibility to set the package (by table properties mechanism) where he wants to generate datatype 
     * beans classes. It is stored in this field.
     * 
     * 
     * @return package name for current datatype.
     */
    public String getPackageName() {        
        return packageName;
    }
    
    /**
     * Used {@link LinkedHashMap} to store fields in order as them defined in DataType table
     */
    @Override
    protected LinkedHashMap<String, IOpenField> fieldMap() {
        if(fieldMap == null){
            fieldMap = new LinkedHashMap<String, IOpenField>();
        }
        return (LinkedHashMap<String, IOpenField>)fieldMap;
    }
    
    @Override
    public Map<String, IOpenField> getFields() {
        Map<String, IOpenField> fields =new LinkedHashMap<String, IOpenField>();
        Iterator<IOpenClass> superClasses = superClasses();
        while (superClasses.hasNext()) {
            fields.putAll(superClasses.next().getFields());
        }
        fields.putAll(fieldMap());
        return fields;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, IOpenField> getDeclaredFields() {
        return (Map<String, IOpenField>)fieldMap().clone();
    }

    public Object newInstance(IRuntimeEnv env) {
        Object instance = null;
        try {
            instance = getInstanceClass().newInstance();
        } catch (InstantiationException e) {            
            LOG.error(this, e);
        } catch (IllegalAccessException e) {            
            LOG.error(this, e);
        } 
        return instance;
    }
    
    @Override
    public IOpenClass getComponentClass() {        
        if (isArray()) {
            return JavaOpenClass.getOpenClass(getInstanceClass().getComponentType());
        }
        return null;
    }

    /**
     * Constructor with all parameters initialization.
     * 
     * @author PUdalau
     */
    public static class OpenFieldsConstructor implements IOpenMethod {

        private IOpenClass openClass;

        public OpenFieldsConstructor(IOpenClass openClass) {
            this.openClass = openClass;
        }

        public IOpenClass getDeclaringClass() {
            return openClass;
        }

        public String getDisplayName(int mode) {
            return openClass.getDisplayName(mode);
        }

        public IMemberMetaInfo getInfo() {
            return null;
        }

        public IOpenMethod getMethod() {
            return this;
        }

        public String getName() {
            return openClass.getName();
        }

        public IMethodSignature getSignature() {
            Map<String, IOpenField> fields = openClass.getFields();
            IOpenClass[] params = new IOpenClass[fields.size()];
            String[] names = new String[fields.size()];
            int i = 0;
            for(Entry<String, IOpenField> field : fields.entrySet()){
                params[i] = field.getValue().getType();
                names[i] = field.getKey();
                i++;
            }
            return new MethodSignature(params, names);
        }

        public IOpenClass getType() {
            return openClass;
        }

        public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
            Object result = openClass.newInstance(env);
            int i = 0;
            for(IOpenField field : openClass.getFields().values()){
                field.set(result, params[i], env);
                i++;
            }
            return result;
        }

        public boolean isStatic() {
            return true;
        }

        @Override
        public String toString() {
            return openClass.getName();
        }

    };

    /**
     * <code>toString()</code> method.
     * 
     * @author PUdalau
     *
     */
    public static class ToStringMethod implements IOpenMethod{
        private IOpenClass openClass;
        
        public ToStringMethod(IOpenClass forClass) {
            this.openClass = forClass;
        }

        public IMethodSignature getSignature() {
            return IMethodSignature.VOID;
        }

        public IOpenClass getDeclaringClass() {
            return openClass;
        }

        public IMemberMetaInfo getInfo() {
            return null;
        }

        public IOpenClass getType() {
            return JavaOpenClass.STRING;
        }

        public boolean isStatic() {
            return false;
        }

        public String getDisplayName(int mode) {
            return getName();
        }

        public String getName() {
            return "toString";
        }

        public IOpenMethod getMethod() {
            return this;
        }

        public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
            StringBuilder builder = new StringBuilder(openClass.getDisplayName(0) + "{ ");
            Map<String, IOpenField> fields = openClass.getFields();
            for (Entry<String, IOpenField> field : fields.entrySet()) {
                builder.append(field.getKey() + "=" + field.getValue().get(target, env) + " ");
            }
            builder.append('}');
            return builder.toString();
        }
    }
    
    /**
     * Method that compares two objects by fields defined in some {@link IOpenClass}
     * 
     * @author PUdalau
     */
    public static class EqualsMethod implements IOpenMethod{
        private IOpenClass openClass;
        
        public EqualsMethod(IOpenClass forClass) {
            this.openClass = forClass;
        }

        public IMethodSignature getSignature() {
            return new MethodSignature(new IOpenClass[]{JavaOpenClass.OBJECT});
        }

        public IOpenClass getDeclaringClass() {
            return openClass;
        }

        public IMemberMetaInfo getInfo() {
            return null;
        }

        public IOpenClass getType() {
            return JavaOpenClass.BOOLEAN;
        }

        public boolean isStatic() {
            return false;
        }

        public String getDisplayName(int mode) {
            return getName();
        }

        public String getName() {
            return "equals";
        }

        public IOpenMethod getMethod() {
            return this;
        }

        public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
            EqualsBuilder builder = new EqualsBuilder();
            Map<String, IOpenField> fields = openClass.getFields();
            for (IOpenField field : fields.values()) {
                builder.append(field.get(target, env), field.get(params[0], env));
            }
            return builder.isEquals();
        }
    }

    /**
     * Methods that returns hash code calculated using fields.
     * @author PUdalau
     *
     */
    public static class HashCodeMethod implements IOpenMethod{
        private IOpenClass openClass;
        
        public HashCodeMethod(IOpenClass forClass) {
            this.openClass = forClass;
        }

        public IMethodSignature getSignature() {
            return IMethodSignature.VOID;
        }

        public IOpenClass getDeclaringClass() {
            return openClass;
        }

        public IMemberMetaInfo getInfo() {
            return null;
        }

        public IOpenClass getType() {
            return JavaOpenClass.INT;
        }

        public boolean isStatic() {
            return false;
        }

        public String getDisplayName(int mode) {
            return getName();
        }

        public String getName() {
            return "hashCode";
        }

        public IOpenMethod getMethod() {
            return this;
        }

        public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
            HashCodeBuilder builder = new HashCodeBuilder();
            Map<String, IOpenField> fields = openClass.getFields();
            for (IOpenField field : fields.values()) {
                builder.append(field.get(target, env));
            }
            return builder.toHashCode();
        }
    }
}
