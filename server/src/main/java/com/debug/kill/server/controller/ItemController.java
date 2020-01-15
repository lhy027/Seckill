package com.debug.kill.server.controller;

import com.debug.kill.model.entity.ItemKill;
import com.debug.kill.server.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @Author:lhy
 * @Date: 2020/1/6 9:44
 **/
@Controller
public class ItemController {
   private static final Logger log= LoggerFactory.getLogger(ItemController.class);

    private static final String prefix="item";

    @Autowired
    private ItemService itemService;

    @RequestMapping(value={"/","/index",prefix+"/list",prefix+"/index.html"},method = RequestMethod.GET)
    public String list(ModelMap modelMap){
        try {
           List<ItemKill> list=itemService.getKillItems();
           modelMap.put("list",list);
           log.info("获取秒杀商品列表数据：{}",list);
        }catch (Exception e){
           log.error("获取待秒杀商品列表-发生异常：",e.fillInStackTrace());

            return "redirect:/base/error";
        }
        return "list";
    }

    //详情页
    @RequestMapping(value=prefix+"/detail/{id}",method = RequestMethod.GET)
    public String detail(@PathVariable Integer id,ModelMap modelMap){
      if (id==null || id<=0){
          return "redirect:/base/error";
      }

      try {
          ItemKill itemKill=itemService.getKillDetail(id);
          modelMap.put("detail",itemKill);
      }catch (Exception e){
          log.error("获取待秒杀商品详情页-发生异常：id={}",id,e.fillInStackTrace());
          return "redirect:/base/error";
      }
        return"info";
    }


}
