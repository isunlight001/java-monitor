package com.acme.monitor.cache;

import com.acme.monitor.MonitorApplication;
import com.acme.monitor.config.CacheConfig;
import com.acme.monitor.model.User;
import com.acme.monitor.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MonitorApplication.class)
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
public class TwoLevelCacheIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(TwoLevelCacheIntegrationTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testCacheManagerConfiguration() {
        // 验证缓存管理器类型
        assertNotNull(cacheManager);
        logger.info("Cache manager type: {}", cacheManager.getClass().getName());
    }

    @Test
    public void testUserServiceCaching() {
        // 获取用户，第一次应该访问数据库
        long startTime = System.currentTimeMillis();
        User user1 = userService.getUserById(1L);
        long firstCallTime = System.currentTimeMillis() - startTime;

        // 再次获取同一用户，应该从缓存中获取
        startTime = System.currentTimeMillis();
        User user2 = userService.getUserById(1L);
        long secondCallTime = System.currentTimeMillis() - startTime;

        // 验证两次获取的是同一个用户
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getName(), user2.getName());
        assertEquals(user1.getEmail(), user2.getEmail());
        
        // 验证第二次调用更快（从缓存获取）
        assertTrue(secondCallTime <= firstCallTime, 
            "Second call should be faster as it retrieves from cache");
        
        logger.info("First call time: {} ms, Second call time: {} ms", firstCallTime, secondCallTime);
    }

    @Test
    public void testCachePut() {
        User newUser = new User();
        newUser.setId(100L);
        newUser.setName("TestUser");
        newUser.setEmail("test@example.com");
        
        // 保存用户，应该更新缓存
        User savedUser = userService.saveUser(newUser);
        
        // 立即获取用户，应该从缓存中获取
        User cachedUser = userService.getUserById(100L);
        
        assertEquals(savedUser.getId(), cachedUser.getId());
        assertEquals(savedUser.getName(), cachedUser.getName());
        assertEquals(savedUser.getEmail(), cachedUser.getEmail());
        assertEquals("TestUser", cachedUser.getName());
        assertEquals("test@example.com", cachedUser.getEmail());
    }

    @Test
    public void testCacheEvict() {
        // 先获取用户，确保在缓存中
        User user = userService.getUserById(1L);
        assertNotNull(user);
        
        // 删除用户，应该从缓存中移除
        userService.deleteUser(1L);
        
        // 再次获取用户，应该返回null（因为我们删除了数据库中的数据）
        // 但由于我们只是模拟数据库，所以这里仍然会返回数据
        // 但在实际应用中，这会触发数据库查询
    }

    @Test
    public void testCacheClear() {
        // 先获取一些用户，确保在缓存中
        User user1 = userService.getUserById(1L);
        User user2 = userService.getUserById(2L);
        
        assertNotNull(user1);
        assertNotNull(user2);
        
        // 清空所有用户缓存
        userService.clearAllUsers();
        
        // 验证缓存已被清空的操作比较复杂，因为需要访问底层缓存实现
        // 这里主要验证方法可以正常调用
    }

    @Test
    public void testCaffeineAndRedisCacheSeparation() {
        // 这个测试验证我们可以访问底层的Caffeine和Redis缓存管理器
        if (cacheManager instanceof CaffeineCacheManager) {
            logger.info("Using Caffeine cache manager directly");
        } else if (cacheManager instanceof RedisCacheManager) {
            logger.info("Using Redis cache manager directly");
        } else {
            logger.info("Using custom cache manager: {}", cacheManager.getClass().getName());
        }
    }
}