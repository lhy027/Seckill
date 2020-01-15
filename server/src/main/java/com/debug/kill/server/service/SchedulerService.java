package com.debug.kill.server.service;

import com.debug.kill.model.entity.ItemKillSuccess;
import com.debug.kill.model.mapper.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author:lhy
 * @Date: 2020/1/8 19:15
 **/
@Service
public class SchedulerService {
    private static final Logger log= LoggerFactory.getLogger(SchedulerService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private Environment env;

    //@Scheduled(cron = "0/10 * * * * ?") //cron表达式
    @Scheduled(cron = "0 0/30 * * * ?") //cron表达式
    public void schedulerExpireOrders(){
        try{
            List<ItemKillSuccess> list=itemKillSuccessMapper.selectExpireOrders();
            if (list!=null && !list.isEmpty()){
                //java8的写法
                list.stream().forEach(i -> {
                    if (i!=null && i.getDiffTime() > env.getProperty("scheduler.expire.orders.time",Integer.class)){
                        itemKillSuccessMapper.expireOrder(i.getCode());
                    }
                });
            }

        }catch (Exception e){
            log.error("定时获取status=0的订单并判断是否超过TTL，然后进行失效-发生异常：",e.fillInStackTrace());
        }
    }

}
