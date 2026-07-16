package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class UserProfileWorkflowSuiteRunner {
    private UserProfileWorkflowSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test ho so nguoi dung ERP",
                "Chay tat ca nhom testcase ho so nguoi dung",
                UserProfileSearchTest.class,
                UserProfileDetailInteractionTest.class,
                UserProfileNameUpdateWorkflowTest.class,
                UserProfileAvatarUpdateWorkflowTest.class);
    }
}
