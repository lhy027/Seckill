package com.debug.kill.model.dto;

import com.debug.kill.model.entity.ItemKillSuccess;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author:lhy
 * @Date: 2020/1/6 14:50
 **/
@Data
public class KillSuccessUserInfo extends ItemKillSuccess
        implements Serializable {
    private String userName;

    private String phone;

    private String email;

    private String itemName;

    @Override
    public String toString() {
        return super.toString()+"\nKillSuccessUserInfo{" +
                "userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}

