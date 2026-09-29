package com.example.taskflow;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 通知模块集成测试。验证五个触发点里最典型的三个（拉人进团队/评论/分配），
 * 以及列表、未读数、单条已读、全部已读四个接口。
 */
class NotificationTest extends BaseIntegrationTest {

    /** 拉人进团队 -> 被拉的人收到 TEAM_INVITED 通知；走完读/未读闭环 */
    @Test
    void teamInvite_notifiesAddedUser() throws Exception {
        String tokenA = registerAndLogin("alice");
        long idB = registerUser("bob");
        String tokenB = login("bob");

        long teamId = createTeam(tokenA, "团队");
        // A（OWNER）拉 B 进团队
        mockMvc.perform(post("/api/teams/" + teamId + "/members")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", idB))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // B 收到一条团队通知，未读 1
        String list = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].type").value("TEAM_INVITED"))
                .andReturn().getResponse().getContentAsString();
        long notifId = objectMapper.readTree(list).path("data").path("records").path(0).path("id").asLong();

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(jsonPath("$.data").value(1));

        // 单条已读 -> 未读归零
        mockMvc.perform(put("/api/notifications/" + notifId + "/read")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(jsonPath("$.data").value(0));
    }

    /** 评论任务 -> 创建者收到 TASK_COMMENTED，评论者自己不收到 */
    @Test
    void taskComment_notifiesCreatorExcludingCommenter() throws Exception {
        String tokenA = registerAndLogin("alice");
        long idB = registerUser("bob");
        String tokenB = login("bob");

        long projectId = setupProject(tokenA);
        // B 先进项目
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", idB))))
                .andExpect(status().isOk());
        long taskId = createTask(tokenA, projectId, "任务");

        // B 评论（A 是创建者，无负责人）
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "hi"))))
                .andExpect(status().isOk());

        // 创建者 A 收到评论通知
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].type").value("TASK_COMMENTED"));

        // 评论者 B 只收到进项目的通知，没有收到评论通知
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.records[0].type").value("PROJECT_INVITED"));
    }

    /** 分配负责人 -> 新负责人收到 TASK_ASSIGNED */
    @Test
    void taskAssign_notifiesAssignee() throws Exception {
        String tokenA = registerAndLogin("alice");
        long idB = registerUser("bob");
        String tokenB = login("bob");

        long projectId = setupProject(tokenA);
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", idB))))
                .andExpect(status().isOk());
        long taskId = createTask(tokenA, projectId, "任务");

        mockMvc.perform(put("/api/tasks/" + taskId + "/assignee")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("assigneeId", idB))))
                .andExpect(status().isOk());

        // B 先收到进项目通知，再收到分配通知，共 2 条（两条时间戳同秒，不依赖排序）
        String list = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(list).path("data").path("records");
        boolean hasAssign = false;
        for (JsonNode n : data) {
            if ("TASK_ASSIGNED".equals(n.path("type").asText())
                    && n.path("relatedId").asLong() == taskId) {
                hasAssign = true;
            }
        }
        assertTrue(hasAssign, "应包含一条关联该任务的 TASK_ASSIGNED 通知");
    }

    /** 全部已读 */
    @Test
    void markAllRead() throws Exception {
        String tokenA = registerAndLogin("alice");
        long idB = registerUser("bob");
        String tokenB = login("bob");

        long teamId = createTeam(tokenA, "团队");
        long projectId = createProject(tokenA, teamId, "项目");
        // 拉 B 进团队 + 进项目 -> 两条通知
        mockMvc.perform(post("/api/teams/" + teamId + "/members")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", idB))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", idB))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(jsonPath("$.data").value(2));

        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(jsonPath("$.data").value(0));
    }
}
