package com.example.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 任务模块集成测试：创建默认值、状态机流转。
 */
class TaskTest extends BaseIntegrationTest {

    @Test
    void createTask_shouldDefaultToTodoAndMedium() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);

        mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "写周报"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("写周报"))
                .andExpect(jsonPath("$.data.status").value("TODO"))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"));
    }

    @Test
    void updateStatus_validTransition_shouldSucceed() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);
        long taskId = createTask(token, projectId, "任务");

        // TODO -> IN_PROGRESS 合法
        mockMvc.perform(put("/api/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    void updateStatus_illegalTransition_shouldBeRejected() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);
        long taskId = createTask(token, projectId, "任务");

        // TODO -> DONE 非法（必须经 IN_PROGRESS）
        mockMvc.perform(put("/api/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "DONE"))))
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void updateStatus_assignee_canEdit() throws Exception {
        // 负责人（参与级）能改状态，但非创建者/非负责人不能
        String alice = registerAndLogin("alice");
        long projectId = setupProject(alice);

        // bob 加入项目（alice 是项目创建者，可加成员）
        long bobId = registerUser("bob");
        String bob = login("bob");
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", bobId))))
                .andExpect(jsonPath("$.code").value(200));

        // alice 建任务并指派给 bob
        long taskId = createTaskWithAssignee(alice, projectId, "任务", bobId);

        // bob 是负责人，能改状态
        mockMvc.perform(put("/api/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_PROGRESS"))))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    private long createTaskWithAssignee(String token, long projectId, String title, long assigneeId) throws Exception {
        String body = mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", title, "assigneeId", assigneeId))))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
