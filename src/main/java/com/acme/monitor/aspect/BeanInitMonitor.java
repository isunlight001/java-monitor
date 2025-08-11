package com.acme.monitor.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BeanInitMonitor implements BeanPostProcessor, ApplicationContextAware, SmartLifecycle {
    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> postProcessBeforeInitTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> postProcessAfterInitTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> initCosts = new ConcurrentHashMap<>();
    
    // 记录所有Bean的初始化时间
    private final List<BeanInitInfo> beanInitInfos = new ArrayList<>();
    private ApplicationContext applicationContext;
    private boolean running = false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        long currentTime = System.nanoTime();
        startTimes.put(beanName, currentTime);
        postProcessBeforeInitTimes.put(beanName, currentTime);
        
        if (log.isDebugEnabled()) {
            log.debug("Bean {} - Before Initialization: {}", beanName, currentTime);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        long currentTime = System.nanoTime();
        postProcessAfterInitTimes.put(beanName, currentTime);
        
        Long startTime = startTimes.get(beanName);
        if (startTime != null) {
            long totalCost = (currentTime - startTime) / 1_000_000; // 转换为毫秒
            initCosts.put(beanName, totalCost);
            
            // 计算各阶段耗时
            Long beforeInitTime = postProcessBeforeInitTimes.get(beanName);
            long beforeInitCost = beforeInitTime != null ? (beforeInitTime - startTime) / 1_000_000 : 0;
            
            long afterInitCost = (currentTime - beforeInitTime) / 1_000_000;
            
            // 记录Bean初始化信息
            beanInitInfos.add(new BeanInitInfo(beanName, bean.getClass().getName(), totalCost, beforeInitCost, afterInitCost));
            
            // 超过50ms的Bean记录警告日志
            if (totalCost > 50) {
                log.warn("Bean {} total init cost: {}ms (beforeInit: {}ms, afterInit: {}ms)", 
                        beanName, totalCost, beforeInitCost, afterInitCost);
            } else if (log.isDebugEnabled()) {
                log.debug("Bean {} total init cost: {}ms (beforeInit: {}ms, afterInit: {}ms)", 
                        beanName, totalCost, beforeInitCost, afterInitCost);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Bean {} - After Initialization: {}", beanName, currentTime);
        }
        return bean;
    }
    
    /**
     * 获取初始化时间超过指定阈值的Bean列表，按耗时降序排列
     * @param thresholdMs 阈值（毫秒）
     * @return 初始化时间超过阈值的Bean列表
     */
    public List<BeanInitInfo> getSlowBeans(long thresholdMs) {
        return beanInitInfos.stream()
                .filter(info -> info.getTotalTime() >= thresholdMs)
                .sorted(Comparator.comparingLong(BeanInitInfo::getTotalTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有Bean的初始化时间，按耗时降序排列
     * @return 所有Bean的初始化时间列表
     */
    public List<BeanInitInfo> getAllBeans() {
        return beanInitInfos.stream()
                .sorted(Comparator.comparingLong(BeanInitInfo::getTotalTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取初始化最慢的前N个Bean
     * @param topN 前N个
     * @return 初始化最慢的前N个Bean列表
     */
    public List<BeanInitInfo> getTopSlowBeans(int topN) {
        return beanInitInfos.stream()
                .sorted(Comparator.comparingLong(BeanInitInfo::getTotalTime).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void start() {
        running = true;
        log.info("BeanInitMonitor started");
    }

    @Override
    public void stop() {
        running = false;
        log.info("BeanInitMonitor stopped");
        // 应用关闭时打印所有Bean的初始化信息
        printAllBeanInitInfo();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 打印所有Bean的初始化信息
     */
    private void printAllBeanInitInfo() {
        log.info("=== Bean Initialization Summary ===");
        List<BeanInitInfo> sortedBeans = getAllBeans();
        for (BeanInitInfo info : sortedBeans) {
            log.info("Bean: {} | Class: {} | Total: {}ms | BeforeInit: {}ms | AfterInit: {}ms", 
                    info.getBeanName(), info.getClassName(), info.getTotalTime(), 
                    info.getBeforeInitTime(), info.getAfterInitTime());
        }
        log.info("=== End of Bean Initialization Summary ===");
    }
    
    /**
     * Bean初始化信息类
     */
    public static class BeanInitInfo {
        private final String beanName;
        private final String className;
        private final long totalTime; // 总耗时（毫秒）
        private final long beforeInitTime; // 初始化前阶段耗时（毫秒）
        private final long afterInitTime; // 初始化后阶段耗时（毫秒）
        
        public BeanInitInfo(String beanName, String className, long totalTime, 
                           long beforeInitTime, long afterInitTime) {
            this.beanName = beanName;
            this.className = className;
            this.totalTime = totalTime;
            this.beforeInitTime = beforeInitTime;
            this.afterInitTime = afterInitTime;
        }
        
        public String getBeanName() {
            return beanName;
        }
        
        public String getClassName() {
            return className;
        }
        
        public long getTotalTime() {
            return totalTime;
        }
        
        public long getBeforeInitTime() {
            return beforeInitTime;
        }
        
        public long getAfterInitTime() {
            return afterInitTime;
        }
        
        @Override
        public String toString() {
            return String.format("BeanInitInfo{beanName='%s', className='%s', totalTime=%d ms, beforeInit=%d ms, afterInit=%d ms}", 
                    beanName, className, totalTime, beforeInitTime, afterInitTime);
        }
    }
}