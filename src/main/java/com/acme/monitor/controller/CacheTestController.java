package com.acme.monitor.controller;

import com.acme.monitor.model.User;
import com.acme.monitor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheTestController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    @PostMapping("/user")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    
    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    
    @DeleteMapping("/user/clear")
    public void clearAllUsers() {
        userService.clearAllUsers();
    }
}