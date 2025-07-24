package com.shop.service.mqclient.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class OrderTimeoutRabbitConfig {


    @Bean
    public Queue OrderTimeoutQueue(){
        Map<String, Object> arguments = new HashMap<>(2);
        // 绑定该队列到私信交换机
        arguments.put("x-dead-letter-exchange", "OrderTimeoutDeadExchange");
        arguments.put("x-dead-letter-routing-key", "OrderTimeoutDeadRouter");
        return new Queue("OrderTimeoutQueue",true,false,false,arguments);
    }

    @Bean
    public Queue OrderTimeoutDeadQueue(){
        return new Queue("OrderTimeoutDeadQueue",true);
    }

    @Bean
    DirectExchange OrderTimeoutDeadExchange(){
        return new DirectExchange("OrderTimeoutDeadExchange",true,false,null);
    }
    @Bean
    DirectExchange OrderTimeoutExchange(){
        return new DirectExchange("OrderTimeoutExchange",true,false,null);
    }

    @Bean
    Binding OrderTimeoutBinding(){
        return new Binding("OrderTimeoutQueue",
                Binding.DestinationType.QUEUE,
                "OrderTimeoutExchange",
                "OrderTimeoutRouter"
                ,null);
    }

    @Bean
    Binding OrderTimeoutDeadBinding(){
        return new Binding("OrderTimeoutDeadQueue",
                Binding.DestinationType.QUEUE,
                "OrderTimeoutDeadExchange",
                "OrderTimeoutDeadRouter"
                ,null);
    }
}
