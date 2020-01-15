package com.debug.kill.server.service;

import com.debug.kill.model.entity.ItemKill;
import com.debug.kill.model.mapper.ItemKillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author:lhy
 * @Date: 2020/1/6 10:15
 **/
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Override
    public List<ItemKill> getKillItems() throws Exception {
        return itemKillMapper.selectAll();
    }

    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        ItemKill itemKill=itemKillMapper.selectById(id);
        if(itemKill==null){
            throw new Exception("获取秒杀商品详情数据不存在");
        }
        return itemKill;
    }
}
