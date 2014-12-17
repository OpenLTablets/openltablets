package org.openl.rules.ruleservice.ws.databinding;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openl.meta.BigDecimalValue;
import org.openl.meta.BigIntegerValue;
import org.openl.meta.ByteValue;
import org.openl.meta.DoubleValue;
import org.openl.meta.FloatValue;
import org.openl.meta.IntValue;
import org.openl.meta.LongValue;
import org.openl.meta.ShortValue;
import org.openl.meta.StringValue;
import org.openl.rules.calc.SpreadsheetResult;
import org.openl.rules.calculation.result.convertor2.CompoundStep;
import org.openl.rules.context.DefaultRulesRuntimeContext;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.helpers.DoubleRange;
import org.openl.rules.helpers.IntRange;
import org.openl.rules.ruleservice.databinding.JacksonObjectMapperFactoryBean;
import org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.ruleservice.context.IRulesRuntimeContextType;
import org.openl.rules.table.Point;
import org.openl.rules.variation.ArgumentReplacementVariation;
import org.openl.rules.variation.ComplexVariation;
import org.openl.rules.variation.DeepCloningVariation;
import org.openl.rules.variation.JXPathVariation;
import org.openl.rules.variation.Variation;
import org.openl.rules.variation.VariationsResult;
import org.openl.util.RangeWithBounds.BoundType;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonObjectMapperFactoryBeanTest {

    private static class TestClass {
        ByteValue byteValue;
        ShortValue shortValue;
        IntValue intValue;
        LongValue longValue;
        FloatValue floatValue;
        DoubleValue doubleValue;
        StringValue stringValue;
        BigDecimalValue bigDecimalValue;
        BigIntegerValue bigIntegerValue;

        public TestClass() {
        }

        public ByteValue getByteValue() {
            return byteValue;
        }

        public void setByteValue(ByteValue byteValue) {
            this.byteValue = byteValue;
        }

        public ShortValue getShortValue() {
            return shortValue;
        }

        public void setShortValue(ShortValue shortValue) {
            this.shortValue = shortValue;
        }

        public IntValue getIntValue() {
            return intValue;
        }

        public void setIntValue(IntValue intValue) {
            this.intValue = intValue;
        }

        public LongValue getLongValue() {
            return longValue;
        }

        public void setLongValue(LongValue longValue) {
            this.longValue = longValue;
        }

        public FloatValue getFloatValue() {
            return floatValue;
        }

        public void setFloatValue(FloatValue floatValue) {
            this.floatValue = floatValue;
        }

        public DoubleValue getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(DoubleValue doubleValue) {
            this.doubleValue = doubleValue;
        }

        public StringValue getStringValue() {
            return stringValue;
        }

        public void setStringValue(StringValue stringValue) {
            this.stringValue = stringValue;
        }

        public BigDecimalValue getBigDecimalValue() {
            return bigDecimalValue;
        }

        public void setBigDecimalValue(BigDecimalValue bigDecimalValue) {
            this.bigDecimalValue = bigDecimalValue;
        }

        public BigIntegerValue getBigIntegerValue() {
            return bigIntegerValue;
        }

        public void setBigIntegerValue(BigIntegerValue bigIntegerValue) {
            this.bigIntegerValue = bigIntegerValue;
        }
    }

    @Test
    public void testOpenLTypes() throws JsonProcessingException, IOException {
        JacksonObjectMapperFactoryBean bean = new JacksonObjectMapperFactoryBean();
        ObjectMapper objectMapper = bean.createJacksonDatabinding();
        TestClass testBean = new TestClass();
        ByteValue byteValue = new ByteValue((byte) 255);
        ShortValue shortValue = new ShortValue((short) 12345);
        IntValue intValue = new IntValue(12345);
        LongValue longValue = new LongValue(12345);
        FloatValue floatValue = new FloatValue(123.2f);
        DoubleValue doubleValue = new DoubleValue(123.2d);
        StringValue stringValue = new StringValue("someString");
        BigDecimalValue bigDecimalValue = new BigDecimalValue(new BigDecimal("1231241235235123612361235235325325.12345234"));
        BigIntegerValue bigIntegerValue = new BigIntegerValue(new BigInteger("1231241235235123612361235235325325"));

        testBean.setByteValue(byteValue);
        testBean.setShortValue(shortValue);
        testBean.setIntValue(intValue);
        testBean.setLongValue(longValue);
        testBean.setFloatValue(floatValue);
        testBean.setDoubleValue(doubleValue);
        testBean.setStringValue(stringValue);
        testBean.setBigIntegerValue(bigIntegerValue);
        testBean.setBigDecimalValue(bigDecimalValue);

        String text = objectMapper.writeValueAsString(testBean);

        testBean = objectMapper.readValue(text, TestClass.class);

        Assert.assertEquals(testBean.getByteValue(), byteValue);
        Assert.assertEquals(testBean.getShortValue(), shortValue);
        Assert.assertEquals(testBean.getIntValue(), intValue);
        Assert.assertEquals(testBean.getLongValue(), longValue);
        Assert.assertEquals(testBean.getFloatValue(), floatValue);
        Assert.assertEquals(testBean.getDoubleValue(), doubleValue);
        Assert.assertEquals(testBean.getStringValue(), stringValue);
        Assert.assertEquals(testBean.getBigDecimalValue(), bigDecimalValue);
        Assert.assertEquals(testBean.getBigIntegerValue(), bigIntegerValue);

    }

    @Test
    public void testVariations() throws JsonProcessingException, IOException {
        JacksonObjectMapperFactoryBean bean = new JacksonObjectMapperFactoryBean();
        bean.setEnableDefaultTyping(false);
        bean.setSupportVariations(true);
        ObjectMapper objectMapper = bean.createJacksonDatabinding();

        ArgumentReplacementVariation v = new ArgumentReplacementVariation("variationID", 1, new DoubleValue(123d));

        ComplexVariation complexVariation = new ComplexVariation("complexVariationId", v);

        String text = objectMapper.writeValueAsString(complexVariation);

        Variation v1 = objectMapper.readValue(text, Variation.class);
        Assert.assertTrue(v1 instanceof ComplexVariation);
        Assert.assertEquals("complexVariationId", v1.getVariationID());

        text = objectMapper.writeValueAsString(new DeepCloningVariation("deepCloningID", complexVariation));
        v1 = objectMapper.readValue(text, Variation.class);
        Assert.assertTrue(v1 instanceof DeepCloningVariation);
        Assert.assertEquals("deepCloningID", v1.getVariationID());

        text = objectMapper.writeValueAsString(new JXPathVariation("jaxPathID", 1, "invalidPath", "value"));
        v1 = objectMapper.readValue(text, Variation.class);
        Assert.assertTrue(v1 instanceof JXPathVariation);
        Assert.assertEquals("jaxPathID", v1.getVariationID());

        VariationsResult<CompoundStep> variationsResult = new VariationsResult<CompoundStep>();
        variationsResult.registerFailure("variationErrorID", "errorMessage");
        variationsResult.registerResult("variationID", new CompoundStep());
        text = objectMapper.writeValueAsString(variationsResult);
        variationsResult = objectMapper.readValue(text, new TypeReference<VariationsResult<CompoundStep>>() {
        });
        Assert.assertTrue(variationsResult instanceof VariationsResult);
        Assert.assertEquals("errorMessage", variationsResult.getFailureErrorForVariation("variationErrorID"));
    }

    @Test
    public void testSpreadsheetResult() throws JsonProcessingException, IOException {
        JacksonObjectMapperFactoryBean bean = new JacksonObjectMapperFactoryBean();
        bean.setSupportVariations(true);
        ObjectMapper objectMapper = bean.createJacksonDatabinding();
        String text = objectMapper.writeValueAsString(new SpreadsheetResult(3, 3));
        SpreadsheetResult result = objectMapper.readValue(text, SpreadsheetResult.class);
        Assert.assertTrue(result instanceof SpreadsheetResult);
        
        text = objectMapper.writeValueAsString(new Point(1, 1));
        Point p = objectMapper.readValue(text, Point.class);
        Assert.assertTrue(p instanceof Point);
        Assert.assertEquals(1, p.getColumn());
        Assert.assertEquals(1, p.getRow());
    }
    
    @Test
    public void testRange() throws JsonProcessingException, IOException {
        JacksonObjectMapperFactoryBean bean = new JacksonObjectMapperFactoryBean();
        bean.setSupportVariations(true);
        ObjectMapper objectMapper = bean.createJacksonDatabinding();
        String text = objectMapper.writeValueAsString(new DoubleRange(0.0d, 1d, BoundType.EXCLUDING, BoundType.EXCLUDING));
        DoubleRange result = objectMapper.readValue(text, DoubleRange.class);
        Assert.assertTrue(result instanceof DoubleRange);

        text = objectMapper.writeValueAsString(new IntRange(199, 299));
        IntRange intRange = objectMapper.readValue(text, IntRange.class);
        Assert.assertTrue(intRange instanceof IntRange);
        Assert.assertEquals(199, intRange.getMin());
        Assert.assertEquals(299, intRange.getMax());
    }
    
    @Test
    public void testIRulesRuntimeContext() throws JsonProcessingException, IOException {
        
        Assert.assertNull("Not use this annotation for simple JSON!", IRulesRuntimeContextType.class.getAnnotation(JsonTypeInfo.class));
        Assert.assertNull("Not use this annotation for simple JSON!", org.openl.rules.ruleservice.databinding.jackson.org.openl.rules.context.IRulesRuntimeContextType.class.getAnnotation(JsonTypeInfo.class));
        
        DefaultRulesRuntimeContext context = new DefaultRulesRuntimeContext();
        Date date = new Date();
        context.setCurrentDate(date);
        context.setLob("LOB");
        JacksonObjectMapperFactoryBean bean = new JacksonObjectMapperFactoryBean();
        bean.setSupportVariations(true);
        ObjectMapper objectMapper = bean.createJacksonDatabinding();
        String text = objectMapper.writeValueAsString(context);
        
        IRulesRuntimeContext iRulesRuntimeContext = objectMapper.readValue(text, IRulesRuntimeContext.class);
        
        Assert.assertEquals(date, iRulesRuntimeContext.getCurrentDate());
        Assert.assertEquals("LOB", iRulesRuntimeContext.getLob());
    }
    
    
    public static class Wrapper{
        public Animal animal;
    }
    
    public static class Animal{
        public String name;
    }
    
    public static class Dog extends Animal{
    }
    
    public static class Cat extends Animal{
    }
    
    @Test
    public void testOverrideTypes() throws JsonProcessingException, IOException {
        JacksonObjectMapperFactoryBean bean = new JacksonObjectMapperFactoryBean();
        bean.setSupportVariations(true);
        bean.setEnableDefaultTyping(false);
        Set<String> overrideTypes = new HashSet<String>();
        overrideTypes.add(Animal.class.getName());
        overrideTypes.add(Dog.class.getName());
        overrideTypes.add(Cat.class.getName());
        bean.setOverrideTypes(overrideTypes);
        Wrapper wrapper = new Wrapper();
        wrapper.animal = new Dog();
        ObjectMapper objectMapper = bean.createJacksonDatabinding();
        String text = objectMapper.writeValueAsString(wrapper);
        Wrapper w = objectMapper.readValue(text, Wrapper.class);
        Assert.assertTrue(w.animal instanceof Dog);
    }
}
