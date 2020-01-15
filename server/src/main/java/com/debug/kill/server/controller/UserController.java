package com.debug.kill.server.controller;

import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author:lhy
 * @Date: 2020/1/11 14:36
 **/
@Controller
public class UserController  {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * 跳到登录页
     * @return
     */
    @RequestMapping(value = {"/to/login","/unauth"})
    public String toLogin(){
        return "login";
    }

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(@RequestParam String userName, @RequestParam String password, ModelMap modelMap){
        String errorMsg="";
        try {
            if (!SecurityUtils.getSubject().isAuthenticated()){
               UsernamePasswordToken token= new UsernamePasswordToken(userName,password);
                //交给真正的Realm做认证和授权
               SecurityUtils.getSubject().login(token);
            }
        }catch (UnknownAccountException e){
            errorMsg=e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch (DisabledAccountException e){
            errorMsg=e.getMessage();
            modelMap.addAttribute("userName",userName);
        }catch (IncorrectCredentialsException e){
            errorMsg="用户登录异常，请联系管理员！";
            e.printStackTrace();
        } catch (Exception e){
            errorMsg="用户登录异常，请联系管理员！";
            e.printStackTrace();
        }
        if(StringUtils.isBlank(errorMsg)){
            return "redirect:/index";
        }else {
            modelMap.addAttribute("errorMsg",errorMsg);
            return "login";
        }

    }

    //退出
    @RequestMapping(value = "/logout")
    public String logout(){
        //注销用户信息
        SecurityUtils.getSubject().logout();
        return "login";
    }
}
