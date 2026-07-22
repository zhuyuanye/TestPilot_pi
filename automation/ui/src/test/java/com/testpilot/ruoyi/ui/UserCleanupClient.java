package com.testpilot.ruoyi.ui;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class UserCleanupClient {
    private static final int HTTP_TIMEOUT_MILLIS = 10_000;
    private static final RestAssuredConfig REQUEST_CONFIG = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", HTTP_TIMEOUT_MILLIS)
                    .setParam("http.socket.timeout", HTTP_TIMEOUT_MILLIS));

    private final String baseUrl = requiredEnvironment("RUOYI_BASE_URL").replaceAll("/+$", "");

    void deleteAllExact(String userName) {
        String token = login();
        for (Map<String, Object> user : exactUsers(token, userName)) {
            delete(token, ((Number) user.get("userId")).longValue());
        }
        if (!exactUsers(token, userName).isEmpty()) {
            throw new AssertionError("UI 测试数据清理后用户仍可查询：userName=" + userName);
        }
    }

    private String login() {
        Response captcha = request().get("/captchaImage");
        requireSuccess(captcha, "读取验证码配置");
        if (!Boolean.FALSE.equals(captcha.jsonPath().getBoolean("captchaEnabled"))) {
            throw new IllegalStateException("测试环境启用了验证码，无法执行 API 兜底清理");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", requiredEnvironment("RUOYI_ADMIN_USERNAME"));
        body.put("password", requiredEnvironment("RUOYI_ADMIN_PASSWORD"));
        body.put("code", "");
        body.put("uuid", "");

        Response response = request().body(body).post("/login");
        requireSuccess(response, "UI 测试数据清理登录");
        String token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new AssertionError("UI 测试数据清理登录响应缺少 token");
        }
        return token;
    }

    private List<Map<String, Object>> exactUsers(String token, String userName) {
        Response response = authenticated(token)
                .queryParam("pageNum", 1)
                .queryParam("pageSize", 100)
                .queryParam("userName", userName)
                .get("/system/user/list");
        requireSuccess(response, "查询 UI 测试用户");

        List<Map<String, Object>> rows = response.jsonPath().getList("rows");
        if (rows == null) {
            return List.of();
        }
        return rows.stream()
                .filter(row -> userName.equals(row.get("userName")))
                .toList();
    }

    private void delete(String token, long userId) {
        Response response = authenticated(token).delete("/system/user/{userId}", userId);
        requireSuccess(response, "清理 UI 测试用户 userId=" + userId);
    }

    private RequestSpecification authenticated(String token) {
        return request().header("Authorization", "Bearer " + token);
    }

    private RequestSpecification request() {
        return RestAssured.given()
                .config(REQUEST_CONFIG)
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private static void requireSuccess(Response response, String action) {
        Integer code = businessCode(response);
        if (response.statusCode() != 200 || !Integer.valueOf(200).equals(code)) {
            throw new AssertionError(action + "失败：HTTP=" + response.statusCode()
                    + ", code=" + code + ", msg=" + businessMessage(response));
        }
    }

    private static Integer businessCode(Response response) {
        try {
            return response.jsonPath().getInt("code");
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String businessMessage(Response response) {
        try {
            return response.jsonPath().getString("msg");
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少环境变量：" + name);
        }
        return value;
    }
}
