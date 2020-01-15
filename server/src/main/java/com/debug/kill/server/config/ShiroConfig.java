package com.debug.kill.server.config;/**
 * Created by Administrator on 2019/7/2.
 */

import com.debug.kill.server.service.CustomRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * shiro的通用化配置
 * @Author:debug (SteadyJack)
 * @Date: 2019/7/2 17:54
 **/
@Configuration
public class ShiroConfig {

    @Bean
    public CustomRealm customRealm(){
        return new CustomRealm();
    }

    @Bean
    public SecurityManager securityManager(){
        DefaultWebSecurityManager securityManager=new DefaultWebSecurityManager();
        securityManager.setRealm(customRealm());
        return securityManager;
    }

    /**
     * 对一些url进行严格控制
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(){
        ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager());
        bean.setLoginUrl("/to/login");
        //访问一些没有权限链接的跳转到/unauth
        bean.setUnauthorizedUrl("/unauth");

        //访问url对应的一些动作
        Map<String, String> filterChainDefinitionMap=new HashMap<>();
        //匿名操作
        filterChainDefinitionMap.put("/to/login","anon");

        filterChainDefinitionMap.put("/**","anon");

        //访问带/kill/execute/*的连接执行授权操作(authc)
        filterChainDefinitionMap.put("/kill/execute/*","authc");
        filterChainDefinitionMap.put("/item/detail/*","authc");

        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
    }

}




























