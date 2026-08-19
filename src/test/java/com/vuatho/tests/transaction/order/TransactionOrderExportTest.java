package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.annotations.Test;

/** Kiểm tra xuất Excel độc lập, theo bộ lọc và ma trận của Đơn dịch vụ. */
public class TransactionOrderExportTest extends TransactionOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderExportTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Xuất Excel");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_010)
    public void exportsCurrentServiceOrder() { verifyExportForSubtype(2, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_261)
    public void exportsCurrentBusinessPayment() { verifyExportForSubtype(22, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_262)
    public void exportsCurrentWarrantyFee() { verifyExportForSubtype(24, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_263)
    public void exportsCurrentWarrantyCollection() { verifyExportForSubtype(36, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_264)
    public void exportsCurrentWarrantyPayment() { verifyExportForSubtype(37, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_265)
    public void exportsCurrentPenaltyFee() { verifyExportForSubtype(15, this::verifyExport); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_011)
    public void exportsSearchServiceOrder() { verifyExportForSubtype(2, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_266)
    public void exportsSearchBusinessPayment() { verifyExportForSubtype(22, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_267)
    public void exportsSearchWarrantyFee() { verifyExportForSubtype(24, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_268)
    public void exportsSearchWarrantyCollection() { verifyExportForSubtype(36, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_269)
    public void exportsSearchWarrantyPayment() { verifyExportForSubtype(37, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_270)
    public void exportsSearchPenaltyFee() { verifyExportForSubtype(15, this::verifyExportAfterSearch); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_012)
    public void exportsSuccessServiceOrder() { verifyExportForSubtype(2, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_271)
    public void exportsSuccessBusinessPayment() { verifyExportForSubtype(22, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_272)
    public void exportsSuccessWarrantyFee() { verifyExportForSubtype(24, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_273)
    public void exportsSuccessWarrantyCollection() { verifyExportForSubtype(36, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_274)
    public void exportsSuccessWarrantyPayment() { verifyExportForSubtype(37, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_275)
    public void exportsSuccessPenaltyFee() { verifyExportForSubtype(15, this::verifyExportAfterStatus); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_013)
    public void exportsPaypalServiceOrder() { verifyExportForSubtype(2, this::verifyExportAfterGateway); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_276)
    public void exportsPaypalBusinessPayment() { verifyExportForSubtype(22, this::verifyExportAfterGateway); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_277)
    public void exportsPaypalWarrantyFee() { verifyExportForSubtype(24, this::verifyExportAfterGateway); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_278)
    public void exportsPaypalWarrantyCollection() { verifyExportForSubtype(36, this::verifyExportAfterGateway); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_279)
    public void exportsPaypalWarrantyPayment() { verifyExportForSubtype(37, this::verifyExportAfterGateway); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_280)
    public void exportsPaypalPenaltyFee() { verifyExportForSubtype(15, this::verifyExportAfterGateway); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_014)
    public void exportsSelectedDayServiceOrder() { verifyExportForSubtype(2, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_281)
    public void exportsSelectedDayBusinessPayment() { verifyExportForSubtype(22, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_282)
    public void exportsSelectedDayWarrantyFee() { verifyExportForSubtype(24, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_283)
    public void exportsSelectedDayWarrantyCollection() { verifyExportForSubtype(36, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_284)
    public void exportsSelectedDayWarrantyPayment() { verifyExportForSubtype(37, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_285)
    public void exportsSelectedDayPenaltyFee() { verifyExportForSubtype(15, this::verifyExportAfterDate); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_015)
    public void exportsCombinedServiceOrder() { verifyExportForSubtype(2, this::verifyExportAfterCombinedFilters); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_286)
    public void exportsCombinedBusinessPayment() { verifyExportForSubtype(22, this::verifyExportAfterCombinedFilters); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_287)
    public void exportsCombinedWarrantyFee() { verifyExportForSubtype(24, this::verifyExportAfterCombinedFilters); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_288)
    public void exportsCombinedWarrantyCollection() { verifyExportForSubtype(36, this::verifyExportAfterCombinedFilters); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_289)
    public void exportsCombinedWarrantyPayment() { verifyExportForSubtype(37, this::verifyExportAfterCombinedFilters); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_290)
    public void exportsCombinedPenaltyFee() { verifyExportForSubtype(15, this::verifyExportAfterCombinedFilters); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_016)
    public void exportsPendingMomoMatrixCell() { verifyMatrix("Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_017)
    public void exportsPendingPaypalMatrixCell() { verifyMatrix("Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_018)
    public void exportsPendingOnepayMatrixCell() { verifyMatrix("Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_019)
    public void exportsPendingBankingMatrixCell() { verifyMatrix("Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_020)
    public void exportsPendingNeoxMatrixCell() { verifyMatrix("Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_021)
    public void exportsSuccessMomoMatrixCell() { verifyMatrix("Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_022)
    public void exportsSuccessPaypalMatrixCell() { verifyMatrix("Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_023)
    public void exportsSuccessOnepayMatrixCell() { verifyMatrix("Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_024)
    public void exportsSuccessBankingMatrixCell() { verifyMatrix("Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_025)
    public void exportsSuccessNeoxMatrixCell() { verifyMatrix("Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_026)
    public void exportsFailedMomoMatrixCell() { verifyMatrix("Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_027)
    public void exportsFailedPaypalMatrixCell() { verifyMatrix("Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_028)
    public void exportsFailedOnepayMatrixCell() { verifyMatrix("Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_029)
    public void exportsFailedBankingMatrixCell() { verifyMatrix("Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_030)
    public void exportsFailedNeoxMatrixCell() { verifyMatrix("Thất bại", "NEOX"); }

    private void verifyMatrix(String status, String gateway) {
        verifyExportMatrixCellOnFirstSubtype(status, gateway);
    }
}
