package com.vuatho.tests.workerprofile;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy tập trung test danh sách, chi tiết, tìm kiếm và bộ lọc hồ sơ thợ.
 */
public final class ListDetailFilterSuiteRunner {
    private ListDetailFilterSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test danh sach chi tiet bo loc ho so tho ERP",
                "Chay tat ca nhom testcase danh sach chi tiet bo loc ho so tho",
                ListDetailTest.class,
                PaginationTest.class,
                TransactionHistoryTest.class,
                ServiceOrderListTest.class,
                ReferralListTest.class,
                PostListTest.class,
                ViolationPenaltyTest.class,
                ViolationPenaltyReductionTest.class,
                ViolationPenaltyRemovalTest.class,
                FilterTest.class);
    }
}
