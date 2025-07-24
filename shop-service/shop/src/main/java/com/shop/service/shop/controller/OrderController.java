package com.shop.service.shop.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.BuyCarEntity;
import com.shop.service.module.entity.OrderEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.entity.UserEntity;
import com.shop.service.module.mapper.BuyCarMapper;
import com.shop.service.module.mapper.OrderMapper;
import com.shop.service.module.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list/page/user")
    @Cacheable(value="userOrderList")
    public Result getListForPageUser(
            @RequestParam(value = "pno",defaultValue = "1",required = false) int pno,
            @RequestParam(value = "psize",defaultValue = "10",required = false) int psize,
            @RequestParam(value = "orderNo",defaultValue = "",required = false) String orderNo,
            @RequestParam(value = "status",required = false) Integer status,
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        if(token!=null){
            Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
            JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
            JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
            Page p = new Page(pno,psize);
            QueryWrapper q = new QueryWrapper();
            q.eq("user_id",userInfo.get("id").toString());
            if(orderNo.trim().length()>0){
                q.like("order_no",orderNo);
            }
            if(status!=null){
                q.eq("status",status);
            }
            q.orderByDesc("insert_time");
            return   orderService.getListForPage(p,q,OrderMapper.class);
        }else{
            return Result.end(500,"","用户未登录");
        }
    }

    @GetMapping("/list/page")
    @Cacheable(value="userOrderList")
    public Result getListForPage(
            @RequestParam(value = "pno",defaultValue = "1",required = false) int pno,
            @RequestParam(value = "psize",defaultValue = "10",required = false) int psize,
            @RequestParam(value = "orderNo",defaultValue = "",required = false) String orderNo,
            @RequestParam(value = "status",required = false) Integer status,
            @RequestParam(value = "phone",defaultValue = "",required = false) String phone,
             @RequestParam(value = "type",required = false) Integer type
    ){

        Page p = new Page(pno,psize);
        QueryWrapper q = new QueryWrapper();
        if(orderNo.trim().length()>0){
            q.like("order_no",orderNo);
        }
        if(status!=null){
            q.eq("status",status);
        }
        if(phone.trim().length()>0){
            q.like("phone",phone);
        }
        if(type != null){
            q.eq("type",type);
        }
        q.orderByDesc("insert_time");
        return   orderService.getListForPage(p,q,OrderMapper.class);

    }

    @GetMapping("/send")
    @CacheEvict(value = {"orderList","userOrderList"},allEntries = true)
    public Result send(@RequestParam("id") Long id,@RequestParam("postCode") String postCode){
        return orderService.send(id,postCode);
    }

    @GetMapping("/get/goods")
    @CacheEvict(value = {"orderList","userOrderList"},allEntries = true)
    public Result getGoods(@RequestParam("id") Long id){
        return orderService.getGoods(id);
    }

    @GetMapping("/find/id/{id}")
    public Result findById(@PathVariable("id") Long id){
        return   orderService.findById(id,"com.shop.service.module.mapper.OrderMapper.selectById");
    }

    @GetMapping("/find/orderNo/{orderNo}")
    public Result findByOrderNo(@PathVariable("orderNo") String orderNo){

        QueryWrapper q = new QueryWrapper();
        q.eq("order_no",orderNo);
        return  orderService.selectOne(q,OrderMapper.class);
    }

    @PostMapping("/fallback/goods")
    @CacheEvict(value = {"orderList","userOrderList"},allEntries = true)
    public Result fallbackGoods(@RequestBody JSONObject order){
        OrderEntity o = JSONObject.parseObject(order.toJSONString(), OrderEntity.class);
        return orderService.fallbackGoods(o);
    }

    @GetMapping("/fallback/goods/over")
    @CacheEvict(value = {"orderList","userOrderList"},allEntries = true)
    public Result fallbackGoodsOver(@RequestParam("id") Long id){
        return orderService.fallbackGoodsOver(id);
    }



    @PutMapping("/insert")
    @CacheEvict(value = {"orderList","userOrderList","goodsList"},allEntries = true)
    public  Result insert (@RequestBody OrderEntity orderEntity, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(token!=null){
            Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
            JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
            JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
            orderEntity.setUserId(Long.valueOf(userInfo.get("id").toString()));
            orderEntity.setStatus(0);
            orderEntity.setInsertTime(new Date());
            return   orderService.insertOrder(orderEntity,userInfo);
        }else{
            return Result.end(500,"","用户未登录");
        }
    }

    @PutMapping("/update")
    @CacheEvict(value = {"orderList","userOrderList"},allEntries = true)
    public Result update(@RequestBody OrderEntity orderEntity){
        return   orderService.updateByMapper(orderEntity, OrderMapper.class);
    }

    @DeleteMapping("/delete/id/{id}")
    @CacheEvict(value = {"orderList","userOrderList"},allEntries = true)
    public Result deleteById(@PathVariable("id") Long id){
        return   orderService.deleteById(id,"com.shop.service.module.mapper.OrderMapper.deleteById");
    }

    @PutMapping("/insert/buy-car")
    @CacheEvict(value = {"orderList","userOrderList","buyCar","goodsList"},allEntries = true)
    public Result insertFromBuyCar(@RequestBody JSONObject params,
                                   HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(token!=null){
            Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
            JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
            JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
            return orderService.insertFromBuyCar(params,userInfo);
        }else{
            return Result.end(500,"","用户未登录");
        }
    }

    @PutMapping("/update/team/id/{id}/password/{password}")
    @CacheEvict(value = {"orderList","userOrderList","buyCar","goodsList"},allEntries = true)
    public Result updateFromTeam(
            @PathVariable("id")String id,
            @PathVariable("password")String password,HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
        JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
        JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
        UserEntity user = JSONObject.parseObject(userInfo.toJSONString(),UserEntity.class);
        if(new BCryptPasswordEncoder().matches(password,userInfo.get("password").toString())) {
            return orderService.updateFromTeam(id,user);
        }else{
            return Result.end(500,"","密码输入错误");
        }
    }

    @DeleteMapping("/delete-order/id/{id}")
    @CacheEvict(value = {"orderList","userOrderList","buyCar","goodsList"},allEntries = true)
    public Result removeOrder(
            @PathVariable("id")String id,
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
        JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
        JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
        UserEntity user = JSONObject.parseObject(userInfo.toJSONString(),UserEntity.class);
        return orderService.removeOrder(id,user);
    }
}
