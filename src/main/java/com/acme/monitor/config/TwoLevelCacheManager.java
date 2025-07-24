package com.acme.monitor.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;

public class TwoLevelCacheManager implements CacheManager {
    private final CacheManager caffeineCacheManager;
    private final CacheManager redisCacheManager;

    public TwoLevelCacheManager(CacheManager caffeineCacheManager, CacheManager redisCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
        this.redisCacheManager = redisCacheManager;
    }

    @Override
    public Cache getCache(String name) {
        return new TwoLevelCache(name, caffeineCacheManager.getCache(name), redisCacheManager.getCache(name));
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.emptySet();
    }
}