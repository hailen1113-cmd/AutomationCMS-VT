package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra tìm kiếm và toàn bộ bộ lọc VT Care. */
public class TransactionInsuranceFilterTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceFilterTest.class,
                "Lịch sử giao dịch", "VT Care - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_005)
    public void searchesUserAndRestoresRowsForDailyFee() {
        verifySearchForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_128)
    public void searchesUserAndRestoresRowsForRefund() {
        verifySearchForSubtype(subtype(26));
    }

    private void verifySearchForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        verifySearchAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_006,
            dataProvider = "insuranceSelectFilters")
    public void dailyFeeSelectFiltersExposeExpectedOptions(String ariaLabel, List<String> expected) {
        verifySelectFilterOptions(subtype(25), ariaLabel, expected);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_129,
            dataProvider = "insuranceSelectFilters")
    public void refundSelectFiltersExposeExpectedOptions(String ariaLabel, List<String> expected) {
        verifySelectFilterOptions(subtype(26), ariaLabel, expected);
    }

    private void verifySelectFilterOptions(TransactionCategoryPage.Subtype subtype,
                                           String ariaLabel, List<String> expected) {
            openInsuranceSubtype(subtype);
            var result = transactionPage.optionsForFilter(ariaLabel);
            Assert.assertEquals(result.ariaLabel(), ariaLabel);
            Assert.assertEquals(result.options(), expected);
            Assert.assertTrue(result.beforeResetUrl().contains(
                    "tab=insurance&type=" + subtype.type()), result.beforeResetUrl());
            Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_017)
    public void everyFilterOptionKeepsSelectedSubtypeContract() {
        verifyEveryFilterOptionKeepsSelectedSubtypeContract(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_130)
    public void everyFilterOptionKeepsRefundSubtypeContract() {
        verifyEveryFilterOptionKeepsSelectedSubtypeContract(subtype(26));
    }

    private void verifyEveryFilterOptionKeepsSelectedSubtypeContract(
            TransactionCategoryPage.Subtype subtype) {
            openInsuranceSubtype(subtype);
            for (Object[] filter : insuranceSelectFilters()) {
                String ariaLabel = String.valueOf(filter[0]);
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) filter[1];
                for (String option : options) {
                    var result = transactionPage.selectOption(ariaLabel, option);
                    Assert.assertTrue(result.selectedText().contains(option), result.selectedText());
                    Assert.assertTrue(!result.rows().isEmpty() || result.empty(), result.pageText());
                    Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()),
                            result.url());
                    Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
                }
                transactionPage.resetAndReadOverviewFilters();
            }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_018)
    public void futureDatesCannotBeSelectedAndDateRequiresSelection() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_105)
    public void futureDatesCannotBeSelectedAndDateRequiresSelectionType26() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(26));
    }

    private void verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var future = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(future.disabled());
        Assert.assertTrue(future.disabledCount() > 0);
        var defaults = advancedPage().dateControlDefaults();
        Assert.assertEquals(defaults.startTime(), "00:00");
        Assert.assertEquals(defaults.endTime(), "23:59");
        Assert.assertTrue(defaults.applyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_019)
    public void singleDayOnlyReturnsRowsFromSelectedDate() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(25));
    }




    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_187)
    public void singleDayOnlyReturnsRowsFromSelectedDateType26() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(26));
    }

    private void verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        String date = result.startDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assert.assertTrue(result.selectedText().contains(date));
        Assert.assertTrue(result.selectedText().contains("00:00"));
        Assert.assertTrue(result.selectedText().contains("23:59"));
        Assert.assertTrue(transactionPage.currentUrl().contains("type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_020)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtype() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_188)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType26() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(26));
    }

    private void verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = transactionPage.unmatchedSearchAndReset("NO_VT_CARE_TRANSACTION_987654321");
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        Assert.assertEquals(result.restored().stream().map(TransactionCategoryPage.TransactionRow::signature).toList(),
                result.before().stream().map(TransactionCategoryPage.TransactionRow::signature).toList());
        Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_059)
    public void filtersPendingRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "trạng thái-filter", "Đang chờ", "Trạng thái"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_131)
    public void filtersPendingRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "trạng thái-filter", "Đang chờ", "Trạng thái"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_060)
    public void filtersSuccessRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "trạng thái-filter", "Thành công", "Trạng thái"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_132)
    public void filtersSuccessRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "trạng thái-filter", "Thành công", "Trạng thái"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_061)
    public void filtersFailedRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "trạng thái-filter", "Thất bại", "Trạng thái"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_133)
    public void filtersFailedRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "trạng thái-filter", "Thất bại", "Trạng thái"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_062)
    public void filtersMomoRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_134)
    public void filtersMomoRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_063)
    public void filtersPaypalRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_135)
    public void filtersPaypalRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_064)
    public void filtersOnepayRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_136)
    public void filtersOnepayRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_065)
    public void filtersBankingRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_137)
    public void filtersBankingRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_066)
    public void filtersNeoxRowsAcrossBothSubtypes() { verifySingleFilter(subtype(25), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_138)
    public void filtersNeoxRowsAcrossBothSubtypesType26() { verifySingleFilter(subtype(26), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_067)
    public void filtersPendingMomoMatrixCell() { verifyMatrix(subtype(25), "Đang chờ", "MOMO"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_139)
    public void filtersPendingMomoMatrixCellType26() { verifyMatrix(subtype(26), "Đang chờ", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_068)
    public void filtersPendingPaypalMatrixCell() { verifyMatrix(subtype(25), "Đang chờ", "PAYPAL"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_140)
    public void filtersPendingPaypalMatrixCellType26() { verifyMatrix(subtype(26), "Đang chờ", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_069)
    public void filtersPendingOnepayMatrixCell() { verifyMatrix(subtype(25), "Đang chờ", "ONEPAY"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_141)
    public void filtersPendingOnepayMatrixCellType26() { verifyMatrix(subtype(26), "Đang chờ", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_070)
    public void filtersPendingBankingMatrixCell() { verifyMatrix(subtype(25), "Đang chờ", "BANKING"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_142)
    public void filtersPendingBankingMatrixCellType26() { verifyMatrix(subtype(26), "Đang chờ", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_071)
    public void filtersPendingNeoxMatrixCell() { verifyMatrix(subtype(25), "Đang chờ", "NEOX"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_143)
    public void filtersPendingNeoxMatrixCellType26() { verifyMatrix(subtype(26), "Đang chờ", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_072)
    public void filtersSuccessMomoMatrixCell() { verifyMatrix(subtype(25), "Thành công", "MOMO"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_144)
    public void filtersSuccessMomoMatrixCellType26() { verifyMatrix(subtype(26), "Thành công", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_073)
    public void filtersSuccessPaypalMatrixCell() { verifyMatrix(subtype(25), "Thành công", "PAYPAL"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_145)
    public void filtersSuccessPaypalMatrixCellType26() { verifyMatrix(subtype(26), "Thành công", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_074)
    public void filtersSuccessOnepayMatrixCell() { verifyMatrix(subtype(25), "Thành công", "ONEPAY"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_146)
    public void filtersSuccessOnepayMatrixCellType26() { verifyMatrix(subtype(26), "Thành công", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_075)
    public void filtersSuccessBankingMatrixCell() { verifyMatrix(subtype(25), "Thành công", "BANKING"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_147)
    public void filtersSuccessBankingMatrixCellType26() { verifyMatrix(subtype(26), "Thành công", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_076)
    public void filtersSuccessNeoxMatrixCell() { verifyMatrix(subtype(25), "Thành công", "NEOX"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_148)
    public void filtersSuccessNeoxMatrixCellType26() { verifyMatrix(subtype(26), "Thành công", "NEOX"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_077)
    public void filtersFailedMomoMatrixCell() { verifyMatrix(subtype(25), "Thất bại", "MOMO"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_149)
    public void filtersFailedMomoMatrixCellType26() { verifyMatrix(subtype(26), "Thất bại", "MOMO"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_078)
    public void filtersFailedPaypalMatrixCell() { verifyMatrix(subtype(25), "Thất bại", "PAYPAL"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_150)
    public void filtersFailedPaypalMatrixCellType26() { verifyMatrix(subtype(26), "Thất bại", "PAYPAL"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_079)
    public void filtersFailedOnepayMatrixCell() { verifyMatrix(subtype(25), "Thất bại", "ONEPAY"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_151)
    public void filtersFailedOnepayMatrixCellType26() { verifyMatrix(subtype(26), "Thất bại", "ONEPAY"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_080)
    public void filtersFailedBankingMatrixCell() { verifyMatrix(subtype(25), "Thất bại", "BANKING"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_152)
    public void filtersFailedBankingMatrixCellType26() { verifyMatrix(subtype(26), "Thất bại", "BANKING"); }
    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_081)
    public void filtersFailedNeoxMatrixCell() { verifyMatrix(subtype(25), "Thất bại", "NEOX"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_153)
    public void filtersFailedNeoxMatrixCellType26() { verifyMatrix(subtype(26), "Thất bại", "NEOX"); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_082)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossBothSubtypes() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_106)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossBothSubtypesType26() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossBothSubtypesForSubtype(subtype(26));
    }

    private void verifyResetClearsSuccessPaypalAndSelectedDayAcrossBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().resetStatusGatewayAndDate("Thành công", "PAYPAL");
        Assert.assertTrue(result.status().contains("Chọn trạng thái"), result.status());
        Assert.assertTrue(result.gateway().contains("Chọn cổng thanh toán"), result.gateway());
        Assert.assertTrue(result.date().contains("Chọn khoảng ngày giờ"), result.date());
        Assert.assertEquals(result.page(), 1);
        Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_083)
    public void successFilterUpdatesTotalAndPaginationAcrossBothSubtypes() {
        verifySuccessFilterUpdatesTotalAndPaginationAcrossBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_107)
    public void successFilterUpdatesTotalAndPaginationAcrossBothSubtypesType26() {
        verifySuccessFilterUpdatesTotalAndPaginationAcrossBothSubtypesForSubtype(subtype(26));
    }

    private void verifySuccessFilterUpdatesTotalAndPaginationAcrossBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().totalAndPaginationAfterStatusFilter();
        Assert.assertTrue(result.selectedStatus().contains("Thành công"),
                result.selectedStatus());
        Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
        Assert.assertEquals(result.rows().size(), Math.min(result.afterTotal(), 20));
        Assert.assertEquals(result.afterPagination(), result.afterTotal() > 20);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_084)
    public void successFilterPersistsAfterOpeningDetailAcrossBothSubtypes() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_108)
    public void successFilterPersistsAfterOpeningDetailAcrossBothSubtypesType26() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossBothSubtypesForSubtype(subtype(26));
    }

    private void verifySuccessFilterPersistsAfterOpeningDetailAcrossBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().filterPersistsAfterDetail("Thành công");
        Assert.assertTrue(result.selectedStatus().contains("Thành công"),
                result.selectedStatus());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertTrue(result.listUrlBefore().contains("type=" + subtype.type()));
        String closedUrl = result.browserLocationAfterClose();
        Assert.assertTrue(closedUrl.contains("tab=insurance"), closedUrl);
        Assert.assertFalse(closedUrl.contains("id="), closedUrl);
        if (subtype.type() == 25) {
            Assert.assertTrue(!closedUrl.contains("type=")
                            || closedUrl.contains("type=25"),
                    "Đóng chi tiết làm mất subtype Trừ phí VT Care: " + closedUrl);
        } else {
            Assert.assertTrue(closedUrl.contains("type=" + subtype.type()),
                    "Đóng chi tiết làm mất subtype " + subtype.label() + ": " + closedUrl);
        }
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_085)
    public void combinesSuccessPaypalAndFirstRowDayAcrossBothSubtypes() {
        verifyCombinesSuccessPaypalAndFirstRowDayAcrossBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_109)
    public void combinesSuccessPaypalAndFirstRowDayAcrossBothSubtypesType26() {
        verifyCombinesSuccessPaypalAndFirstRowDayAcrossBothSubtypesForSubtype(subtype(26));
    }

    private void verifyCombinesSuccessPaypalAndFirstRowDayAcrossBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().combineStatusGatewayAndDate("Thành công", "PAYPAL");
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertTrue(result.selectedGateway().contains("PAYPAL"));
        if (result.rows().isEmpty()) {
            Assert.assertTrue(result.empty(), result.pageText());
        } else {
            Assert.assertTrue(result.rows().stream().allMatch(row ->
                    row.status().equals("Thành công") && row.gateway().equals("PAYPAL")
                            && row.createdAt().toLocalDate().equals(result.date())));
        }
        Assert.assertTrue(result.url().contains("type=" + subtype.type()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_086)
    public void dismissingSelectedDateWithoutApplyKeepsRowsAcrossBothSubtypes() {
        verifyDismissingSelectedDateWithoutApplyKeepsRowsAcrossBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_110)
    public void dismissingSelectedDateWithoutApplyKeepsRowsAcrossBothSubtypesType26() {
        verifyDismissingSelectedDateWithoutApplyKeepsRowsAcrossBothSubtypesForSubtype(subtype(26));
    }

    private void verifyDismissingSelectedDateWithoutApplyKeepsRowsAcrossBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().dismissDateWithoutApply();
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_087)
    public void clearingAppliedDayRestoresTotalAcrossBothSubtypes() {
        verifyClearingAppliedDayRestoresTotalAcrossBothSubtypesForSubtype(subtype(25));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_111)
    public void clearingAppliedDayRestoresTotalAcrossBothSubtypesType26() {
        verifyClearingAppliedDayRestoresTotalAcrossBothSubtypesForSubtype(subtype(26));
    }

    private void verifyClearingAppliedDayRestoresTotalAcrossBothSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openInsuranceSubtype(subtype);
        var result = advancedPage().clearAppliedDateFilter();
        Assert.assertEquals(result.restoredTotal(), result.originalTotal());
        Assert.assertTrue(result.clearControlGone());
        Assert.assertTrue(result.selectedDate().contains("Chọn khoảng ngày giờ"));
    }

    private void verifySingleFilter(TransactionCategoryPage.Subtype subtype,
                                    String ariaLabel, String option, String column) {
            openInsuranceSubtype(subtype);
            var result = transactionPage.selectOption(ariaLabel, option);
            assertRowsOrEmpty(result, column, option);
            Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
    }

    private void verifyMatrix(TransactionCategoryPage.Subtype subtype,
                              String status, String gateway) {
            openInsuranceSubtype(subtype);
            transactionPage.selectOption("trạng thái-filter", status);
            var result = transactionPage.selectOption("cổng thanh toán-filter", gateway);
            if (result.rows().isEmpty()) {
                Assert.assertTrue(result.empty(), result.pageText());
            } else {
                Assert.assertTrue(result.rows().stream().allMatch(row ->
                        row.value("Trạng thái").equals(status)
                                && row.value("Cổng thanh toán").equals(gateway)),
                        result.pageText());
            }
            Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
    }

    private void assertRowsOrEmpty(TransactionCategoryPage.SelectOptionSnapshot result,
                                   String column, String expected) {
        if (result.rows().isEmpty()) {
            Assert.assertTrue(result.empty(), result.pageText());
            Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"), result.pageText());
        } else {
            Assert.assertTrue(result.rows().stream()
                    .allMatch(row -> row.value(column).equals(expected)), result.pageText());
        }
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
