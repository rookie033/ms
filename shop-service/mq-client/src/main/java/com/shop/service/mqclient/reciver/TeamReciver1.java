package com.shop.service.mqclient.reciver;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RabbitListener(queues = "TeamDirectQueue")
@Component
public class TeamReciver1 {

    @RabbitHandler
    public void process(JSONObject j){
        System.out.println("recvicer1");
        System.out.println(j);
    }
}
