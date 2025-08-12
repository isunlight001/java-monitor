package com.acme.monitor.controller;

import com.acme.monitor.MonitorApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ops")
public class RestartController {
    @Autowired
    private ConfigurableApplicationContext context;

    @PostMapping("/restart")
    public String restart() {
        Thread restartThread = new Thread(() -> {
            context.close();                    // 关闭旧容器
            context = SpringApplication.run(MonitorApplication.class); // 重新跑
        });
        restartThread.setDaemon(false);
        restartThread.start();
        return "restarting...";
    }
}
