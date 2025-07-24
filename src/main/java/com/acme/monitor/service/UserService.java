package com.acme.monitor.service;

import com.acme.monitor.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // 模拟数据库存储
    private final Map<Long, User> userDatabase = new HashMap<>();
    
    public UserService() {
        // 初始化一些测试数据
        userDatabase.put(1L, new User(1L, "Alice", "alice@example.com"));
        userDatabase.put(2L, new User(2L, "Bob", "bob@example.com"));
        userDatabase.put(3L, new User(3L, "Charlie", "charlie@example.com"));
    }
    
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        logger.info("Fetching user from database with id: {}", id);
        // 模拟数据库查询延迟
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return userDatabase.get(id);
    }
    
    @CachePut(value = "users", key = "#user.id")
    public User saveUser(User user) {
        logger.info("Saving user to database: {}", user);
        userDatabase.put(user.getId(), user);
        return user;
    }
    
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        logger.info("Deleting user from database with id: {}", id);
        userDatabase.remove(id);
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void clearAllUsers() {
        logger.info("Clearing all users from cache");
        userDatabase.clear();
    }
}