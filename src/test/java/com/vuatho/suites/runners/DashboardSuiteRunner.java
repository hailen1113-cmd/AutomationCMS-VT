package com.vuatho.suites.runners;

import com.vuatho.core.TestNgRunner;
import com.vuatho.tests.dashboard.*;

/**
 * Chạy tập trung toàn bộ nhóm kiểm thử giao diện và dữ liệu Dashboard.
 */
public final class DashboardSuiteRunner {
    private DashboardSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test Dashboard ERP",
                "Chay tat ca nhom testcase Dashboard",
                DashboardCoreUiTest.class,
                DashboardSummaryCardsTest.class,
                DashboardWorkbookUiTest.class,
                DashboardLogoutTest.class);
    }
}
