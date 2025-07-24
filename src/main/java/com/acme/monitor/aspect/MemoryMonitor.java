package com.acme.monitor.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MemoryMonitor {
    private static final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);
    
    @PostConstruct
    public void initialize() {
        // 添加JVM关闭钩子来捕获内存溢出
        Thread monitorThread = new Thread(new MemoryMonitorTask());
        monitorThread.setDaemon(true);
        monitorThread.setName("Memory-Monitor-Thread");
        monitorThread.setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof OutOfMemoryError) {
                logger.error("OutOfMemoryError detected in thread {}: ", thread.getName(), throwable);
            } else {
                logger.error("Unexpected error in thread {}: ", thread.getName(), throwable);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutdown hook triggered");
        }));
    }
    
    private static class MemoryMonitorTask implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(MemoryMonitorTask.class);
        
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 定期检查内存使用情况
                    checkMemoryUsage();
                    Thread.sleep(5000); // 每5秒检查一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Memory monitor thread interrupted");
                    break;
                } catch (Exception e) {
                    logger.error("Error in memory monitoring", e);
                }
            }
        }
        
        private void checkMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
            
            // 如果内存使用超过90%，记录警告
            if (memoryUsagePercentage > 90) {
                logger.warn("High memory usage detected: {}% (Used: {} MB, Max: {} MB)", 
                           String.format("%.2f", memoryUsagePercentage), 
                           usedMemory / (1024 * 1024), 
                           maxMemory / (1024 * 1024));
            }
        }
    }
    
    // 静态方法用于手动触发内存信息记录
    public static void logMemoryStatus() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;
        
        logger.info("Memory Status - Usage: {}% (Used: {} MB, Total: {} MB, Max: {} MB)",
                   String.format("%.2f", memoryUsagePercentage),
                   usedMemory / (1024 * 1024),
                   totalMemory / (1024 * 1024),
                   maxMemory / (1024 * 1024));
    }
}