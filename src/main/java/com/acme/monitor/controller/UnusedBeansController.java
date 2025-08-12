package com.acme.monitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 未使用Bean检测控制器
 * 
 * 提供REST API接口用于检测和分析Spring容器中可能未被使用的Bean
 * 这些Bean可能是在项目演进过程中遗留下来的无用组件，占用内存和启动时间
 */
@RestController
@RequestMapping("/api/monitor/beans")
public class UnusedBeansController {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 获取所有Bean的名称和类型信息
     * 
     * @return 所有Bean的名称和类型信息
     */
    @GetMapping("/all")
    public Map<String, String> getAllBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Map<String, String> beans = new HashMap<>();
        
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            beans.put(beanName, bean.getClass().getName());
        }
        
        return beans;
    }
    
    /**
     * 获取可能未被使用的Bean列表
     * 
     * 通过分析Bean的依赖关系，找出没有被其他Bean依赖的Bean
     * 这些Bean可能是无用的，需要进一步分析确认
     * 
     * @return 可能未被使用的Bean列表
     */
    @GetMapping("/unused")
    public List<Map<String, String>> getUnusedBeans() {
        ConfigurableListableBeanFactory beanFactory = 
            (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        
        // 获取所有Bean的依赖关系
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String[] dependsOn = beanDefinition.getDependsOn();
            if (dependsOn != null) {
                dependencies.put(beanName, new HashSet<>(Arrays.asList(dependsOn)));
            } else {
                dependencies.put(beanName, new HashSet<>());
            }
        }
        
        // 找出没有被其他Bean依赖的Bean（即入度为0的Bean）
        Set<String> allBeans = new HashSet<>(Arrays.asList(beanNames));
        Set<String> dependentBeans = dependencies.values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        
        Set<String> unusedBeans = new HashSet<>(allBeans);
        unusedBeans.removeAll(dependentBeans);
        
        // 过滤掉Spring框架自身的Bean
        List<Map<String, String>> result = new ArrayList<>();
        for (String beanName : unusedBeans) {
            // 跳过Spring框架的Bean
            if (beanName.startsWith("org.springframework") || beanName.startsWith("scopedTarget.")) {
                continue;
            }
            
            Object bean = applicationContext.getBean(beanName);
            Map<String, String> beanInfo = new HashMap<>();
            beanInfo.put("name", beanName);
            beanInfo.put("class", bean.getClass().getName());
            beanInfo.put("package", bean.getClass().getPackage().getName());
            result.add(beanInfo);
        }
        
        return result;
    }
    
    /**
     * 获取可能的孤儿Bean列表
     * 
     * 这些Bean不仅没有被其他Bean依赖，而且它们自身也没有依赖其他Bean
     * 更可能是完全无用的组件
     * 
     * @return 可能的孤儿Bean列表
     */
    @GetMapping("/orphans")
    public List<Map<String, String>> getOrphanBeans() {
        ConfigurableListableBeanFactory beanFactory = 
            (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        
        // 获取所有Bean的依赖关系
        Map<String, Set<String>> dependencies = new HashMap<>();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            String[] dependsOn = beanDefinition.getDependsOn();
            if (dependsOn != null) {
                dependencies.put(beanName, new HashSet<>(Arrays.asList(dependsOn)));
            } else {
                dependencies.put(beanName, new HashSet<>());
            }
        }
        
        // 找出没有被其他Bean依赖且自身也没有依赖的Bean
        Set<String> allBeans = new HashSet<>(Arrays.asList(beanNames));
        Set<String> dependentBeans = dependencies.values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        
        Set<String> unusedBeans = new HashSet<>(allBeans);
        unusedBeans.removeAll(dependentBeans);
        
        List<Map<String, String>> result = new ArrayList<>();
        for (String beanName : unusedBeans) {
            // 跳过Spring框架的Bean
            if (beanName.startsWith("org.springframework") || beanName.startsWith("scopedTarget.")) {
                continue;
            }
            
            // 检查这个Bean是否也没有依赖其他Bean
            Set<String> beanDependencies = dependencies.get(beanName);
            if (beanDependencies != null && beanDependencies.isEmpty()) {
                Object bean = applicationContext.getBean(beanName);
                Map<String, String> beanInfo = new HashMap<>();
                beanInfo.put("name", beanName);
                beanInfo.put("class", bean.getClass().getName());
                beanInfo.put("package", bean.getClass().getPackage().getName());
                result.add(beanInfo);
            }
        }
        
        return result;
    }
}