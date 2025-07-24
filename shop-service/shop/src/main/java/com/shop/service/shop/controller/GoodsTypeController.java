package com.shop.service.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.GoodsTypeEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.mapper.GoodsTypeMapper;
import com.shop.service.module.service.GoodsTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/goods-type")
@RestController
public class GoodsTypeController {
    @Autowired
    private GoodsTypeService goodsTypeService;

    @GetMapping("/list/page")
    public Result getListForPage(
            @RequestParam(value = "pno",defaultValue = "1") int pno,
            @RequestParam(value = "psize",defaultValue = "10") int psize,
            @RequestParam(value = "name",defaultValue = "")String name


    ){
        Page p= new Page(pno,psize);
        QueryWrapper q = new QueryWrapper();
        q.like("name",name);
        return goodsTypeService.getListForPage(p,q, GoodsTypeMapper.class);
    }

    @GetMapping("/list/all")
    public Result getListAll(){
        return goodsTypeService.getListAll(null,GoodsTypeMapper.class);
    }
    @GetMapping("/find/id/{id}")
    public Result findById(@PathVariable("id") Long id){
        return goodsTypeService.findById(id,"com.shop.service.module.mapper.GoodsTypeMapper.selectById");
    }

    @PutMapping("/insert")
    public Result insert(@RequestBody GoodsTypeEntity goodsTypeEntity){
        return goodsTypeService.insertOne(goodsTypeEntity,"com.shop.service.module.mapper.GoodsTypeMapper.insert");
    }
    @PutMapping("/update")
    public Result update(@RequestBody GoodsTypeEntity goodsTypeEntity){
        return goodsTypeService.updateByMapper(goodsTypeEntity,GoodsTypeMapper.class);
    }

    @DeleteMapping("/delete/id/{id}")
    public Result deleteById(@PathVariable("id") Long id){
        return goodsTypeService.deleteById(id,"com.shop.service.module.mapper.GoodsTypeMapper.deleteById");
    }
}
