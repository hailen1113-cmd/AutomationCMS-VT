package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/** Chạy toàn bộ testcase Đơn Khách - Thợ. */
public final class CustomerWorkerOrderSuiteRunner {
    private CustomerWorkerOrderSuiteRunner() {
    }

    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Đơn Khách - Thợ",
                "Toàn bộ testcase dữ liệu và xử lý thật",
                "data-interaction",
                CustomerWorkerOrderOverviewTest.class,
                CustomerWorkerOrderSearchFilterTest.class,
                CustomerWorkerOrderViewStatisticsTest.class,
                CustomerWorkerOrderPaginationTest.class,
                CustomerWorkerOrderDetailTest.class,
                CustomerWorkerOrderWorkflowTest.class);
    }
}
