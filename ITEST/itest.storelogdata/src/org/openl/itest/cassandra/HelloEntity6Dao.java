package org.openl.itest.cassandra;

import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;

@Dao
public interface HelloEntity6Dao {
    @Insert
    CompletionStage<Void> insert(HelloEntity6 entity);
}
