package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy nhanh các luồng quan trọng nhất để xác nhận môi trường ERP sẵn sàng.
 */
public final class ErpQuickSmokeSuiteRunner {
    private ErpQuickSmokeSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Quick Automation Suite",
                "Login, Dashboard and Menu Load Pages",
                LoginDashboardSourceAccessTest.class,
                CrossMenuSidebarNavigationTest.class);
    }
}
