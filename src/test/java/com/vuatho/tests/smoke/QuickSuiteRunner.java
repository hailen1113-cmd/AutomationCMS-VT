package com.vuatho.tests.smoke;

import com.vuatho.tests.crossmenu.SidebarNavigationTest;
import com.vuatho.tests.dashboard.LoginSourceAccessTest;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy nhanh các luồng quan trọng nhất để xác nhận môi trường ERP sẵn sàng.
 */
public final class QuickSuiteRunner {
    private QuickSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Quick Automation Suite",
                "Login, Dashboard and Menu Load Pages",
                LoginSourceAccessTest.class,
                SidebarNavigationTest.class);
    }
}
