package com.acme.monitor.aspect;

import com.acme.monitor.controller.DatabaseTestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
public class DatabaseExceptionTest {

    @Autowired
    private DatabaseTestController databaseTestController;

    @MockBean
    private DataSource dataSource;

    @BeforeEach
    public void setUp() throws SQLException {
        // 模拟数据库连接失败
        when(dataSource.getConnection()).thenThrow(new SQLException("Simulated database connection failure"));
    }

    @Test
    public void testDatabaseConnectionFailure() {
        // 验证是否抛出 DataAccessResourceFailureException
        assertThrows(DataAccessResourceFailureException.class, () -> {
            databaseTestController.triggerDbConnectionFailure();
        });
    }
}