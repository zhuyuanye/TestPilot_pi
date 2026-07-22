package com.testpilot.ruoyi;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "RUOYI_ENABLE_SELF_DELETE_TEST", matches = "(?i)true")
class UserManagementSelfDeleteApiTest extends AbstractUserManagementApiTest {

    @Test
    @DisplayName("TC-014 | AC-24 | P0 当前管理员不能删除自己")
    void shouldRejectDeletingCurrentUserAndKeepFixtureQueryable() {
        Response current = api.currentUser();
        RuoYiApiClient.requireHttpOk(current, "获取当前用户");
        RuoYiApiClient.requireBusinessSuccess(current, "获取当前用户");
        long currentUserId = current.jsonPath().getLong("user.userId");
        String currentUserName = current.jsonPath().getString("user.userName");

        Response deleted = api.deleteUser(currentUserId);
        requireRejected(deleted, "尝试删除当前登录用户", "当前用户", "删除");

        Response currentAfter = api.currentUser();
        RuoYiApiClient.requireHttpOk(currentAfter, "删除尝试后获取当前用户");
        RuoYiApiClient.requireBusinessSuccess(currentAfter, "删除尝试后获取当前用户");
        assertEquals(currentUserId, currentAfter.jsonPath().getLong("user.userId"),
                "删除尝试后当前登录用户 ID 不应改变");

        Map<String, Object> listUser = api.findExactUser(currentUserName);
        assertNotNull(listUser, "删除尝试后当前用户仍应出现在用户列表中");
        assertEquals(currentUserId, RuoYiApiClient.numberValue(listUser.get("userId")),
                "列表应返回当前登录用户");

        Response detail = api.getUser(currentUserId);
        RuoYiApiClient.requireHttpOk(detail, "删除尝试后查询当前用户详情");
        RuoYiApiClient.requireBusinessSuccess(detail, "删除尝试后查询当前用户详情");
        assertEquals(currentUserId, detail.jsonPath().getLong("data.userId"),
                "详情应返回当前登录用户");
    }
}
