package com.testpilot.ruoyi;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserManagementP0ApiTest extends AbstractUserManagementApiTest {

    @Test
    @DisplayName("TC-002/006/007/015 | AC-01/08/16/19/20/21/22/23 | P0 用户生命周期闭环")
    void shouldCompleteP0UserLifecycle() {
        String userName = prepareUserName(TestData.uniqueUserName("p0life"));
        String caseVariant = prepareUserName(TestData.caseVariant(userName));
        Map<String, Object> request = TestData.validUserWithoutPhone(userName, "TC002");

        Map<String, Object> created = requireAddedUser(api.addUser(request), userName, "新增用户");
        long userId = RuoYiApiClient.numberValue(created.get("userId"));
        assertEquals(userName, created.get("userName"), "列表应返回准确用户名");
        assertEquals(request.get("nickName"), created.get("nickName"), "列表应返回准确昵称");
        assertEquals("0", created.get("status"), "未指定状态时应默认为正常");

        requireRejected(api.addUser(request), "使用完全相同用户名重复新增", "存在");

        Map<String, Object> caseRequest = new LinkedHashMap<>(request);
        caseRequest.put("userName", caseVariant);
        caseRequest.put("nickName", "N-case-variant");
        requireRejected(api.addUser(caseRequest), "使用仅大小写不同的用户名新增", "存在");

        Response deleted = api.deleteUser(userId);
        RuoYiApiClient.requireHttpOk(deleted, "删除普通用户");
        RuoYiApiClient.requireBusinessSuccess(deleted, "删除普通用户");
        assertNull(api.findExactUser(userName), "删除后用户列表中不应存在该用户");

        Response detail = api.getUser(userId);
        RuoYiApiClient.requireHttpOk(detail, "查询已删除用户详情");
        Integer detailCode = RuoYiApiClient.businessCode(detail);
        Object detailUserId = detail.jsonPath().get("data.userId");
        boolean returnedDeletedUser = Integer.valueOf(200).equals(detailCode)
                && detailUserId instanceof Number number
                && number.longValue() == userId;
        assertFalse(returnedDeletedUser, "删除后用户详情不应返回被删除用户");
    }

    @Test
    @DisplayName("TC-003 | AC-02 | P0 用户名必填")
    void shouldRejectMissingUserName() {
        Map<String, Object> request = TestData.validUserWithoutPhone(
                TestData.uniqueUserName("tc003"), "TC003");
        request.put("userName", "");

        requireRejected(api.addUser(request), "用户名为空时新增用户", "用户", "空");
    }

    @Test
    @DisplayName("TC-008 | AC-09 | P0 用户昵称必填")
    void shouldRejectMissingNickName() {
        String userName = prepareUserName(TestData.uniqueUserName("tc008"));
        Map<String, Object> request = TestData.validUserWithoutPhone(userName, "TC008");
        request.put("nickName", "");

        requireRejected(api.addUser(request), "昵称为空时新增用户", "昵称", "空");
    }

    @Test
    @DisplayName("TC-009 | AC-10 | P0 密码必填")
    void shouldRejectMissingPassword() {
        String userName = prepareUserName(TestData.uniqueUserName("tc009"));
        Map<String, Object> request = TestData.validUserWithoutPhone(userName, "TC009");
        request.put("password", "");

        requireRejected(api.addUser(request), "密码为空时新增用户", "密码", "空");
    }

    @Test
    @DisplayName("TC-016 | AC-25 | P0 删除不存在用户不影响其他用户")
    void shouldNotChangeOtherUsersWhenDeletingMissingUser() {
        String userName = prepareUserName(TestData.uniqueUserName("tc016"));
        Map<String, Object> request = TestData.validUserWithoutPhone(userName, "TC016");
        Map<String, Object> before = requireAddedUser(api.addUser(request), userName, "创建基准用户");

        long missingUserId = TestData.nonexistentUserId();
        assertNull(api.findUserByIdInList(missingUserId), "删除前应确认目标用户不存在");

        Response deleteMissing = api.deleteUser(missingUserId);
        RuoYiApiClient.requireHttpOk(deleteMissing, "删除不存在用户");
        assertNotNull(RuoYiApiClient.businessCode(deleteMissing), "删除不存在用户应返回业务状态");
        if (!Integer.valueOf(200).equals(RuoYiApiClient.businessCode(deleteMissing))) {
            assertNotNull(RuoYiApiClient.businessMessage(deleteMissing), "失败时应返回可理解的信息");
        }

        Map<String, Object> after = api.findExactUser(userName);
        assertNotNull(after, "删除不存在用户后基准用户仍应可查询");
        assertEquals(before.get("userId"), after.get("userId"), "基准用户 ID 不应改变");
        assertEquals(before.get("userName"), after.get("userName"), "基准用户名不应改变");
        assertEquals(before.get("nickName"), after.get("nickName"), "基准用户昵称不应改变");
        assertEquals(before.get("status"), after.get("status"), "基准用户状态不应改变");
    }
}
