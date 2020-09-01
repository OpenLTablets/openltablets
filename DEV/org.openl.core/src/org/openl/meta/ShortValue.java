package org.openl.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.openl.binding.impl.Operators;
import org.openl.exception.OpenLRuntimeException;
import org.openl.meta.ShortValue.ShortValueAdapter;
import org.openl.meta.explanation.ExplanationNumberValue;
import org.openl.meta.number.CastOperand;
import org.openl.meta.number.Formulas;
import org.openl.meta.number.NumberOperations;
import org.openl.rules.util.Avg;
import org.openl.rules.util.Statistics;
import org.openl.rules.util.Sum;
import org.openl.util.ArrayTool;
import org.openl.util.math.MathUtils;

@XmlRootElement
@XmlJavaTypeAdapter(ShortValueAdapter.class)
public class ShortValue extends ExplanationNumberValue<ShortValue> implements Comparable<ShortValue> {

    private static final long serialVersionUID = 5259931539737847856L;

    public static final ShortValue ZERO = new ShortValue((short) 0);
    public static final ShortValue ONE = new ShortValue((short) 1);
    public static final ShortValue MINUS_ONE = new ShortValue((short) -1);

    public static class ShortValueAdapter extends XmlAdapter<Short, ShortValue> {
        @Override
        public ShortValue unmarshal(Short val) {
            if (val == null) {
                return null;
            }
            return new ShortValue(val);
        }

        @Override
        public Short marshal(ShortValue val) {
            if (val == null) {
                return null;
            }
            return val.getValue();
        }
    }

    private final short value;

    private static DoubleValue[] toDoubleValues(ShortValue[] values) {
        if (values == null) {
            return null;
        }
        DoubleValue[] doubleValues = new DoubleValue[values.length];
        int i = 0;
        for (ShortValue value : values) {
            doubleValues[i] = autocast(value, DoubleValue.ZERO);
            i++;
        }
        return doubleValues;
    }

    private static ShortValue instance(Number result, NumberOperations operation, ShortValue... values) {
        return result == null ? null : new ShortValue(new ShortValue(result.shortValue()), operation, values);
    }

    private static ShortValue instance(ShortValue result, NumberOperations operation, ShortValue... values) {
        return result == null ? null : new ShortValue(result, operation, values);
    }

    public static ShortValue max(ShortValue... values) {
        return instance(Statistics.max(values), NumberOperations.MAX, values);
    }

    public static ShortValue min(ShortValue... values) {
        return instance(Statistics.min(values), NumberOperations.MIN, values);
    }

    public static ShortValue sum(ShortValue... values) {
        return instance(Sum.sum(unwrap(values)), NumberOperations.SUM, values);
    }

    public static DoubleValue avg(ShortValue... values) {
        return DoubleValue.instance(Avg.avg(unwrap(values)), NumberOperations.AVG, toDoubleValues(values));
    }

    public static DoubleValue median(ShortValue... values) {
        return DoubleValue.instance(MathUtils.median(unwrap(values)), NumberOperations.MEDIAN, toDoubleValues(values));
    }

    /**
     *
     * @param value of variable which should be copied
     * @param name of new variable
     * @return the new org.openl.meta.ShortValue variable with name <b>name</b> and value <b>value</b>
     */
    public static ShortValue copy(ShortValue value, String name) {
        if (value.getName() == null) {
            value.setName(name);

            return value;
        } else if (!value.getName().equals(name)) {
            ShortValue result = new ShortValue(value, NumberOperations.COPY, value);
            result.setName(name);

            return result;
        }
        return value;
    }

    // REM
    /**
     * Divides left hand operand by right hand operand and returns remainder
     *
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return remainder from division value1 by value2
     */
    public static ShortValue rem(ShortValue value1, ShortValue value2) {
        // Commented to support operations with nulls. See also MathUtils.mod()
        // validate(value1, value2, Formulas.REM.toString());
        if (value1 == null || value2 == null) {
            return ZERO;
        }

        return new ShortValue(value1,
            value2,
            Operators.rem(value1.getValue(), value2.getValue()),
            Formulas.REM);
    }

    /**
     * Adds left hand operand to right hand operand
     *
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of addition operation
     */
    public static ShortValue add(ShortValue value1, ShortValue value2) {
        if (value1 == null) {
            return value2;
        }

        if (value2 == null) {
            return value1;
        }

        return new ShortValue(value1,
            value2,
            Operators.add(value1.getValue(), value2.getValue()),
            Formulas.ADD);
    }

    // MULTIPLY
    /**
     * Multiplies left hand operand to right hand operand
     *
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of multiplication operation
     */
    public static ShortValue multiply(ShortValue value1,
                                      ShortValue value2) {
        if (value1 == null || value2 == null) {
            return null;
        }

        return new ShortValue(value1,
            value2,
            Operators.multiply(value1.getValue(), value2.getValue()),
            Formulas.MULTIPLY);
    }

    // SUBTRACT
    /**
     * Subtracts left hand operand to right hand operand
     *
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of subtraction operation
     */
    public static ShortValue subtract(ShortValue value1,
                                      ShortValue value2) {
        if (value1 == null && value2 == null) {
            return null;
        }

        if (value1 == null) {
            return negative(value2);
        }

        if (value2 == null) {
            return value1;
        }

        return new ShortValue(value1,
            value2,
            Operators.subtract(value1.getValue(), value2.getValue()),
            Formulas.SUBTRACT);
    }

    // DIVIDE
    /**
     * Divides left hand operand by right hand operand
     *
     * @param value1 org.openl.meta.ShortValue
     * @param value2 org.openl.meta.ShortValue
     * @return the result of division operation
     */
    public static org.openl.meta.DoubleValue divide(ShortValue value1,
                                                    ShortValue value2) {
        if (value1 == null || value2 == null) {
            return null;
        }

        if (value2.doubleValue() == 0) {
            throw new OpenLRuntimeException("Division by zero");
        }

        return new org.openl.meta.DoubleValue(new DoubleValue(value1.doubleValue()),
            new DoubleValue(value2.doubleValue()),
            Operators.divide(value1.getValue(), value2.getValue()),
            Formulas.DIVIDE);
    }

    // QUAOTIENT
    /**
     * Divides left hand operand by right hand operand
     *
     * @param number org.openl.meta.ShortValue
     * @param divisor org.openl.meta.ShortValue
     * @return LongValue the result of division operation
     */
    public static LongValue quotient(ShortValue number, ShortValue divisor) {
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
    public static ShortValue mod(ShortValue number, ShortValue divisor) {
        if (number != null && divisor != null) {
            ShortValue result = new ShortValue(
                MathUtils.mod(number.getValue(), divisor.getValue()));
            return new ShortValue(result, NumberOperations.MOD, number, divisor);
        }
        return null;
    }

    /**
     * Sorts the array <b>values</b> in ascending order and returns the value from array <b>values</b> at position
     * <b>position</b>
     *
     * @param values array of org.openl.meta.ShortValue values
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static ShortValue small(ShortValue[] values, int position) {
        return instance(MathUtils.small(unwrap(values), position), NumberOperations.SMALL, values);
    }

    /**
     * Sorts the array <b>values</b> in descending order and returns the value from array <b>values</b> at position
     * <b>position</b>
     *
     * @param values array of org.openl.meta.ShortValue values
     * @param position int value
     * @return the value from array <b>values</b> at position <b>position</b>
     */
    public static ShortValue big(ShortValue[] values, int position) {
        return instance(MathUtils.big(unwrap(values), position), NumberOperations.BIG, values);
    }

    /**
     *
     * @param value1
     * @param value2
     * @return the result of value1 raised to the power of value2
     */
    public static ShortValue pow(ShortValue value1, ShortValue value2) {
        // Commented to support operations with nulls
        // "null" means that data does not exist
        //
        // validate(value1, value2, NumberOperations.POW);
        if (value1 == null) {
            return value2 == null ? null : new ShortValue((short) 0);
        } else if (value2 == null) {
            return value1;
        }

        return new ShortValue(new ShortValue(
            Operators.pow(value1.getValue(), value2.getValue())), NumberOperations.POW, value1, value2);
    }

    /**
     *
     * @param value
     * @return the absolute value (module) of the value <b>value </b>
     */
    public static ShortValue abs(ShortValue value) {
        // Commented to support operations with nulls.
        // validate(value, NumberOperations.ABS);
        if (value == null) {
            return null;
        }
        // evaluate result
        ShortValue result = new ShortValue((short) Math.abs(value.getValue()));
        // create instance with information about last operation
        return new ShortValue(result, NumberOperations.ABS, value);
    }

    /**
     *
     * @param value
     * @return the negative value of the <b>value</b>
     */
    public static ShortValue negative(ShortValue value) {
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
    public static ShortValue inc(ShortValue value) {
        return add(value, ONE);
    }

    /**
     *
     * @param value
     * @return the <b>value</b>
     */
    public static ShortValue positive(ShortValue value) {
        return value;
    }

    /**
     *
     * @param value
     * @return the <b>value </b> decreased by 1
     */
    public static ShortValue dec(ShortValue value) {
        return subtract(value, ONE);
    }

    // Autocasts

    /**
     * Is used to overload implicit cast operators from byte to org.openl.meta.ShortValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static ShortValue autocast(byte x, ShortValue y) {
        return new ShortValue(x);
    }

    /**
     * Is used to overload implicit cast operators from short to org.openl.meta.ShortValue
     *
     * @param x
     * @param y is needed to avoid ambiguity in Java method resolution
     * @return the casted value to org.openl.meta.ShortValue
     */
    public static ShortValue autocast(short x, ShortValue y) {
        return new ShortValue(x);
    }

    // Constructors
    public ShortValue(short value) {
        this.value = value;
    }

    /** Formula constructor **/
    public ShortValue(ShortValue lv1, ShortValue lv2, short value, Formulas operand) {
        super(lv1, lv2, operand);
        this.value = value;
    }

    /** Cast constructor **/
    public ShortValue(short value, ExplanationNumberValue<?> beforeCastValue, boolean autocast) {
        super(beforeCastValue, new CastOperand("ShortValue", autocast));
        this.value = value;
    }

    /**
     * Copy the current value with new name <b>name</b>
     */
    @Override
    public ShortValue copy(String name) {
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
    public short getValue() {
        return value;
    }

    // Equals
    @Override
    /**
     * Indicates whether some other object is "equal to" this org.openl.meta.ShortValue variable.
     */
    public boolean equals(Object obj) {
        return obj instanceof ShortValue && value == ((ShortValue) obj).value;
    }

    // sort
    /**
     * Sorts the array <b>values</b>
     *
     * @param values an array for sorting
     * @return the sorted array
     */
    public static ShortValue[] sort(ShortValue[] values) {
        ShortValue[] sortedArray = null;
        if (values != null) {
            sortedArray = new ShortValue[values.length];
            ShortValue[] notNullArray = ArrayTool.removeNulls(values);

            Arrays.sort(notNullArray);

            /* Filling sortedArray by sorted and null values */
            System.arraycopy(notNullArray, 0, sortedArray, 0, notNullArray.length);
        }
        return sortedArray;
    }

    // ******* Autocasts*************

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

    public static ShortValue cast(char x, ShortValue y) {
        return new ShortValue((short) x);
    }

    public static ShortValue cast(int x, ShortValue y) {
        return new ShortValue((short) x);
    }

    public static ShortValue cast(long x, ShortValue y) {
        return new ShortValue((short) x);
    }

    public static ShortValue cast(float x, ShortValue y) {
        return new ShortValue((short) x);
    }

    public static ShortValue cast(double x, ShortValue y) {
        return new ShortValue((short) x);
    }

    public static ShortValue cast(BigInteger x, ShortValue y) {
        return new ShortValue(x.shortValue());
    }

    public static ShortValue cast(BigDecimal x, ShortValue y) {
        return new ShortValue(x.shortValue());
    }

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

    public static ByteValue cast(ShortValue x, ByteValue y) {
        if (x == null) {
            return null;
        }
        return new ByteValue(x.byteValue(), x, false);
    }

    public static BigInteger cast(ShortValue x, BigInteger y) {
        return BigInteger.valueOf(x.shortValue());
    }

    public static BigDecimal cast(ShortValue x, BigDecimal y) {
        return BigDecimal.valueOf(x.shortValue());
    }

    public ShortValue(String valueString) {
        value = Short.parseShort(valueString);
    }

    /** Function constructor **/
    public ShortValue(ShortValue result, NumberOperations function, ShortValue... params) {
        super(function, params);
        this.value = result.shortValue();
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
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public int compareTo(ShortValue o) {
        return value - o.value;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    private static Short[] unwrap(ShortValue[] values) {
        if (values == null) {
            return null;
        }
        values = ArrayTool.removeNulls(values);
        Short[] shortArray = new Short[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                shortArray[i] = values[i].getValue();
            }
        }
        return shortArray;
    }
}
