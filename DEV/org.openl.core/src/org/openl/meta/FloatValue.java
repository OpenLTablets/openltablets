package org.openl.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openl.binding.impl.Operators;
import org.openl.binding.impl.operator.Comparison;
import org.openl.meta.FloatValue.FloatValueAdapter;
import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.meta.number.CastOperand;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.NumberOperations;
import org.openl.rules.util.Avg;
import org.openl.rules.util.Round;
import org.openl.rules.util.Statistics;
import org.openl.rules.util.Sum;
import org.openl.util.ArrayTool;
import org.openl.util.math.MathUtils;

@XmlRootElement
@XmlJavaTypeAdapter(FloatValueAdapter.class)
@Deprecated
public class FloatValue extends ExplanationNumberValue<FloatValue> implements Comparable<FloatValue> {

    private static final long serialVersionUID = -8235832583740963916L;

    private static final FloatValue ZERO = new FloatValue(0);
    private static final FloatValue ONE = new FloatValue(1);
    private static final FloatValue MINUS_ONE = new FloatValue(-1);

    public static class FloatValueAdapter extends XmlAdapter<Float, FloatValue> {
        @Override
        public FloatValue unmarshal(Float val) {
            if (val == null) {
                return null;
            }
            return new FloatValue(val);
        }

        @Override
        public Float marshal(FloatValue val) {
            if (val == null) {
                return null;
            }
            return val.getValue();
        }
    }

    private final float value;

    private static FloatValue instance(Float result, NumberOperations operation, FloatValue... values) {
        return result == null ? null : new FloatValue(result, operation, values);
    }

    private static FloatValue instance(FloatValue result, NumberOperations operation, FloatValue... values) {
        return result == null ? null : new FloatValue(result.floatValue(), operation, values);
    }

    public static FloatValue max(FloatValue... values) {
        return instance(Statistics.max(values), NumberOperations.MAX, values);
    }

    public static FloatValue min(FloatValue... values) {
        return instance(Statistics.min(values), NumberOperations.MIN, values);
    }

    public static FloatValue sum(FloatValue... values) {
        return instance(Sum.sum(unwrap(values)), NumberOperations.SUM, values);
    }

    public static FloatValue avg(FloatValue... values) {
        return instance(Avg.avg(unwrap(values)), NumberOperations.AVG, values);
    }

    public static FloatValue median(FloatValue... values) {
        return instance(MathUtils.median(unwrap(values)), NumberOperations.MEDIAN, values);
    }

    /**
     * Compares two values
     *
     * @param value1
     * @param value2
     * @return true if value1 equal value2
     */
    public static boolean eq(FloatValue value1, FloatValue value2) {
        if (value1 == null || value2 == null) {
            return value1 == value2;
        }
        return Comparison.eq(value1.getValue(), value2.getValue());
    }

    /**
     * Compares two values
     *
     * @param value1
     * @param value2
     * @return true if value1 greater or equal value2
     */
    public static Boolean ge(FloatValue value1, FloatValue value2) {
        Float v1 = value1 == null ? null : value1.value;
        Float v2 = value2 == null ? null : value2.value;
        return Comparison.ge(v1, v2);
    }

    /**
     * Compares two values
     *
     * @param value1
     * @param value2
     * @return true if value1 greater value2
     */
    public static Boolean gt(FloatValue value1, FloatValue value2) {
        Float v1 = value1 == null ? null : value1.value;
        Float v2 = value2 == null ? null : value2.value;
        return Comparison.gt(v1, v2);
    }

    /**
     * Compares two values
     *
     * @param value1
     * @param value2
     * @return true if value1 less or equal value2
     */
    public static Boolean le(FloatValue value1, FloatValue value2) {
        Float v1 = value1 == null ? null : value1.value;
        Float v2 = value2 == null ? null : value2.value;
        return Comparison.le(v1, v2);
    }

    /**
     * Compares two values
     *
     * @param value1
     * @param value2
     * @return true if value1 less value2
     */
    public static Boolean lt(FloatValue value1, FloatValue value2) {
        Float v1 = value1 == null ? null : value1.value;
        Float v2 = value2 == null ? null : value2.value;
        return Comparison.lt(v1, v2);
    }

    /**
     * Compares two values
     *
     * @param value1
     * @param value2
     * @return true if value1 not equal value2
     */
    public static boolean ne(FloatValue value1, FloatValue value2) {
        if (value1 == null || value2 == null) {
            return value1 != value2;
        }

        return Comparison.ne(value1.getValue(), value2.getValue());
    }

    /**
     *
     * @param value of variable which should be copied
     * @param name of new variable
     * @return the new org.openl.meta.FloatValue variable with name <b>name</b> and value <b>value</b>
     */
    public static FloatValue copy(FloatValue value, String name) {
        if (value.getName() == null) {
            value.setName(name);

            return value;
        } else if (!value.getName().equals(name)) {
            FloatValue result = new FloatValue(value.floatValue(),
                NumberOperations.COPY,
                value);
            result.setName(name);

            return result;
        }
        return value;
    }

    // REM
    /**
     * Divides left hand operand by right hand operand and returns remainder
     *
     * @param value1 org.openl.meta.FloatValue
     * @param value2 org.openl.meta.FloatValue
     * @return remainder from division value1 by value2
     */
    public static FloatValue rem(FloatValue value1, FloatValue value2) {
        // Commented to support operations with nulls. See also MathUtils.mod()
        // validate(value1, value2, Formulas.REM.toString());
        if (value1 == null || value2 == null) {
            return ZERO;
        }

        return new FloatValue(value1,
            value2,
            Operators.rem(value1.getValue(), value2.getValue()),
            Formulas.REM);
    }

    // ADD
    public static String add(FloatValue value1, String value2) {
        return value1 + value2;
    }

    public static String add(String value1, FloatValue value2) {
        return value1 + value2;
    }

    /**
     * Adds left hand operand to right hand operand
     *
     * @param value1 org.openl.meta.FloatValue
     * @param value2 org.openl.meta.FloatValue
     * @return the result of addition operation
     */
    public static FloatValue add(FloatValue value1, FloatValue value2) {
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new FloatValue(value1,
            value2,
            Operators.add(value1.getValue(), value2.getValue()),
            Formulas.ADD);
    }

    // MULTIPLY
    /**
     * Multiplies left hand operand to right hand operand
     *
     * @param value1 org.openl.meta.FloatValue
     * @param value2 org.openl.meta.FloatValue
     * @return the result of multiplication operation
     */
    public static FloatValue multiply(FloatValue value1,
                                      FloatValue value2) {
        if (value1 == null || value2 == null) {
            return null;
        }

        return new FloatValue(value1,
            value2,
            Operators.multiply(value1.getValue(), value2.getValue()),
            Formulas.MULTIPLY);
    }

    // SUBTRACT
    /**
     * Subtracts left hand operand to right hand operand
     *
     * @param value1 org.openl.meta.FloatValue
     * @param value2 org.openl.meta.FloatValue
     * @return the result of subtraction operation
     */
    public static FloatValue subtract(FloatValue value1,
                                      FloatValue value2) {
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            return negative(value2);
        }

        if (value2 == null) {
            return value1;
        }

        return new FloatValue(value1,
            value2,
            Operators.subtract(value1.getValue(), value2.getValue()),
            Formulas.SUBTRACT);
    }

    // DIVIDE
    /**
     * Divides left hand operand by right hand operand
     *
     * @param value1 org.openl.meta.FloatValue
     * @param value2 org.openl.meta.FloatValue
     * @return the result of division operation
     */
    public static FloatValue divide(FloatValue value1, FloatValue value2) {
        if (value1 == null || value2 == null) {
            return null;
        }

        return new FloatValue(value1,
            value2,
            Operators.divide(value1.getValue(), value2.getValue()),
            Formulas.DIVIDE);
    }

    // QUAOTIENT
    /**
     * Divides left hand operand by right hand operand
     *
     * @param number org.openl.meta.FloatValue
     * @param divisor org.openl.meta.FloatValue
     * @return LongValue the result of division operation
     */
    public static LongValue quotient(FloatValue number, FloatValue divisor) {
        if (number != null && divisor != null) {
            LongValue result = new LongValue(MathUtils.quotient(number.getValue(), divisor.getValue()));
            return new LongValue(result, NumberOperations.QUOTIENT, null);
        }
        return null;
    }

    /**
     *
     * @param number
     * @param divisor
     * @return the remainder after a number is divided by a divisor. The result is a numeric value and has the same sign
     *         as the devisor.
     */
    public static FloatValue mod(FloatValue number, FloatValue divisor) {
        if (number != null && divisor != null) {
            float result = MathUtils.mod(number.getValue(), divisor.getValue());
            return new FloatValue(result, NumberOperations.MOD, number, divisor);
        }
        return null;
    }

    /**
     * Sorts the array <b>values</b> in ascending order and returns the value from array <b>values</b> at position
     * <b>position</b>
     *
     * @param values array of org.openl.meta.FloatValue values
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static FloatValue small(FloatValue[] values, int position) {
        return instance(MathUtils.small(unwrap(values), position), NumberOperations.SMALL, values);
    }

    /**
     * Sorts the array <b>values</b> in descending order and returns the value from array <b>values</b> at position
     * <b>position</b>
     *
     * @param values array of org.openl.meta.FloatValue values
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static FloatValue big(FloatValue[] values, int position) {
        return instance(MathUtils.big(unwrap(values), position), NumberOperations.BIG, values);
    }

    /**
     *
     * @param value1
     * @param value2
     * @return the result of value1 raised to the power of value2
     */
    public static FloatValue pow(FloatValue value1, FloatValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        //
        // validate(value1, value2, NumberOperations.POW);
        if (value1 == null) {
            return value2 == null ? null : new FloatValue(0);
        } else if (value2 == null) {
            return value1;
        }

        return new FloatValue(Operators.pow(value1.getValue(), value2.getValue()),
            NumberOperations.POW,
            value1,
            value2);
    }

    /**
     *
     * @param value
     * @return the absolute value (module) of the value <b>value </b>
     */
    public static FloatValue abs(FloatValue value) {
        // Commented to support operations with nulls.
        // validate(value, NumberOperations.ABS);
        if (value == null) {
            return null;
        }
        // evaluate result
        float result = Math.abs(value.getValue());
        // create instance with information about last operation
        return new FloatValue(result, NumberOperations.ABS, value);
    }

    /**
     *
     * @param value
     * @return the negative value of the <b>value</b>
     */
    public static FloatValue negative(FloatValue value) {
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
    public static FloatValue inc(FloatValue value) {
        return add(value, ONE);
    }

    /**
     *
     * @param value
     * @return the <b>value</b>
     */
    public static FloatValue positive(FloatValue value) {
        return value;
    }

    /**
     *
     * @param value
     * @return the <b>value </b> decreased by 1
     */
    public static FloatValue dec(FloatValue value) {
        return subtract(value, ONE);
    }

    // Autocasts

    /**
     * Is used to overload implicit cast operators from byte to org.openl.meta.FloatValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.FloatValue
     */
    public static FloatValue autocast(byte x, FloatValue y) {
        return new FloatValue(x);
    }

    /**
     * Is used to overload implicit cast operators from short to org.openl.meta.FloatValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.FloatValue
     */
    public static FloatValue autocast(short x, FloatValue y) {
        return new FloatValue(x);
    }

    /**
     * Is used to overload implicit cast operators from int to org.openl.meta.FloatValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.FloatValue
     */
    public static FloatValue autocast(int x, FloatValue y) {
        return new FloatValue(x);
    }

    /**
     * Is used to overload implicit cast operators from char to org.openl.meta.FloatValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.FloatValue
     */
    public static FloatValue autocast(char x, FloatValue y) {
        return new FloatValue(x);
    }

    /**
     * Is used to overload implicit cast operators from long to org.openl.meta.FloatValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.FloatValue
     */
    public static FloatValue autocast(long x, FloatValue y) {
        return new FloatValue(x);
    }

    /**
     * Is used to overload implicit cast operators from float to org.openl.meta.FloatValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.FloatValue
     */
    public static FloatValue autocast(float x, FloatValue y) {
        return new FloatValue(x);
    }

    // Constructors
    public FloatValue(float value) {
        this.value = value;
    }

    /** Formula constructor **/
    public FloatValue(FloatValue lv1, FloatValue lv2, float value, Formulas operand) {
        super(lv1, lv2, operand);
        this.value = value;
    }

    /** Cast constructor **/
    public FloatValue(float value, ExplanationNumberValue<?> beforeCastValue, boolean autocast) {
        super(beforeCastValue, new CastOperand("FloatValue", autocast));
        this.value = value;
    }

    /**
     * Copy the current value with new name <b>name</b>
     */
    @Override
    public FloatValue copy(String name) {
        return copy(this, name);
    }

    /**
     * Prints the value of the current variable
     */
    @Override
    public String printValue() {
        return String.valueOf(value);
    }

    /**
     * Returns the value of the current variable
     */
    public float getValue() {
        return value;
    }

    // Equals
    @Override
    /**
     * Indicates whether some other object is "equal to" this org.openl.meta.FloatValue variable.
     */
    public boolean equals(Object obj) {
        if (obj instanceof FloatValue) {
            FloatValue secondObj = (FloatValue) obj;
            return Comparison.eq(getValue(), secondObj.getValue());
        }

        return false;
    }

    // sort
    /**
     * Sorts the array <b>values</b>
     *
     * @param values an array for sorting
     * @return the sorted array
     */
    public static FloatValue[] sort(FloatValue[] values) {
        FloatValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new FloatValue[values.length];
            FloatValue[] notNullArray = ArrayTool.removeNulls(values);

            Arrays.sort(notNullArray);

            /* Filling sortedArray by sorted and null values */
            System.arraycopy(notNullArray, 0, sortedArray, 0, notNullArray.length);
        }
        return sortedArray;
    }

    // ******* Autocasts*************

    public static DoubleValue autocast(FloatValue x, DoubleValue y) {
        if (x == null) {
            return null;
        }
        return new DoubleValue(new BigDecimal(String.valueOf(x.getValue())).doubleValue(), x, true);
    }

    public static BigDecimalValue autocast(FloatValue x, BigDecimalValue y) {
        if (x == null) {
            return null;
        }
        return new BigDecimalValue(String.valueOf(x.getValue()), x, true);
    }

    // ******* Casts *************

    public static FloatValue cast(double x, FloatValue y) {
        return new FloatValue((float) x);
    }

    public static FloatValue cast(BigInteger x, FloatValue y) {
        return new FloatValue(x.floatValue());
    }

    public static FloatValue cast(BigDecimal x, FloatValue y) {
        return new FloatValue(x.floatValue());
    }

    public static byte cast(FloatValue x, byte y) {
        return x.byteValue();
    }

    public static short cast(FloatValue x, short y) {
        return x.shortValue();
    }

    public static char cast(FloatValue x, char y) {
        return (char) x.floatValue();
    }

    public static int cast(FloatValue x, int y) {
        return x.intValue();
    }

    public static long cast(FloatValue x, long y) {
        return x.longValue();
    }

    public static float cast(FloatValue x, float y) {
        return x.floatValue();
    }

    public static double cast(FloatValue x, double y) {
        return new BigDecimal(String.valueOf(x.floatValue())).doubleValue();
    }

    public static ByteValue cast(FloatValue x, ByteValue y) {
        if (x == null) {
            return null;
        }
        return new ByteValue(x.byteValue(), x, false);
    }

    public static ShortValue cast(FloatValue x, ShortValue y) {
        if (x == null) {
            return null;
        }
        return new ShortValue(x.shortValue(), x, false);
    }

    public static IntValue cast(FloatValue x, IntValue y) {
        if (x == null) {
            return null;
        }
        return new IntValue(x.intValue(), x, false);
    }

    public static LongValue cast(FloatValue x, LongValue y) {
        if (x == null) {
            return null;
        }
        return new LongValue(x.longValue(), x, false);
    }

    public static BigInteger cast(FloatValue x, BigInteger y) {
        return new BigDecimal(String.valueOf(x.floatValue())).toBigInteger();
    }

    public static BigDecimal cast(FloatValue x, BigDecimal y) {
        return new BigDecimal(String.valueOf(x.floatValue()));
    }

    public static FloatValue round(FloatValue value) {
        if (value == null) {
            return null;
        }

        float rounded = Round.round(value.value, 0);
        return new FloatValue(rounded, NumberOperations.ROUND, value);
    }

    public static FloatValue round(FloatValue value, int scale) {
        if (value == null) {
            return null;
        }

        float rounded = Round.round(value.value, scale);
        return new FloatValue(rounded, NumberOperations.ROUND, value, new FloatValue(scale));
    }

    public static FloatValue round(FloatValue value, int scale, int roundingMethod) {
        if (value == null) {
            return null;
        }

        float rounded = Round.round(value.value, scale, roundingMethod);
        return new FloatValue(rounded, NumberOperations.ROUND, value, new FloatValue(scale));
    }

    public FloatValue(String valueString) {
        this.value = Float.parseFloat(valueString);
    }

    /** Function constructor **/
    public FloatValue(float result, NumberOperations function, FloatValue... params) {
        super(function, params);
        this.value = result;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public int compareTo(FloatValue o) {
        return Float.compare(value, o.value);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(value);
    }

    private static Float[] unwrap(FloatValue[] values) {
        if (values == null) {
            return null;
        }
        values = ArrayTool.removeNulls(values);

        Float[] unwrappedArray = new Float[values.length];
        for (int i = 0; i < values.length; i++) {
            unwrappedArray[i] = values[i].getValue();
        }
        return unwrappedArray;
    }

}
