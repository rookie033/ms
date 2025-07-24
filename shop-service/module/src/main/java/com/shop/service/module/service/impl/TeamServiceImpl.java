package com.shop.service.module.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.Result;
import com.shop.service.module.entity.TeamEntity;
import com.shop.service.module.entity.TeamUserEntity;
import com.shop.service.module.mapper.TeamMapper;
import com.shop.service.module.mapper.TeamUserMapper;
import com.shop.service.module.service.TeamService;
import com.shop.service.module.util.RedisLockHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class TeamServiceImpl extends BaseServiceImpl implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private RedisLockHelper redisLockHelper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TeamUserMapper teamUserMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Result getTeamListForPage(int pno, int psize, String name, Integer isOnSale, Integer type) {
        Page<TeamEntity> p = new Page<>(pno,psize);
        Page<TeamEntity> page = teamMapper.getTeamListForPage(p,name,isOnSale,type);
        JSONObject data = this.setPageResult(page.getRecords(), page);

        return Result.end(200,data,"查询成功");
    }

    @Override
    public Result setOnSale(Long id, Integer isOnSale) {
        TeamEntity team = teamMapper.selectById(id);
        team.setIsOnSale(isOnSale);
        teamMapper.updateById(team);
        return Result.end(200,"","设置成功");
    }

    @Override
    public Result insertTeam(Long id, Long userId) {
        //            抢锁
        int count = 5;
        boolean lock = false;
        System.out.println("用户"+userId+"开始抢锁");
        while (count>0){
            try {
                lock = redisLockHelper.lock("team_lock");
                if (lock){
                    break;
                }
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count-- ;
        }
//            五次抢锁失败返回错误信息
        if(!lock){
            System.out.println("用户"+userId+"抢锁失败");
            return Result.end(500,"","当前抢购人数太多请重试");
        }
        System.out.println("用户"+userId+"抢锁成功");
//        查询当前用户是否参加活动
        System.out.println("用户"+userId+"抢锁成功");
        QueryWrapper q = new QueryWrapper();
        q.eq("team_id",id);
        q.eq("user_id",userId);

        TeamUserEntity teamUser = teamUserMapper.selectOne(q);
        System.out.println("查询用户是否参与过活动");
//        当查询不到记录证明用户可以参与拼团
//        设置抢锁重试次数
        String prefix = "team_activity_";

        if(teamUser== null){

            System.out.println("用户没参与过活动");
//            抢锁成功判断内存中是否有活动信息如果没有就创建一个
            System.out.println("查询内存中的活动是否存在");
            String t = (String) redisTemplate.opsForValue().get(prefix + id);
            TeamEntity teamEntity = null;
            if(t == null){
                teamEntity = teamMapper.selectById(id);
                System.out.println("内存不存在，创建一个");
                redisTemplate.opsForValue().setIfAbsent(prefix+id,JSONObject.toJSONString(teamEntity),
                        Duration.ofMillis(
                                teamEntity.getEndTime().getTime()-System.currentTimeMillis()
                        ));
            }else{
                System.out.println("内存存在，读取内存的活动数据");
                teamEntity = JSONObject.parseObject(t,TeamEntity.class);
            }

//            判断活动状态是否结束
            if(teamEntity.getStatus()!=0||teamEntity.getEndTime().getTime()-System.currentTimeMillis()<0){
                redisLockHelper.delete("team_lock");
                return Result.end(500,"","活动已结束");
            }
//            判断活动是否拼满
            if(teamEntity.getSize()==teamEntity.getHasMember()){
                redisLockHelper.delete("team_lock");
                return Result.end(500,"","活动已拼满");
            }
            System.out.println("预➖库存并设置到内存");
//            如果全部通过则预减库存并通知消息队列处理业务
            teamEntity.setHasMember(teamEntity.getHasMember()+1);
            redisTemplate.opsForValue().getAndSet(prefix+id,JSONObject.toJSONString(teamEntity));
            JSONObject j = new JSONObject();
            j.put("team_id",id);
            j.put("user_id",userId);
            System.out.println("通知消息队列");
            rabbitTemplate.convertAndSend("OrderInitExchange",
                    "OrderInitRouter",
                    j);
            redisLockHelper.delete("team_lock");
            return Result.end(200,"","拼团成功正在生成订单");
        }else{
            redisLockHelper.delete("team_lock");
            return Result.end(500,"","你已经参与过该活动");
        }

    }

    @Override
    public Result getResult(Long id, Long userId) {
        String orderPrefix = "team_order_";
        Integer t = (Integer) redisTemplate.opsForValue().get(orderPrefix+id+userId);
        if(t == null){
            return Result.end(101,"","订单未创建成功");
        }else{
            if(t == -1){
                return Result.end(500,"","订单创建失败，请重新下单");
            }else{
                return Result.end(200,"","订单创建成功,请前往订单查看");
            }
        }
    }

    @Override
    public Result getMyTeamListForPage(int pno, int psize, Long id, Integer type) {
        Page<TeamEntity> p = new Page<>(pno,psize);
        Page<TeamEntity> page = teamMapper.getMyTeamListForPage(p,type,id);
        JSONObject data = this.setPageResult(page.getRecords(), page);
        return Result.end(200,data,"查询成功");
    }

    @Override
    public void makeActivityTimeout(Long id) {
        TeamEntity team = teamMapper.selectById(id);
        if(team.getStatus() == 0){
            team.setStatus(1);
        }

        teamMapper.updateById(team);
    }

    @Override
    public Result getTeamUserList(Long id) {
        List<TeamUserEntity> list = teamUserMapper.getTeamUserList(id);
        JSONObject data  = new JSONObject();
        data.put("list",list);
        return Result.end(200,data,"查询成功");
    }
}
