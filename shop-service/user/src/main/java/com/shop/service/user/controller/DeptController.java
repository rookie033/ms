package com.shop.service.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.DeptEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.mapper.DeptMapper;
import com.shop.service.module.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RequestMapping("/dept")
@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;


    @GetMapping("/list/all")
    public Result getListAll(){
        return deptService.getListAll(null, DeptMapper.class);
    }

    @GetMapping("/list/page")
    public Result getListForPage(
            @RequestParam(value = "pno",defaultValue = "1") int pno,
            @RequestParam(value = "psize",defaultValue = "10") int psize){
        Page p = new Page(pno,psize);
        QueryWrapper q = new QueryWrapper();
            return deptService.getListForPage(p,q,DeptMapper.class);
    }

    @PutMapping("/insert")
    public Result insert(@Validated @RequestBody DeptEntity deptEntity){
        return deptService.insertOne(deptEntity,"com.shop.service.module.mapper.DeptMapper.insert");
    }

    @Valid
    @GetMapping("/find/id/{id}")
    public Result findById(@NotBlank(message = "id不可以为空")
                           @PathVariable("id") Long id){
        return deptService.findById(id,"com.shop.service.module.mapper.DeptMapper.selectById");
    }

    @PutMapping("/update")
    public Result update(@Validated @RequestBody DeptEntity deptEntity){
        return deptService.updateByMapper(deptEntity,DeptMapper.class);
    }

    @DeleteMapping("/delete/id/{id}")
    public Result deleteById(@NotBlank(message = "id不可以为空")
                           @PathVariable("id") Long id){
        return deptService.findById(id,"com.shop.service.module.mapper.DeptMapper.deleteById");
    }

    @GetMapping("/list/user")
    public Result getDeptUserList(){
        return deptService.selectDeptUser();
    }

    @PutMapping("/add/user")
    public Result addDeptUser(@RequestParam("ids")Long[] ids,@RequestParam("id") Long id){
        return deptService.addDeptUser(ids,id);
    }

    @DeleteMapping("/delete/user")
    public Result deleteDeptUser(@RequestParam Long id){
        return deptService.deleteDeptUser(id);
    }
}
