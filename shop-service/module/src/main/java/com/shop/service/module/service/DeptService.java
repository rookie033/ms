package com.shop.service.module.service;

import com.shop.service.module.entity.Result;

public interface DeptService extends BaseService {

    Result selectDeptUser();

    Result addDeptUser(Long[] ids, Long id);

    Result deleteDeptUser(Long id);
}
