package com.testpilot.ruoyi;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

final class TestData {
    private static final String RUN_ID = compactUuid().substring(0, 6);
    private static final AtomicLong PHONE_SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private TestData() {
    }

    static String uniqueUserName(String testCase) {
        String suffix = compactUuid().substring(0, 8);
        String normalizedCase = testCase.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        String value = "u" + RUN_ID + normalizedCase + suffix;
        return value.substring(0, Math.min(20, value.length()));
    }

    static String userNameOfLength(int length) {
        if (length < 1 || length > 32) {
            throw new IllegalArgumentException("用户名测试长度超出生成范围：" + length);
        }
        String source = compactUuid() + compactUuid();
        return source.substring(0, length);
    }

    static String chineseUserName() {
        return "测" + RUN_ID + compactUuid().substring(0, 4);
    }

    static String symbolUserName() {
        return "u-" + RUN_ID + "_!";
    }

    static String uniquePhone() {
        long suffix = Math.floorMod(PHONE_SEQUENCE.getAndIncrement(), 1_000_000_000L);
        return "13" + String.format("%09d", suffix);
    }

    static String uniquePassword() {
        return "T" + compactUuid().substring(0, 10) + "9";
    }

    static long nonexistentUserId() {
        return Long.MAX_VALUE - Math.floorMod(PHONE_SEQUENCE.getAndIncrement(), 1_000_000L);
    }

    static Map<String, Object> validUser(String userName, String testCase) {
        return user(userName, uniquePassword(), uniquePhone(), testCase);
    }

    static Map<String, Object> validUserWithoutPhone(String userName, String testCase) {
        return user(userName, uniquePassword(), null, testCase);
    }

    static Map<String, Object> user(String userName, String password, String phone, String testCase) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userName", userName);
        user.put("nickName", "N-" + RUN_ID + "-" + testCase);
        user.put("password", password);
        if (phone != null) {
            user.put("phonenumber", phone);
        }
        return user;
    }

    static String caseVariant(String userName) {
        char first = userName.charAt(0);
        char changed = Character.isUpperCase(first) ? Character.toLowerCase(first) : Character.toUpperCase(first);
        return changed + userName.substring(1);
    }

    private static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
