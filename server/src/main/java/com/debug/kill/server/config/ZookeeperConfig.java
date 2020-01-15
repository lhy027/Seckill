package com.debug.kill.server.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @Author:lhy
 * @Date: 2020/1/10 16:31
 **/
@Configuration
public class ZookeeperConfig {
    @Autowired
    public Environment env;

    @Bean
    public CuratorFramework curatorFramework(){
        CuratorFramework curatorFramework=CuratorFrameworkFactory.builder()
                .connectString(env.getProperty("zk.host"))
                .namespace("zk.namespace")
                //重试策略
                .retryPolicy(new RetryNTimes(5,1000))
                .build();
        curatorFramework.start();
        return curatorFramework;

    }
}
