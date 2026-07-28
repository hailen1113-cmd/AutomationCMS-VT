package com.vuatho.tests.customerworkerorder;

import com.vuatho.core.TestNgRunner;

/**
 * Entry point chạy toàn bộ testcase có tương tác dữ liệu của Đơn Khách - Thợ.
 *
 * <p>Suite gom các file theo chức năng để bàn giao có thể chạy một lần. Danh
 * sách có cả case chỉ đọc và {@link CustomerWorkerOrderWorkflowTest} chứa case
 * mutation thật; không chạy suite trên dữ liệu production.</p>
 */
public final class CustomerWorkerOrderSuiteRunner {
    /** Ngăn khởi tạo vì lớp chỉ cung cấp entry point tĩnh. */
    private CustomerWorkerOrderSuiteRunner() {
    }

    /** Chạy toàn bộ group data-interaction, bao gồm cả mutation ở cuối suite. */
    public static void main(String[] args) {
        TestNgRunner.runGroup(
                "Đơn Khách - Thợ",
                "Toàn bộ testcase dữ liệu và xử lý thật",
                "data-interaction",
                // Nhóm đọc/đối soát dữ liệu, không đổi tiến trình đơn.
                CustomerWorkerOrderOverviewTest.class,
                CustomerWorkerOrderSearchFilterTest.class,
                CustomerWorkerOrderViewModeTest.class,
                CustomerWorkerOrderExportTest.class,
                CustomerWorkerOrderStatusStatisticsTest.class,
                CustomerWorkerOrderWarrantyStatisticsTest.class,
                CustomerWorkerOrderPaginationTest.class,
                CustomerWorkerOrderDetailTest.class,
                CustomerWorkerOrderAdvancePopupTest.class,
                // Đặt cuối vì nhóm này xác nhận chuyển bước và hủy đơn thật.
                CustomerWorkerOrderWorkflowTest.class);
    }
}
