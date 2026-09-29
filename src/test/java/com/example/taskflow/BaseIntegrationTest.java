package com.example.taskflow;

import com.example.taskflow.common.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试基类。
 * 每个测试都起完整 Spring 容器 + MockMvc 走真实 HTTP 链路（真 JWT、真过滤链、真权限、真 SQL）。
 * 关键设计：
 * 1. @MockBean CacheService 把缓存打成空操作（get 恒 null、set/delete 无效果），
 *    让测试专注业务逻辑、不依赖 Redis、避免缓存污染。
 * 2. @BeforeEach 清空所有表，保证每个测试从干净数据开始，互不影响。
 * 3. 用真实 MySQL 测试库 taskflow_test（见 src/test/resources/application.yml）。
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /** 把缓存 mock 掉：get 返回 null（总是未命中）、set/delete 空操作 */
    @MockBean
    protected CacheService cacheService;

    /** 把 Redis 模板 mock 掉：限流器 fail-open 直接放行，测试不依赖 Redis、也不产生真实连接 */
    @MockBean
    protected StringRedisTemplate stringRedisTemplate;

    /** 测试统一密码（满足 @Size(min=6)） */
    protected static final String PASSWORD = "123456";

    /** 清库顺序：先清子表再清父表（无外键约束，顺序其实不重要，但按依赖排更清晰） */
    private static final String[] TABLES = {
            "notification",
            "task_log", "task_comment", "task",
            "project_member", "project",
            "team_member", "team",
            "user"
    };

    @BeforeEach
    void cleanDatabase() {
        for (String table : TABLES) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }
    }

    // ---------- 通用工具方法 ----------

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    /** 注册一个用户，返回其 userId */
    protected long registerUser(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "@test.com",
                                "password", PASSWORD,
                                "nickname", username))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    /** 登录，返回 token */
    protected String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    /** 注册 + 登录一步到位，返回 token */
    protected String registerAndLogin(String username) throws Exception {
        registerUser(username);
        return login(username);
    }

    /** 建团队，返回 teamId */
    protected long createTeam(String token, String name) throws Exception {
        String body = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    /** 在团队下建项目，返回 projectId */
    protected long createProject(String token, long teamId, String name) throws Exception {
        String body = mockMvc.perform(post("/api/teams/" + teamId + "/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    /** 建任务，返回 taskId */
    protected long createTask(String token, long projectId, String title) throws Exception {
        String body = mockMvc.perform(post("/api/projects/" + projectId + "/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", title))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    /** 建团队 + 建项目，返回 projectId（单用户场景常用） */
    protected long setupProject(String token) throws Exception {
        long teamId = createTeam(token, "团队");
        return createProject(token, teamId, "项目");
    }
}
