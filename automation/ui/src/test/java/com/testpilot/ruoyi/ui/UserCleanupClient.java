package com.testpilot.ruoyi.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class UserCleanupClient {
    private final String baseUrl = environment("RUOYI_BASE_URL", "http://localhost:8080");

    void deleteIfPresent(String userName) {
        String token = login();
        Response list = RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .queryParam("pageNum", 1)
                .queryParam("pageSize", 20)
                .queryParam("userName", userName)
                .get("/system/user/list");
        if (list.statusCode() != 200 || list.jsonPath().getInt("code") != 200) {
            throw new AssertionError("UI 测试数据清理查询失败");
        }

        List<Map<String, Object>> rows = list.jsonPath().getList("rows");
        if (rows == null) {
            return;
        }
        rows.stream()
                .filter(row -> userName.equals(row.get("userName")))
                .map(row -> ((Number) row.get("userId")).longValue())
                .findFirst()
                .ifPresent(userId -> delete(token, userId));
    }

    private String login() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", requiredEnvironment("RUOYI_USERNAME"));
        body.put("password", requiredEnvironment("RUOYI_PASSWORD"));
        body.put("code", "");
        body.put("uuid", "");

        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(body)
                .post("/login");
        if (response.statusCode() != 200 || response.jsonPath().getInt("code") != 200) {
            throw new AssertionError("UI 测试数据清理登录失败");
        }
        return response.jsonPath().getString("token");
    }

    private void delete(String token, long userId) {
        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .delete("/system/user/{userId}", userId);
        if (response.statusCode() != 200 || response.jsonPath().getInt("code") != 200) {
            throw new AssertionError("UI 测试数据清理删除失败，userId=" + userId);
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少环境变量：" + name);
        }
        return value;
    }

    private static String environment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
