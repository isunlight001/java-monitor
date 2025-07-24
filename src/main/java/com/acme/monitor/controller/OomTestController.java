package com.acme.monitor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OomTestController {

    @GetMapping("/trigger-oom")
    public String triggerOom() {
        // 创建一个无限循环，直到抛出 OutOfMemoryError
        List<Object> list = new ArrayList<>();
        while (true) {
            list.add(new byte[1024 * 1024]); // 每次添加1MB的数据
        }
    }
}