package com.shop.service.module.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.service.module.entity.GoodsEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.mapper.GoodsMapper;
import com.shop.service.module.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoodsServiceImpl extends BaseServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional
    public Result getGoodsListForPage(int pno, int psize, String name, Long goodsTypeId, Integer isOnSale) {
        Page<GoodsEntity> p = new Page<>(pno,psize);
        Page<GoodsEntity> page = goodsMapper.getGoodsListForPage(p, name, goodsTypeId, isOnSale);
        JSONObject data = this.setPageResult(page.getRecords(), page);
        return Result.end(200,data,"查询成功");
    }

    @Override
    public Result setOnSale(Long id, Integer isOnSale) {
        GoodsEntity goods = goodsMapper.selectById(id);
        goods.setIsOnSale(isOnSale);
        goodsMapper.updateById(goods);
        return Result.end(200,"","设置成功");
    }

}
