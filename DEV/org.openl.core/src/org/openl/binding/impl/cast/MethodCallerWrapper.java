package org.openl.binding.impl.cast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodCallerWrapper {
    Class<? extends MethodCallerWrapperFactory> value() default DefaultMethodCallerWrapperFactory.class;
}
