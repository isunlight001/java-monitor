package com.acme.monitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

public class TwoLevelCache implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(TwoLevelCache.class);

    private final String name;
    private final Cache caffeineCache;
    private final Cache redisCache;

    public TwoLevelCache(String name, Cache caffeineCache, Cache redisCache) {
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisCache = redisCache;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        long startTime = System.currentTimeMillis();
        try {
            // 先从Caffeine中获取
            ValueWrapper valueWrapper = caffeineCache.get(key);
            if (valueWrapper != null) {
                logger.debug("Cache hit from Caffeine: {}", key);
                return valueWrapper;
            }

            // Caffeine中没有，再从Redis中获取
            valueWrapper = redisCache.get(key);
            if (valueWrapper != null) {
                logger.debug("Cache hit from Redis: {}", key);
                // 放入Caffeine中，下次直接从内存获取
                caffeineCache.put(key, valueWrapper.get());
                return valueWrapper;
            }

            logger.debug("Cache miss for key: {}", key);
            return null;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                logger.warn("Cache get operation took {} ms for key: {}", duration, key);
            }
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        long startTime = System.currentTimeMillis();
        try {
            ValueWrapper wrapper = get(key);
            return wrapper == null ? null : (T) wrapper.get();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                logger.warn("Cache get operation with type took {} ms for key: {}", duration, key);
            }
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        long startTime = System.currentTimeMillis();
        try {
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                return (T) wrapper.get();
            }

            try {
                T value = valueLoader.call();
                put(key, value);
                return value;
            } catch (Exception e) {
                throw new Cache.ValueRetrievalException(key, valueLoader, e);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                logger.warn("Cache get operation with valueLoader took {} ms for key: {}", duration, key);
            }
        }
    }

    @Override
    public void put(Object key, Object value) {
        long startTime = System.currentTimeMillis();
        try {
            // 同时放入两级缓存
            caffeineCache.put(key, value);
            redisCache.put(key, value);
            logger.debug("Value put into both Caffeine and Redis caches: {}", key);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                logger.warn("Cache put operation took {} ms for key: {}", duration, key);
            }
        }
    }

    @Override
    public void evict(Object key) {
        long startTime = System.currentTimeMillis();
        try {
            // 从两级缓存中都删除
            caffeineCache.evict(key);
            redisCache.evict(key);
            logger.debug("Value evicted from both Caffeine and Redis caches: {}", key);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                logger.warn("Cache evict operation took {} ms for key: {}", duration, key);
            }
        }
    }

    @Override
    public void clear() {
        long startTime = System.currentTimeMillis();
        try {
            // 清空两级缓存
            caffeineCache.clear();
            redisCache.clear();
            logger.debug("Both Caffeine and Redis caches cleared");
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 10) {
                logger.warn("Cache clear operation took {} ms", duration);
            }
        }
    }
}