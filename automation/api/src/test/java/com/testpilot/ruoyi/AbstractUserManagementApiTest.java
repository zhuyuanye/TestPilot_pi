package com.testpilot.ruoyi;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractUserManagementApiTest {
    protected RuoYiApiClient api;
    private final Set<String> cleanupUserNames = new LinkedHashSet<>();

    @BeforeEach
    void authenticate() {
        api = new RuoYiApiClient();
        api.login();
    }

    @AfterEach
    void cleanupCreatedUsers() {
        if (api == null) {
            return;
        }
        for (String userName : cleanupUserNames) {
            for (String error : api.cleanupUsers(userName)) {
                System.err.println("[cleanup] " + error);
            }
        }
    }

    protected String prepareUserName(String userName) {
        api.removeExistingTestUsers(userName);
        cleanupUserNames.add(userName);
        return userName;
    }

    protected Map<String, Object> requireAddedUser(Response response, String userName, String action) {
        RuoYiApiClient.requireHttpOk(response, action);
        RuoYiApiClient.requireBusinessSuccess(response, action);
        Map<String, Object> user = api.findExactUser(userName);
        assertNotNull(user, action + "成功后应能通过列表查询到用户");
        return user;
    }

    protected void requireRejected(Response response, String action, String... messageParts) {
        RuoYiApiClient.requireHttpOk(response, action);
        RuoYiApiClient.requireBusinessFailure(response, action);
        String message = RuoYiApiClient.businessMessage(response);
        for (String part : messageParts) {
            assertTrue(message.contains(part), action + "失败信息应包含“" + part + "”，实际为：" + message);
        }
    }
}
