package com.acme.monitor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * 数据库配置类
 * 只有在启用数据库功能时才会导入相关配置，以提高启动速度
 */
@Configuration
@ConditionalOnProperty(name = "app.database.enabled", havingValue = "true", matchIfMissing = false)
@Import({
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class DatabaseConfig {
}