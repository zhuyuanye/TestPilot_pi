package com.testpilot.ruoyi;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RuoYiApiClient {
    private static final int HTTP_TIMEOUT_MILLIS = 10_000;
    private static final RestAssuredConfig REQUEST_CONFIG = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", HTTP_TIMEOUT_MILLIS)
                    .setParam("http.socket.timeout", HTTP_TIMEOUT_MILLIS));

    private final String baseUrl;
    private final String adminUserName;
    private final String adminPassword;
    private String token;

    RuoYiApiClient() {
        baseUrl = requiredEnvironment("RUOYI_BASE_URL").replaceAll("/+$", "");
        adminUserName = requiredEnvironment("RUOYI_ADMIN_USERNAME");
        adminPassword = requiredEnvironment("RUOYI_ADMIN_PASSWORD");
    }

    void login() {
        Response captcha = request().get("/captchaImage");
        requireHttpOk(captcha, "读取验证码配置");
        requireBusinessSuccess(captcha, "读取验证码配置");
        if (!Boolean.FALSE.equals(captcha.jsonPath().getBoolean("captchaEnabled"))) {
            throw new IllegalStateException("测试环境启用了验证码，请先将 sys.account.captchaEnabled 设为 false");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", adminUserName);
        body.put("password", adminPassword);
        body.put("code", "");
        body.put("uuid", "");

        Response response = request().body(body).post("/login");
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
                .queryParam("pageSize", 100)
                .queryParam("userName", userName)
                .get("/system/user/list");
    }

    Response listUserById(long userId) {
        return authenticated()
                .queryParam("pageNum", 1)
                .queryParam("pageSize", 10)
                .queryParam("userId", userId)
                .get("/system/user/list");
    }

    Response getUser(long userId) {
        return authenticated().get("/system/user/{userId}", userId);
    }

    Response currentUser() {
        return authenticated().get("/getInfo");
    }

    Response deleteUser(long userId) {
        return authenticated().delete("/system/user/{userId}", userId);
    }

    Map<String, Object> findExactUser(String userName) {
        List<Map<String, Object>> users = findExactUsers(userName);
        return users.isEmpty() ? null : users.get(0);
    }

    Map<String, Object> findUserByIdInList(long userId) {
        Response response = listUserById(userId);
        requireHttpOk(response, "按 ID 查询用户列表");
        requireBusinessSuccess(response, "按 ID 查询用户列表");
        List<Map<String, Object>> rows = rows(response);
        return rows.stream()
                .filter(row -> userId == numberValue(row.get("userId")))
                .findFirst()
                .orElse(null);
    }

    List<Map<String, Object>> findExactUsers(String userName) {
        Response response = listUsers(userName);
        requireHttpOk(response, "按用户名查询用户");
        requireBusinessSuccess(response, "按用户名查询用户");
        return rows(response).stream()
                .filter(row -> userName.equals(row.get("userName")))
                .toList();
    }

    void removeExistingTestUsers(String userName) {
        for (Map<String, Object> user : findExactUsers(userName)) {
            long userId = numberValue(user.get("userId"));
            Response response = deleteUser(userId);
            requireHttpOk(response, "前置清理测试用户");
            requireBusinessSuccess(response, "前置清理测试用户");
        }
    }

    List<String> cleanupUsers(String userName) {
        List<String> errors = new ArrayList<>();
        try {
            for (Map<String, Object> user : findExactUsers(userName)) {
                long userId = numberValue(user.get("userId"));
                Response response = deleteUser(userId);
                if (response.statusCode() != 200 || !Integer.valueOf(200).equals(businessCode(response))) {
                    errors.add("删除测试用户失败：userName=" + userName
                            + ", HTTP=" + response.statusCode()
                            + ", code=" + businessCode(response)
                            + ", msg=" + businessMessage(response));
                }
            }
            if (findExactUser(userName) != null) {
                errors.add("删除后用户仍出现在列表中：userName=" + userName);
            }
        } catch (RuntimeException | AssertionError error) {
            errors.add("清理测试用户异常：userName=" + userName + ", error=" + error.getMessage());
        }
        return errors;
    }

    static void requireHttpOk(Response response, String action) {
        if (response.statusCode() != 200) {
            throw new AssertionError(action + " HTTP 状态异常：" + response.statusCode());
        }
    }

    static void requireBusinessSuccess(Response response, String action) {
        Integer code = businessCode(response);
        if (!Integer.valueOf(200).equals(code)) {
            throw new AssertionError(action + "业务失败：code=" + code + ", msg=" + businessMessage(response));
        }
    }

    static void requireBusinessFailure(Response response, String action) {
        Integer code = businessCode(response);
        String message = businessMessage(response);
        if (code == null || code == 200) {
            throw new AssertionError(action + "应返回业务失败，但实际 code=" + code);
        }
        if (message == null || message.isBlank()) {
            throw new AssertionError(action + "失败响应缺少可理解的信息");
        }
    }

    static Integer businessCode(Response response) {
        try {
            return response.jsonPath().getInt("code");
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static String businessMessage(Response response) {
        try {
            return response.jsonPath().getString("msg");
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    static long numberValue(Object value) {
        if (!(value instanceof Number number)) {
            throw new AssertionError("响应字段不是数值：" + value);
        }
        return number.longValue();
    }

    private RequestSpecification authenticated() {
        if (token == null) {
            throw new IllegalStateException("请先登录");
        }
        return request().header("Authorization", "Bearer " + token);
    }

    private RequestSpecification request() {
        return RestAssured.given()
                .config(REQUEST_CONFIG)
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    private static List<Map<String, Object>> rows(Response response) {
        List<Map<String, Object>> rows = response.jsonPath().getList("rows");
        return rows == null ? List.of() : rows;
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少环境变量：" + name);
        }
        return value;
    }
}
