package org.openl.meta.explanation;

import javax.xml.bind.annotation.XmlTransient;

import org.openl.exception.OpenlNotCheckedException;
import org.openl.meta.IMetaInfo;
import org.openl.meta.number.CastOperand;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.NumberOperations;
import org.openl.util.tree.ITreeElement;

/**
 * Number value that supports explanation operations.
 * 
 * @author DLiauchuk
 *
 * @param <T> type that extends {@link ExplanationNumberValue}
 */
public abstract class ExplanationNumberValue<T extends ExplanationNumberValue<T>> extends Number implements Comparable<Number>,
    ExplanationForNumber<T> {
    
    private static final long serialVersionUID = -5461468496220613277L;
    
    /** 
     * Explanator for current value.
     */
    private transient ExplanationForNumber<T> explanation;

    public ExplanationNumberValue() {
    }
    
    /** Formula constructor */
    public ExplanationNumberValue(T dv1, T dv2, Formulas operand) {
        /** initialize explanation for formula value */
        this.explanation = new FormulaExplanationValue<T>(dv1, dv2, operand);
    }
    
    /** Function constructor */
    public ExplanationNumberValue(NumberOperations function, T[] params) {
        /** initialize explanation for function value */
        this.explanation = new FunctionExplanationValue<T>(function, params);
    }
    
    /** Casting constructor */
    @SuppressWarnings("unchecked")
    public ExplanationNumberValue(ExplanationNumberValue<?> previousValue, CastOperand operand) {
        /** initialize explanation for cast value */
        this.explanation = new CastExplanationValue(previousValue, operand);
    }

    /**
     * @return explanation for a formula value.
     */
    public FormulaExplanationValue<T> getFormula() {
        return (FormulaExplanationValue<T>) explanation;
    }

    /**
     * @return explanation for a function value.
     */
    public FunctionExplanationValue<T> getFunction() {
        return (FunctionExplanationValue<T>) explanation;
    }

    /**
     * @return explanation for a cast value.
     */
    public CastExplanationValue getCast() {
        return (CastExplanationValue) explanation;
    }

    public boolean isFormula() {
        return explanation instanceof FormulaExplanationValue;
    }

    public boolean isFunction() {
        return explanation instanceof FunctionExplanationValue;
    }

    public boolean isCast() {
        return explanation instanceof CastExplanationValue;
    }

    public abstract T copy(String name);

    /**
     * Lazy initialization of explanation to reduce memory usage in executionMode=true
     */
    private ExplanationForNumber<T> getExplanation() {
        if (explanation == null) {
            explanation = new SingleValueExplanation<T>();
        }
        return explanation;
    }

    @XmlTransient
    public IMetaInfo getMetaInfo() {
        return getExplanation().getMetaInfo();
    }
    
    public void setMetaInfo(IMetaInfo metaInfo) {
        getExplanation().setMetaInfo(metaInfo);
    }
    
    public String getName() {
        return getExplanation().getName();
    }
    
    public String getDisplayName(int mode) {
        return getExplanation().getDisplayName(mode);
    }

    public void setFullName(String name) {
        getExplanation().setFullName(name);
    }

    public void setName(String name) {
        getExplanation().setName(name);
    }

    public Iterable<? extends ITreeElement<T>> getChildren() {
        return getExplanation().getChildren();
    }

    public boolean isLeaf() {
        return getExplanation().isLeaf();
    }

    public String getType() {
        return getExplanation().getType();
    }

    @SuppressWarnings("unchecked")
    public T getObject() {    
        return (T) this;
    }
    
    @Override
    public String toString() {
        return printValue();
    }
    
    /**
     * Returns the equal element from collection
     * @param values
     * @param result
     * @return
     */
    protected static <T extends ExplanationNumberValue<T>> ExplanationNumberValue<T> getAppropriateValue(ExplanationNumberValue<T>[] values, ExplanationNumberValue<?> result) {
        for (ExplanationNumberValue<T> value : values) {
            if (value == result || value != null && value.equals(result)) {
                return value;
            }
        }
        return null;
    }
    
    protected static OpenlNotCheckedException getTwoArgumentsException(String operation) {
        return new OpenlNotCheckedException(String.format("None of the arguments for '%s' operation can be null", 
            operation));
    }
    
    protected static OpenlNotCheckedException getOneArgumentException(NumberOperations operation) {
        return new OpenlNotCheckedException(String.format("Argument couldn`t be null for '%s' operation", 
            operation.toString()));
    }
    
    protected static void validate(ExplanationNumberValue<?> value1, ExplanationNumberValue<?> value2, 
            NumberOperations operation) {
        if (value1 == null || value2 == null) {
            throw getTwoArgumentsException(operation.toString());
        }
    }
    
    protected static void validate(ExplanationNumberValue<?> value, NumberOperations operation) {
        if (value == null) {
            throw getOneArgumentException(operation);
        }
    }
    
    protected static void validate(ExplanationNumberValue<?> value1, ExplanationNumberValue<?> value2, 
            String operation) {
        if (value1 == null || value2 == null) {
            throw getTwoArgumentsException(operation);
        }
    }
    
    
    
}
