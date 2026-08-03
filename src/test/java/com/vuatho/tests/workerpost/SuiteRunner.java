package com.vuatho.tests.workerpost;

import com.vuatho.core.TestNgRunner;

/** Runner độc lập cho toàn bộ testcase read-only menu Quản lí bài đăng. */
// Điểm chạy thủ công duy nhất; lớp này chỉ đăng ký test class, không chứa @Test.
public final class SuiteRunner {
    private SuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Bộ test Quản lí bài đăng ERP",
                "Ưu tiên testcase có thao tác và dữ liệu trả về",
                "data-interaction",
                OverviewTest.class,
                NavigationTest.class,
                PaginationTest.class,
                MediaTest.class,
                ResponsiveTest.class,
                ApprovalRejectionTest.class);
    }
}
