package com.acme.monitor.controller;

import com.acme.monitor.aspect.BeanInitMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Bean详细信息控制器
 * 
 * 提供REST API接口用于查看Bean初始化的详细操作信息
 */
@RestController
@RequestMapping("/api/monitor/bean/details")
public class BeanDetailsController {
    
    @Autowired
    private BeanInitMonitor beanInitMonitor;
    
    /**
     * 获取所有Bean的详细信息
     * 
     * @return 所有Bean的详细信息列表
     */
    @GetMapping("/all")
    public List<BeanInitMonitor.BeanDetails> getAllBeanDetails() {
        return beanInitMonitor.getAllBeanDetails();
    }
    
    /**
     * 根据Bean名称获取特定Bean的详细信息
     * 
     * @param beanName Bean名称
     * @return Bean的详细信息
     */
    @GetMapping("/{beanName}")
    public BeanInitMonitor.BeanDetails getBeanDetails(@PathVariable String beanName) {
        return beanInitMonitor.getBeanDetails(beanName);
    }
}