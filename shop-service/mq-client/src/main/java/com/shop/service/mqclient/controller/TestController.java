package com.shop.service.mqclient.controller;

import com.alibaba.fastjson.JSONObject;
import com.shop.service.module.entity.Test;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("/hello")
    public String hello(){
        Test t = new Test();
        t.setId("1");
        System.out.println(JSONObject.toJSONString(t));
        return "hello";
    }
    @RequestMapping("/send")
    public String testSender(@RequestParam(value = "data",defaultValue = "")String data){
        JSONObject j = new JSONObject();
        j.put("name","123");
        j.put("age","18");
        j.put("data",data);
        rabbitTemplate.convertAndSend("TeamDirectExchange"
                ,"TeamDirectRouting",j);


        return j.toJSONString();
    }

    @RequestMapping("/send/await")
    public String testSender1(@RequestParam(value = "data",defaultValue = "")String data){
        JSONObject j = new JSONObject();
        j.put("name","123");
        j.put("age","18");
        j.put("data",data);
        rabbitTemplate.convertAndSend("teamTimeOutExchange"
                ,"teamTimeOutRouter",j,message -> {
//                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setExpiration(1 * 1000 * 10 + "");
                    return message;
                });


        return j.toJSONString();
    }
}
