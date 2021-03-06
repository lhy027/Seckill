package com.debug.kill.server.controller;

import com.debug.kill.api.enums.StatusCode;
import com.debug.kill.api.response.BaseResponse;
import com.debug.kill.model.dto.KillSuccessUserInfo;
import com.debug.kill.model.mapper.ItemKillSuccessMapper;
import com.debug.kill.server.dto.KillDto;
import com.debug.kill.server.service.KillService;
import jodd.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @Author:lhy
 * @Date: 2020/1/6 15:01
 **/
@Controller
public class KillController {
    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    private static final String prefix = "kill";

    @Autowired
    private KillService killService;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;


    /**
     * 订单详情
     * http://localhost:8092/kill/kill/record/detail/412372274822524928
     * @return
     */
    @RequestMapping(value =prefix+"/record/detail/{orderNo}" ,method = RequestMethod.GET)
    public String killRecodeDetail(@PathVariable String orderNo, ModelMap modelMap){
        if (StringUtil.isBlank(orderNo)){
            return "error";
        }
        KillSuccessUserInfo info=itemKillSuccessMapper.selectByCode(orderNo);
        if (info==null){
            return "error";
        }
        modelMap.put("info",info);
        return "killRecord";
    }


    /***
     * 商品秒杀核心业务逻辑
     * @param dto
     * @param result
     * @return
     */
    @RequestMapping(value = prefix+"/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session){
        if (result.hasErrors() || dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Object uId=session.getAttribute("uid");
        if (uId==null){
            return new BaseResponse(StatusCode.UserNotLogin);
        }
//        Integer userId=dto.getUserId();
//        userId=10;
       Integer userId= (Integer)uId ;

        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            //Boolean res=killService.killItem(dto.getKillId(),userId);
            //Boolean res=killService.killItem(dto.getKillId(),userId);

                        //ToDo：基于redisson分布式锁操作
            Boolean res=killService.killItemV4(dto.getKillId(),userId);
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"基于redisson分布式锁操作---哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;

    }



    /***
     * 商品秒杀核心业务逻辑----压力测试
     * @param dto
     * @param result
     * @return
     */
    @RequestMapping(value = prefix+"/execute/lock",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeLock(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session){
        if (result.hasErrors() || dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        //Object uId=session.getAttribute("uid");
//        if (uId==null){
//            return new BaseResponse(StatusCode.UserNotLogin);
//        }


        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {

//            //ToDo：不加分布式锁操作
//            Boolean res=killService.killItemV2(dto.getKillId(),dto.getUserId());
//            if (!res){
//                return new BaseResponse(StatusCode.Fail.getCode(),"不加分布式锁---哈哈~商品已抢购完毕或者不在抢购时间段哦!");
//            }
//            //ToDo：基于redis分布式锁操作（会存在死锁问题）
//            Boolean res=killService.killItemV3(dto.getKillId(),dto.getUserId());
//            if (!res){
//                return new BaseResponse(StatusCode.Fail.getCode(),"基于redis分布式锁操作---哈哈~商品已抢购完毕或者不在抢购时间段哦!");
//            }
//            //ToDo：基于redisson分布式锁操作
            Boolean res=killService.killItemV4(dto.getKillId(),dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"基于redisson分布式锁操作---哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
//            //ToDo：基于zookeeper分布式锁操作
//            Boolean res=killService.killItemV5(dto.getKillId(),dto.getUserId());
//            if (!res){
//                return new BaseResponse(StatusCode.Fail.getCode(),"基于zookeeper分布式锁操作---哈哈~商品已抢购完毕或者不在抢购时间段哦!");
//            }
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;

    }


    //抢购成功跳转页面
    @RequestMapping(value = prefix+"/execute/success",method = RequestMethod.GET)
    public String executeSuccess(){
        return "executeSuccess";
    }

    //抢购失败跳转页面
    @RequestMapping(value = prefix+"/execute/fail",method = RequestMethod.GET)
    public String executeFail(){
        return "executeFail";
    }
}
