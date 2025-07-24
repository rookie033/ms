package com.shop.service.module.feign;

import com.alibaba.fastjson.JSONObject;
import com.shop.service.module.entity.LogEntity;
import com.shop.service.module.feign.fallback.LogFeignServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "log-service",fallback = LogFeignServiceImpl.class)
public interface LogFeignService {

    @PostMapping("/insert")
    public JSONObject insert(@RequestBody LogEntity logEntity);
}
