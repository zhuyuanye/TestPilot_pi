package com.testpilot.ruoyi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RuoYiApiClient {
    private final String baseUrl;
    private String token;

    RuoYiApiClient() {
        this.baseUrl = environment("RUOYI_BASE_URL", "http://localhost:8080");
    }

    void login() {
        Response captcha = RestAssured.given()
                .baseUri(baseUrl)
                .get("/captchaImage");
        requireHttpOk(captcha, "读取验证码配置");
        if (captcha.jsonPath().getBoolean("captchaEnabled")) {
            throw new IllegalStateException("测试环境启用了验证码，请先关闭 sys.account.captchaEnabled");
        }

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
        requireHttpOk(response, "登录");
        requireBusinessSuccess(response, "登录");
        token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new AssertionError("登录成功响应中缺少 token");
        }
    }

    Response addUser(Map<String, Object> user) {
        return authenticated().body(user).post("/system/user");
    }

    Response listUsers(String userName) {
        return authenticated()
                .queryParam("pageNum", 1)
                .queryParam("pageSize", 20)
                .queryParam("userName", userName)
                .get("/system/user/list");
    }

    Response currentUser() {
        return authenticated().get("/getInfo");
    }

    Response deleteUser(long userId) {
        return authenticated().delete("/system/user/{userId}", userId);
    }

    Long findExactUserId(String userName) {
        Response response = listUsers(userName);
        requireHttpOk(response, "查询用户");
        requireBusinessSuccess(response, "查询用户");
        List<Map<String, Object>> rows = response.jsonPath().getList("rows");
        if (rows == null) {
            return null;
        }
        return rows.stream()
                .filter(row -> userName.equals(row.get("userName")))
                .map(row -> ((Number) row.get("userId")).longValue())
                .findFirst()
                .orElse(null);
    }

    Map<String, Object> findExactUser(String userName) {
        Response response = listUsers(userName);
        requireHttpOk(response, "查询用户");
        requireBusinessSuccess(response, "查询用户");
        List<Map<String, Object>> rows = response.jsonPath().getList("rows");
        if (rows == null) {
            return null;
        }
        return rows.stream()
                .filter(row -> userName.equals(row.get("userName")))
                .findFirst()
                .orElse(null);
    }

    void deleteIfPresent(String userName) {
        Long userId = findExactUserId(userName);
        if (userId != null) {
            Response response = deleteUser(userId);
            requireHttpOk(response, "清理测试用户");
            requireBusinessSuccess(response, "清理测试用户");
        }
    }

    static void requireHttpOk(Response response, String action) {
        if (response.statusCode() != 200) {
            throw new AssertionError(action + " HTTP 状态异常：" + response.statusCode());
        }
    }

    static void requireBusinessSuccess(Response response, String action) {
        JsonPath json = response.jsonPath();
        Integer code = json.getInt("code");
        if (!Integer.valueOf(200).equals(code)) {
            throw new AssertionError(action + "业务失败：code=" + code + ", msg=" + json.getString("msg"));
        }
    }

    private io.restassured.specification.RequestSpecification authenticated() {
        if (token == null) {
            throw new IllegalStateException("请先登录");
        }
        return RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token);
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
