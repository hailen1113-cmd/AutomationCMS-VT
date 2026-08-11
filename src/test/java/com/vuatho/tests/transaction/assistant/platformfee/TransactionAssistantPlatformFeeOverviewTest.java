package com.vuatho.tests.transaction.assistant.platformfee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPlatformFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra cấu trúc và dữ liệu tổng quan của tab Thợ phụ. */
public class TransactionAssistantPlatformFeeOverviewTest extends TransactionAssistantPlatformFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPlatformFeeOverviewTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Phí nền tảng - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_003)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_004)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }

}
