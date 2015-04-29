package org.openl.meta;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.ArrayUtils;
import org.openl.binding.impl.Operators;
import org.openl.exception.OpenlNotCheckedException;
import org.openl.meta.LongValue.LongValueAdapter;
import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.meta.number.CastOperand;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.LogicalExpressions;
import org.openl.meta.number.NumberOperations;
import org.openl.util.ArrayTool;
import org.openl.util.math.MathUtils;

@XmlRootElement
@XmlJavaTypeAdapter(LongValueAdapter.class)
public class LongValue extends ExplanationNumberValue<LongValue> {

    private static final long serialVersionUID = -437788531108803012L;

    private static final LongValue ZERO = new LongValue((long) 0);
    private static final LongValue ONE = new LongValue((long) 1);
    private static final LongValue MINUS_ONE = new LongValue((long) -1);

    public static class LongValueAdapter extends XmlAdapter<Long,LongValue> {
        public LongValue unmarshal(Long val) throws Exception {
            return new LongValue(val);
        }
        
        public Long marshal(LongValue val) throws Exception {
            return val.getValue();
        }
    }
    
    // <<< INSERT Functions >>>
    private long value;


    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 equal value2
     */
    public static boolean eq(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
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
    public static boolean ge(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        validate(value1, value2, LogicalExpressions.GE.toString());

        return Operators.ge(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 greater value2
     */
    public static boolean gt(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        validate(value1, value2, LogicalExpressions.GT.toString());

        return Operators.gt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less or equal value2
     */
    public static boolean le(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        validate(value1, value2, LogicalExpressions.LE.toString());

        return Operators.le(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 less value2
     */
    public static boolean lt(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        validate(value1, value2, LogicalExpressions.LT.toString());

        return Operators.lt(value1.getValue(), value2.getValue());
    }
    /**
     * Compares two values
     * @param value1
     * @param value2
     * @return true if  value1 not equal value2
     */
    public static boolean ne(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        if (value1 == null || value2 == null){
            return value1 != value2;
        }

        return Operators.ne(value1.getValue(), value2.getValue());
    }

     /**
     * average
     * @param values  array of org.openl.meta.LongValue values
     * @return the average value from the array
     */
    public static org.openl.meta.LongValue avg(org.openl.meta.LongValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        long[] primitiveArray = unwrap(values);
        long avg = MathUtils.avg(primitiveArray);
        return new org.openl.meta.LongValue(new org.openl.meta.LongValue(avg), NumberOperations.AVG, values);
    }
     /**
     * sum
     * @param values  array of org.openl.meta.LongValue values
     * @return the sum value from the array
     */
    public static org.openl.meta.LongValue sum(org.openl.meta.LongValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        long[] primitiveArray = unwrap(values);
        long sum = MathUtils.sum(primitiveArray);
        return new org.openl.meta.LongValue(new org.openl.meta.LongValue(sum), NumberOperations.SUM, values);
    }
     /**
     * median
     * @param values  array of org.openl.meta.LongValue values
     * @return the median value from the array
     */
    public static org.openl.meta.LongValue median(org.openl.meta.LongValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        long[] primitiveArray = unwrap(values);
        long median = MathUtils.median(primitiveArray);
        return new org.openl.meta.LongValue(new org.openl.meta.LongValue(median), NumberOperations.MEDIAN, values);
    }

     /**
     * Compares value1 and value2 and returns the max value
     * @param value1
     * @param value2
     * @return max value
     */
    public static org.openl.meta.LongValue max(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MAX.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.LongValue(MathUtils.max(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MAX,
            new org.openl.meta.LongValue[] { value1, value2 });
    }
     /**
     * Compares value1 and value2 and returns the min value
     * @param value1
     * @param value2
     * @return min value
     */
    public static org.openl.meta.LongValue min(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        // validate(value1, value2, NumberOperations.MIN.toString());
        if (value1 == null)
            return value2; 
        if (value2 == null)
            return value1; 

        return new org.openl.meta.LongValue(MathUtils.min(value1.getValue(), value2.getValue()) ? value1 : value2,
            NumberOperations.MIN,
            new org.openl.meta.LongValue[] { value1, value2 });
    }

    /**
     * 
     * @param values an array org.openl.meta.LongValue, must not be null
     * @return org.openl.meta.LongValue the max element from array
     */
    public static org.openl.meta.LongValue max(org.openl.meta.LongValue[] values) {
        org.openl.meta.LongValue result = (org.openl.meta.LongValue) MathUtils.max(values);

        return new org.openl.meta.LongValue((org.openl.meta.LongValue) getAppropriateValue(values, result),
            NumberOperations.MAX_IN_ARRAY, values);
    }
    /**
     * 
     * @param values an array org.openl.meta.LongValue, must not be null
     * @return org.openl.meta.LongValue the min element from array
     */
    public static org.openl.meta.LongValue min(org.openl.meta.LongValue[] values) {
        org.openl.meta.LongValue result = (org.openl.meta.LongValue) MathUtils.min(values);

        return new org.openl.meta.LongValue((org.openl.meta.LongValue) getAppropriateValue(values, result),
            NumberOperations.MIN_IN_ARRAY, values);
    }
        /**
     * 
     * @param value of variable which should be copied
     * @param name of new variable
     * @return the new org.openl.meta.LongValue variable with name <b>name</b> and value <b>value</b>
     */
    public static org.openl.meta.LongValue copy(org.openl.meta.LongValue value, String name) {
        if (value.getName() == null) {
            value.setName(name);

            return value;
        } else if (!value.getName().equals(name)) {
            org.openl.meta.LongValue result = new org.openl.meta.LongValue (value, NumberOperations.COPY, 
                new org.openl.meta.LongValue[] { value });
            result.setName(name);

            return result;
        }
        return value;
    }

    //REM
    /**
     * Divides left hand operand by right hand operand and returns remainder
     * @param value1 org.openl.meta.LongValue 
     * @param value2 org.openl.meta.LongValue 
     * @return remainder from division value1 by value2
     */
    public static org.openl.meta.LongValue rem(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        // Commented to support operations with nulls. See also MathUtils.mod()
        // validate(value1, value2, Formulas.REM.toString());
        if (value1 == null || value2 == null) {
            return ZERO;
        }

        return new org.openl.meta.LongValue(value1, value2, Operators.rem(value1.getValue(), value2.getValue()),
            Formulas.REM);
    }

    //ADD
    public static LongValue add(LongValue value1, String value2) {
        if (value2 == null) {
            return value1;
        }

        if (value1 == null) {
            return new LongValue(Long.valueOf(value2));
        }
        
        long v = Long.valueOf(value2);

        return new org.openl.meta.LongValue(value1, new LongValue(v), Operators.add(value1.getValue(), v),
            Formulas.ADD);
    }
    
    public static LongValue add(String value1, LongValue value2) {
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return new LongValue(Long.valueOf(value1));
        }
        
        long v = Long.valueOf(value1);
        
        return new org.openl.meta.LongValue(new LongValue(v), value2, Operators.add(v, value2.getValue()),
            Formulas.ADD);
    }   
    
     /**
     * Adds left hand operand to right hand operand
     * @param value1 org.openl.meta.LongValue
     * @param value2 org.openl.meta.LongValue
     * @return the result of addition operation
     */
    public static org.openl.meta.LongValue add(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
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

        return new org.openl.meta.LongValue(value1, value2, Operators.add(value1.getValue(), value2.getValue()),
            Formulas.ADD);
}

    // MULTIPLY
     /**
     * Multiplies left hand operand to right hand operand
     * @param value1 org.openl.meta.LongValue
     * @param value2 org.openl.meta.LongValue
     * @return the result of multiplication  operation
     */
    public static org.openl.meta.LongValue multiply(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.MULTIPLY.toString());
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.LongValue(value1, value2, Operators.multiply(value1.getValue(), value2.getValue()),
            Formulas.MULTIPLY);
    }

    //SUBTRACT
    /**
     * Subtracts left hand operand to right hand operand
     * @param value1 org.openl.meta.LongValue
     * @param value2 org.openl.meta.LongValue
     * @return the result of subtraction  operation
     */
    public static org.openl.meta.LongValue subtract(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
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

        return new org.openl.meta.LongValue(value1, value2, Operators.subtract(value1.getValue(), value2.getValue()), 
            Formulas.SUBTRACT);
    }

    // DIVIDE
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.LongValue
     * @param value2 org.openl.meta.LongValue
     * @return the result of division  operation
     */
    public static org.openl.meta.LongValue divide(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        // temporary commented to support operations with nulls
        //
        //        validate(value1, value2, Formulas.DIVIDE.toString());
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            if (value2 != null && value2.doubleValue() != 0) {
                return new org.openl.meta.LongValue(value1, value2, divide(ONE, value2).getValue(), Formulas.DIVIDE);
            }
        }

        if (value2 == null) {
            return new org.openl.meta.LongValue(value1, value2, value1.getValue(), Formulas.DIVIDE);
        }

        if (value2.doubleValue() == 0) {
            throw new OpenlNotCheckedException("Division by zero");
        }

        return new org.openl.meta.LongValue(value1, value2, Operators.divide(value1.getValue(), value2.getValue()),
            Formulas.DIVIDE);
    }

    // QUAOTIENT
    /**
     * Divides left hand operand by right hand operand
     * @param value1 org.openl.meta.LongValue
     * @param value2 org.openl.meta.LongValue
     * @return LongValue the result of division  operation
     */
    public static LongValue quotient(org.openl.meta.LongValue number, org.openl.meta.LongValue divisor) {
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
    public static DoubleValue product(org.openl.meta.LongValue[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        long[] primitiveArray = unwrap(values);
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
    public static org.openl.meta.LongValue mod(org.openl.meta.LongValue number, org.openl.meta.LongValue divisor) {
        if (number != null && divisor != null) {
            org.openl.meta.LongValue result = new org.openl.meta.LongValue(MathUtils.mod(number.getValue(), divisor.getValue()));
            return new org.openl.meta.LongValue(result, NumberOperations.MOD, new org.openl.meta.LongValue[]{number, divisor} );
        }
        return null;
    }

    /**
     * Sorts the array <b>values</b> in ascending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.LongValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.LongValue small(org.openl.meta.LongValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        long[] primitiveArray = unwrap(values);
        long small = MathUtils.small(primitiveArray, position);
        return new org.openl.meta.LongValue((org.openl.meta.LongValue) getAppropriateValue(values, new org.openl.meta.LongValue(small)), 
            NumberOperations.SMALL, values);
    }

    /**
     * Sorts the array <b>values</b> in descending order and returns the value from array <b>values</b> at position <b>position</b>
     * @param values array of org.openl.meta.LongValue values 
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static org.openl.meta.LongValue big(org.openl.meta.LongValue[] values, int position) {
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }
        long[] primitiveArray = unwrap(values);
        long big = MathUtils.big(primitiveArray, position);
        return new org.openl.meta.LongValue((org.openl.meta.LongValue) getAppropriateValue(values, new org.openl.meta.LongValue(big)),
            NumberOperations.BIG, values);
    }

    /**
     * 
     * @param value1
     * @param value2
     * @return the result of value1 raised to the power of value2
     */
    public static org.openl.meta.LongValue pow(org.openl.meta.LongValue value1, org.openl.meta.LongValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        //
        // validate(value1, value2, NumberOperations.POW);
        if (value1 == null) {
            return value2 == null ? null : new org.openl.meta.LongValue((long) 0);
        } else if (value2 == null) {
            return value1;
        }

        return new org.openl.meta.LongValue(new org.openl.meta.LongValue(Operators.pow(value1.getValue(), value2.getValue())), 
            NumberOperations.POW, new org.openl.meta.LongValue[] { value1, value2 });
    }

    /**
     * 
     * @param value
     * @return the absolute value (module) of the value <b>value </b>
     */
    public static org.openl.meta.LongValue abs(org.openl.meta.LongValue value) {
        // Commented to support operations with nulls.
        // validate(value, NumberOperations.ABS);
        if (value == null) {
            return null;
        }
        // evaluate result
        org.openl.meta.LongValue result = new org.openl.meta.LongValue(Operators.abs(value.getValue()));
        // create instance with information about last operation
        return new org.openl.meta.LongValue(result, NumberOperations.ABS, new org.openl.meta.LongValue[] { value });
    }

    /**
     * 
     * @param value
     * @return the negative value of the <b>value</b>
     */
    public static org.openl.meta.LongValue negative(org.openl.meta.LongValue value) {
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
    public static org.openl.meta.LongValue inc(org.openl.meta.LongValue value) {
        return add(value, ONE);
    }

    /**
     * 
     * @param value
     * @return the <b>value</b>
     */
    public static org.openl.meta.LongValue positive(org.openl.meta.LongValue value) {
        return value;
    }

    /**
     * 
     * @param value
     * @return the <b>value </b> decreased by 1
     */
    public static org.openl.meta.LongValue dec(org.openl.meta.LongValue value) {
        return subtract(value, ONE);
    }

    // Autocasts

    /**
     * Is used to overload implicit cast operators from byte to org.openl.meta.LongValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.LongValue
     */
    public static org.openl.meta.LongValue autocast(byte x, org.openl.meta.LongValue y) {
        return new org.openl.meta.LongValue((long) x);
    }
    /**
     * Is used to overload implicit cast operators from short to org.openl.meta.LongValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.LongValue
     */
    public static org.openl.meta.LongValue autocast(short x, org.openl.meta.LongValue y) {
        return new org.openl.meta.LongValue((long) x);
    }
    /**
     * Is used to overload implicit cast operators from int to org.openl.meta.LongValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.LongValue
     */
    public static org.openl.meta.LongValue autocast(int x, org.openl.meta.LongValue y) {
        return new org.openl.meta.LongValue((long) x);
    }
    /**
     * Is used to overload implicit cast operators from long to org.openl.meta.LongValue
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.LongValue
     */
    public static org.openl.meta.LongValue autocast(long x, org.openl.meta.LongValue y) {
        return new org.openl.meta.LongValue((long) x);
    }

    // Constructors
    public LongValue(long value) {
        this.value = value;
    }

    /**Formula constructor**/
    public LongValue(org.openl.meta.LongValue lv1, org.openl.meta.LongValue lv2, long value, Formulas operand) {
        super(lv1, lv2, operand);
        this.value = value;
    }

    /**Cast constructor**/
    public LongValue(long value, ExplanationNumberValue<?> beforeCastValue, boolean autocast) {
        super(beforeCastValue, new CastOperand("LongValue", autocast));
        this.value = value;
    }

    /**
    *Copy the current value with new name <b>name</b>
    */
    @Override
    public org.openl.meta.LongValue copy(String name) {
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
    public long getValue() {
        return value;
    }

    /**
    * Sets the value of the current variable
    */
    public void setValue(long value) {
        this.value = value;
    }

    //Equals
    @Override
     /**
     * Indicates whether some other object is "equal to" this org.openl.meta.LongValue variable. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof org.openl.meta.LongValue) {
            org.openl.meta.LongValue secondObj = (org.openl.meta.LongValue) obj;
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
    public static org.openl.meta.LongValue[] sort (org.openl.meta.LongValue[] values ) {
        org.openl.meta.LongValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new org.openl.meta.LongValue[values.length];
           org.openl.meta.LongValue[] notNullArray = ArrayTool.removeNulls(values);

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

    public static LongValue autocast(Long x, LongValue y) {
        if (x == null) {
            return null;
        }

        return new LongValue(x);
    }

    public static FloatValue autocast(LongValue x, FloatValue y) {
        if (x == null) {
            return null;
        }

        return new FloatValue(x.getValue(), x, true);
    }

    public static DoubleValue autocast(LongValue x, DoubleValue y) {
        if (x == null) {
            return null;
        }

        return new DoubleValue(x.getValue(), x, true);
    }

    public static BigIntegerValue autocast(LongValue x, BigIntegerValue y) {
        if (x == null) {
            return null;
        }
        return new BigIntegerValue(String.valueOf(x.getValue()), x, true);
    }

    public static BigDecimalValue autocast(LongValue x, BigDecimalValue y) {
        if (x == null) {
            return null;
        }
        return new BigDecimalValue(String.valueOf(x.getValue()), x, true);
    }
    
    public static String autocast(LongValue x, String y) {
        if (x == null) {
            return null;
        }
        return x.toString();
    }
    
    public static Integer distance(LongValue x, String y) {
        return 11;
    }

    public static LongValue autocast(String x, LongValue y) {
        if (x == null) {
            return null;
        }
        return new LongValue(Long.valueOf(x));
    }
    
    public static Integer distance(String x, LongValue y) {
        return 10;
    }

    // ******* Casts *************

    public static byte cast(LongValue x, byte y) {
        return x.byteValue();
    }

    public static short cast(LongValue x, short y) {
        return x.shortValue();
    }

    public static char cast(LongValue x, char y) {
        return (char) x.longValue();
    }

    public static int cast(LongValue x, int y) {
        return x.intValue();
    }

    public static long cast(LongValue x, long y) {
        return x.longValue();
    }

    public static float cast(LongValue x, float y) {
        return x.floatValue();
    }

    public static double cast(LongValue x, double y) {
        return x.doubleValue();
    }

    public static Long cast(LongValue x, Long y) {
        if (x == null) {
            return null;
        }

        return x.longValue();
    }

    public static ByteValue cast(LongValue x, ByteValue y) {
        if (x == null) {
            return null;
        }
        return new ByteValue(x.byteValue(), x, false);
    }

    public static ShortValue cast(LongValue x, ShortValue y) {
        if (x == null) {
            return null;
        }
        return new ShortValue(x.shortValue(), x, false);
    }

    public static IntValue cast(LongValue x, IntValue y) {
        if (x == null) {
            return null;
        }
        return new IntValue(x.intValue(), x, false);
    }

    public LongValue(String valueString) {
        value = Long.parseLong(valueString);
    }

    /** Function constructor **/
    public LongValue(LongValue result, NumberOperations function, LongValue[] params) {
        super(function, params);
        this.value = result.longValue();
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
        return value;
    }

    public int compareTo(Number o) {
        return value < o.longValue() ? -1 : (value == o.longValue() ? 0 : 1);
    }

    @Override
    public int hashCode() {
        return ((Long) value).hashCode();
    }

    private static long[] unwrap(LongValue[] values) {
        values = ArrayTool.removeNulls(values);

        long[] longArray = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            longArray[i] = values[i].getValue();
        }
        return longArray;

    }

}
