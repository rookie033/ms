package com.shop.service.mqclient.reciver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.shop.service.module.entity.GoodsEntity;
import com.shop.service.module.entity.OrderEntity;
import com.shop.service.module.entity.TeamEntity;
import com.shop.service.module.entity.TeamUserEntity;
import com.shop.service.module.mapper.GoodsMapper;
import com.shop.service.module.mapper.OrderMapper;
import com.shop.service.module.mapper.TeamMapper;
import com.shop.service.module.mapper.TeamUserMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
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
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@RabbitListener(queues = "OrderInitQueue")
@Component
public class OrderReciver {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @RabbitHandler
    @Transactional
    public void process(JSONObject data, Message message, Channel channel){
        System.out.println("订单队列");
        String prefix = "team_activity_";
        String orderPrefix = "team_order_";
        String id = data.get("team_id").toString();
        String userId = data.get("user_id").toString();
        System.out.println("发送的用户id是："+userId);
        long tag = message.getMessageProperties().getDeliveryTag();
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
        OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
        TeamMapper teamMapper = sqlSession.getMapper(TeamMapper.class);
        TeamUserMapper teamUserMapper = sqlSession.getMapper(TeamUserMapper.class);
        GoodsMapper goodsMapper = sqlSession.getMapper(GoodsMapper.class);
        try {
            String t1 = (String)redisTemplate.opsForValue().get(prefix+id);
            TeamEntity team = JSONObject.parseObject(t1, TeamEntity.class);
            Long goodsId = team.getGoodsId();
            OrderEntity order = new OrderEntity();
            order.setUserId(Long.valueOf(userId));
            order.setGoodsId(goodsId);
            String orderNo = "PTDD-"+System.currentTimeMillis()+ UUID.randomUUID().toString().split("-")[0].toUpperCase(Locale.ROOT);
            order.setOrderNo(orderNo);
            order.setInsertTime(new Date());
            order.setStatus(0);
            order.setTeamId(Long.valueOf(id));
            order.setActivityName(team.getName());
            order.setActivityDiscount(team.getTeamDiscount());
            GoodsEntity goods = goodsMapper.selectById(goodsId);
            order.setGoodsDiscount(goods.getDiscount());
            order.setGoodsName(goods.getName());
            order.setGoodsPrice(goods.getPrice());
            order.setCount(1);
            order.setPay(goods.getPrice()*goods.getDiscount()*team.getTeamDiscount()/100);
            orderMapper.insert(order);
            TeamUserEntity t = new TeamUserEntity();
            t.setTeamId(Long.valueOf(id));
            t.setUserId(Long.valueOf(userId));
            teamUserMapper.insert(t);

            if(team.getSize() == team.getHasMember()){
                team.setStatus(2);
            }
            teamMapper.updateById(team);
            Boolean res = redisTemplate.opsForValue().setIfAbsent(orderPrefix + id + userId, 1, Duration.ofMinutes(15));
            System.out.println(res);
            JSONObject j = new JSONObject();


            channel.basicAck(message.getMessageProperties().getDeliveryTag(),true);
            sqlSession.commit();
            sqlSession.close();

            j.put("user_id",userId);
            j.put("orderId",order.getId());
            j.put("team_id",id);
            j.put("order_no",order.getOrderNo());
            System.out.println(order.getId());
            rabbitTemplate.convertAndSend("OrderTimeoutExchange","OrderTimeoutRouter",j,
                    message1 -> {
                        message1.getMessageProperties().setExpiration((15*60*1000)+"");
                        return message1;
                    });
        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }


        }
    }
}
