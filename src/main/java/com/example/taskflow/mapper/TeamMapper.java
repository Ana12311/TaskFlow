package com.example.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskflow.entity.Team;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队 Mapper。
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
}
