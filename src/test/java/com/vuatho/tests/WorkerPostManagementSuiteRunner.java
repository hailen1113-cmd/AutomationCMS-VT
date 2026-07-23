package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/** Runner độc lập cho toàn bộ testcase read-only menu Quản lí bài đăng. */
// Điểm chạy thủ công duy nhất; lớp này chỉ đăng ký test class, không chứa @Test.
public final class WorkerPostManagementSuiteRunner {
    private WorkerPostManagementSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Bộ test Quản lí bài đăng ERP",
                "Ưu tiên testcase có thao tác và dữ liệu trả về",
                "data-interaction",
                WorkerPostManagementOverviewTest.class,
                WorkerPostManagementNavigationTest.class,
                WorkerPostManagementPaginationTest.class,
                WorkerPostManagementMediaTest.class,
                WorkerPostManagementResponsiveTest.class,
                WorkerPostManagementApprovalRejectionTest.class);
    }
}
