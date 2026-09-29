package com.example.taskflow;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 评论模块集成测试：发表、非成员拦截、只能删自己的评论。
 */
class TaskCommentTest extends BaseIntegrationTest {

    @Test
    void member_canComment() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);
        long taskId = createTask(token, projectId, "任务");

        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "干得漂亮"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("干得漂亮"))
                .andExpect(jsonPath("$.data.nickname").value("alice"));
    }

    @Test
    void nonMember_cannotComment() throws Exception {
        String alice = registerAndLogin("alice");
        long projectId = setupProject(alice);
        long taskId = createTask(alice, projectId, "任务");

        String bob = registerAndLogin("bob");
        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "偷评论"))))
                .andExpect(jsonPath("$.code").value(40302));
    }

    @Test
    void deleteOthersComment_shouldBeForbidden() throws Exception {
        String alice = registerAndLogin("alice");
        long projectId = setupProject(alice);
        long taskId = createTask(alice, projectId, "任务");

        // bob 加入项目（项目成员能看评论，但只能删自己的）
        long bobId = registerUser("bob");
        String bob = login("bob");
        mockMvc.perform(post("/api/projects/" + projectId + "/members")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", bobId))))
                .andExpect(jsonPath("$.code").value(200));

        // alice 发评论
        long commentId = createComment(alice, taskId, "alice的评论");

        // bob 删 alice 的评论 -> 无权限
        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + bob))
                .andExpect(jsonPath("$.code").value(40303));
    }

    @Test
    void deleteOwnComment_shouldSucceed() throws Exception {
        String token = registerAndLogin("alice");
        long projectId = setupProject(token);
        long taskId = createTask(token, projectId, "任务");

        long commentId = createComment(token, taskId, "我的评论");

        mockMvc.perform(delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(200));
    }

    private long createComment(String token, long taskId, String content) throws Exception {
        String body = mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", content))))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }
}
