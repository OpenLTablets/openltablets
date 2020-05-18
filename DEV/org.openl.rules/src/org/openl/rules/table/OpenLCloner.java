package org.openl.rules.table;

import java.lang.reflect.InvocationHandler;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.openl.meta.*;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMember;
import org.slf4j.Logger;

import com.rits.cloning.Cloner;
import com.rits.cloning.IInstantiationStrategy;

/**
 * Extension for {@link Cloner}. To add OpenL classes to prevent cloning instances of them.
 *
 * TODO: should be analyzed variations of tracing different rules. Check if we have issues with mutatation of listed
 * below OpenL not cloned classes.
 *
 * @author DLiauchuk
 *
 */
public class OpenLCloner extends Cloner {

    protected OpenLCloner() {
        super(new ObjenesisInstantiationStrategy());
        dontCloneClasses();
    }

    private void dontCloneClasses() {
        /*
         * Always cloning them degrades the performance very much. It becomes impossible to open the trace.
         */
        registerImmutable(ByteValue.class);
        registerImmutable(ShortValue.class);
        registerImmutable(IntValue.class);
        registerImmutable(LongValue.class);
        registerImmutable(FloatValue.class);
        // registerImmutable(DoubleValue.class); EPBDS-6604 DoubleValue is not immutable
        registerImmutable(BigIntegerValue.class);
        registerImmutable(BigDecimalValue.class);
        registerImmutable(StringValue.class);
        dontClone(ValueMetaInfo.class);

        dontCloneInstanceOf(Logger.class);
        dontCloneInstanceOf(IOpenClass.class);
        dontCloneInstanceOf(IOpenMember.class);
        dontCloneInstanceOf(InvocationHandler.class);
    }

    /* Required for correct working with classloaders. */
    public static class ObjenesisInstantiationStrategy implements IInstantiationStrategy {
        private final Objenesis objenesis = new ObjenesisStd();

        @Override
        public <T> T newInstance(Class<T> c) {
            return objenesis.newInstance(c);
        }

        private static ObjenesisInstantiationStrategy instance = new ObjenesisInstantiationStrategy();

        public static ObjenesisInstantiationStrategy getInstance() {
            return instance;
        }
    }

}
