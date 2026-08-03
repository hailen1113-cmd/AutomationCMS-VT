package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy toàn bộ testcase form, validation và tạo đơn thật. */
public final class OrderCreateSuiteRunner {
    private OrderCreateSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Đơn hàng Đồng phục",
                "Toàn bộ testcase tạo đơn",
                OrderCreateFormTest.class,
                OrderCreateValidationTest.class,
                OrderCreateSubmissionTest.class);
    }
}
