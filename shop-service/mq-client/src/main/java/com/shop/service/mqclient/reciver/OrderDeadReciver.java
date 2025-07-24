package com.shop.service.mqclient.reciver;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.shop.service.module.entity.OrderEntity;
import com.shop.service.module.entity.TeamEntity;
import com.shop.service.module.mapper.GoodsMapper;
import com.shop.service.module.mapper.OrderMapper;
import com.shop.service.module.mapper.TeamMapper;
import com.shop.service.module.mapper.TeamUserMapper;
import com.shop.service.module.util.RedisLockHelper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;

@RabbitListener(queues = "OrderTimeoutDeadQueue")
@Component
public class OrderDeadReciver {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private TeamUserMapper teamUserMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RedisLockHelper redisLockHelper;

    @RabbitHandler
    @Transactional
    public void process(JSONObject j, Message message, Channel channel){
        System.out.println("订单死信队列");
        System.out.println(j);
        Object o = j.get("orderId");
        String orderId = "";
        Object orderNo = j.get("order_no");
        System.out.println(orderNo);


        try {
            if(o!=null){
                orderId = o.toString();
                String id = j.get("team_id").toString();
                OrderEntity order = orderMapper.selectById(orderId);
                if(order!= null){
                    String prefix = "team_activity_";
                    if(order.getStatus() == 0){
                        boolean lock = false;
                        System.out.println("死信队列抢锁");
                        while (lock == false){
                            lock = redisLockHelper.lock("remove_team_user");
                            Thread.sleep(100);
                        }
                        System.out.println("死信队列抢锁成功");
                        orderMapper.deleteById(orderId);
                        String t1 = (String)redisTemplate.opsForValue().get(prefix+id);
                        TeamEntity team = JSONObject.parseObject(t1, TeamEntity.class);
                        team.setStatus(0);
                        team.setHasMember(team.getHasMember()-1);
                        teamMapper.updateById(team);
                        redisTemplate.opsForValue().getAndSet(prefix+id,JSONObject.toJSONString(team));
                        QueryWrapper q = new QueryWrapper();
                        q.eq("user_id",order.getUserId());
                        q.eq("team_id",order.getTeamId());
                        teamUserMapper.delete(q);
                        redisLockHelper.delete("remove_team_user");
                    }
                }

            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
}
