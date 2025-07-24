package com.shop.service.module.service;

import com.shop.service.module.entity.AddressEntity;

public interface AddressService extends BaseService {
    void setDefaultAddress(AddressEntity addressEntity);
}
