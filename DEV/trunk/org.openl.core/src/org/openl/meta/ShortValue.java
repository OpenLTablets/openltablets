package org.openl.meta;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.openl.binding.impl.Operators;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.meta.number.CastOperand;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.LogicalExpressions;
import org.openl.meta.number.NumberOperations;
import org.openl.util.ArrayTool;
import org.openl.util.math.MathUtils;

public class ShortValue extends ExplanationNumberValue<ShortValue> {

    private static final long serialVersionUID = 5259931539737847856L;
    
    private static final ShortValue ZERO = new ShortValue((short) 0);
    private static final ShortValue ONE = new ShortValue((short) 1);
    private static final ShortValue MINUS_ONE = new ShortValue((short) -1);

    // <<< INSERT Functions >>>
    private short value;


    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 equal value2
     */
    public static boolean eq(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        validate(value1, value2, LogicalExpressions.EQ.toString());

        return Operators.eq(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 greater or equal value2
     */
    public static boolean ge(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        validate(value1, value2, LogicalExpressions.GE.toString());

        return Operators.ge(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 greater value2
     */
    public static boolean gt(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        validate(value1, value2, LogicalExpressions.GT.toString());

        return Operators.gt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less or equal value2
     */
    public static boolean le(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        validate(value1, value2, LogicalExpressions.LE.toString());

        return Operators.le(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less value2
     */
    public static boolean lt(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        validate(value1, value2, LogicalExpressions.LT.toString());

        return Operators.lt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 not equal value2
     */
    public static boolean ne(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        validate(value1, value2, LogicalExpressions.NE.toString());

        return Operators.ne(value1.getValue(), value2.getValue());
    }

     /**
     * average
     * @param values  array of org.openl.meta.ShortValue values
     * @return the average value from the array
     */
    public static org.openl.meta.ShortValue avg(org.openl.meta.ShortValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        short[] primitiveArray = unwrap(values);
        short avg = MathUtils.avg(primitiveArray);
        return new org.openl.meta.ShortValue(new org.openl.meta.ShortValue(avg), NumberOperations.AVG, values);
    }
     /**
     * sum
     * @param values  array of org.openl.meta.ShortValue values
     * @return the sum value from the array
     */
    public static org.openl.meta.ShortValue sum(org.openl.meta.ShortValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        short[] primitiveArray = unwrap(values);
        short sum = MathUtils.sum(primitiveArray);
        return new org.openl.meta.ShortValue(new org.openl.meta.ShortValue(sum), NumberOperations.SUM, values);
    }
     /**
     * median
     * @param values  array of org.openl.meta.ShortValue values
     * @return the median value from the array
     */
    public static org.openl.meta.ShortValue median(org.openl.meta.ShortValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        short[] primitiveArray = unwrap(values);
        short median = MathUtils.median(primitiveArray);
        return new org.openl.meta.ShortValue(new org.openl.meta.ShortValue(median), NumberOperations.MEDIAN, values);
    }

     /**
     * Compares value1 and value2 and returns the max value
     * @param value1
     * @param value2
     * @return max value
     */
    public static org.openl.meta.ShortValue max(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MAX.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.ShortValue(MathUtils.max(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MAX,
            new org.openl.meta.ShortValue[] { value1, value2 });
    }
     /**
     * Compares value1 and value2 and returns the min value
     * @param value1
     * @param value2
     * @return min value
     */
    public static org.openl.meta.ShortValue min(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MIN.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.ShortValue(MathUtils.min(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MIN,
            new org.openl.meta.ShortValue[] { value1, value2 });
    }

    /**
     * 
     * @param values an array org.openl.meta.ShortValue, must not be null
     * @return org.openl.meta.ShortValue the max element from array
     */
    public static org.openl.meta.ShortValue max(org.openl.meta.ShortValue[] values) {
        org.openl.meta.ShortValue result = (org.openl.meta.ShortValue) MathUtils.max(values);

        return new org.openl.meta.ShortValue((org.openl.meta.ShortValue) getAppropriateValue(values, result),
            NumberOperations.MAX_IN_ARRAY, values);
    }
    /**
     * 
     * @param values an array org.openl.meta.ShortValue, must not be null
     * @return org.openl.meta.ShortValue the min element from array
     */
    public static org.openl.meta.ShortValue min(org.openl.meta.ShortValue[] values) {
        org.openl.meta.ShortValue result = (org.openl.meta.ShortValue) MathUtils.min(values);

        return new org.openl.meta.ShortValue((org.openl.meta.ShortValue) getAppropriateValue(values, result),
            NumberOperations.MIN_IN_ARRAY, values);
    }
        /**
     * 
     * @param value of variable which should be copied
     * @param name of new variable
     * @return the new org.openl.meta.ShortValue variable with name <b>name</b> and value <b>value</b>
     */
    public static org.openl.meta.ShortValue copy(org.openl.meta.ShortValue value, String name) {
        if (value.getName() == null) {
            value.setName(name);

            return value;
        } else if (!value.getName().equals(name)) {
            org.openl.meta.ShortValue result = new org.openl.meta.ShortValue (value, NumberOperations.COPY, 
                new org.openl.meta.ShortValue[] { value });
            result.setName(name);

            return result;
        }
        return value;
    }

    //REM
    /**
     * Divides left hand operand by right hand operand and returns remainder
     * @param value1 org.openl.meta.ShortValue 
     * @param value2 org.openl.meta.ShortValue 
     * @return remainder from division value1 by value2
     */
    public static org.openl.meta.ShortValue rem(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        // Commented to support operations with nulls. See also MathUtils.mod()
        // validate(value1, value2, Formulas.REM.toString());
        if (value1 == null || value2 == null) {
            return ZERO;
        }

        return new org.openl.meta.ShortValue(value1, value2, Operators.rem(value1.getValue(), value2.getValue()),
            Formulas.REM);
    }

    //ADD
     /**
     * Adds left hand operand to right hand operand
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of addition operation
     */
    public static org.openl.meta.ShortValue add(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
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

        return new org.openl.meta.ShortValue(value1, value2, Operators.add(value1.getValue(), value2.getValue()),
            Formulas.ADD);
}

    // MULTIPLY
     /**
     * Multiplies left hand operand to right hand operand
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of multiplication  operation
     */
    public static org.openl.meta.ShortValue multiply(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.MULTIPLY.toString());
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.ShortValue(value1, value2, Operators.multiply(value1.getValue(), value2.getValue()),
            Formulas.MULTIPLY);
    }

    //SUBTRACT
    /**
     * Subtracts left hand operand to right hand operand
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of subtraction  operation
     */
    public static org.openl.meta.ShortValue subtract(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
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

        return new org.openl.meta.ShortValue(value1, value2, Operators.subtract(value1.getValue(), value2.getValue()), 
            Formulas.SUBTRACT);
    }

    // DIVIDE
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of division  operation
     */
    public static org.openl.meta.ShortValue divide(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.DIVIDE.toString());
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            if (value2 != null && value2.doubleValue() != 0) {
                return new org.openl.meta.ShortValue(value1, value2, divide(ONE, value2).getValue(), Formulas.DIVIDE);
            }
        }

        if (value2 == null) {
            return new org.openl.meta.ShortValue(value1, value2, value1.getValue(), Formulas.DIVIDE);
        }

        if (value2.doubleValue() == 0) {
            throw new OpenlNotCheckedException("Division by zero");
        }

        return new org.openl.meta.ShortValue(value1, value2, Operators.divide(value1.getValue(), value2.getValue()),
            Formulas.DIVIDE);
    }

    // QUAOTIENT
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return LongValue the result of division  operation
     */
    public static LongValue quotient(org.openl.meta.ShortValue number, org.openl.meta.ShortValue divisor) {
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
    public static DoubleValue product(org.openl.meta.ShortValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        short[] primitiveArray = unwrap(values);
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
    public static org.openl.meta.ShortValue mod(org.openl.meta.ShortValue number, org.openl.meta.ShortValue divisor) {
        if (number != null && divisor != null) {
            org.openl.meta.ShortValue result = new org.openl.meta.ShortValue(MathUtils.mod(number.getValue(), divisor.getValue()));
            return new org.openl.meta.ShortValue(result, NumberOperations.MOD, new org.openl.meta.ShortValue[]{number, divisor} );
        }
        return null;
    }

    /**
     * Sorts the array <b>values</b> in ascending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.ShortValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.ShortValue small(org.openl.meta.ShortValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        short[] primitiveArray = unwrap(values);
        short small = MathUtils.small(primitiveArray, position);
        return new org.openl.meta.ShortValue((org.openl.meta.ShortValue) getAppropriateValue(values, new org.openl.meta.ShortValue(small)), 
            NumberOperations.SMALL, values);
    }

    /**
     * Sorts the array <b>values</b> in descending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.ShortValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.ShortValue big(org.openl.meta.ShortValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        short[] primitiveArray = unwrap(values);
        short big = MathUtils.big(primitiveArray, position);
        return new org.openl.meta.ShortValue((org.openl.meta.ShortValue) getAppropriateValue(values, new org.openl.meta.ShortValue(big)),
            NumberOperations.BIG, values);
    }

    /**
     * 
     * @param value1
     * @param value2
     * @return the result of value1 raised to the power of value2
     */
    public static org.openl.meta.ShortValue pow(org.openl.meta.ShortValue value1, org.openl.meta.ShortValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        //
        // validate(value1, value2, NumberOperations.POW);
        if (value1 == null) {
            return value2 == null ? null : new org.openl.meta.ShortValue((short) 0);
        } else if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.ShortValue(new org.openl.meta.ShortValue(Operators.pow(value1.getValue(), value2.getValue())), 
            NumberOperations.POW, new org.openl.meta.ShortValue[] { value1, value2 });
    }

    /**
     * 
     * @param value
     * @return the absolute value (module) of the value <b>value </b>
     */
    public static org.openl.meta.ShortValue abs(org.openl.meta.ShortValue value) {
        // Commented to support operations with nulls.
        // validate(value, NumberOperations.ABS);
        if (value == null) {
            return null;
        }
        // evaluate result
        org.openl.meta.ShortValue result = new org.openl.meta.ShortValue(Operators.abs(value.getValue()));
        // create instance with information about last operation
        return new org.openl.meta.ShortValue(result, NumberOperations.ABS, new org.openl.meta.ShortValue[] { value });
    }

    /**
     * 
     * @param value
     * @return the negative value of the <b>value</b>
     */
    public static org.openl.meta.ShortValue negative(org.openl.meta.ShortValue value) {
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
    public static org.openl.meta.ShortValue inc(org.openl.meta.ShortValue value) {
        return add(value, ONE);
    }

    /**
     * 
     * @param value
     * @return the <b>value</b>
     */
    public static org.openl.meta.ShortValue positive(org.openl.meta.ShortValue value) {
        return value;
    }

    /**
     * 
     * @param value
     * @return the <b>value </b> decreased by 1
     */
    public static org.openl.meta.ShortValue dec(org.openl.meta.ShortValue value) {
        return subtract(value, ONE);
    }

    // Autocasts

    /**
     * Is used to overload implicit cast operators from byte to org.openl.meta.ShortValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static org.openl.meta.ShortValue autocast(byte x, org.openl.meta.ShortValue y) {
        return new org.openl.meta.ShortValue((short) x);
    }
    /**
     * Is used to overload implicit cast operators from short to org.openl.meta.ShortValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static org.openl.meta.ShortValue autocast(short x, org.openl.meta.ShortValue y) {
        return new org.openl.meta.ShortValue((short) x);
    }
    /**
     * Is used to overload implicit cast operators from int to org.openl.meta.ShortValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static org.openl.meta.ShortValue autocast(int x, org.openl.meta.ShortValue y) {
        return new org.openl.meta.ShortValue((short) x);
    }
    /**
     * Is used to overload implicit cast operators from long to org.openl.meta.ShortValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static org.openl.meta.ShortValue autocast(long x, org.openl.meta.ShortValue y) {
        return new org.openl.meta.ShortValue((short) x);
    }
    /**
     * Is used to overload implicit cast operators from float to org.openl.meta.ShortValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static org.openl.meta.ShortValue autocast(float x, org.openl.meta.ShortValue y) {
        return new org.openl.meta.ShortValue((short) x);
    }
    /**
     * Is used to overload implicit cast operators from double to org.openl.meta.ShortValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static org.openl.meta.ShortValue autocast(double x, org.openl.meta.ShortValue y) {
        return new org.openl.meta.ShortValue((short) x);
    }

    // Constructors
    public ShortValue(short value) {
        this.value = value;
    }

    public ShortValue(short value, String name) {
        super(name);
        this.value = value;
    }

    public ShortValue(short value, IMetaInfo metaInfo) {
        super(metaInfo);
        this.value = value;
    }

    /**Formula constructor**/
    public ShortValue(org.openl.meta.ShortValue lv1, org.openl.meta.ShortValue lv2, short value, Formulas operand) {
        super(lv1, lv2, operand);
        this.value = value;
    }

    /**Cast constructor**/
    public ShortValue(short value, ExplanationNumberValue<?> beforeCastValue, boolean autocast) {
        super(beforeCastValue, new CastOperand("ShortValue", autocast));
        this.value = value;
    }

    /**
    *Copy the current value with new name <b>name</b>
    */
    @Override
    public org.openl.meta.ShortValue copy(String name) {
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
    public short getValue() {
        return value;
    }

    /**
    * Sets the value of the current variable
    */
    public void setValue(short value) {
        this.value = value;
    }

    //Equals
    @Override
     /**
     * Indicates whether some other object is "equal to" this org.openl.meta.ShortValue variable. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof org.openl.meta.ShortValue) {
            org.openl.meta.ShortValue secondObj = (org.openl.meta.ShortValue) obj;
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
    public static org.openl.meta.ShortValue[] sort (org.openl.meta.ShortValue[] values ) {
        org.openl.meta.ShortValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new org.openl.meta.ShortValue[values.length];
           org.openl.meta.ShortValue[] notNullArray = ArrayTool.removeNulls(values);

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

    public static ShortValue autocast(Short x, ShortValue y) {
        if (x == null) {
            return null;
        }

        return new ShortValue(x);
    }

    public static IntValue autocast(ShortValue x, IntValue y) {
        if (x == null) {
            return null;
        }

        return new IntValue(x.getValue(), x, true);
    }

    public static LongValue autocast(ShortValue x, LongValue y) {
        if (x == null) {
            return null;
        }

        return new LongValue(x.getValue(), x, true);
    }

    public static FloatValue autocast(ShortValue x, FloatValue y) {
        if (x == null) {
            return null;
        }

        return new FloatValue(x.getValue(), x, true);
    }

    public static DoubleValue autocast(ShortValue x, DoubleValue y) {
        if (x == null) {
            return null;
        }

        return new DoubleValue(x.getValue(), x, true);
    }

    public static BigIntegerValue autocast(ShortValue x, BigIntegerValue y) {
        if (x == null) {
            return null;
        }
        return new BigIntegerValue(String.valueOf(x.getValue()), x, true);
    }

    public static BigDecimalValue autocast(ShortValue x, BigDecimalValue y) {
        if (x == null) {
            return null;
        }
        return new BigDecimalValue(String.valueOf(x.getValue()), x, true);
    }

    // ******* Casts*************

    public static byte cast(ShortValue x, byte y) {
        return x.byteValue();
    }

    public static short cast(ShortValue x, short y) {
        return x.shortValue();
    }

    public static char cast(ShortValue x, char y) {
        return (char) x.shortValue();
    }

    public static int cast(ShortValue x, int y) {
        return x.intValue();
    }

    public static long cast(ShortValue x, long y) {
        return x.longValue();
    }

    public static float cast(ShortValue x, float y) {
        return x.floatValue();
    }

    public static double cast(ShortValue x, double y) {
        return x.doubleValue();
    }

    public static Short cast(ShortValue x, Short y) {
        if (x == null) {
            return null;
        }

        return x.shortValue();
    }

    public static ByteValue cast(ShortValue x, ByteValue y) {
        if (x == null) {
            return null;
        }
        return new ByteValue(x.byteValue(), x, false);
    }

    public ShortValue(String valueString) {
        value = Short.parseShort(valueString);
    }    

    /** Function constructor **/
    public ShortValue(ShortValue result, NumberOperations function, ShortValue[] params) {
        super(result, function, params);
        this.value = result.shortValue();
    }   
    
    @Override
    public double doubleValue() {        
        return (double) value;
    }

    @Override
    public float floatValue() {        
        return (float) value;
    }

    @Override
    public int intValue() {        
        return (int) value;
    }
    
    @Override
    public long longValue() {        
        return (long) value;
    }

    public int compareTo(Number o) {
        return value - o.shortValue();
    }

    @Override
    public int hashCode() {
        return ((Short) value).hashCode();
    }    
    
    private static short[] unwrap(ShortValue[] values) {
        values = ArrayTool.removeNulls(values);
        short[] shortArray = new short[values.length];
        for (int i = 0; i < values.length; i++) {
            shortArray[i] = values[i].getValue();
        }
        return shortArray;
    }
    

}
