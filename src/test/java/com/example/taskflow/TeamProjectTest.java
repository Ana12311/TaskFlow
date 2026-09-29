package com.example.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 团队 + 项目模块集成测试：创建、成员权限隔离。
 */
class TeamProjectTest extends BaseIntegrationTest {

    @Test
    void createTeam_shouldMakeCreatorOwner() throws Exception {
        String token = registerAndLogin("alice");

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "研发一组", "description", "负责核心"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("研发一组"))
                .andExpect(jsonPath("$.data.myRole").value("OWNER"));
    }

    @Test
    void createProject_shouldSucceed() throws Exception {
        String token = registerAndLogin("alice");
        long teamId = createTeam(token, "团队A");

        mockMvc.perform(post("/api/teams/" + teamId + "/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "项目A"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("项目A"))
                .andExpect(jsonPath("$.data.teamId").value(teamId));
    }

    @Test
    void createProject_notTeamMember_shouldBeForbidden() throws Exception {
        String alice = registerAndLogin("alice");
        long teamId = createTeam(alice, "团队A");

        // bob 不是团队成员，不能在这个团队建项目
        String bob = registerAndLogin("bob");
        mockMvc.perform(post("/api/teams/" + teamId + "/projects")
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "偷建项目"))))
                .andExpect(jsonPath("$.code").value(40301));
    }

    @Test
    void nonMember_accessProjectDetail_shouldBeForbidden() throws Exception {
        String alice = registerAndLogin("alice");
        long projectId = setupProject(alice);

        // bob 不在项目里，不能看项目详情
        String bob = registerAndLogin("bob");
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(40302));
    }

    @Test
    void deleteProject_byCreator_shouldCascadeDeleteTasks() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);
        long taskId = createTask(token, projectId, "任务");

        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));

        // 项目没了
        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(40402));

        // 级联：任务也删了
        Integer taskCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task WHERE id = ?", Integer.class, taskId);
        assertEquals(0, taskCount);
    }

    @Test
    void deleteProject_notCreator_shouldBeForbidden() throws Exception {
        String alice = registerAndLogin("alice");
        long projectId = setupProject(alice);
        long bobId = registerUser("bob");
        String bob = login("bob");
        // alice 拉 bob 进项目（普通成员，非创建者）
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", bobId))))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(40303));
    }
}
