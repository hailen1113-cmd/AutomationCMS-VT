package com.vuatho.suites.runners;

import com.vuatho.core.TestNgRunner;
import com.vuatho.tests.ekyc.api.*;

/**
 * Chạy tập trung toàn bộ test hợp đồng API backend eKYC.
 */
public final class EkycBackendApiContractSuiteRunner {
    private EkycBackendApiContractSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test API contract eKYC ERP",
                "Chay tat ca nhom API contract eKYC",
                EkycDashboardApiContractTest.class,
                EkycListApiContractTest.class,
                EkycDetailApiContractTest.class,
                EkycSecurityApiContractTest.class,
                EkycMutationApiContractTest.class);
    }
}
