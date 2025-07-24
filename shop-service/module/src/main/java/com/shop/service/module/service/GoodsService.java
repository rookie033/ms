package com.shop.service.module.service;

import com.shop.service.module.entity.Result;

public interface GoodsService extends BaseService {
    Result getGoodsListForPage(int pno, int psize, String name, Long goodsTypeId, Integer isOnSale);

    Result setOnSale(Long id, Integer isOnSale);

}
