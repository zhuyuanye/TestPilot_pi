package com.testpilot.ruoyi;

import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserLifecycleTest {
    private RuoYiApiClient api;

    @BeforeEach
    void login() {
        api = new RuoYiApiClient();
        api.login();
    }

    @Test
    @DisplayName("TC-01/02/05/14：新增、查询、重复校验与删除闭环（AC-01/02/04/11）")
    void shouldCompleteUserLifecycle() {
        String userName = TestData.uniqueUserName();
        Map<String, Object> user = TestData.validUser(userName);

        try {
            Response add = api.addUser(user);
            RuoYiApiClient.requireHttpOk(add, "新增用户");
            RuoYiApiClient.requireBusinessSuccess(add, "新增用户");

            Map<String, Object> created = api.findExactUser(userName);
            assertNotNull(created, "新增成功后应能通过列表查询到用户");
            assertEquals("0", created.get("status"), "新增用户默认状态应为正常");

            Response duplicate = api.addUser(user);
            RuoYiApiClient.requireHttpOk(duplicate, "重复新增用户");
            assertTrue(duplicate.jsonPath().getInt("code") != 200, "重复用户名必须被拒绝");
            assertTrue(duplicate.jsonPath().getString("msg").contains("已存在"), "失败信息应可理解");

            Long userId = ((Number) created.get("userId")).longValue();
            Response deleted = api.deleteUser(userId);
            RuoYiApiClient.requireHttpOk(deleted, "删除用户");
            RuoYiApiClient.requireBusinessSuccess(deleted, "删除用户");

            assertNull(api.findExactUserId(userName), "删除后用户列表中不应存在该用户");
        } finally {
            api.deleteIfPresent(userName);
        }
    }

    @Test
    @DisplayName("TC-16：当前登录用户不能删除自己（AC-13）")
    void shouldRejectDeletingCurrentUser() {
        Response current = api.currentUser();
        RuoYiApiClient.requireHttpOk(current, "获取当前用户");
        RuoYiApiClient.requireBusinessSuccess(current, "获取当前用户");
        long currentUserId = current.jsonPath().getLong("user.userId");

        Response deleted = api.deleteUser(currentUserId);
        RuoYiApiClient.requireHttpOk(deleted, "尝试删除当前用户");
        assertTrue(deleted.jsonPath().getInt("code") != 200, "系统必须拒绝删除当前登录用户");
        assertTrue(deleted.jsonPath().getString("msg").contains("当前用户不能删除"), "失败信息应可理解");
    }
}
