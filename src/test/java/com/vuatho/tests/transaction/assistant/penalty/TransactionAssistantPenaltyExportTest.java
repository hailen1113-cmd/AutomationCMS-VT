package com.vuatho.tests.transaction.assistant.penalty;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionAssistantPenaltyTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra xuất Excel của loại Tiền phạt. */
public class TransactionAssistantPenaltyExportTest extends TransactionAssistantPenaltyTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantPenaltyExportTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Tiền phạt - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_085)
    public void exportsCurrentSubtype() {
        verifyExport(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_086)
    public void exportsFilteredStatusToARealFile() {
        verifyExportAfterStatus(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_094)
    public void exportsSearchResults() {
        verifyExportAfterSearch(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_095)
    public void exportsGatewayResults() {
        verifyExportAfterGateway(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_096)
    public void exportsSelectedDay() {
        verifyExportAfterDate(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_097)
    public void exportsCombinedFilters() {
        verifyExportAfterCombinedFilters(subtype());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_113)
    public void exportsPenaltyPendingMomo() { matrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_114)
    public void exportsPenaltyPendingPaypal() { matrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_115)
    public void exportsPenaltyPendingOnepay() { matrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_116)
    public void exportsPenaltyPendingBanking() { matrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_117)
    public void exportsPenaltyPendingNeox() { matrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_118)
    public void exportsPenaltySuccessMomo() { matrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_119)
    public void exportsPenaltySuccessPaypal() { matrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_120)
    public void exportsPenaltySuccessOnepay() { matrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_121)
    public void exportsPenaltySuccessBanking() { matrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_122)
    public void exportsPenaltySuccessNeox() { matrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_123)
    public void exportsPenaltyFailedMomo() { matrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_124)
    public void exportsPenaltyFailedPaypal() { matrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_125)
    public void exportsPenaltyFailedOnepay() { matrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_126)
    public void exportsPenaltyFailedBanking() { matrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_127)
    public void exportsPenaltyFailedNeox() { matrix("Thất bại", "NEOX"); }

    private void matrix(String status, String gateway) {
        verifyExportAfterStatusAndGateway(subtype(), status, gateway);
    }
}
