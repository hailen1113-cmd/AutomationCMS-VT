package com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy riêng tính năng Xuất hàng cho nhân sự. */
public final class StaffExportSuiteRunner {
    private StaffExportSuiteRunner() { }
    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Xuất hàng cho nhân sự", StaffExportFormTest.class,
                StaffExportValidationTest.class, StaffExportSubmissionTest.class);
    }
}
