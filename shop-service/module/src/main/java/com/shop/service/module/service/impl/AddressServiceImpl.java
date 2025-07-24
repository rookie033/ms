package com.shop.service.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.service.module.entity.AddressEntity;
import com.shop.service.module.mapper.AddressMapper;
import com.shop.service.module.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Component
public class AddressServiceImpl extends BaseServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public void setDefaultAddress(AddressEntity addressEntity) {
        QueryWrapper<AddressEntity> q = new QueryWrapper();
        q.eq("user_id",addressEntity.getUserId());
        List<AddressEntity> list = addressMapper.selectList(q);
        list.stream().forEach(item -> {
            item.setIsDefault(0);
            addressMapper.updateById(item);
        });

    }
}
