package com.debug.kill.server.service;


import com.debug.kill.model.dto.KillSuccessUserInfo;
import com.debug.kill.model.entity.ItemKillSuccess;
import com.debug.kill.model.mapper.ItemKillMapper;
import com.debug.kill.model.mapper.ItemKillSuccessMapper;
import com.debug.kill.server.dto.MailDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @Author:lhy
 * @Date: 2020/1/7 17:06
 **/
@Service
public class RabbitReceiverService {
    public static final Logger log= LoggerFactory.getLogger(RabbitReceiverService.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    /**
     * 秒杀异步邮件通知-接收消息
     */
    @RabbitListener(queues = {"${mq.kill.item.success.email.queue}"},containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info){
        try {
            log.info("11111111秒杀异步邮件通知-接收消息:{}",info);

            //TODO:下面才是真正邮件通知消息
//            MailDto dto=new MailDto(env.getProperty("mail.kill.item.success.subject"),"这是测试内容",new String[]{info.getEmail()});
//            mailService.sendSimpleEmail(dto);

            final String content=String.format(env.getProperty("mail.kill.item.success.content"),info.getItemName(),info.getCode());
            MailDto dto=new MailDto(env.getProperty("mail.kill.item.success.subject"),content,new String[]{info.getEmail()});
            mailService.sendHTMLMail(dto);

        }catch (Exception e){
            log.error("秒杀异步邮件通知-接收消息-发生异常：",e.fillInStackTrace());
        }
    }

    /**
     * 用户秒杀成功后超时未支付-监听者
     * @param info
     */
    @RabbitListener(queues = {"${mq.kill.item.success.kill.dead.real.queue}"},containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo info){
        try {
            log.info("用户秒杀成功后超时未支付-监听者-接收消息:{}",info);

           if (info!=null){
               ItemKillSuccess entity=itemKillSuccessMapper.selectByPrimaryKey(info.getCode());
               if (entity!=null && entity.getStatus().intValue()==0){
                   //秒杀结果设置成"-1"(无效)
                   itemKillSuccessMapper.expireOrder(info.getCode());
                   //秒杀活动如果还未结束，商品总数total加1
                   // （注：秒杀活动的时间要大于订单超时未交易的时间在实际生产当中这种情况应该比较少见，
                   // 如果出现这样的情况我觉得秒杀活动是没有多大意义的）
                   itemKillMapper.updateIsAddKillItemV2(info.getKillId());
               }else {
                   log.info("死信entity为null或者entity.getStatus().intValue()！=0");
               }
           }
        }catch (Exception e){
            log.error("用户秒杀成功后超时未支付-监听者-发生异常：",e.fillInStackTrace());
        }
    }
}
