package com.acme.monitor.aspect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BeanInitMonitor implements BeanPostProcessor {
    private final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> initCosts = new ConcurrentHashMap<>();
    
    // 记录所有Bean的初始化时间
    private final List<BeanInitInfo> beanInitInfos = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        startTimes.put(beanName, System.nanoTime());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Long startTime = startTimes.get(beanName);
        if (startTime != null) {
            long cost = (System.nanoTime() - startTime) / 1_000_000; // 转换为毫秒
            initCosts.put(beanName, cost);
            
            // 记录Bean初始化信息
            beanInitInfos.add(new BeanInitInfo(beanName, bean.getClass().getName(), cost));
            
            // 超过50ms的Bean记录警告日志
            if (cost > 50) {
                log.warn("Bean {} init cost: {}ms", beanName, cost);
            }
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
                .filter(info -> info.getInitTime() >= thresholdMs)
                .sorted(Comparator.comparingLong(BeanInitInfo::getInitTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有Bean的初始化时间，按耗时降序排列
     * @return 所有Bean的初始化时间列表
     */
    public List<BeanInitInfo> getAllBeans() {
        return beanInitInfos.stream()
                .sorted(Comparator.comparingLong(BeanInitInfo::getInitTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取初始化最慢的前N个Bean
     * @param topN 前N个
     * @return 初始化最慢的前N个Bean列表
     */
    public List<BeanInitInfo> getTopSlowBeans(int topN) {
        return beanInitInfos.stream()
                .sorted(Comparator.comparingLong(BeanInitInfo::getInitTime).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
    
    /**
     * Bean初始化信息类
     */
    public static class BeanInitInfo {
        private final String beanName;
        private final String className;
        private final long initTime; // 毫秒
        
        public BeanInitInfo(String beanName, String className, long initTime) {
            this.beanName = beanName;
            this.className = className;
            this.initTime = initTime;
        }
        
        public String getBeanName() {
            return beanName;
        }
        
        public String getClassName() {
            return className;
        }
        
        public long getInitTime() {
            return initTime;
        }
        
        @Override
        public String toString() {
            return String.format("BeanInitInfo{beanName='%s', className='%s', initTime=%d ms}", 
                    beanName, className, initTime);
        }
    }
}