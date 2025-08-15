package com.acme.monitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@ConditionalOnProperty(name = "app.database.enabled", havingValue = "true", matchIfMissing = false)
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/trigger-db-connection-failure")
    public String triggerDbConnectionFailure() {
        try {
            // 尝试获取数据库连接
            Connection connection = dataSource.getConnection();
            return "Database connection established successfully.";
        } catch (SQLException e) {
            // 这里会触发 DataAccessResourceFailureException 如果数据库连接不上
            throw new org.springframework.dao.DataAccessResourceFailureException("Failed to connect to database", e);
        }
    }
}