package com.debug.kill.server.service;

import com.debug.kill.model.entity.ItemKill;
import com.debug.kill.model.entity.ItemKillSuccess;
import com.debug.kill.model.mapper.ItemKillMapper;
import com.debug.kill.model.mapper.ItemKillSuccessMapper;
import com.debug.kill.server.enums.SysConstant;
import com.debug.kill.server.utils.RandomUtil;
import com.debug.kill.server.utils.SnowFlake;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author:lhy
 * @Date: 2020/1/6 15:08
 **/
@Service
public class KillServiceImpl implements KillService{
    private static final Logger log= LoggerFactory.getLogger(KillService.class);

    private SnowFlake snowFlake=new SnowFlake(2,3);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitSenderService rabbitSenderService;


    /**
     * 商品秒杀核心业务逻辑的处理
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        Boolean result=false;

        if (itemKillSuccessMapper.countByKillUserId(killId,userId)<=0){
            ItemKill itemKill=itemKillMapper.selectById(killId);
            if(itemKill!=null && 1==itemKill.getCanKill()){
                //TODO:扣减库存-减一
                int res=itemKillMapper.updateKillItem(killId);
                //TODO:扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if (res>0){
                   commonRecordKillSuccessInfo(itemKill,userId);
                    result=true;
                }
            }

        }else {
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }

    /**
     * 商品秒杀核心业务逻辑的处理----mysql优化
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        Boolean result=false;

        if (itemKillSuccessMapper.countByKillUserId(killId,userId)<=0){
            ItemKill itemKill=itemKillMapper.selectByIdV2(killId);
            if(itemKill!=null && 1==itemKill.getCanKill()){
                //TODO:扣减库存-减一
                int res=itemKillMapper.updateKillItemV2(killId);
                //TODO:扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if (res>0){
                    commonRecordKillSuccessInfo(itemKill,userId);
                    result=true;
                }
            }

        }else {
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 商品秒杀核心业务逻辑的处理----redis的分布式锁
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        Boolean result = false;
        if (itemKillSuccessMapper.countByKillUserId(killId,userId)<=0){
            //TODO:借助Redis的原子操作实现分布式锁-对共享操作-资源进行控制
             ValueOperations valueOperations=stringRedisTemplate.opsForValue();
             final String key=new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
             final String value=RandomUtil.generateOrderCode();
             Boolean cacheRes=valueOperations.setIfAbsent(key,value); //是否设置成功，只有key不存在的情况下才能设置成功返回true
            //TOOD：redis如果这时发生宕机，会产生死锁
             if(cacheRes){
                 log.info("redis的key值是："+key);
                 stringRedisTemplate.expire(key,30, TimeUnit.SECONDS); //在30秒后进行释放，为给定的密钥设置生存时间。
                 try {
                     ItemKill itemKill=itemKillMapper.selectByIdV2(killId);
                     if(itemKill!=null && 1==itemKill.getCanKill()){
                         //TODO:扣减库存-减一
                         int res=itemKillMapper.updateKillItemV2(killId);
                         //TODO:扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                         if (res>0){
                             commonRecordKillSuccessInfo(itemKill,userId);
                             result=true;
                         }
                     }

                 }catch (Exception e){
                     throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕");
                 }finally {
                     if (value.equals(valueOperations.get(key).toString())){
                         stringRedisTemplate.delete(key);
                     }
                 }
             }
        }else {
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }



    @Autowired
    private RedissonClient redissonClient;
    /**
     * 商品秒杀核心业务逻辑的处理----redisson的分布式锁
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        Boolean result=false;

        final String lockKey=new StringBuffer().append(killId).append(userId).append("-RedissonLock").toString();
        RLock rLock=redissonClient.getLock(lockKey);
        try {
            //最多等待30秒，上锁以后10秒自动解锁
            Boolean cacheRes=rLock.tryLock(30,10,TimeUnit.SECONDS);
            if (cacheRes){
               //TODO:核心业务逻辑
                if (itemKillSuccessMapper.countByKillUserId(killId,userId)<=0){
                    ItemKill itemKill=itemKillMapper.selectByIdV2(killId);
                    if(itemKill!=null && 1==itemKill.getCanKill()){
                        //TODO:扣减库存-减一
                        int res=itemKillMapper.updateKillItemV2(killId);
                        //TODO:扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                        if (res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result=true;
                        }
                    }

                }else{
                    throw new Exception("redisson-您已经抢购过该商品了!");
            }
            }
        }finally {
            rLock.unlock();
        }
        return result;
    }

    @Autowired
    private CuratorFramework curatorFramework;

    private static final String pathPrefix="/kill/zkLock";

    /**
     * 商品秒杀核心业务逻辑的处理-基于ZooKeeper的分布式锁
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV5(Integer killId, Integer userId) throws Exception {
        Boolean result=false;
        InterProcessMutex mutex=new InterProcessMutex(curatorFramework,pathPrefix+killId+userId+"-lock");
        try {
            if(mutex.acquire(10L,TimeUnit.SECONDS)){
                //TODO:核心业务逻辑
                if (itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill=itemKillMapper.selectByIdV2(killId);
                    if (itemKill!=null && 1==itemKill.getCanKill() && itemKill.getTotal()>0){
                        int res=itemKillMapper.updateKillItemV2(killId);
                        if (res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result=true;
                        }
                    }
                }else{
                    throw new Exception("zookeeper-您已经抢购过该商品了!");
                }
            }
        }catch (Exception e){
            throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕！");
        }finally {
            if (mutex!=null){
                mutex.release();
            }
        }
        return result;
    }


    private void commonRecordKillSuccessInfo(ItemKill kill,Integer userId)
            throws Exception{
        ItemKillSuccess entity=new ItemKillSuccess();
        String orderNo=String.valueOf(snowFlake.nextId());

        entity.setCode(orderNo);//雪花算法，生成订单号
        entity.setItemId(kill.getItemId());
        entity.setKillId(kill.getId());
        entity.setUserId(userId.toString());
        entity.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        entity.setCreateTime(DateTime.now().toDate());
        //TODO:类似于单例模式的双重校验，不断校验，开销有点大
        if (itemKillSuccessMapper.countByKillUserId(kill.getId(),userId)<=0){
            int res=itemKillSuccessMapper.insertSelective(entity);
            if(res>0){
                //TODO:进行异步邮件消息通知
                rabbitSenderService.sendKillSuccessEmailMsg(orderNo);

                //TODO:入死信队列，用于 “失效” 超过指定的TTL时间时仍然未支付的订单
                rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNo);
            }
        }
    }
}
