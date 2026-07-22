package com.testpilot.ruoyi;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserManagementValidationApiTest extends AbstractUserManagementApiTest {

    @ParameterizedTest(name = "{0}: 用户名长度 {1}，预期成功={2}")
    @MethodSource("userNameLengthCases")
    @DisplayName("TC-004 | AC-03/04/05/06 | 用户名长度边界")
    void shouldEnforceUserNameLengthBoundaries(String row, int length, boolean expectedSuccess) {
        String userName = prepareUserName(TestData.userNameOfLength(length));
        Map<String, Object> request = TestData.validUserWithoutPhone(userName, "TC004-" + row);

        Response response = api.addUser(request);
        if (expectedSuccess) {
            Map<String, Object> created = requireAddedUser(response, userName, "提交用户名边界 " + row);
            assertEquals(length, ((String) created.get("userName")).length(), "保存后的用户名长度应保持不变");
        } else {
            requireRejected(response, "提交用户名边界 " + row, "用户", "长度");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("userNameCharacterCases")
    @DisplayName("TC-005 | AC-07 | 用户名不限制字符类型")
    void shouldAcceptSupportedUserNameCharacterTypes(String row, String userName) {
        prepareUserName(userName);
        Map<String, Object> request = TestData.validUserWithoutPhone(userName, "TC005-" + row);

        Map<String, Object> created = requireAddedUser(api.addUser(request), userName, "提交用户名字符类型 " + row);
        assertEquals(userName, created.get("userName"), "保存后的用户名应与提交值一致");
    }

    @ParameterizedTest(name = "{0}: 密码长度 {1}，预期成功={2}")
    @MethodSource("passwordLengthCases")
    @DisplayName("TC-010 | AC-11/12/13/14 | 密码长度边界")
    void shouldEnforcePasswordLengthBoundaries(String row, int length, boolean expectedSuccess) {
        String userName = prepareUserName(TestData.uniqueUserName("tc010" + row));
        String password = "a".repeat(length);
        Map<String, Object> request = TestData.user(userName, password, null, "TC010-" + row);

        Response response = api.addUser(request);
        if (expectedSuccess) {
            requireAddedUser(response, userName, "提交密码边界 " + row);
        } else {
            requireRejected(response, "提交密码边界 " + row, "密码", "长度");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("simplePasswordCases")
    @DisplayName("TC-011 | AC-15 | 密码不要求字符复杂度")
    void shouldAcceptPasswordWithoutCharacterComplexity(String row, String password) {
        String userName = prepareUserName(TestData.uniqueUserName("tc011" + row));
        Map<String, Object> request = TestData.user(userName, password, null, "TC011-" + row);

        requireAddedUser(api.addUser(request), userName, "提交单一字符类型密码 " + row);
    }

    @Test
    @DisplayName("TC-012 | AC-17 | 合法手机号可以新增用户")
    void shouldAcceptValidPhoneNumber() {
        String userName = prepareUserName(TestData.uniqueUserName("tc012"));
        String phone = TestData.uniquePhone();
        Map<String, Object> request = TestData.user(
                userName, TestData.uniquePassword(), phone, "TC012");

        Map<String, Object> created = requireAddedUser(api.addUser(request), userName, "提交合法手机号");
        assertEquals(phone, created.get("phonenumber"), "保存后的手机号应与提交值一致");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPhoneCases")
    @DisplayName("TC-013 | AC-18 | 非法手机号不能新增用户")
    void shouldRejectInvalidPhoneNumbers(String row, String phone) {
        String userName = prepareUserName(TestData.uniqueUserName("tc013" + row));
        Map<String, Object> request = TestData.user(
                userName, TestData.uniquePassword(), phone, "TC013-" + row);

        requireRejected(api.addUser(request), "提交非法手机号 " + row, "手机");
    }

    private static Stream<Arguments> userNameLengthCases() {
        return Stream.of(
                Arguments.of("U1", 1, false),
                Arguments.of("U2", 2, true),
                Arguments.of("U3", 20, true),
                Arguments.of("U4", 21, false));
    }

    private static Stream<Arguments> userNameCharacterCases() {
        return Stream.of(
                Arguments.of("C1-中文", TestData.chineseUserName()),
                Arguments.of("C2-符号", TestData.symbolUserName()));
    }

    private static Stream<Arguments> passwordLengthCases() {
        return Stream.of(
                Arguments.of("P1", 4, false),
                Arguments.of("P2", 5, true),
                Arguments.of("P3", 20, true),
                Arguments.of("P4", 21, false));
    }

    private static Stream<Arguments> simplePasswordCases() {
        return Stream.of(
                Arguments.of("S1-仅字母", "aaaaa"),
                Arguments.of("S2-仅数字", "11111"));
    }

    private static Stream<Arguments> invalidPhoneCases() {
        String valid = TestData.uniquePhone();
        return Stream.of(
                Arguments.of("M1-10位", valid.substring(0, 10)),
                Arguments.of("M2-12位", valid + "0"),
                Arguments.of("M3-首位非1", "2" + valid.substring(1)),
                Arguments.of("M4-包含非数字", valid.substring(0, 10) + "A"));
    }
}
