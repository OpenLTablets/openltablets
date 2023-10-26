package org.openl.rules.ruleservice.storelogdata;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.openl.binding.MethodUtil;
import org.openl.rules.ruleservice.core.interceptors.AnnotationUtils;
import org.openl.rules.ruleservice.core.interceptors.ServiceInvocationAdviceListener;
import org.openl.rules.ruleservice.core.interceptors.ServiceMethodAdvice;
import org.openl.rules.ruleservice.storelogdata.advice.ObjectSerializerAware;
import org.openl.rules.ruleservice.storelogdata.advice.StoreLogDataAdvice;
import org.openl.rules.ruleservice.storelogdata.annotation.InjectObjectSerializer;
import org.openl.rules.ruleservice.storelogdata.annotation.PrepareStoreLogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreLogDataServiceInvocationAdviceListener implements ServiceInvocationAdviceListener {
    private final Logger log = LoggerFactory.getLogger(StoreLogDataServiceInvocationAdviceListener.class);

    @Autowired
    private StoreLogDataManager storeLogDataManager;

    public void process(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException,
            Instantiator postProcessAdvice,
            Predicate<PrepareStoreLogData> predicate) {

        PrepareStoreLogData[] annotations = interfaceMethod.getAnnotationsByType(PrepareStoreLogData.class);
        prepare(interfaceMethod, args, result, lastOccurredException, postProcessAdvice, predicate, annotations);
    }

    private <T> void prepare(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException,
            Instantiator postProcessAdvice,
            Predicate<PrepareStoreLogData> predicate,
            PrepareStoreLogData[] prepareStoreLogDataArray) {
        Collection<Consumer<Void>> destroyFunctions = new ArrayList<>();
        try {
            StoreLogData storeLogData = null;
            IdentityHashMap<Inject<?>, Object> cache = new IdentityHashMap<>();
            for (PrepareStoreLogData storeLogging : prepareStoreLogDataArray) {
                if (predicate.test(storeLogging)) {
                    StoreLogDataAdvice storeLogDataAdvice = null;
                    Class<? extends StoreLogDataAdvice> clazz = storeLogging.value();
                    try {
                        storeLogDataAdvice = postProcessAdvice.instantiate(clazz);
                        storeLogData = processAwareInterfaces(interfaceMethod,
                            storeLogData,
                            storeLogDataAdvice,
                            cache,
                            destroyFunctions);
                    } catch (Exception e) {
                        String msg = String.format(
                            "Failed to instantiate store log data advice for method '%s'. Please, check that class '%s' is not abstract and has a default constructor.",
                            MethodUtil.printQualifiedMethodName(interfaceMethod),
                            clazz.getTypeName());
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
        } finally {
            destroyFunctions.forEach(e -> e.accept(null));
        }
    }

    private StoreLogData processAwareInterfaces(Method interfaceMethod,
            StoreLogData storeLogData,
            StoreLogDataAdvice storeLogDataAdvice,
            IdentityHashMap<Inject<?>, Object> cache,
            Collection<Consumer<Void>> destroyFunctions) {
        storeLogData = injectObjectSerializer(storeLogData, storeLogDataAdvice);
        for (var storeLogDataService : storeLogDataManager.getServices()) {
            for (var inject : storeLogDataService.additionalInjects()) {
                var annotationClass = inject.getAnnotationClass();
                try {
                    var resource = cache.get(inject);
                    if (resource == null) {
                        var resource1 = AnnotationUtils.inject(storeLogDataAdvice, annotationClass,
                            e -> inject.getResource(interfaceMethod, e));
                        cache.put(inject, resource1);
                        if (resource1 != null) {
                            destroyFunctions.add(e -> inject.destroy(resource1));
                        }
                    } else {
                        AnnotationUtils.inject(storeLogDataAdvice, annotationClass, e -> resource);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Failed to inject a resource through annotation '{}'",
                        annotationClass.getTypeName(),
                        e);
                }
            }
        }
        return storeLogData;
    }

    private StoreLogData injectObjectSerializer(StoreLogData storeLogData, StoreLogDataAdvice storeLogDataAdvice) {
        if (storeLogDataAdvice instanceof ObjectSerializerAware) {
            ObjectSerializerAware objectSerializerAware = (ObjectSerializerAware) storeLogDataAdvice;
            if (storeLogData == null) {
                storeLogData = StoreLogDataHolder.get(); // Lazy local
                // variable
                // initialization
            }
            objectSerializerAware.setObjectSerializer(storeLogData.getObjectSerializer());
        }
        try {
            AnnotationUtils.inject(storeLogDataAdvice,
                InjectObjectSerializer.class,
                e -> StoreLogDataHolder.get().getObjectSerializer());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to inject a resource through annotation '{}'",
                InjectObjectSerializer.class.getTypeName(),
                e);
        }
        return storeLogData;
    }

    @Override
    public void beforeServiceMethodAdvice(ServiceMethodAdvice serviceMethodAdvice,
            Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException,
            Instantiator postProcessAdvice) {
        if (storeLogDataManager.isEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                postProcessAdvice,
                e -> e.before() && e.bindToServiceMethodAdvice().equals(serviceMethodAdvice.getClass()));
        }
    }

    @Override
    public void afterServiceMethodAdvice(ServiceMethodAdvice serviceMethodAdvice,
            Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException,
            Instantiator postProcessAdvice) {
        if (storeLogDataManager.isEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                postProcessAdvice,
                e -> !e.before() && e.bindToServiceMethodAdvice().equals(serviceMethodAdvice.getClass()));
        }
    }

    @Override
    public void beforeMethodInvocation(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception ex,
            Instantiator postProcessAdvice) {
        if (storeLogDataManager.isEnabled()) {
            process(interfaceMethod,
                args,
                result,
                ex,
                postProcessAdvice,
                e -> e.before() && e.bindToServiceMethodAdvice().equals(PrepareStoreLogData.Default.class));
        }
    }

    @Override
    public void afterMethodInvocation(Method interfaceMethod,
            Object[] args,
            Object result,
            Exception lastOccurredException,
            Instantiator postProcessAdvice) {
        if (storeLogDataManager.isEnabled()) {
            process(interfaceMethod,
                args,
                result,
                lastOccurredException,
                postProcessAdvice,
                e -> !e.before() && e.bindToServiceMethodAdvice().equals(PrepareStoreLogData.Default.class));
        }
    }

}
