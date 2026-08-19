package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Kiểm tra tìm kiếm, bộ lọc và Reset của sáu loại Đơn dịch vụ. */
public class TransactionOrderFilterTest extends TransactionOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderFilterTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_005)
    public void searchesUserAndRestoresRowsAcrossAllSubtypes() {
        verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_185)
    public void searchesUserAndRestoresRowsAcrossAllSubtypesType22() {
        verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_186)
    public void searchesUserAndRestoresRowsAcrossAllSubtypesType24() {
        verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_187)
    public void searchesUserAndRestoresRowsAcrossAllSubtypesType36() {
        verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_188)
    public void searchesUserAndRestoresRowsAcrossAllSubtypesType37() {
        verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_189)
    public void searchesUserAndRestoresRowsAcrossAllSubtypesType15() {
        verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifySearchesUserAndRestoresRowsAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        verifySearchAndReset(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_006)
    public void selectFiltersExposeExpectedOptionsAcrossAllSubtypes() {
        verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_190)
    public void selectFiltersExposeExpectedOptionsAcrossAllSubtypesType22() {
        verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_191)
    public void selectFiltersExposeExpectedOptionsAcrossAllSubtypesType24() {
        verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_192)
    public void selectFiltersExposeExpectedOptionsAcrossAllSubtypesType36() {
        verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_193)
    public void selectFiltersExposeExpectedOptionsAcrossAllSubtypesType37() {
        verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_194)
    public void selectFiltersExposeExpectedOptionsAcrossAllSubtypesType15() {
        verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifySelectFiltersExposeExpectedOptionsAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        for (Object[] filter : orderSelectFilters()) {
            String ariaLabel = String.valueOf(filter[0]);
            @SuppressWarnings("unchecked")
            List<String> expected = (List<String>) filter[1];
            var result = transactionPage.optionsForFilter(ariaLabel);
            Assert.assertEquals(result.options(), expected);
            Assert.assertTrue(result.beforeResetUrl().contains(
                    "tab=order&type=" + subtype.type()), result.beforeResetUrl());
            Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_041)
    public void futureDatesCannotBeSelectedAndDateRequiresSelection() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_195)
    public void futureDatesCannotBeSelectedAndDateRequiresSelectionType22() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_196)
    public void futureDatesCannotBeSelectedAndDateRequiresSelectionType24() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_197)
    public void futureDatesCannotBeSelectedAndDateRequiresSelectionType36() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_198)
    public void futureDatesCannotBeSelectedAndDateRequiresSelectionType37() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_199)
    public void futureDatesCannotBeSelectedAndDateRequiresSelectionType15() {
        verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(subtype(15));
    }

    private void verifyFutureDatesCannotBeSelectedAndDateRequiresSelectionForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var future = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(future.disabled());
        Assert.assertTrue(future.disabledCount() > 0);
        var defaults = advancedPage().dateControlDefaults();
        Assert.assertEquals(defaults.startTime(), "00:00");
        Assert.assertEquals(defaults.endTime(), "23:59");
        Assert.assertTrue(defaults.applyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_042)
    public void singleDayOnlyReturnsRowsFromSelectedDate() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_200)
    public void singleDayOnlyReturnsRowsFromSelectedDateType22() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_201)
    public void singleDayOnlyReturnsRowsFromSelectedDateType24() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_202)
    public void singleDayOnlyReturnsRowsFromSelectedDateType36() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_203)
    public void singleDayOnlyReturnsRowsFromSelectedDateType37() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_204)
    public void singleDayOnlyReturnsRowsFromSelectedDateType15() {
        verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(subtype(15));
    }

    private void verifySingleDayOnlyReturnsRowsFromSelectedDateForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        String date = result.startDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assert.assertTrue(result.selectedText().contains(date));
        Assert.assertTrue(result.selectedText().contains("00:00"));
        Assert.assertTrue(result.selectedText().contains("23:59"));
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_043)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtype() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_205)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType22() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_206)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType24() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_207)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType36() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_208)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType37() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_209)
    public void unmatchedSearchShowsEmptyStateAndResetRestoresSubtypeType15() {
        verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(subtype(15));
    }

    private void verifyUnmatchedSearchShowsEmptyStateAndResetRestoresSubtypeForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = transactionPage.unmatchedSearchAndReset(
                "NO_ORDER_TRANSACTION_987654321");
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        Assert.assertEquals(signatures(result.restored()), signatures(result.before()));
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_044)
    public void filtersPendingRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "trạng thái-filter", "Đang chờ", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_220)
    public void filtersPendingRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "trạng thái-filter", "Đang chờ", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_221)
    public void filtersPendingRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "trạng thái-filter", "Đang chờ", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_222)
    public void filtersPendingRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "trạng thái-filter", "Đang chờ", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_223)
    public void filtersPendingRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "trạng thái-filter", "Đang chờ", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_224)
    public void filtersPendingRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "trạng thái-filter", "Đang chờ", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_045)
    public void filtersSuccessRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "trạng thái-filter", "Thành công", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_225)
    public void filtersSuccessRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "trạng thái-filter", "Thành công", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_226)
    public void filtersSuccessRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "trạng thái-filter", "Thành công", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_227)
    public void filtersSuccessRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "trạng thái-filter", "Thành công", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_228)
    public void filtersSuccessRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "trạng thái-filter", "Thành công", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_229)
    public void filtersSuccessRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "trạng thái-filter", "Thành công", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_046)
    public void filtersFailedRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "trạng thái-filter", "Thất bại", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_230)
    public void filtersFailedRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "trạng thái-filter", "Thất bại", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_231)
    public void filtersFailedRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "trạng thái-filter", "Thất bại", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_232)
    public void filtersFailedRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "trạng thái-filter", "Thất bại", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_233)
    public void filtersFailedRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "trạng thái-filter", "Thất bại", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_234)
    public void filtersFailedRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "trạng thái-filter", "Thất bại", "Trạng thái");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_047)
    public void filtersMomoRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_235)
    public void filtersMomoRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_236)
    public void filtersMomoRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_237)
    public void filtersMomoRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_238)
    public void filtersMomoRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_239)
    public void filtersMomoRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "cổng thanh toán-filter", "MOMO", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_048)
    public void filtersPaypalRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_240)
    public void filtersPaypalRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_241)
    public void filtersPaypalRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_242)
    public void filtersPaypalRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_243)
    public void filtersPaypalRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_244)
    public void filtersPaypalRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "cổng thanh toán-filter", "PAYPAL", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_049)
    public void filtersOnepayRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_245)
    public void filtersOnepayRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_246)
    public void filtersOnepayRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_247)
    public void filtersOnepayRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_248)
    public void filtersOnepayRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_249)
    public void filtersOnepayRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "cổng thanh toán-filter", "ONEPAY", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_050)
    public void filtersBankingRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_250)
    public void filtersBankingRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_251)
    public void filtersBankingRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_252)
    public void filtersBankingRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_253)
    public void filtersBankingRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_254)
    public void filtersBankingRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "cổng thanh toán-filter", "BANKING", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_051)
    public void filtersNeoxRowsAcrossAllSubtypes() {
        verifySingleFilter(subtype(2), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_255)
    public void filtersNeoxRowsAcrossAllSubtypesType22() {
        verifySingleFilter(subtype(22), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_256)
    public void filtersNeoxRowsAcrossAllSubtypesType24() {
        verifySingleFilter(subtype(24), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_257)
    public void filtersNeoxRowsAcrossAllSubtypesType36() {
        verifySingleFilter(subtype(36), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_258)
    public void filtersNeoxRowsAcrossAllSubtypesType37() {
        verifySingleFilter(subtype(37), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_259)
    public void filtersNeoxRowsAcrossAllSubtypesType15() {
        verifySingleFilter(subtype(15), "cổng thanh toán-filter", "NEOX", "Cổng thanh toán");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_052)
    public void filtersAllFifteenStatusGatewayCombinationsOnDefaultSubtype() {
        TransactionCategoryPage.Subtype subtype = category().subtypes().get(0);
        openOrderSubtype(subtype);
        for (String status : List.of("Đang chờ", "Thành công", "Thất bại")) {
            for (String gateway : List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX")) {
                transactionPage.resetAndReadOverviewFilters();
                transactionPage.selectOption("trạng thái-filter", status);
                var result = transactionPage.selectOption("cổng thanh toán-filter", gateway);
                if (result.rows().isEmpty()) {
                    Assert.assertTrue(result.empty(), status + " + " + gateway);
                } else {
                    Assert.assertTrue(result.rows().stream().allMatch(row ->
                            status.equals(row.value("Trạng thái"))
                                    && gateway.equals(row.value("Cổng thanh toán"))),
                            status + " + " + gateway + ": " + result.pageText());
                }
                assertSubtype(subtype);
            }
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_053)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypes() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_210)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesType22() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_211)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesType24() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_212)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesType36() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_213)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesType37() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_214)
    public void resetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesType15() {
        verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifyResetClearsSuccessPaypalAndSelectedDayAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().resetStatusGatewayAndDate("Thành công", "PAYPAL");
        Assert.assertTrue(result.status().contains("Chọn trạng thái"), result.status());
        Assert.assertTrue(result.gateway().contains("Chọn cổng thanh toán"), result.gateway());
        Assert.assertTrue(result.date().contains("Chọn khoảng ngày giờ"), result.date());
        Assert.assertEquals(result.page(), 1);
        assertSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_054)
    public void successFilterPersistsAfterOpeningDetailAcrossAllSubtypes() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_215)
    public void successFilterPersistsAfterOpeningDetailAcrossAllSubtypesType22() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_216)
    public void successFilterPersistsAfterOpeningDetailAcrossAllSubtypesType24() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_217)
    public void successFilterPersistsAfterOpeningDetailAcrossAllSubtypesType36() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_218)
    public void successFilterPersistsAfterOpeningDetailAcrossAllSubtypesType37() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_219)
    public void successFilterPersistsAfterOpeningDetailAcrossAllSubtypesType15() {
        verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(subtype(15));
    }

    private void verifySuccessFilterPersistsAfterOpeningDetailAcrossAllSubtypesForSubtype(TransactionCategoryPage.Subtype subtype) {
        openOrderSubtype(subtype);
        var result = advancedPage().filterPersistsAfterDetail("Thành công");
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertFalse(result.browserLocationAfterClose().contains("id="));
        assertSubtype(subtype);
    }

    private void verifySingleFilter(TransactionCategoryPage.Subtype subtype,
                                    String ariaLabel, String option, String column) {
        openOrderSubtype(subtype);
            var result = transactionPage.selectOption(ariaLabel, option);
            if (result.rows().isEmpty()) {
                Assert.assertTrue(result.empty(), result.pageText());
                Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"), result.pageText());
            } else {
                Assert.assertTrue(result.rows().stream()
                        .allMatch(row -> option.equals(row.value(column))), result.pageText());
            }
            assertSubtype(subtype);
    }

    private List<String> signatures(List<TransactionCategoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionCategoryPage.TransactionRow::signature).toList();
    }

    private void assertSubtype(TransactionCategoryPage.Subtype subtype) {
        Assert.assertTrue(transactionPage.currentUrl().contains(
                "tab=order&type=" + subtype.type()), transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
