package com.shop.service.module.feign.fallback;

import com.alibaba.fastjson.JSONObject;
import com.shop.service.module.entity.LogEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.mapper.LogMapper;
import com.shop.service.module.feign.LogFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogFeignServiceImpl implements LogFeignService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public JSONObject insert(LogEntity logEntity) {
        System.err.println("日志服务请求失败，启动本地插入日志");
        logMapper.insert(logEntity);
        System.err.println("本地日志插入完毕");
        return JSONObject.parseObject(JSONObject.toJSONString(Result.end(500,"","日志异常")));
    }
}
