package com.acme.monitor.controller;

import com.acme.monitor.aspect.BeanInitMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Bean初始化监控控制器
 * 
 * 提供REST API接口用于查看Bean初始化时间信息，帮助定位启动慢的Bean
 */
@RestController
@RequestMapping("/api/monitor/bean")
public class BeanInitMonitorController {
    
    @Autowired
    private BeanInitMonitor beanInitMonitor;
    
    /**
     * 获取所有Bean的初始化时间信息，按耗时降序排列
     * 
     * @return 所有Bean的初始化时间信息列表
     */
    @GetMapping("/all")
    public List<BeanInitMonitor.BeanInitInfo> getAllBeans() {
        return beanInitMonitor.getAllBeans();
    }
    
    /**
     * 获取初始化时间超过指定阈值的Bean列表
     * 
     * @param thresholdMs 阈值（毫秒），默认为50ms
     * @return 初始化时间超过阈值的Bean列表
     */
    @GetMapping("/slow")
    public List<BeanInitMonitor.BeanInitInfo> getSlowBeans(
            @RequestParam(defaultValue = "50") long thresholdMs) {
        return beanInitMonitor.getSlowBeans(thresholdMs);
    }
    
    /**
     * 获取初始化最慢的前N个Bean
     * 
     * @param topN 前N个，默认为10
     * @return 初始化最慢的前N个Bean列表
     */
    @GetMapping("/top")
    public List<BeanInitMonitor.BeanInitInfo> getTopSlowBeans(
            @RequestParam(defaultValue = "10") int topN) {
        return beanInitMonitor.getTopSlowBeans(topN);
    }
}