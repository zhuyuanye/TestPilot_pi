package com.testpilot.ruoyi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class TestData {
    private TestData() {
    }

    static String uniqueUserName() {
        long suffix = System.currentTimeMillis() % 1_000_000_000L;
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "tp" + suffix + random;
    }

    static String uniquePhone() {
        long tail = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return "1" + String.format("%010d", tail);
    }

    static Map<String, Object> validUser(String userName) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userName", userName);
        user.put("nickName", "TestPilot测试用户");
        user.put("password", "Test12345");
        user.put("phonenumber", uniquePhone());
        user.put("status", "0");
        return user;
    }
}
