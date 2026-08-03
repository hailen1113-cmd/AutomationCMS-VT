package com.vuatho.tests.ekyc;

import com.vuatho.tests.dashboard.LoginSourceAccessTest;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy luồng smoke từ đăng nhập ERP đến truy cập và kiểm tra màn hình eKYC.
 */
public final class LoginSuiteRunner {
    private LoginSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        runSuite();
    }

    /**
     * Thực thi suite trong luồng kiểm thử.
     */
    public static void runSuite() {
        TestNgRunner.run(
                "ERP Login and eKYC Suite",
                "Login and eKYC Checks",
                LoginSourceAccessTest.class,
                WorkbookCatalogTest.class,
                ReviewWorkflowTest.class,
                InformationEditWorkflowTest.class,
                InformationClearWorkflowTest.class);
    }
}
