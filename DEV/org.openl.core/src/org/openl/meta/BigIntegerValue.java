package org.openl.meta;

import java.math.BigInteger;
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

public class BigIntegerValue extends ExplanationNumberValue<BigIntegerValue> {

    private static final long serialVersionUID = -3936317402079096501L;

    private static final BigIntegerValue ZERO = new BigIntegerValue("0");
    private static final BigIntegerValue ONE = new BigIntegerValue("1");
    private static final BigIntegerValue MINUS_ONE = new BigIntegerValue("-1");

    // <<< INSERT Functions >>>
    private java.math.BigInteger value;


    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 equal value2
     */
    public static boolean eq(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
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
    public static boolean ge(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        validate(value1, value2, LogicalExpressions.GE.toString());

        return Operators.ge(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 greater value2
     */
    public static boolean gt(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        validate(value1, value2, LogicalExpressions.GT.toString());

        return Operators.gt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less or equal value2
     */
    public static boolean le(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        validate(value1, value2, LogicalExpressions.LE.toString());

        return Operators.le(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less value2
     */
    public static boolean lt(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        validate(value1, value2, LogicalExpressions.LT.toString());

        return Operators.lt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 not equal value2
     */
    public static boolean ne(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        if (value1 == null || value2 == null){
            return value1 != value2;
        }

        return Operators.ne(value1.getValue(), value2.getValue());
    }

     /**
     * average
     * @param values  array of org.openl.meta.BigIntegerValue values
     * @return the average value from the array
     */
    public static org.openl.meta.BigIntegerValue avg(org.openl.meta.BigIntegerValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        java.math.BigInteger[] primitiveArray = unwrap(values);
        java.math.BigInteger avg = MathUtils.avg(primitiveArray);
        return new org.openl.meta.BigIntegerValue(new org.openl.meta.BigIntegerValue(avg), NumberOperations.AVG, values);
    }
     /**
     * sum
     * @param values  array of org.openl.meta.BigIntegerValue values
     * @return the sum value from the array
     */
    public static org.openl.meta.BigIntegerValue sum(org.openl.meta.BigIntegerValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        java.math.BigInteger[] primitiveArray = unwrap(values);
        java.math.BigInteger sum = MathUtils.sum(primitiveArray);
        return new org.openl.meta.BigIntegerValue(new org.openl.meta.BigIntegerValue(sum), NumberOperations.SUM, values);
    }
     /**
     * median
     * @param values  array of org.openl.meta.BigIntegerValue values
     * @return the median value from the array
     */
    public static org.openl.meta.BigIntegerValue median(org.openl.meta.BigIntegerValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        java.math.BigInteger[] primitiveArray = unwrap(values);
        java.math.BigInteger median = MathUtils.median(primitiveArray);
        return new org.openl.meta.BigIntegerValue(new org.openl.meta.BigIntegerValue(median), NumberOperations.MEDIAN, values);
    }

     /**
     * Compares value1 and value2 and returns the max value
     * @param value1
     * @param value2
     * @return max value
     */
    public static org.openl.meta.BigIntegerValue max(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MAX.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.BigIntegerValue(MathUtils.max(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MAX,
            new org.openl.meta.BigIntegerValue[] { value1, value2 });
    }
     /**
     * Compares value1 and value2 and returns the min value
     * @param value1
     * @param value2
     * @return min value
     */
    public static org.openl.meta.BigIntegerValue min(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MIN.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.BigIntegerValue(MathUtils.min(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MIN,
            new org.openl.meta.BigIntegerValue[] { value1, value2 });
    }

    /**
     * 
     * @param values an array org.openl.meta.BigIntegerValue, must not be null
     * @return org.openl.meta.BigIntegerValue the max element from array
     */
    public static org.openl.meta.BigIntegerValue max(org.openl.meta.BigIntegerValue[] values) {
        org.openl.meta.BigIntegerValue result = (org.openl.meta.BigIntegerValue) MathUtils.max(values);

        return new org.openl.meta.BigIntegerValue((org.openl.meta.BigIntegerValue) getAppropriateValue(values, result),
            NumberOperations.MAX_IN_ARRAY, values);
    }
    /**
     * 
     * @param values an array org.openl.meta.BigIntegerValue, must not be null
     * @return org.openl.meta.BigIntegerValue the min element from array
     */
    public static org.openl.meta.BigIntegerValue min(org.openl.meta.BigIntegerValue[] values) {
        org.openl.meta.BigIntegerValue result = (org.openl.meta.BigIntegerValue) MathUtils.min(values);

        return new org.openl.meta.BigIntegerValue((org.openl.meta.BigIntegerValue) getAppropriateValue(values, result),
            NumberOperations.MIN_IN_ARRAY, values);
    }
        /**
     * 
     * @param value of variable which should be copied
     * @param name of new variable
     * @return the new org.openl.meta.BigIntegerValue variable with name <b>name</b> and value <b>value</b>
     */
    public static org.openl.meta.BigIntegerValue copy(org.openl.meta.BigIntegerValue value, String name) {
        if (value.getName() == null) {
            value.setName(name);

            return value;
        } else if (!value.getName().equals(name)) {
            org.openl.meta.BigIntegerValue result = new org.openl.meta.BigIntegerValue (value, NumberOperations.COPY, 
                new org.openl.meta.BigIntegerValue[] { value });
            result.setName(name);

            return result;
        }
        return value;
    }

    //REM
    /**
     * Divides left hand operand by right hand operand and returns remainder
     * @param value1 org.openl.meta.BigIntegerValue 
     * @param value2 org.openl.meta.BigIntegerValue 
     * @return remainder from division value1 by value2
     */
    public static org.openl.meta.BigIntegerValue rem(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // Commented to support operations with nulls. See also MathUtils.mod()
        // validate(value1, value2, Formulas.REM.toString());
        if (value1 == null || value2 == null) {
            return ZERO;
        }

        return new org.openl.meta.BigIntegerValue(value1, value2, Operators.rem(value1.getValue(), value2.getValue()),
            Formulas.REM);
    }

    //ADD
     /**
     * Adds left hand operand to right hand operand
     * @param value1 org.openl.meta.BigIntegerValue
     * @param value2 org.openl.meta.BigIntegerValue
     * @return the result of addition operation
     */
    public static org.openl.meta.BigIntegerValue add(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.ADD.toString());
        //conditions big types
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.BigIntegerValue(value1, value2, Operators.add(value1.getValue(), value2.getValue()),
            Formulas.ADD);
}

    // MULTIPLY
     /**
     * Multiplies left hand operand to right hand operand
     * @param value1 org.openl.meta.BigIntegerValue
     * @param value2 org.openl.meta.BigIntegerValue
     * @return the result of multiplication  operation
     */
    public static org.openl.meta.BigIntegerValue multiply(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.MULTIPLY.toString());
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.BigIntegerValue(value1, value2, Operators.multiply(value1.getValue(), value2.getValue()),
            Formulas.MULTIPLY);
    }

    //SUBTRACT
    /**
     * Subtracts left hand operand to right hand operand
     * @param value1 org.openl.meta.BigIntegerValue
     * @param value2 org.openl.meta.BigIntegerValue
     * @return the result of subtraction  operation
     */
    public static org.openl.meta.BigIntegerValue subtract(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
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

        return new org.openl.meta.BigIntegerValue(value1, value2, Operators.subtract(value1.getValue(), value2.getValue()), 
            Formulas.SUBTRACT);
    }

    // DIVIDE
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.BigIntegerValue
     * @param value2 org.openl.meta.BigIntegerValue
     * @return the result of division  operation
     */
    public static org.openl.meta.BigIntegerValue divide(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.DIVIDE.toString());
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            if (value2 != null && value2.doubleValue() != 0) {
                return new org.openl.meta.BigIntegerValue(value1, value2, divide(ONE, value2).getValue(), Formulas.DIVIDE);
            }
        }

        if (value2 == null) {
            return new org.openl.meta.BigIntegerValue(value1, value2, value1.getValue(), Formulas.DIVIDE);
        }

        if (value2.doubleValue() == 0) {
            throw new OpenlNotCheckedException("Division by zero");
        }

        return new org.openl.meta.BigIntegerValue(value1, value2, Operators.divide(value1.getValue(), value2.getValue()),
            Formulas.DIVIDE);
    }

    // QUAOTIENT
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.BigIntegerValue
     * @param value2 org.openl.meta.BigIntegerValue
     * @return LongValue the result of division  operation
     */
    public static LongValue quotient(org.openl.meta.BigIntegerValue number, org.openl.meta.BigIntegerValue divisor) {
        if (number != null && divisor != null) {
            LongValue result = new LongValue(MathUtils.quotient(number.getValue(), divisor.getValue()));
            return new LongValue(result, NumberOperations.QUOTIENT, null);
        }
        return null;
    }

    // generated product function for big types
     /**
     * Multiplies the numbers from the provided array and returns the product as a number.
     * @param values an array of IntValue which will be converted to DoubleValue
     * @return the product as a number
     */
    public static org.openl.meta.BigIntegerValue product(org.openl.meta.BigIntegerValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        java.math.BigInteger[] primitiveArray = unwrap(values);
        java.math.BigInteger product = MathUtils.product(primitiveArray);
        // we loose the parameters, but not the result of computation.
        return new org.openl.meta.BigIntegerValue(new org.openl.meta.BigIntegerValue(product), NumberOperations.PRODUCT, null);
    }
     /**
     *   
     * @param number
     * @param divisor
     * @return the remainder after a number is divided by a divisor. The result is a numeric value and has the same sign as the devisor.
     */
    public static org.openl.meta.BigIntegerValue mod(org.openl.meta.BigIntegerValue number, org.openl.meta.BigIntegerValue divisor) {
        if (number != null && divisor != null) {
            org.openl.meta.BigIntegerValue result = new org.openl.meta.BigIntegerValue(MathUtils.mod(number.getValue(), divisor.getValue()));
            return new org.openl.meta.BigIntegerValue(result, NumberOperations.MOD, new org.openl.meta.BigIntegerValue[]{number, divisor} );
        }
        return null;
    }

    /**
     * Sorts the array <b>values</b> in ascending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.BigIntegerValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.BigIntegerValue small(org.openl.meta.BigIntegerValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        java.math.BigInteger[] primitiveArray = unwrap(values);
        java.math.BigInteger small = MathUtils.small(primitiveArray, position);
        return new org.openl.meta.BigIntegerValue((org.openl.meta.BigIntegerValue) getAppropriateValue(values, new org.openl.meta.BigIntegerValue(small)), 
            NumberOperations.SMALL, values);
    }

    /**
     * Sorts the array <b>values</b> in descending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.BigIntegerValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.BigIntegerValue big(org.openl.meta.BigIntegerValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        java.math.BigInteger[] primitiveArray = unwrap(values);
        java.math.BigInteger big = MathUtils.big(primitiveArray, position);
        return new org.openl.meta.BigIntegerValue((org.openl.meta.BigIntegerValue) getAppropriateValue(values, new org.openl.meta.BigIntegerValue(big)),
            NumberOperations.BIG, values);
    }

    /**
     * 
     * @param value1
     * @param value2
     * @return the result of value1 raised to the power of value2
     */
    public static org.openl.meta.BigIntegerValue pow(org.openl.meta.BigIntegerValue value1, org.openl.meta.BigIntegerValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        //
        // validate(value1, value2, NumberOperations.POW);
        if (value1 == null) {
            return value2 == null ? null : new org.openl.meta.BigIntegerValue("0");
        } else if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.BigIntegerValue(new org.openl.meta.BigIntegerValue(Operators.pow(value1.getValue(), value2.getValue())), 
            NumberOperations.POW, new org.openl.meta.BigIntegerValue[] { value1, value2 });
    }

    /**
     * 
     * @param value
     * @return the absolute value (module) of the value <b>value </b>
     */
    public static org.openl.meta.BigIntegerValue abs(org.openl.meta.BigIntegerValue value) {
        // Commented to support operations with nulls.
        // validate(value, NumberOperations.ABS);
        if (value == null) {
            return null;
        }
        // evaluate result
        org.openl.meta.BigIntegerValue result = new org.openl.meta.BigIntegerValue(Operators.abs(value.getValue()));
        // create instance with information about last operation
        return new org.openl.meta.BigIntegerValue(result, NumberOperations.ABS, new org.openl.meta.BigIntegerValue[] { value });
    }

    /**
     * 
     * @param value
     * @return the negative value of the <b>value</b>
     */
    public static org.openl.meta.BigIntegerValue negative(org.openl.meta.BigIntegerValue value) {
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
    public static org.openl.meta.BigIntegerValue inc(org.openl.meta.BigIntegerValue value) {
        return add(value, ONE);
    }

    /**
     * 
     * @param value
     * @return the <b>value</b>
     */
    public static org.openl.meta.BigIntegerValue positive(org.openl.meta.BigIntegerValue value) {
        return value;
    }

    /**
     * 
     * @param value
     * @return the <b>value </b> decreased by 1
     */
    public static org.openl.meta.BigIntegerValue dec(org.openl.meta.BigIntegerValue value) {
        return subtract(value, ONE);
    }

    // Autocasts

    /**
     * Is used to overload implicit cast operators from byte to org.openl.meta.BigIntegerValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.BigIntegerValue
     */
    public static org.openl.meta.BigIntegerValue autocast(byte x, org.openl.meta.BigIntegerValue y) {
        return new org.openl.meta.BigIntegerValue(String.valueOf(x));
    }
    /**
     * Is used to overload implicit cast operators from short to org.openl.meta.BigIntegerValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.BigIntegerValue
     */
    public static org.openl.meta.BigIntegerValue autocast(short x, org.openl.meta.BigIntegerValue y) {
        return new org.openl.meta.BigIntegerValue(String.valueOf(x));
    }
    /**
     * Is used to overload implicit cast operators from int to org.openl.meta.BigIntegerValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.BigIntegerValue
     */
    public static org.openl.meta.BigIntegerValue autocast(int x, org.openl.meta.BigIntegerValue y) {
        return new org.openl.meta.BigIntegerValue(String.valueOf(x));
    }
    /**
     * Is used to overload implicit cast operators from long to org.openl.meta.BigIntegerValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.BigIntegerValue
     */
    public static org.openl.meta.BigIntegerValue autocast(long x, org.openl.meta.BigIntegerValue y) {
        return new org.openl.meta.BigIntegerValue(String.valueOf(x));
    }

    // Constructors
    public BigIntegerValue(java.math.BigInteger value) {
        this.value = value;
    }

    public BigIntegerValue(java.math.BigInteger value, String name) {
        super(name);
        this.value = value;
    }

    public BigIntegerValue(java.math.BigInteger value, IMetaInfo metaInfo) {
        super(metaInfo);
        this.value = value;
    }

    /**Formula constructor**/
    public BigIntegerValue(org.openl.meta.BigIntegerValue lv1, org.openl.meta.BigIntegerValue lv2, java.math.BigInteger value, Formulas operand) {
        super(lv1, lv2, operand);
        this.value = value;
    }

    /**Cast constructor**/
    public BigIntegerValue(String valueString, ExplanationNumberValue<?> beforeCastValue, boolean autocast) {
        super(beforeCastValue, new CastOperand("BigIntegerValue", autocast));
        this.value = new java.math.BigInteger(valueString);
    }

    /**
    *Copy the current value with new name <b>name</b>
    */
    @Override
    public org.openl.meta.BigIntegerValue copy(String name) {
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
    public java.math.BigInteger getValue() {
        return value;
    }

    /**
    * Sets the value of the current variable
    */
    public void setValue(java.math.BigInteger value) {
        this.value = value;
    }

    //Equals
    @Override
     /**
     * Indicates whether some other object is "equal to" this org.openl.meta.BigIntegerValue variable. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof org.openl.meta.BigIntegerValue) {
            org.openl.meta.BigIntegerValue secondObj = (org.openl.meta.BigIntegerValue) obj;
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
    public static org.openl.meta.BigIntegerValue[] sort (org.openl.meta.BigIntegerValue[] values ) {
        org.openl.meta.BigIntegerValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new org.openl.meta.BigIntegerValue[values.length];
           org.openl.meta.BigIntegerValue[] notNullArray = ArrayTool.removeNulls(values);

            Arrays.sort(notNullArray);

            /* Filling sortedArray by sorted and null values */
            for (int i = 0; i < notNullArray.length; i++) {
                sortedArray[i] = notNullArray[i];
            }
        }
        return sortedArray;
    }
        // <<< END INSERT Functions >>>

    // ******* Autocasts 8*************

    public static BigIntegerValue autocast(BigInteger x, BigIntegerValue y) {
        if (x == null) {
            return null;
        }

        return new BigIntegerValue(x);
    }

    public static BigIntegerValue autocast(BigIntegerValue x, BigDecimalValue y) {
        if (x == null) {
            return null;
        }
        return new BigIntegerValue(String.valueOf(x.getValue()), x, true);
    }

    // ******* Casts 8*************

    public static byte cast(BigIntegerValue x, byte y) {
        return x.byteValue();
    }

    public static short cast(BigIntegerValue x, short y) {
        return x.shortValue();
    }

    public static char cast(BigIntegerValue x, char y) {
        return (char) x.intValue();
    }

    public static int cast(BigIntegerValue x, int y) {
        return x.intValue();
    }

    public static long cast(BigIntegerValue x, long y) {
        return x.longValue();
    }

    public static float cast(BigIntegerValue x, float y) {
        return x.floatValue();
    }

    public static double cast(BigIntegerValue x, double y) {
        return x.doubleValue();
    }

    public static BigInteger cast(BigIntegerValue x, BigInteger y) {
        if (x == null) {
            return null;
        }

        return x.getValue();
    }

    public static ByteValue cast(BigIntegerValue x, ByteValue y) {
        if (x == null) {
            return null;
        }
        return new ByteValue(x.byteValue(), x, false);
    }

    public static ShortValue cast(BigIntegerValue x, ShortValue y) {
        if (x == null) {
            return null;
        }
        return new ShortValue(x.shortValue(), x, false);
    }

    public static IntValue cast(BigIntegerValue x, IntValue y) {
        if (x == null) {
            return null;
        }
        return new IntValue(x.intValue(), x, false);
    }

    public static LongValue cast(BigIntegerValue x, LongValue y) {
        if (x == null) {
            return null;
        }
        return new LongValue(x.longValue(), x, false);
    }

    public static FloatValue cast(BigIntegerValue x, FloatValue y) {
        if (x == null) {
            return null;
        }
        return new FloatValue(x.floatValue(), x, false);
    }

    public static DoubleValue cast(BigIntegerValue x, DoubleValue y) {
        if (x == null) {
            return null;
        }
        return new DoubleValue(x.doubleValue(), x, false);
    }

    public BigIntegerValue(String valueString) {
        value = new BigInteger(valueString);
    }

    public BigIntegerValue(String value, String name) {
        super(name);
        this.value = new BigInteger(value);
    }

    public BigIntegerValue(String value, IMetaInfo metaInfo) {
        super(metaInfo);
        this.value = new BigInteger(value);
    }

    /** Function constructor **/
    public BigIntegerValue(BigIntegerValue result, NumberOperations function, BigIntegerValue[] params) {
        super(function, params);
        this.value = result.getValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    public int compareTo(Number o) {
        if (o == null) {
            return 1;
        } else if (o instanceof BigIntegerValue) {
            return value.compareTo(((BigIntegerValue) o).getValue());
        } else {
            throw new OpenlNotCheckedException("Can`t compare BigIntegerValue with unknown type.");
        }
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    private static BigInteger[] unwrap(BigIntegerValue[] values) {
        values = ArrayTool.removeNulls(values);

        BigInteger[] unwrapArray = new BigInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            unwrapArray[i] = values[i].value;
        }
        return unwrapArray;
    }


}
