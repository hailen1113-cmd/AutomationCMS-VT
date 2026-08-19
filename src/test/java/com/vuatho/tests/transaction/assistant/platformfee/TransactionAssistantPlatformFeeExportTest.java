package com.vuatho.tests.transaction.assistant.platformfee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPlatformFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra xuất Excel của tab Thợ phụ. */
public class TransactionAssistantPlatformFeeExportTest extends TransactionAssistantPlatformFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPlatformFeeExportTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Phí nền tảng - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_010)
    public void exportsCurrentSubtype() {
        verifyExport();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_028)
    public void exportsFilteredStatusToARealFile() {
        verifyExportAfterStatus(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_090)
    public void exportsSearchResults() {
        verifyExportAfterSearch(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_091)
    public void exportsGatewayResults() {
        verifyExportAfterGateway(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_092)
    public void exportsSelectedDay() {
        verifyExportAfterDate(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_093)
    public void exportsCombinedFilters() {
        verifyExportAfterCombinedFilters(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_098)
    public void exportsPlatformPendingMomo() { matrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_099)
    public void exportsPlatformPendingPaypal() { matrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_100)
    public void exportsPlatformPendingOnepay() { matrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_101)
    public void exportsPlatformPendingBanking() { matrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_102)
    public void exportsPlatformPendingNeox() { matrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_103)
    public void exportsPlatformSuccessMomo() { matrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_104)
    public void exportsPlatformSuccessPaypal() { matrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_105)
    public void exportsPlatformSuccessOnepay() { matrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_106)
    public void exportsPlatformSuccessBanking() { matrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_107)
    public void exportsPlatformSuccessNeox() { matrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_108)
    public void exportsPlatformFailedMomo() { matrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_109)
    public void exportsPlatformFailedPaypal() { matrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_110)
    public void exportsPlatformFailedOnepay() { matrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_111)
    public void exportsPlatformFailedBanking() { matrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_112)
    public void exportsPlatformFailedNeox() { matrix("Thất bại", "NEOX"); }

    private void matrix(String status, String gateway) {
        verifyExportAfterStatusAndGateway(subtype(), status, gateway);
    }
}
