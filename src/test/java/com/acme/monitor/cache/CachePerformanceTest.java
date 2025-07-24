package com.acme.monitor.cache;

import com.acme.monitor.model.User;
import com.acme.monitor.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CachePerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(CachePerformanceTest.class);

    @Autowired
    private UserService userService;

    @Test
    public void testSequentialCachePerformance() {
        long startTime, endTime;
        User user;

        // 第一次访问，从数据库加载
        startTime = System.currentTimeMillis();
        user = userService.getUserById(1L);
        endTime = System.currentTimeMillis();
        long firstAccessTime = endTime - startTime;
        logger.info("First access time: {} ms", firstAccessTime);

        // 后续访问，从缓存加载
        startTime = System.currentTimeMillis();
        user = userService.getUserById(1L);
        endTime = System.currentTimeMillis();
        long cachedAccessTime = endTime - startTime;
        logger.info("Cached access time: {} ms", cachedAccessTime);

        // 验证缓存访问明显更快
        assertTrue(cachedAccessTime < firstAccessTime, 
            "Cached access should be faster than database access");
        
        // 缓存访问应该非常快（小于10ms）
        assertTrue(cachedAccessTime < 10, 
            "Cached access should be very fast (< 10ms)");
    }

    @Test
    public void testConcurrentCacheAccess() throws ExecutionException, InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // 预热缓存
        userService.getUserById(1L);
        
        // 并发访问缓存
        CompletableFuture<?>[] futures = new CompletableFuture[threadCount];
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                User user = userService.getUserById(1L);
                assertNotNull(user);
                assertEquals("Alice", user.getName());
            }, executor);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures).get();
        long endTime = System.currentTimeMillis();
        
        long totalTime = endTime - startTime;
        logger.info("Concurrent access by {} threads took {} ms", threadCount, totalTime);
        
        executor.shutdown();
    }

    @Test
    public void testCacheWarmUp() {
        long startTime, endTime;
        
        // 测试大量数据访问的缓存预热效果
        startTime = System.currentTimeMillis();
        for (long i = 1; i <= 100; i++) {
            userService.getUserById(i % 3 + 1); // 循环访问用户1,2,3
        }
        endTime = System.currentTimeMillis();
        long warmUpTime = endTime - startTime;
        
        logger.info("Cache warm-up for 100 requests took {} ms", warmUpTime);
        
        // 再次访问同样的数据，应该更快
        startTime = System.currentTimeMillis();
        for (long i = 1; i <= 100; i++) {
            userService.getUserById(i % 3 + 1); // 循环访问用户1,2,3
        }
        endTime = System.currentTimeMillis();
        long afterWarmUpTime = endTime - startTime;
        
        logger.info("After warm-up, 100 requests took {} ms", afterWarmUpTime);
    }
}