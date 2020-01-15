package com.debug.kill.server.service;

import com.debug.kill.model.entity.ItemKill;

import java.util.List;

/**
 * @Author:lhy
 * @Date: 2020/1/6 9:56
 **/
public interface ItemService {
    /**
     * 获取待秒杀商品列表
     * @return
     * @throws Exception
     */
    List<ItemKill> getKillItems() throws Exception;

    /**
     * 获取商品详情页
     * @return
     * @throws Exception
     */
    ItemKill getKillDetail(Integer id) throws Exception;
}
