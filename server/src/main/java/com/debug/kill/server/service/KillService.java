package com.debug.kill.server.service;

/**
 * @Author:lhy
 * @Date: 2020/1/6 15:03
 **/
public interface KillService {
    Boolean killItem(Integer killId,Integer userId) throws Exception;
    Boolean killItemV2(Integer killId,Integer userId) throws Exception;
    Boolean killItemV3(Integer killId,Integer userId) throws Exception;
    Boolean killItemV4(Integer killId,Integer userId) throws Exception;
    Boolean killItemV5(Integer killId,Integer userId) throws Exception;
}
