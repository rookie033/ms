package com.shop.service.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.AddressEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.mapper.AddressMapper;
import com.shop.service.module.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/address")
@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/list/page")
    public Result getListForPage(
            @RequestParam(value = "pno",defaultValue = "1") int pno,
            @RequestParam(value = "psize",defaultValue = "10") int psize,
            HttpServletRequest request
    ){
        String token = request.getHeader("Authorization");
        if(token!=null){
            Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
            JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
            JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
            Page page = new Page(pno,psize);
            QueryWrapper q = new QueryWrapper();
            q.eq("user_id",userInfo.get("id").toString());
            q.orderByDesc("is_default");
            return addressService.getListForPage(page,q, AddressMapper.class);
        }else{
            return Result.end(500,"","用户未登录");
        }
    }
    @GetMapping("/find/id/{id}")
    public Result findById(@PathVariable("id") Long id){
        return addressService.findById(id,"com.shop.service.module.mapper.AddressMapper.selectById");
    }

    @PutMapping("insert")
    public  Result insert (@RequestBody AddressEntity addressEntity,HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(token!=null){
            Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
            JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
            JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
            addressEntity.setUserId(Long.valueOf(userInfo.get("id").toString()));
            if(addressEntity.getIsDefault().equals(1)){
                addressService.setDefaultAddress(addressEntity);
            }
            return addressService.insertOne(addressEntity,"com.shop.service.module.mapper.AddressMapper.insert");
        }else{
            return Result.end(500,"","用户未登录");
        }
    }

    @PutMapping("/update")
    public Result update(@RequestBody AddressEntity addressEntity){
        if(addressEntity.getIsDefault().equals(1)){
            addressService.setDefaultAddress(addressEntity);
        }
        return addressService.updateByMapper(addressEntity,AddressMapper.class);
    }

    @DeleteMapping("/delete/id/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        return addressService.deleteById(id,"com.shop.service.module.mapper.AddressMapper.deleteById");
    }
}
