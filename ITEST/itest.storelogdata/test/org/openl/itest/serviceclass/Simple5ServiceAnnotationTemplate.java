package org.openl.itest.serviceclass;

import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.ruleservice.storelogdata.cassandra.annotation.StoreLogDataToCassandra;
import org.openl.rules.ruleservice.storelogdata.db.annotation.StoreLogDataToDB;
import org.openl.rules.ruleservice.storelogdata.elasticsearch.annotation.StoreLogDataToElasticsearch;

public interface Simple5ServiceAnnotationTemplate {

    @StoreLogDataToCassandra
    @StoreLogDataToElasticsearch
    @StoreLogDataToDB
    String Hello(IRulesRuntimeContext runtimeContext, Integer hour);

}
