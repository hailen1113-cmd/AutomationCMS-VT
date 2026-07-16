package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

public final class EkycBackendApiContractSuiteRunner {
    private EkycBackendApiContractSuiteRunner() {
    }

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
