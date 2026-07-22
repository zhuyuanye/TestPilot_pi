package com.testpilot.ruoyi;

import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnabledIfEnvironmentVariable(named = "RUN_REQUIREMENT_GAPS", matches = "(?i)true")
class UserRequirementGapTest {
    private RuoYiApiClient api;

    @BeforeEach
    void login() {
        api = new RuoYiApiClient();
        api.login();
    }

    @Test
    @DisplayName("TC-04：21 位用户名应被后端拒绝（AC-03）")
    void shouldRejectUserNameLongerThanTwentyCharacters() {
        String userName = "tp" + "x".repeat(19);
        Map<String, Object> user = TestData.validUser(userName);

        try {
            Response response = api.addUser(user);
            RuoYiApiClient.requireHttpOk(response, "提交21位用户名");
            assertNotEquals(200, response.jsonPath().getInt("code"),
                    "需求规定用户名最多20位，但后端接受了21位用户名");
        } finally {
            api.deleteIfPresent(userName);
        }
    }

    @Test
    @DisplayName("TC-13：非法手机号应被后端拒绝（AC-10）")
    void shouldRejectInvalidPhoneNumber() {
        String userName = TestData.uniqueUserName();
        Map<String, Object> user = TestData.validUser(userName);
        user.put("phonenumber", "123");

        try {
            Response response = api.addUser(user);
            RuoYiApiClient.requireHttpOk(response, "提交非法手机号");
            assertNotEquals(200, response.jsonPath().getInt("code"),
                    "需求规定手机号必须合法，但后端只校验了最大长度");
        } finally {
            api.deleteIfPresent(userName);
        }
    }
}
