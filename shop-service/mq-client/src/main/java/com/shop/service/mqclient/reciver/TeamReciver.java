package com.shop.service.mqclient.reciver;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@RabbitListener(queues = "TeamDirectQueue")
@Component
public class TeamReciver {

    @RabbitHandler
    public void process(JSONObject msg){
        System.out.println("reciver0");
        System.out.println(msg);
    }
}
