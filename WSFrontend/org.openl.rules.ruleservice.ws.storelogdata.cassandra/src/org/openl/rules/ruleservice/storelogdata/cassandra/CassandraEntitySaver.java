package org.openl.rules.ruleservice.storelogdata.cassandra;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openl.rules.ruleservice.storelogdata.cassandra.annotation.EntityOperations;

public class CassandraEntitySaver {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraEntitySaver.class);

    private final Object dao;
    private final EntityOperations<Object, Object> entityOperations;

    public CassandraEntitySaver(EntityOperations<Object, Object> entityOperations, Object dao) {
        super();
        this.dao = Objects.requireNonNull(dao, "dao cannot be null");
        this.entityOperations = Objects.requireNonNull(entityOperations, "entityOperations cannot be null");
    }

    public CompletionStage<Void> insert(Object entity) {
        return entityOperations.insert(dao, entity).exceptionally(e -> {
            LOG.error("Failed to save cassandra entity.", e);
            return null;
        });
    }
}
