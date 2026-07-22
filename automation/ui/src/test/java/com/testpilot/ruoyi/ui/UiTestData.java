package com.testpilot.ruoyi.ui;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

final class UiTestData {
    private static final String RUN_ID = compactUuid().substring(0, 6);
    private static final AtomicLong PHONE_SEQUENCE = new AtomicLong(System.currentTimeMillis());

    private UiTestData() {
    }

    static String uniqueUserName() {
        return "ui" + RUN_ID + compactUuid().substring(0, 10);
    }

    static String uniqueNickName() {
        return "UI-" + RUN_ID + "-" + compactUuid().substring(0, 6);
    }

    static String uniquePhone() {
        long suffix = Math.floorMod(PHONE_SEQUENCE.getAndIncrement(), 1_000_000_000L);
        return "13" + String.format("%09d", suffix);
    }

    static String uniquePassword() {
        return "T" + compactUuid().substring(0, 10) + "9";
    }

    private static String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
