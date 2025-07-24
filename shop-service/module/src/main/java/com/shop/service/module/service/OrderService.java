package com.shop.service.module.service;

import com.alibaba.fastjson.JSONObject;
import com.shop.service.module.entity.OrderEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.entity.UserEntity;

public interface OrderService extends BaseService {
    Result insertOrder(OrderEntity orderEntity, JSONObject userInfo);

    Result insertFromBuyCar(JSONObject params, JSONObject userInfo);

    Result send(Long id, String postCode);

    Result getGoods(Long id);

    Result fallbackGoods(OrderEntity order);

    Result fallbackGoodsOver(Long id);

    Result updateFromTeam(String id, UserEntity id1);

    Result removeOrder(String id, UserEntity user);
}
