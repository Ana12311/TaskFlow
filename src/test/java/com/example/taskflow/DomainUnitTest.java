package com.example.taskflow;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.taskflow.common.enums.Role;
import com.example.taskflow.common.enums.TaskStatus;
import com.example.taskflow.vo.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 纯逻辑单元测试：不启动 Spring 容器，直接测状态机、权限判断、分页 VO 的纯函数逻辑。
 * 与集成测试（BaseIntegrationTest 那套）互补：集成测链路，单元测逻辑。
 */
class DomainUnitTest {

    // ---------- 任务状态机 ----------

    @Test
    void taskStatus_todoTransitions() {
        assertTrue(TaskStatus.TODO.canTransitionTo(TaskStatus.IN_PROGRESS));
        assertTrue(TaskStatus.TODO.canTransitionTo(TaskStatus.CANCELLED));
        assertFalse(TaskStatus.TODO.canTransitionTo(TaskStatus.DONE));
        assertFalse(TaskStatus.TODO.canTransitionTo(TaskStatus.TODO));
    }

    @Test
    void taskStatus_doneCannotReopen() {
        assertFalse(TaskStatus.DONE.canTransitionTo(TaskStatus.TODO));
        assertTrue(TaskStatus.DONE.canTransitionTo(TaskStatus.CANCELLED));
    }

    @Test
    void taskStatus_cancelledOnlyBackToTodo() {
        assertTrue(TaskStatus.CANCELLED.canTransitionTo(TaskStatus.TODO));
        assertFalse(TaskStatus.CANCELLED.canTransitionTo(TaskStatus.IN_PROGRESS));
        assertFalse(TaskStatus.CANCELLED.canTransitionTo(TaskStatus.DONE));
    }

    // ---------- 角色权限 ----------

    @Test
    void role_ownerAndAdminCanManageMembers() {
        assertTrue(Role.OWNER.canManageMembers());
        assertTrue(Role.ADMIN.canManageMembers());
        assertFalse(Role.MEMBER.canManageMembers());
    }

    // ---------- 分页 VO ----------

    @Test
    void pageResult_ofMapsPageFields() {
        IPage<?> page = mock(IPage.class);
        when(page.getTotal()).thenReturn(25L);
        when(page.getCurrent()).thenReturn(2L);
        when(page.getSize()).thenReturn(10L);

        PageResult<String> result = PageResult.of(page, List.of("a", "b"));

        assertEquals(25L, result.getTotal());
        assertEquals(2L, result.getPageNum());
        assertEquals(10L, result.getPageSize());
        assertEquals(List.of("a", "b"), result.getRecords());
    }
}
