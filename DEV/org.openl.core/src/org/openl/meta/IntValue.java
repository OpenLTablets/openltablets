package org.openl.meta;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.openl.binding.impl.Operators;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.meta.number.CastOperand;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.LogicalExpressions;
import org.openl.meta.number.NumberOperations;
import org.openl.util.ArrayTool;
import org.openl.util.math.MathUtils;

public class IntValue extends ExplanationNumberValue<IntValue> {
    
    private static final long serialVersionUID = -3821702883606493390L;    
    
    private static final IntValue ZERO = new IntValue((int) 0);
    private static final IntValue ONE = new IntValue((int) 1);
    private static final IntValue MINUS_ONE = new IntValue((int) -1);

    // <<< INSERT Functions >>>
    private int value;

    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 equal value2
     */
    public static boolean eq(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        if (value1 == null || value2 == null){
            return value1 == value2;
        }
        return Operators.eq(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 greater or equal value2
     */
    public static boolean ge(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        validate(value1, value2, LogicalExpressions.GE.toString());

        return Operators.ge(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 greater value2
     */
    public static boolean gt(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        validate(value1, value2, LogicalExpressions.GT.toString());

        return Operators.gt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less or equal value2
     */
    public static boolean le(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        validate(value1, value2, LogicalExpressions.LE.toString());

        return Operators.le(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less value2
     */
    public static boolean lt(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        validate(value1, value2, LogicalExpressions.LT.toString());

        return Operators.lt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 not equal value2
     */
    public static boolean ne(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        if (value1 == null || value2 == null){
            return value1 != value2;
        }

        return Operators.ne(value1.getValue(), value2.getValue());
    }

     /**
     * average
     * @param values  array of org.openl.meta.IntValue values
     * @return the average value from the array
     */
    public static org.openl.meta.IntValue avg(org.openl.meta.IntValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        int[] primitiveArray = unwrap(values);
        int avg = MathUtils.avg(primitiveArray);
        return new org.openl.meta.IntValue(new org.openl.meta.IntValue(avg), NumberOperations.AVG, values);
    }
     /**
     * sum
     * @param values  array of org.openl.meta.IntValue values
     * @return the sum value from the array
     */
    public static org.openl.meta.IntValue sum(org.openl.meta.IntValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        int[] primitiveArray = unwrap(values);
        int sum = MathUtils.sum(primitiveArray);
        return new org.openl.meta.IntValue(new org.openl.meta.IntValue(sum), NumberOperations.SUM, values);
    }
     /**
     * median
     * @param values  array of org.openl.meta.IntValue values
     * @return the median value from the array
     */
    public static org.openl.meta.IntValue median(org.openl.meta.IntValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        int[] primitiveArray = unwrap(values);
        int median = MathUtils.median(primitiveArray);
        return new org.openl.meta.IntValue(new org.openl.meta.IntValue(median), NumberOperations.MEDIAN, values);
    }

     /**
     * Compares value1 and value2 and returns the max value
     * @param value1
     * @param value2
     * @return max value
     */
    public static org.openl.meta.IntValue max(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MAX.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.IntValue(MathUtils.max(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MAX,
            new org.openl.meta.IntValue[] { value1, value2 });
    }
     /**
     * Compares value1 and value2 and returns the min value
     * @param value1
     * @param value2
     * @return min value
     */
    public static org.openl.meta.IntValue min(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MIN.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.IntValue(MathUtils.min(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MIN,
            new org.openl.meta.IntValue[] { value1, value2 });
    }

    /**
     * 
     * @param values an array org.openl.meta.IntValue, must not be null
     * @return org.openl.meta.IntValue the max element from array
     */
    public static org.openl.meta.IntValue max(org.openl.meta.IntValue[] values) {
        org.openl.meta.IntValue result = (org.openl.meta.IntValue) MathUtils.max(values);

        return new org.openl.meta.IntValue((org.openl.meta.IntValue) getAppropriateValue(values, result),
            NumberOperations.MAX_IN_ARRAY, values);
    }
    /**
     * 
     * @param values an array org.openl.meta.IntValue, must not be null
     * @return org.openl.meta.IntValue the min element from array
     */
    public static org.openl.meta.IntValue min(org.openl.meta.IntValue[] values) {
        org.openl.meta.IntValue result = (org.openl.meta.IntValue) MathUtils.min(values);

        return new org.openl.meta.IntValue((org.openl.meta.IntValue) getAppropriateValue(values, result),
            NumberOperations.MIN_IN_ARRAY, values);
    }
        /**
     * 
     * @param value of variable which should be copied
     * @param name of new variable
     * @return the new org.openl.meta.IntValue variable with name <b>name</b> and value <b>value</b>
     */
    public static org.openl.meta.IntValue copy(org.openl.meta.IntValue value, String name) {
        if (value.getName() == null) {
            value.setName(name);

            return value;
        } else if (!value.getName().equals(name)) {
            org.openl.meta.IntValue result = new org.openl.meta.IntValue (value, NumberOperations.COPY, 
                new org.openl.meta.IntValue[] { value });
            result.setName(name);

            return result;
        }
        return value;
    }

    //REM
    /**
     * Divides left hand operand by right hand operand and returns remainder
     * @param value1 org.openl.meta.IntValue 
     * @param value2 org.openl.meta.IntValue 
     * @return remainder from division value1 by value2
     */
    public static org.openl.meta.IntValue rem(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // Commented to support operations with nulls. See also MathUtils.mod()
        // validate(value1, value2, Formulas.REM.toString());
        if (value1 == null || value2 == null) {
            return ZERO;
        }

        return new org.openl.meta.IntValue(value1, value2, Operators.rem(value1.getValue(), value2.getValue()),
            Formulas.REM);
    }

    //ADD
     /**
     * Adds left hand operand to right hand operand
     * @param value1 org.openl.meta.IntValue
     * @param value2 org.openl.meta.IntValue
     * @return the result of addition operation
     */
    public static org.openl.meta.IntValue add(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.ADD.toString());
        //conditions for classes that are wrappers over primitives
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.IntValue(value1, value2, Operators.add(value1.getValue(), value2.getValue()),
            Formulas.ADD);
}

    // MULTIPLY
     /**
     * Multiplies left hand operand to right hand operand
     * @param value1 org.openl.meta.IntValue
     * @param value2 org.openl.meta.IntValue
     * @return the result of multiplication  operation
     */
    public static org.openl.meta.IntValue multiply(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.MULTIPLY.toString());
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.IntValue(value1, value2, Operators.multiply(value1.getValue(), value2.getValue()),
            Formulas.MULTIPLY);
    }

    //SUBTRACT
    /**
     * Subtracts left hand operand to right hand operand
     * @param value1 org.openl.meta.IntValue
     * @param value2 org.openl.meta.IntValue
     * @return the result of subtraction  operation
     */
    public static org.openl.meta.IntValue subtract(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.SUBTRACT.toString());
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            return negative(value2);
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.IntValue(value1, value2, Operators.subtract(value1.getValue(), value2.getValue()), 
            Formulas.SUBTRACT);
    }

    // DIVIDE
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.IntValue
     * @param value2 org.openl.meta.IntValue
     * @return the result of division  operation
     */
    public static org.openl.meta.IntValue divide(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.DIVIDE.toString());
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            if (value2 != null && value2.doubleValue() != 0) {
                return new org.openl.meta.IntValue(value1, value2, divide(ONE, value2).getValue(), Formulas.DIVIDE);
            }
        }

        if (value2 == null) {
            return new org.openl.meta.IntValue(value1, value2, value1.getValue(), Formulas.DIVIDE);
        }

        if (value2.doubleValue() == 0) {
            throw new OpenlNotCheckedException("Division by zero");
        }

        return new org.openl.meta.IntValue(value1, value2, Operators.divide(value1.getValue(), value2.getValue()),
            Formulas.DIVIDE);
    }

    // QUAOTIENT
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.IntValue
     * @param value2 org.openl.meta.IntValue
     * @return LongValue the result of division  operation
     */
    public static LongValue quotient(org.openl.meta.IntValue number, org.openl.meta.IntValue divisor) {
        if (number != null && divisor != null) {
            LongValue result = new LongValue(MathUtils.quotient(number.getValue(), divisor.getValue()));
            return new LongValue(result, NumberOperations.QUOTIENT, null);
        }
        return null;
    }

    // generated product function for types that are wrappers over primitives
     /**
     * Multiplies the numbers from the provided array and returns the product as a number.
     * @param values an array of IntValue which will be converted to DoubleValue
     * @return the product as a number
     */
    public static DoubleValue product(org.openl.meta.IntValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        int[] primitiveArray = unwrap(values);
        double product = MathUtils.product(primitiveArray);
        // we loose the parameters, but not the result of computation.
        return new DoubleValue(new DoubleValue(product), NumberOperations.PRODUCT, null);
    }
     /**
     *   
     * @param number
     * @param divisor
     * @return the remainder after a number is divided by a divisor. The result is a numeric value and has the same sign as the devisor.
     */
    public static org.openl.meta.IntValue mod(org.openl.meta.IntValue number, org.openl.meta.IntValue divisor) {
        if (number != null && divisor != null) {
            org.openl.meta.IntValue result = new org.openl.meta.IntValue(MathUtils.mod(number.getValue(), divisor.getValue()));
            return new org.openl.meta.IntValue(result, NumberOperations.MOD, new org.openl.meta.IntValue[]{number, divisor} );
        }
        return null;
    }

    /**
     * Sorts the array <b>values</b> in ascending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.IntValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.IntValue small(org.openl.meta.IntValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        int[] primitiveArray = unwrap(values);
        int small = MathUtils.small(primitiveArray, position);
        return new org.openl.meta.IntValue((org.openl.meta.IntValue) getAppropriateValue(values, new org.openl.meta.IntValue(small)), 
            NumberOperations.SMALL, values);
    }

    /**
     * Sorts the array <b>values</b> in descending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.IntValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.IntValue big(org.openl.meta.IntValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        int[] primitiveArray = unwrap(values);
        int big = MathUtils.big(primitiveArray, position);
        return new org.openl.meta.IntValue((org.openl.meta.IntValue) getAppropriateValue(values, new org.openl.meta.IntValue(big)),
            NumberOperations.BIG, values);
    }

    /**
     * 
     * @param value1
     * @param value2
     * @return the result of value1 raised to the power of value2
     */
    public static org.openl.meta.IntValue pow(org.openl.meta.IntValue value1, org.openl.meta.IntValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        //
        // validate(value1, value2, NumberOperations.POW);
        if (value1 == null) {
            return value2 == null ? null : new org.openl.meta.IntValue((int) 0);
        } else if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.IntValue(new org.openl.meta.IntValue(Operators.pow(value1.getValue(), value2.getValue())), 
            NumberOperations.POW, new org.openl.meta.IntValue[] { value1, value2 });
    }

    /**
     * 
     * @param value
     * @return the absolute value (module) of the value <b>value </b>
     */
    public static org.openl.meta.IntValue abs(org.openl.meta.IntValue value) {
        // Commented to support operations with nulls.
        // validate(value, NumberOperations.ABS);
        if (value == null) {
            return null;
        }
        // evaluate result
        org.openl.meta.IntValue result = new org.openl.meta.IntValue(Operators.abs(value.getValue()));
        // create instance with information about last operation
        return new org.openl.meta.IntValue(result, NumberOperations.ABS, new org.openl.meta.IntValue[] { value });
    }

    /**
     * 
     * @param value
     * @return the negative value of the <b>value</b>
     */
    public static org.openl.meta.IntValue negative(org.openl.meta.IntValue value) {
        if (value == null) {
            return null;
        }
        return multiply(value, MINUS_ONE);
    }

    /**
     * 
     * @param value
     * @return the <b>value</b> increased by 1
     */
    public static org.openl.meta.IntValue inc(org.openl.meta.IntValue value) {
        return add(value, ONE);
    }

    /**
     * 
     * @param value
     * @return the <b>value</b>
     */
    public static org.openl.meta.IntValue positive(org.openl.meta.IntValue value) {
        return value;
    }

    /**
     * 
     * @param value
     * @return the <b>value </b> decreased by 1
     */
    public static org.openl.meta.IntValue dec(org.openl.meta.IntValue value) {
        return subtract(value, ONE);
    }

    // Autocasts

    /**
     * Is used to overload implicit cast operators from byte to org.openl.meta.IntValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.IntValue
     */
    public static org.openl.meta.IntValue autocast(byte x, org.openl.meta.IntValue y) {
        return new org.openl.meta.IntValue((int) x);
    }
    /**
     * Is used to overload implicit cast operators from short to org.openl.meta.IntValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.IntValue
     */
    public static org.openl.meta.IntValue autocast(short x, org.openl.meta.IntValue y) {
        return new org.openl.meta.IntValue((int) x);
    }
    /**
     * Is used to overload implicit cast operators from int to org.openl.meta.IntValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.IntValue
     */
    public static org.openl.meta.IntValue autocast(int x, org.openl.meta.IntValue y) {
        return new org.openl.meta.IntValue((int) x);
    }

    // Constructors
    public IntValue(int value) {
        this.value = value;
    }

    /**Formula constructor**/
    public IntValue(org.openl.meta.IntValue lv1, org.openl.meta.IntValue lv2, int value, Formulas operand) {
        super(lv1, lv2, operand);
        this.value = value;
    }

    /**Cast constructor**/
    public IntValue(int value, ExplanationNumberValue<?> beforeCastValue, boolean autocast) {
        super(beforeCastValue, new CastOperand("IntValue", autocast));
        this.value = value;
    }

    /**
    *Copy the current value with new name <b>name</b>
    */
    @Override
    public org.openl.meta.IntValue copy(String name) {
        return copy(this, name);
    }

    /**
    * Prints the value of the current variable
    */
    public String printValue() {
        return String.valueOf(value);
    }

    /**
    * Returns the value of the current variable
    */
    public int getValue() {
        return value;
    }

    /**
    * Sets the value of the current variable
    */
    public void setValue(int value) {
        this.value = value;
    }

    //Equals
    @Override
     /**
     * Indicates whether some other object is "equal to" this org.openl.meta.IntValue variable. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof org.openl.meta.IntValue) {
            org.openl.meta.IntValue secondObj = (org.openl.meta.IntValue) obj;
            return Operators.eq(getValue(), secondObj.getValue());
        }

        return false;
    }

    // sort
    /**
    * Sorts the array <b>values</b>
    * @param values an array for sorting
    * @return the sorted array
    */
    public static org.openl.meta.IntValue[] sort (org.openl.meta.IntValue[] values ) {
        org.openl.meta.IntValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new org.openl.meta.IntValue[values.length];
           org.openl.meta.IntValue[] notNullArray = ArrayTool.removeNulls(values);

            Arrays.sort(notNullArray);

            /* Filling sortedArray by sorted and null values */
            for (int i = 0; i < notNullArray.length; i++) {
                sortedArray[i] = notNullArray[i];
            }
        }
        return sortedArray;
    }
        // <<< END INSERT Functions >>>
    
    // ******* Autocasts*************

    public static IntValue autocast(Integer x, IntValue y) {
        if (x == null) {
            return null;
        }

        return new IntValue(x);
    }
    
    public static LongValue autocast(IntValue x, LongValue y) {
        if (x == null) {
            return null;
        }

        return new LongValue(x.getValue(), x, true);
    }
    
    public static FloatValue autocast(IntValue x, FloatValue y) {
        if (x == null) {
            return null;
        }

        return new FloatValue(x.getValue(), x, true);
    }
    
    public static DoubleValue autocast(IntValue x, DoubleValue y) {
        if (x == null) {
            return null;
        }

        return new DoubleValue(x.getValue(), x, true);
    }
    
    public static BigIntegerValue autocast(IntValue x, BigIntegerValue y) {
        if (x == null) {
            return null;
        }
        return new BigIntegerValue(String.valueOf(x.getValue()), x, true);
    }
    
    public static BigDecimalValue autocast(IntValue x, BigDecimalValue y) {
        if (x == null) {
            return null;
        }
        return new BigDecimalValue(String.valueOf(x.getValue()), x, true);
    }
    
    // ******* Casts*************

    public static byte cast(IntValue x, byte y) {
        return x.byteValue();
    }

    public static short cast(IntValue x, short y) {
        return x.shortValue();
    }
    
    public static char cast(IntValue x, char y) {
        return (char) x.intValue();
    }

    public static int cast(IntValue x, int y) {
        return x.intValue();
    }

    public static long cast(IntValue x, long y) {
        return x.longValue();
    }

    public static float cast(IntValue x, float y) {
        return x.floatValue();
    }
    
    public static double cast(IntValue x, double y) {
        return x.doubleValue();
    }

    public static Integer cast(IntValue x, Integer y) {
        if (x == null) {
            return null;
        }

        return x.intValue();
    }
    
    public static ByteValue cast(IntValue x, ByteValue y) {
        if (x == null) {
            return null;
        }
        return new ByteValue(x.byteValue(), x, false);
    }
        
    public static ShortValue cast(IntValue x, ShortValue y) {
        if (x == null) {
            return null;
        }
        return new ShortValue(x.shortValue(), x, false);
    }

    public IntValue(String valueString) {        
        value = Integer.parseInt(valueString);
    }

    /**Function constructor**/
    public IntValue(IntValue result, NumberOperations function, IntValue[] params) {
        super(function, params);
        this.value = result.intValue();
    }
    
    @Override
    public double doubleValue() {        
        return (double) value;
    }

    @Override
    public float floatValue() {        
        return (float )value;
    }

    @Override
    public int intValue() {        
        return value;
    }
    
    @Override
    public long longValue() {        
        return (long) value;
    }    

    public int compareTo(Number o) {        
        return value < o.intValue() ? -1 : (value == o.intValue() ? 0 : 1);
    }

    @Override
    public int hashCode() {
        return ((Integer) value).hashCode();
    }   
    
    private static int[] unwrap(IntValue[] values) {
        values = ArrayTool.removeNulls(values);
        
        int[] intArray = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            intArray[i] = values[i].getValue();
        }
        return intArray;
    }
    
}
