package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy luồng smoke từ đăng nhập ERP đến truy cập và kiểm tra màn hình eKYC.
 */
public final class ErpLoginEkycSuiteRunner {
    private ErpLoginEkycSuiteRunner() {
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
                LoginDashboardSourceAccessTest.class,
                EkycWorkbookCatalogTest.class,
                EkycReviewWorkflowTest.class,
                EkycInformationEditWorkflowTest.class,
                EkycInformationClearWorkflowTest.class);
    }
}
