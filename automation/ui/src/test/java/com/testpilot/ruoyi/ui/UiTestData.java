package com.testpilot.ruoyi.ui;

import java.util.concurrent.ThreadLocalRandom;

final class UiTestData {
    private UiTestData() {
    }

    static String uniqueUserName() {
        long suffix = System.currentTimeMillis() % 1_000_000_000L;
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "ui" + suffix + random;
    }

    static String uniquePhone() {
        int secondDigit = ThreadLocalRandom.current().nextInt(3, 10);
        long tail = ThreadLocalRandom.current().nextLong(1_000_000_000L);
        return "1" + secondDigit + String.format("%09d", tail);
    }
}
