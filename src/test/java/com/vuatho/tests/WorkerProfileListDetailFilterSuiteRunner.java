package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy tập trung test danh sách, chi tiết, tìm kiếm và bộ lọc hồ sơ thợ.
 */
public final class WorkerProfileListDetailFilterSuiteRunner {
    private WorkerProfileListDetailFilterSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test danh sach chi tiet bo loc ho so tho ERP",
                "Chay tat ca nhom testcase danh sach chi tiet bo loc ho so tho",
                WorkerProfileListDetailTest.class,
                WorkerProfilePaginationTest.class,
                WorkerProfileTransactionHistoryTest.class,
                WorkerProfileServiceOrderListTest.class,
                WorkerProfileReferralListTest.class,
                WorkerProfilePostListTest.class,
                WorkerProfileViolationPenaltyTest.class,
                WorkerProfileViolationPenaltyReductionTest.class,
                WorkerProfileViolationPenaltyRemovalTest.class,
                WorkerProfileFilterTest.class);
    }
}
