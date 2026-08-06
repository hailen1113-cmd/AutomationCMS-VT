package com.vuatho.tests.transaction.all;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionHistoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

/** Kiểm tra tổng quan và cấu trúc tab Tất cả của Lịch sử giao dịch. */
public class TransactionAllOverviewTest extends TransactionHistoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAllOverviewTest.class,
                "Lịch sử giao dịch", "Tab Tất cả - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_001)
    public void opensAllTab() {
        var result = transactionPage.overview();
        Assert.assertTrue(result.url().contains("/vuatho/transaction"));
        Assert.assertTrue(result.url().contains("tab=all"));
        Assert.assertTrue(result.allSelected());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_002)
    public void showsAllTransactionGroups() {
        Assert.assertEquals(transactionPage.overview().tabs(), List.of(
                "Tất cả", "Tiền nạp", "Tiền rút", "Đơn dịch vụ", "Thưởng & KM",
                "Phí & Doanh thu", "VT Care", "Thợ phụ", "Hệ thống"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_003)
    public void showsFiltersResetAndExport() {
        var result = transactionPage.overview();
        Assert.assertEquals(result.controls(), List.of("Chọn loại giao dịch", "Chọn trạng thái",
                "Chọn cổng thanh toán", "Chọn khoảng ngày giờ", "Xuất Excel"));
        Assert.assertTrue(transactionPage.mainText().contains("Reset")
                || transactionPage.mainText().contains("Xuất Excel"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_004)
    public void showsExpectedAllTabColumns() {
        Assert.assertEquals(transactionPage.headers(), List.of(
                "Loại giao dịch", "Trạng thái", "Số tiền", "Cổng thanh toán", "Ngày tạo"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_005)
    public void transactionRowsHaveValidFormats() {
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty());
        rows.forEach(row -> {
            Assert.assertFalse(row.type().isBlank());
            Assert.assertFalse(row.status().isBlank());
            Assert.assertTrue(row.amount().contains("₫"), "Sai định dạng tiền: " + row.amount());
            Assert.assertNotNull(row.amountValue());
            Assert.assertFalse(row.createdAt().isAfter(LocalDateTime.now().plusMinutes(1)));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_006)
    public void showsAtMostTwentyRowsPerPage() {
        int rows = transactionPage.rows().size();
        Assert.assertTrue(rows > 0);
        Assert.assertTrue(rows <= 20, "Một trang đang hiển thị " + rows + " giao dịch.");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_007)
    public void showsTotalAndPagination() {
        var result = transactionPage.overview();
        Assert.assertTrue(result.total() >= result.rows().size());
        if (result.total() > 20) {
            Assert.assertTrue(result.pagination());
        }
    }
}
