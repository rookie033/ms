package com.shop.service.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netflix.ribbon.proxy.annotation.Http;
import com.shop.service.module.entity.Result;
import com.shop.service.module.entity.UserEntity;
import com.shop.service.module.mapper.UserMapper;
import com.shop.service.module.service.UserService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

@RequestMapping("/shop")
@RestController
@Validated
public class ShopUserController {


    @Autowired
    private UserService userService;

    @PostMapping("/login/password")
    public Result shopLoginPassword(@Validated @RequestBody(required = true) UserEntity userEntity, HttpSession session){
        return userService.userLogin(userEntity.getUsername(),userEntity.getPassword(),session);
    }
    @PostMapping("/code")
    public Result getCode(HttpSession session){
        return userService.getCode(session);
    }
    @Valid
    @GetMapping("/login/code")
    public Result loginByCode(
            @NotBlank(message = "验证码不可以为空")
            @RequestParam("code")
            String code,
            @NotBlank(message = "账号不可以为空")
            @RequestParam("username")
            String username,
                              HttpSession session){
        return userService.userLoginByCode(username,code,session);
    }

    @PostMapping("/register")
    public Result getRegisterUser(@RequestBody(required = false) UserEntity user){
        return userService.register(user);
    }


    @PutMapping("/update")
    public Result updateUser( @RequestBody UserEntity user) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        return userService.updateByMapper(user, UserMapper.class);
    }

    @PutMapping("/insert")
    public Result insert(@RequestBody UserEntity user){
        QueryWrapper q = new QueryWrapper();
        q.eq("username",user.getUsername());
        Result res = userService.selectOne(q, UserMapper.class);
        if(res.getData()!=null){
            System.out.println(res.getData());
            return Result.end(500,"","账号已注册");
        }
        user.setInsertTime(new Date());
        user.setType(1);
        user.setFreeze(0);
        BCryptPasswordEncoder b = new BCryptPasswordEncoder();
        user.setPassword(b.encode(user.getPassword()));
        return userService.insertOne(user,"com.shop.service.module.mapper.UserMapper.insert");
    }

    @GetMapping("/set/freeze")
    public Result setFreeze(@RequestParam("id") Long id,@RequestParam("freeze") Integer freeze){
        return userService.setFreeze(id,freeze);
    }

    @GetMapping("/find/id/{id}")
    public Result findById(@PathVariable("id") Long id){
        return userService.findById(id,"com.shop.service.module.mapper.UserMapper.selectById");
    }

    @GetMapping("/delete/id/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        return userService.deleteById(id,"com.shop.service.module.mapper.UserMapper.deleteById");
    }


    @GetMapping("/list/page")
    public Result getShopUserListForPage(
            @RequestParam(value = "pno",defaultValue = "1") int pno,
            @RequestParam(value = "psize",defaultValue = "10") int psize,
            @RequestParam(value = "beginTime",defaultValue = "") String beginTime,
            @RequestParam(value = "endTime",defaultValue = "") String endTime,
            @RequestParam(value = "phone",defaultValue = "") String phone,
            @RequestParam(value = "username",defaultValue = "") String username,
            @RequestParam(value = "freeze") Integer freeze
    ){
        Page p = new Page(pno,psize);
        QueryWrapper q = new QueryWrapper();

        if(phone.trim().length()>0){
            q.like("phone",phone);
        }
        if(username.trim().length()>0){
            q.like("username",username);
        }
        if(beginTime.trim().length()>0){
            beginTime = beginTime + " 00:00:00";
            q.apply("unix_timestamp(insert_time) >= unix_timestamp('"+beginTime+"')");
        }
        if(endTime.trim().length()>0){
            endTime = endTime + " 23:59:59";
            q.apply("unix_timestamp(insert_time) <= unix_timestamp('"+endTime+"')");
        }
        if(freeze!=null){
            q.eq("freeze",freeze);
        }
        q.eq("type",1);
        return userService.getListForPage(p,q,UserMapper.class);
    }

}
