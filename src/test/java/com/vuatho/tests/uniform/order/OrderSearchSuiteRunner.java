package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;

/** Điểm chạy độc lập toàn bộ testcase search của Đơn hàng Đồng phục. */
public final class OrderSearchSuiteRunner {
    private OrderSearchSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Đơn hàng Đồng phục",
                "Toàn bộ testcase tìm kiếm",
                OrderSearchTest.class);
    }
}
