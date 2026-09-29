package com.example.taskflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskflow.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 团队成员 Mapper。
 */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}
