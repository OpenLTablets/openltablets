package org.openl.itest.serviceclass;

import org.openl.itest.cassandra.HelloEntity1;
import org.openl.itest.cassandra.HelloEntity2;
import org.openl.itest.cassandra.HelloEntity3;
import org.openl.itest.cassandra.HelloEntity8;
import org.openl.itest.db.HelloEntity9;
import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.ruleservice.core.interceptors.annotations.ServiceCallAfterInterceptor;
import org.openl.rules.ruleservice.core.interceptors.annotations.ServiceCallBeforeInterceptor;
import org.openl.rules.ruleservice.storelogdata.annotation.PrepareStoreLogData;
import org.openl.rules.ruleservice.storelogdata.cassandra.annotation.StoreLogDataToCassandra;
import org.openl.rules.ruleservice.storelogdata.db.annotation.StoreLogDataToDB;

public interface Simple4ServiceAnnotationTemplate {

    @StoreLogDataToCassandra(value = { HelloEntity1.class,
            HelloEntity2.class,
            HelloEntity3.class,
            HelloEntity8.class }, sync = true)
    @StoreLogDataToDB(value = { org.openl.itest.db.HelloEntity1.class,
            org.openl.itest.db.HelloEntity2.class,
            org.openl.itest.db.HelloEntity3.class,
            org.openl.itest.db.HelloEntity8.class }, sync = true)
    @PrepareStoreLogData(PrepareStoreLogDataValues.class)
    String Hello(IRulesRuntimeContext runtimeContext, Integer hour);

    @StoreLogDataToCassandra(HelloEntity1.class)
    @StoreLogDataToDB(org.openl.itest.db.HelloEntity1.class)
    @ServiceCallBeforeInterceptor(Simple4ServiceMethodBeforeAdvice.class)
    @ServiceCallAfterInterceptor(Simple4ServiceMethodAfterAdvice.class)
    @PrepareStoreLogData(value = BeforeBeforeInterceptor.class, bindToServiceMethodAdvice = Simple4ServiceMethodBeforeAdvice.class, before = true)
    @PrepareStoreLogData(value = AfterBeforeInterceptor.class, bindToServiceMethodAdvice = Simple4ServiceMethodBeforeAdvice.class)
    @PrepareStoreLogData(value = BeforeMethod.class, before = true)
    @PrepareStoreLogData(value = AfterMethod.class)
    @PrepareStoreLogData(value = BeforeAfterInterceptors.class, bindToServiceMethodAdvice = Simple4ServiceMethodAfterAdvice.class, before = true)
    @PrepareStoreLogData(value = AfterAfterInterceptors.class, bindToServiceMethodAdvice = Simple4ServiceMethodAfterAdvice.class)
    String Hello2(IRulesRuntimeContext runtimeContext, Integer hour);

    @StoreLogDataToDB(value = HelloEntity9.class, sync = true)
    @PrepareStoreLogData(PrepareStoreLogDataValues9.class)
    String Hello3(IRulesRuntimeContext runtimeContext, Integer hour);

}
