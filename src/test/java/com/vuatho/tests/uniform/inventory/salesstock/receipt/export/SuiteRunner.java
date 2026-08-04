package com.vuatho.tests.uniform.inventory.salesstock.receipt.export;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy riêng tính năng Xuất hàng cho nhân sự. */
public final class SuiteRunner {
    private SuiteRunner() { }
    public static void main(String[] args) {
        TestNgRunner.run("Kho bán hàng", "Xuất hàng cho nhân sự", FormTest.class, ValidationTest.class, SubmissionTest.class);
    }
}
