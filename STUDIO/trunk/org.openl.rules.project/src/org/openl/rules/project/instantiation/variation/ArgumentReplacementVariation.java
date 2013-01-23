package org.openl.rules.project.instantiation.variation;


/**
 * Variation for replacement of value of some argument.
 * 
 * It was introduced because field modification variations can not modify root
 * object(argument of method).
 * 
 * @author PUdalau
 */
public class ArgumentReplacementVariation extends Variation {
    private int updatedArgumentIndex;
    private Object valueToSet;
    /**
     * Constructs variation
     */
    public ArgumentReplacementVariation() {
    }
    
    /**
     * Constructs variation.
     * 
     * @param variationID Unique ID for variation.
     * @param updatedArgumentIndex Index of argument to be updated.
     * @param valueToSet Value that will be set to instead of argument.
     */
    public ArgumentReplacementVariation(String variationID, int updatedArgumentIndex, Object valueToSet) {
        super(variationID);
        if (updatedArgumentIndex < 0) {
            throw new IllegalArgumentException("Number of argument to be modified should be non negative.");
        } else {
            this.updatedArgumentIndex = updatedArgumentIndex;
        }
        this.valueToSet = valueToSet;
    }

    @Override
    public Object currentValue(Object[] originalArguments) {
        return originalArguments[updatedArgumentIndex];
    }

    @Override
    public Object[] applyModification(Object[] originalArguments) {
        if (updatedArgumentIndex >= originalArguments.length) {
            throw new VariationRuntimeException("Failed to apply variaion \"" + getVariationID()
                    + "\". Number of argument to modify is [" + updatedArgumentIndex + "] but arguments length is "
                    + originalArguments.length);
        }
        originalArguments[updatedArgumentIndex] = valueToSet;
        return originalArguments;
    }

    @Override
    public void revertModifications(Object[] modifiedArguments, Object previousValue) {
        modifiedArguments[updatedArgumentIndex] = previousValue;
    }

    /**
     * @return Index of arguments to be modified.
     */
    public int getUpdatedArgumentIndex() {
        return updatedArgumentIndex;
    }

    /**
     * @return value to set into field.
     */
    public Object getValueToSet() {
        return valueToSet;
    }
}
