package com.shop.service.mqclient.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class TeamDeadQueueRabbitConfig {


    @Bean
    public Queue teamTimeOutQueue() {
        Map<String, Object> arguments = new HashMap<>(2);
        // 绑定该队列到私信交换机
        arguments.put("x-dead-letter-exchange", "teamTimeOutDlxExchange");
        arguments.put("x-dead-letter-routing-key", "teamTimeOutDlxRouter");

        return new Queue("teamTimeOutQueue", true, false, false, arguments);
    }

    @Bean
    public Queue teamTimeOutDeadQueue() {
        return new Queue("teamTimeOutDeadQueue", true);
    }


    @Bean
    public DirectExchange teamTimeOutDlxExchange() {
        return new DirectExchange("teamTimeOutDlxExchange",true,false);
    }

    /**
     * 死信路由通过 DL_KEY 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding deadLetterBinding() {
        return new Binding("teamTimeOutDeadQueue", Binding.DestinationType.QUEUE, "teamTimeOutDlxExchange", "teamTimeOutDlxRouter", null);

    }

    @Bean
    public DirectExchange teamTimeOutExchange() {
        return new DirectExchange("teamTimeOutExchange",true,false);
    }
    /**
     * 死信路由通过 KEY_R 绑定键绑定到死信队列上.
     *
     * @return the binding
     */
    @Bean
    public Binding redirectBinding() {
        return new Binding("teamTimeOutQueue", Binding.DestinationType.QUEUE, "teamTimeOutExchange", "teamTimeOutRouter", null);
    }
}
