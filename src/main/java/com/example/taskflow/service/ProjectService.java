package com.example.taskflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.taskflow.dto.ProjectCreateRequest;
import com.example.taskflow.dto.ProjectMemberAddRequest;
import com.example.taskflow.entity.Project;
import com.example.taskflow.vo.ProjectDetailVO;
import com.example.taskflow.vo.ProjectVO;

import java.util.List;

/**
 * 项目业务接口。项目 + 项目成员管理，含权限判断。
 */
public interface ProjectService extends IService<Project> {

    /** 在指定团队下创建项目，创建者自动成为项目 OWNER */
    ProjectVO createProject(Long teamId, ProjectCreateRequest request);

    /** 查询团队下的项目列表，需是团队成员 */
    List<ProjectVO> listTeamProjects(Long teamId);

    /** 查询项目详情（含成员列表），仅项目成员可见 */
    ProjectDetailVO getProjectDetail(Long projectId);

    /** 添加项目成员，需项目创建者或团队 OWNER/ADMIN */
    void addMember(Long projectId, ProjectMemberAddRequest request);

    /** 移除项目成员，需项目创建者或团队 OWNER/ADMIN */
    void removeMember(Long projectId, Long userId);

    /** 删除项目（级联删成员、任务、评论、日志、通知），仅项目创建者 */
    void deleteProject(Long projectId);
}
