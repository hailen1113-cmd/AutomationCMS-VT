package com.vuatho.tests.ekyc;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy tập trung toàn bộ test hợp đồng API backend eKYC.
 */
public final class BackendApiContractSuiteRunner {
    private BackendApiContractSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test API contract eKYC ERP",
                "Chay tat ca nhom API contract eKYC",
                DashboardApiContractTest.class,
                ListApiContractTest.class,
                DetailApiContractTest.class,
                SecurityApiContractTest.class,
                MutationApiContractTest.class);
    }
}
