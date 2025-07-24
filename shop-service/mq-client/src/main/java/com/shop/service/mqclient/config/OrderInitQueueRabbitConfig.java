package com.shop.service.mqclient.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderInitQueueRabbitConfig {

    @Bean
    public Queue OrderInitQueue(){
        return new Queue("OrderInitQueue",true);
    }

    @Bean
    public DirectExchange OrderInitExchange(){
        return new DirectExchange("OrderInitExchange",true,false);
    }

    @Bean
    Binding OrderInitQueueBinding(){
        return new Binding("OrderInitQueue", Binding.DestinationType.QUEUE,"OrderInitExchange","OrderInitRouter",null);
    }


}
