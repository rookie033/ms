package com.shop.service.shop.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.Result;
import com.shop.service.module.entity.TeamEntity;
import com.shop.service.module.mapper.OrderMapper;
import com.shop.service.module.mapper.TeamMapper;
import com.shop.service.module.service.TeamService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RequestMapping("/team")
@RestController
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/list/page")
    public Result getListForPage(
            @RequestParam(value = "pno",defaultValue = "1") int pno,
            @RequestParam(value = "psize",defaultValue = "10") int psize,
            @RequestParam(value = "name",defaultValue = "",required = false) String name,
            @RequestParam(value = "isOnSale",required = false) Integer isOnSale,
            @RequestParam(value = "type",required = false) Integer type
    ){
        return teamService.getTeamListForPage(pno,psize,name,isOnSale,type);
    }


    @GetMapping("/list/alive")
    public Result getListAlive(){
        QueryWrapper q = new QueryWrapper();
        q.eq("status",0);
        q.apply("UNIX_TIMESTAMP(current_time) < UNIX_TIMESTAMP(end_time)");
        return teamService.getListAll(q,TeamMapper.class);
    }

    @GetMapping("/list/page/me")
    public Result getListForPage(
            @RequestParam(value = "pno",defaultValue = "1") int pno,
            @RequestParam(value = "psize",defaultValue = "10") int psize,
            @RequestParam(value = "type",required = false) Integer type,
            HttpServletRequest request

    ){
        String token = request.getHeader("Authorization");
        Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
        JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
        JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
        return teamService.getMyTeamListForPage(pno,psize,Long.valueOf(userInfo.get("id").toString()),type);
    }

    @PutMapping("/insert")
    public Result insert(@RequestBody TeamEntity teamEntity){
        teamEntity.setIsOnSale(0);
        teamEntity.setInsertTime(new Date());
        teamEntity.setHasMember(0);
        teamEntity.setStatus(0);

        Date date = teamEntity.getEndTime();

        Result res = teamService.insertOne(teamEntity, "com.shop.service.module.mapper.TeamMapper.insert");
        //设置延时队列处理超时拼团活动
        rabbitTemplate.convertAndSend("teamTimeOutExchange"
                ,"teamTimeOutRouter"
                , JSONObject.toJSON(teamEntity),message -> {
                    message.getMessageProperties().setExpiration((System.currentTimeMillis()- date.getTime())+"");
//                    message.getMessageProperties().setExpiration("1000");
                    return message;
                });
        return res;
    }

    @GetMapping("/find/id/{id}")
    public Result findById(@PathVariable("id") Long id){
        return teamService.findById(id,"com.shop.service.module.mapper.TeamMapper.selectById");
    }


    @PutMapping("/update")
    public Result update(@RequestBody TeamEntity teamEntity){
        return teamService.updateByMapper(teamEntity, TeamMapper.class);
    }

    @DeleteMapping("/delete/id/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        return teamService.deleteById(id,"com.shop.service.module.mapper.TeamMapper.deleteById");
    }

    @GetMapping("/set/onsale")
    public Result setOnSale(
            @RequestParam("id") Long id,
            @RequestParam("isOnSale") Integer isOnSale
    ){
        return teamService.setOnSale(id,isOnSale);
    }

    @PostMapping("/insert/team")
    public Result insertTeam(
            @RequestParam(value = "id") Long id,
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
        JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
        JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());

        return   teamService.insertTeam(id,Long.valueOf(userInfo.get("id").toString()));

    }
    @GetMapping("/get/result")
    public Result getTeamResult(@RequestParam(value = "id") Long id,
                                HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
        JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
        JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
        return teamService.getResult(id,Long.valueOf(userInfo.get("id").toString()));
    }

    @GetMapping("/list/user/team_id/{id}")
    public Result getTeamUserList(@PathVariable("id") Long id){
        return teamService.getTeamUserList(id);
    }
}
