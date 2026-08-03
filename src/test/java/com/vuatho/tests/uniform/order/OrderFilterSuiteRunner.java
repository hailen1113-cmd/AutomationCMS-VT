package com.vuatho.tests.uniform.order;

import com.vuatho.core.TestNgRunner;

/**
 * Điểm chạy toàn bộ testcase bộ lọc của menu Đơn hàng Đồng phục.
 *
 * <p>Từng file con vẫn có {@code main()} để chạy độc lập từ VS Code.</p>
 */
public final class OrderFilterSuiteRunner {
    private OrderFilterSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.run(
                "Đơn hàng Đồng phục",
                "Toàn bộ testcase bộ lọc",
                OrderFilterResetTest.class,
                OrderFilterTest.class,
                OrderFilterCombinationTest.class);
    }
}
