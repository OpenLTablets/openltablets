package org.openl.rules.dt.type;

import org.openl.rules.helpers.DoubleRange;
import org.openl.util.RangeWithBounds.BoundType;

public final class DoubleRangeAdaptor implements IRangeAdaptor<DoubleRange, Double> {
    private final static DoubleRangeAdaptor INSTANCE = new DoubleRangeAdaptor();
    
    private DoubleRangeAdaptor(){
    }
    
    public static IRangeAdaptor<DoubleRange, Double> getInstance(){
        return INSTANCE;
    }

    public Comparable<Double> getMax(DoubleRange range) {
        double max = range.getUpperBound();
        if (max != Double.POSITIVE_INFINITY && range.getUpperBoundType() == BoundType.EXCLUDING) {
        	// the max should be moved to the left,
        	// to ensure that range.getUpperBound() won`t get to the interval
        	//
            max -= Math.ulp(max);
        } else if (max != Double.POSITIVE_INFINITY && range.getUpperBoundType() == BoundType.INCLUDING) {
        	// the max should be moved to the right,
        	// to ensure that range.getUpperBound() will get to the interval
        	//
        	max += Math.ulp(max);
        }
        return max;
    }    
    
    public Comparable<Double> getMin(DoubleRange range) {
        double min = range.getLowerBound();
        if (range.getLowerBoundType() == BoundType.EXCLUDING) {
            min += Math.ulp(min);
        }
        return min;
    }

    public Double adaptValueType(Number value) {        
        return Double.valueOf(value.doubleValue());
    }
}
