package org.openl.rules.cast;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openl.OpenL;
import org.openl.binding.ICastFactory;
import org.openl.binding.impl.cast.IOpenCast;
import org.openl.binding.impl.cast.JavaBoxingCast;
import org.openl.binding.impl.cast.JavaUpCast;
import org.openl.types.java.JavaOpenClass;

public class OpenLClassCastsTest {
    private static OpenL openL;
    private static ICastFactory castFactory;
    
    @BeforeClass
    public static void init(){
        openL = OpenL.getInstance(OpenL.OPENL_JAVA_NAME);
        castFactory = openL.getBinder().getCastFactory();
    }
    
    @Test
    public void testCastDistances() throws Exception {
        JavaOpenClass integerClass = JavaOpenClass.getOpenClass(Integer.class);
        
        IOpenCast autoboxing = castFactory.getCast(integerClass, JavaOpenClass.INT);
        IOpenCast autoboxingWithAutocast = castFactory.getCast(integerClass, JavaOpenClass.DOUBLE);
        IOpenCast cast = castFactory.getCast(JavaOpenClass.DOUBLE, JavaOpenClass.INT);
        assertTrue(autoboxing.getDistance(integerClass, JavaOpenClass.INT) < autoboxingWithAutocast.getDistance(
                integerClass, JavaOpenClass.DOUBLE));
        assertTrue(autoboxingWithAutocast.getDistance(integerClass, JavaOpenClass.DOUBLE) < cast.getDistance(
                JavaOpenClass.DOUBLE, JavaOpenClass.INT));
    }

    @Test
    public void testBoxingUpCast() {
        JavaOpenClass comparableClass = JavaOpenClass.getOpenClass(Comparable.class);
        IOpenCast cast = castFactory.getCast(JavaOpenClass.INT, comparableClass);
        assertNotNull(cast);
        assertEquals(JavaUpCast.UP_CAST_DISTANCE, cast.getDistance(JavaOpenClass.INT, comparableClass));

        cast = castFactory.getCast(JavaOpenClass.DOUBLE, JavaOpenClass.OBJECT);
        assertNotNull(cast);
        assertEquals(JavaUpCast.UP_CAST_DISTANCE, cast.getDistance(JavaOpenClass.INT, JavaOpenClass.OBJECT));
    }
    
    @Test
    @Ignore
    public void testCastFromPrimitiveToOtherPrimitiveWrapper() throws Exception {
        JavaOpenClass doubleWrapperClass = JavaOpenClass.getOpenClass(Double.class);
        
        IOpenCast autocast = castFactory.getCast(JavaOpenClass.INT, doubleWrapperClass);
        assertNotNull(autocast);
        
        IOpenCast autocastNoBoxing = castFactory.getCast(JavaOpenClass.INT, JavaOpenClass.DOUBLE);
        assertTrue(autocastNoBoxing.getDistance(JavaOpenClass.INT, JavaOpenClass.DOUBLE) < autocast.getDistance(
            JavaOpenClass.INT, doubleWrapperClass));
    }

}
