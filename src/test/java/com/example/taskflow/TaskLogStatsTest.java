package com.example.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 操作日志 + 统计接口集成测试。
 */
class TaskLogStatsTest extends BaseIntegrationTest {

    @Test
    void statusChange_shouldRecordOperationLog() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);
        long taskId = createTask(token, projectId, "任务");

        // 改一次状态
        mockMvc.perform(put("/api/tasks/" + taskId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_PROGRESS"))))
                .andExpect(jsonPath("$.code").value(200));

        // 日志应有两条：CREATE + STATUS_CHANGED
        mockMvc.perform(get("/api/tasks/" + taskId + "/logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(2));
    }

    @Test
    void stats_shouldReflectStatusCounts() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);

        long task1 = createTask(token, projectId, "任务1");
        long task2 = createTask(token, projectId, "任务2");

        // task1 -> IN_PROGRESS
        mockMvc.perform(put("/api/tasks/" + task1 + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_PROGRESS"))))
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/projects/" + projectId + "/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.todo").value(1))
                .andExpect(jsonPath("$.data.inProgress").value(1))
                .andExpect(jsonPath("$.data.done").value(0));
    }
}
