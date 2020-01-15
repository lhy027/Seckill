package com.debug.kill.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author:lhy
 * @Date: 2020/1/4 17:07
 **/
@Controller
@RequestMapping("base")
public class BaseController {
    @RequestMapping(value = "/error",method = RequestMethod.GET)
    public String error(){
        return "error";
    }
}
