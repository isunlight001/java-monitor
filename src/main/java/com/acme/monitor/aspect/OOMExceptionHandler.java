package com.acme.monitor.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

@Component
public class OOMExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(OOMExceptionHandler.class);
    private static volatile boolean isOOMLogged = false;
    
    @PostConstruct
    public void registerOOMHandler() {
        // 设置默认的未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (e instanceof OutOfMemoryError) {
                    handleOOM((OutOfMemoryError) e, t);
                } else {
                    logger.error("Uncaught exception in thread {}", t.getName(), e);
                }
            }
        });
        
        // 启动内存监控线程
        MemoryMonitorThread monitorThread = new MemoryMonitorThread();
        monitorThread.setDaemon(true);
        monitorThread.start();
        
        logger.info("OOM Exception Handler registered successfully");
    }
    
    private void handleOOM(OutOfMemoryError error, Thread thread) {
        // 使用双重检查锁定确保只记录一次
        if (!isOOMLogged) {
            synchronized (OOMExceptionHandler.class) {
                if (!isOOMLogged) {
                    isOOMLogged = true;
                    logger.error("OutOfMemoryError caught in thread {}: {}", thread.getName(), error.getMessage(), error);
                    
                    // 记录当前内存状态
                    logMemoryStatus();
                    
                    // 可以在这里添加其他操作，如发送告警等
                    logger.error("Application is in unstable state due to OutOfMemoryError. Immediate action required!");
                }
            }
        }
    }
    
    private void logMemoryStatus() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            logger.error("Memory Status at OOM:");
            logger.error("  Heap Memory: {} MB used / {} MB committed / {} MB max",
                    heapUsage.getUsed() / (1024 * 1024),
                    heapUsage.getCommitted() / (1024 * 1024),
                    heapUsage.getMax() / (1024 * 1024));
            
            logger.error("  Non-Heap Memory: {} MB used / {} MB committed",
                    nonHeapUsage.getUsed() / (1024 * 1024),
                    nonHeapUsage.getCommitted() / (1024 * 1024));
        } catch (Exception e) {
            logger.error("Failed to log memory status", e);
        }
    }
    
    // 内存监控线程，定期检查内存使用情况
    private static class MemoryMonitorThread extends Thread {
        private static final Logger monitorLogger = LoggerFactory.getLogger(MemoryMonitorThread.class);
        private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        public MemoryMonitorThread() {
            super("Memory-Monitor-Thread");
        }
        
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
                    long maxMemory = heapUsage.getMax();
                    long usedMemory = heapUsage.getUsed();
                    
                    if (maxMemory > 0) {
                        double usagePercentage = (double) usedMemory / maxMemory * 100;
                        
                        // 如果内存使用超过95%，记录警告日志
                        if (usagePercentage > 95) {
                            monitorLogger.warn("Critical memory usage: {}% ({} MB / {} MB)",
                                    String.format("%.2f", usagePercentage),
                                    usedMemory / (1024 * 1024),
                                    maxMemory / (1024 * 1024));
                        } else if (usagePercentage > 85) {
                            monitorLogger.info("High memory usage: {}% ({} MB / {} MB)",
                                    String.format("%.2f", usagePercentage),
                                    usedMemory / (1024 * 1024),
                                    maxMemory / (1024 * 1024));
                        }
                    }
                    
                    // 每10秒检查一次
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    monitorLogger.info("Memory monitor thread interrupted");
                    break;
                } catch (Exception e) {
                    monitorLogger.error("Error in memory monitoring", e);
                }
            }
        }
    }
}