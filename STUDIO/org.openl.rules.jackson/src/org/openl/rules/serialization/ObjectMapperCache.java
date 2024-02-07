package org.openl.rules.serialization;

/*-
 * #%L
 * OpenL - STUDIO - Jackson
 * %%
 * Copyright (C) 2016 - 2021 OpenL Tablets
 * %%
 * See the file LICENSE.txt for copying permission.
 * #L%
 */

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class that caches object mapper when it frequently used for the same set of classes.
 */
public class ObjectMapperCache {

    private static final WeakHashMap<Object, ObjectMapper> cache = new WeakHashMap<>();
    private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static volatile ObjectMapper defaultObjectMapper;

    /**
     * ObjectMapper for set of classes
     *
     * @param classes set of classes
     * @return objectMapper
     */
    public static ObjectMapper getObjectMapper(Object key, Class<?>[] classes) {
        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        try {
            readLock.lock();
            ObjectMapper objectMapper = cache.get(key);
            if (objectMapper != null) return objectMapper;
        } finally {
            readLock.unlock();
        }
        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            ObjectMapper objectMapper = cache.get(key);
            if (objectMapper == null) {
                objectMapper = JsonUtils.createJacksonObjectMapper(classes, false);
                cache.put(key, objectMapper);
            }
            return objectMapper;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Default object mapper
     *
     * @return objectMapper
     */
    public static ObjectMapper getDefaultObjectMapper() {
        if (defaultObjectMapper == null) {
            synchronized (ObjectMapperCache.class) {
                if (defaultObjectMapper == null) {
                    defaultObjectMapper = JsonUtils.getDefaultJacksonObjectMapper();
                }
            }
        }
        return defaultObjectMapper;
    }

}
