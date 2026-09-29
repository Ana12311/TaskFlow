package com.example.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskflow.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。继承 BaseMapper<User> 后自带单表 CRUD（selectById、insert、selectOne 等），
 * 简单查询无需写 SQL；复杂查询再在 XML 或注解里写。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
