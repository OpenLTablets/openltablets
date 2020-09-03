package org.openl.rules.ruleservice.storelogdata;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.openl.binding.MethodUtil;
import org.openl.rules.ruleservice.core.interceptors.ServiceInvocationAdviceListener;
import org.openl.rules.ruleservice.core.interceptors.ServiceMethodAdvice;
import org.openl.rules.ruleservice.storelogdata.advice.ObjectSerializerAware;
import org.openl.rules.ruleservice.storelogdata.advice.StoreLogDataAdvice;
import org.openl.rules.ruleservice.storelogdata.annotation.PrepareStoreLogData;
import org.openl.rules.ruleservice.storelogdata.annotation.PrepareStoreLogDatas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreLogDataServiceInvocationAdviceListener implements ServiceInvocationAdviceListener {
    private final Logger log = LoggerFactory.getLogger(StoreLogDataServiceInvocationAdviceListener.class);

    private boolean storeLogDataEnabled = false;

    public boolean isStoreLogDataEnabled() {
        return storeLogDataEnabled;
    }

    public void setStoreLogDataEnabled(boolean storeLogDataEnabled) {
        this.storeLogDataEnabled = storeLogDataEnabled;
    }

    public void process(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException,
            Predicate<PrepareStoreLogData> predicate) {
        PrepareStoreLogDatas prepareStoreLogDatas = interfaceMethod.getAnnotation(PrepareStoreLogDatas.class);
        if (prepareStoreLogDatas != null) {
            StoreLogData storeLogData = null;
            for (PrepareStoreLogData storeLogging : prepareStoreLogDatas.value()) {
                if (predicate.test(storeLogging)) {
                    StoreLogDataAdvice storeLogDataAdvice = null;
                    try {
                        storeLogDataAdvice = storeLogging.value().newInstance();
                        if (storeLogDataAdvice instanceof ObjectSerializerAware) {
                            ObjectSerializerAware objectSerializerAware = (ObjectSerializerAware) storeLogDataAdvice;
                            if (storeLogData == null) {
                                storeLogData = StoreLogDataHolder.get(); // Lazy local
                                                                         // variable
                                // initialization
                            }
                            objectSerializerAware.setObjectSerializer(storeLogData.getObjectSerializer());
                        }
                    } catch (Exception e) {
                        String msg = String.format(
                            "Failed to instantiate store log data advice for method '%s'. Please, check that class '%s' is not abstact and has a default constructor.",
                            MethodUtil.printQualifiedMethodName(interfaceMethod),
                            storeLogging.value().getTypeName());
                        log.error(msg, e);
                    }
                    if (storeLogDataAdvice != null) {
                        if (storeLogData == null) {
                            storeLogData = StoreLogDataHolder.get(); // Lazy local variable
                                                                     // initialization
                        }

                        storeLogDataAdvice.prepare(storeLogData.getCustomValues(), args, result, lastOccurredException);
                    }
                }
            }
        }
    }

    @Override
    public void beforeServiceMethodAdvice(ServiceMethodAdvice serviceMethodAdvice,
            Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException) {
        if (isStoreLogDataEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                e -> e.before() && e.bindToServiceMethodAdvice().equals(serviceMethodAdvice.getClass()));
        }
    }

    @Override
    public void afterServiceMethodAdvice(ServiceMethodAdvice serviceMethodAdvice,
            Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException) {
        if (isStoreLogDataEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                e -> !e.before() && e.bindToServiceMethodAdvice().equals(serviceMethodAdvice.getClass()));
        }
    }

    @Override
    public void beforeMethodInvocation(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException) {
        if (isStoreLogDataEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                e -> e.before() && e.bindToServiceMethodAdvice().equals(PrepareStoreLogData.Default.class));
        }
    }

    @Override
    public void afterMethodInvocation(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException) {
        if (isStoreLogDataEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                e -> !e.before() && e.bindToServiceMethodAdvice().equals(PrepareStoreLogData.Default.class));
        }
    }

}
