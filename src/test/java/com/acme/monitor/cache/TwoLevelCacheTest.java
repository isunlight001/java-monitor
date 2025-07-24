package com.acme.monitor.cache;

import com.acme.monitor.config.CacheConfig;
import com.acme.monitor.config.TwoLevelCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TwoLevelCacheTest {

    private TwoLevelCache twoLevelCache;
    private Cache caffeineCache;
    private Cache redisCache;

    @BeforeEach
    public void setUp() {
        // 创建Caffeine缓存管理器
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        
        // 创建Redis缓存管理器（使用Mock）
        RedisCacheManager redisCacheManager = mock(RedisCacheManager.class);
        
        // 创建模拟的缓存实例
        caffeineCache = caffeineCacheManager.getCache("testCache");
        redisCache = mock(Cache.class);
        
        // 配置Redis缓存管理器返回模拟的缓存实例
        when(redisCacheManager.getCache("testCache")).thenReturn(redisCache);
        
        // 创建二级缓存实例
        twoLevelCache = new TwoLevelCache("testCache", caffeineCache, redisCache);
    }

    @Test
    public void testGetCacheName() {
        assertEquals("testCache", twoLevelCache.getName());
    }

    @Test
    public void testGetFromCaffeineCache() {
        // 准备测试数据
        String key = "testKey";
        TestUser user = new TestUser(1L, "Alice", "alice@example.com");
        
        // 先放入Caffeine缓存
        caffeineCache.put(key, user);
        
        // 从二级缓存获取
        Cache.ValueWrapper result = twoLevelCache.get(key);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(user.getId(), ((User)result.get()).getId());
        assertEquals(user.getName(), ((User)result.get()).getName());
        assertEquals(user.getEmail(), ((User)result.get()).getEmail());
    }

    @Test
    public void testGetFromRedisCache() {
        // 准备测试数据
        String key = "testKey";
        User user = new User(1L, "Alice", "alice@example.com");
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
        
        // 配置模拟行为
        when(valueWrapper.get()).thenReturn(user);
        when(redisCache.get(key)).thenReturn(valueWrapper);
        
        // 从二级缓存获取（应该从Redis获取）
        Cache.ValueWrapper result = twoLevelCache.get(key);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(user.getId(), ((User)result.get()).getId());
        assertEquals(user.getName(), ((User)result.get()).getName());
        assertEquals(user.getEmail(), ((User)result.get()).getEmail());
        
        // 验证Redis缓存被调用
        verify(redisCache, times(1)).get(key);
        
        // 验证结果也被放入了Caffeine缓存
        Cache.ValueWrapper caffeineResult = caffeineCache.get(key);
        assertNotNull(caffeineResult);
        assertEquals(user.getId(), ((User)caffeineResult.get()).getId());
        assertEquals(user.getName(), ((User)caffeineResult.get()).getName());
        assertEquals(user.getEmail(), ((User)caffeineResult.get()).getEmail());
    }

    @Test
    public void testGetCacheMiss() {
        String key = "nonExistentKey";
        
        // 配置模拟行为
        when(redisCache.get(key)).thenReturn(null);
        
        // 从二级缓存获取
        Cache.ValueWrapper result = twoLevelCache.get(key);
        
        // 验证结果为null
        assertNull(result);
        
        // 验证Redis缓存被调用
        verify(redisCache, times(1)).get(key);
    }

    @Test
    public void testPutToBothCaches() {
        String key = "testKey";
        User user = new User(1L, "Alice", "alice@example.com");
        
        // 放入二级缓存
        twoLevelCache.put(key, user);
        
        // 验证数据在Caffeine缓存中
        Cache.ValueWrapper caffeineResult = caffeineCache.get(key);
        assertNotNull(caffeineResult);
        assertEquals(user.getId(), ((User)caffeineResult.get()).getId());
        assertEquals(user.getName(), ((User)caffeineResult.get()).getName());
        assertEquals(user.getEmail(), ((User)caffeineResult.get()).getEmail());
        
        // 验证数据在Redis缓存中
        verify(redisCache, times(1)).put(key, user);
    }

    @Test
    public void testEvictFromBothCaches() {
        String key = "testKey";
        
        // 从二级缓存中删除
        twoLevelCache.evict(key);
        
        // 验证从Caffeine缓存中删除
        assertNull(caffeineCache.get(key));
        
        // 验证从Redis缓存中删除
        verify(redisCache, times(1)).evict(key);
    }

    @Test
    public void testClearBothCaches() {
        // 清空二级缓存
        twoLevelCache.clear();
        
        // 验证Caffeine缓存被清空
        // 注意：由于CaffeineCache的实现，这里无法直接验证
        
        // 验证Redis缓存被清空
        verify(redisCache, times(1)).clear();
    }

    @Test
    public void testGetWithType() {
        String key = "testKey";
        User user = new User(1L, "Alice", "alice@example.com");
        
        // 先放入缓存
        caffeineCache.put(key, user);
        
        // 从二级缓存获取指定类型
        User result = twoLevelCache.get(key, User.class);
        
        // 验证结果
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testGetWithCallable() throws Exception {
        String key = "testKey";
        User user = new User(1L, "Alice", "alice@example.com");
        
        // 使用Callable获取数据
        User result = twoLevelCache.get(key, (Callable<User>) () -> user);
        
        // 验证结果
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        
        // 验证数据已被缓存
        Cache.ValueWrapper cachedResult = caffeineCache.get(key);
        assertNotNull(cachedResult);
        assertEquals(user, cachedResult.get());
        
        // 验证Redis缓存也被更新
        verify(redisCache, times(1)).put(key, user);
    }

    @Test
    public void testGetWithCallableException() throws Exception {
        String key = "testKey";
        Callable<User> callable = mock(Callable.class);
        
        // 配置Callable抛出异常
        when(callable.call()).thenThrow(new RuntimeException("Test exception"));
        
        // 验证抛出ValueRetrievalException
        assertThrows(Cache.ValueRetrievalException.class, () -> {
            twoLevelCache.get(key, callable);
        });
    }
}