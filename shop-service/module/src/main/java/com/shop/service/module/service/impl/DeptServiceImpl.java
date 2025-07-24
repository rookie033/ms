package com.shop.service.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shop.service.module.entity.DeptEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.entity.UserEntity;
import com.shop.service.module.mapper.DeptMapper;
import com.shop.service.module.mapper.UserMapper;
import com.shop.service.module.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Component
public class DeptServiceImpl extends BaseServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result selectDeptUser() {
        List<DeptEntity> list = deptMapper.selectList(null);
        list.stream().forEach(item -> {
            QueryWrapper<UserEntity> q = new QueryWrapper<>();
            q.eq("dept_id",item.getId());
            q.eq("type",0);
            List<UserEntity> listUser = userMapper.selectList(q);
            item.setChildren(listUser);
        });
        return Result.end(200,list,"查询成功");
    }

    @Override
    public Result addDeptUser(Long[] ids, Long id) {
        if(ids!=null&&ids.length>0){
            Arrays.stream(ids).forEach(item -> {
                UserEntity user = userMapper.selectById(item);
                user.setDeptId(Integer.valueOf(id.toString()));
                userMapper.updateById(user);
            });
        }
        return Result.end(200,"","新增成功");
    }

    @Override
    public Result deleteDeptUser(Long id) {
        System.out.println(id);
        UserEntity user = userMapper.selectById(id);
        System.out.println(user);
        user.setDeptId(-1);
        userMapper.updateById(user);
        return Result.end(200,"","删除成功");
    }


}
