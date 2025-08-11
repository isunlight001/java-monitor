package com.acme.monitor.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
    private final Map<String, BeanDetails> beanDetailsMap = new ConcurrentHashMap<>();
    
    // 记录所有Bean的初始化时间
    private final List<BeanInitInfo> beanInitInfos = new ArrayList<>();
    private ApplicationContext applicationContext;
    private boolean running = false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        long currentTime = System.nanoTime();
        startTimes.put(beanName, currentTime);
        postProcessBeforeInitTimes.put(beanName, currentTime);
        
        // 记录Bean详细信息
        beanDetailsMap.put(beanName, new BeanDetails(beanName, bean.getClass()));
        
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
            
            // 获取Bean详细信息
            BeanDetails beanDetails = beanDetailsMap.get(beanName);
            if (beanDetails != null) {
                // 分析Bean的初始化操作
                analyzeBeanInitialization(bean, beanName, beanDetails);
            }
            
            // 记录Bean初始化信息
            beanInitInfos.add(new BeanInitInfo(beanName, bean.getClass().getName(), totalCost, beforeInitCost, afterInitCost));
            
            // 超过50ms的Bean记录警告日志
            if (totalCost > 50) {
                log.warn("Bean {} total init cost: {}ms (beforeInit: {}ms, afterInit: {}ms)", 
                        beanName, totalCost, beforeInitCost, afterInitCost);
                
                // 如果有详细信息，也记录下来
                if (beanDetails != null && !beanDetails.getOperations().isEmpty()) {
                    log.warn("Bean {} initialization operations: {}", beanName, beanDetails.getOperations());
                }
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
     * 分析Bean的初始化操作
     * @param bean Bean实例
     * @param beanName Bean名称
     * @param beanDetails Bean详细信息
     */
    private void analyzeBeanInitialization(Object bean, String beanName, BeanDetails beanDetails) {
        // 检查是否有@PostConstruct注解的方法
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(javax.annotation.PostConstruct.class)) {
                beanDetails.addOperation("PostConstruct method: " + method.getName());
            }
        }
        
        // 检查是否实现了InitializingBean接口
        if (bean instanceof org.springframework.beans.factory.InitializingBean) {
            beanDetails.addOperation("InitializingBean.afterPropertiesSet()");
        }
        
        // 检查是否有init-method配置
        // 注意：这部分需要通过ApplicationContext获取BeanDefinition来检查，简化处理
        
        // 记录一些通用信息
        beanDetails.addOperation("Class: " + bean.getClass().getName());
        beanDetails.addOperation("Fields count: " + bean.getClass().getDeclaredFields().length);
        beanDetails.addOperation("Methods count: " + methods.length);
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
    
    /**
     * 获取Bean的详细信息
     * @param beanName Bean名称
     * @return Bean详细信息
     */
    public BeanDetails getBeanDetails(String beanName) {
        return beanDetailsMap.get(beanName);
    }
    
    /**
     * 获取所有Bean的详细信息
     * @return 所有Bean的详细信息列表
     */
    public List<BeanDetails> getAllBeanDetails() {
        return new ArrayList<>(beanDetailsMap.values());
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
            
            // 打印详细信息
            BeanDetails details = getBeanDetails(info.getBeanName());
            if (details != null && !details.getOperations().isEmpty()) {
                log.info("  Operations: {}", details.getOperations());
            }
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
    
    /**
     * Bean详细信息类
     */
    public static class BeanDetails {
        private final String beanName;
        private final Class<?> beanClass;
        private final List<String> operations = new ArrayList<>();
        
        public BeanDetails(String beanName, Class<?> beanClass) {
            this.beanName = beanName;
            this.beanClass = beanClass;
        }
        
        public void addOperation(String operation) {
            operations.add(operation);
        }
        
        public String getBeanName() {
            return beanName;
        }
        
        public Class<?> getBeanClass() {
            return beanClass;
        }
        
        public List<String> getOperations() {
            return new ArrayList<>(operations);
        }
        
        @Override
        public String toString() {
            return String.format("BeanDetails{beanName='%s', beanClass=%s, operations=%s}", 
                    beanName, beanClass.getName(), operations);
        }
    }
}