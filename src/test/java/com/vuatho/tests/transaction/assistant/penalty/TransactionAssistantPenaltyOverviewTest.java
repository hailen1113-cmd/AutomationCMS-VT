package com.vuatho.tests.transaction.assistant.penalty;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPenaltyTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra cấu trúc và dữ liệu tổng quan của loại Tiền phạt. */
public class TransactionAssistantPenaltyOverviewTest extends TransactionAssistantPenaltyTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPenaltyOverviewTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Tiền phạt - Tổng quan");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_050)
    public void showsExpectedFiltersAndColumns() {
        verifyLayout(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_051)
    public void rowsHaveValidFormats() {
        verifyRowFormats();
    }
}
