package com.vuatho.tests.transaction.all;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.support.TransactionHistoryTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra độc lập và kết hợp các bộ lọc của tab Tất cả. */
public class TransactionAllFilterTest extends TransactionHistoryTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAllFilterTest.class,
                "Lịch sử giao dịch", "Tab Tất cả - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_008)
    public void transactionTypeOptionsAreComplete() {
        List<String> options = transactionPage.filterOptions(TransactionHistoryPage.Filter.TYPE);
        Assert.assertEquals(options, List.of(
                "Tiền nạp", "Tiền rút", "Đơn dịch vụ", "Nhiệm vụ", "Phần thưởng",
                "Rút tiền thưởng về ví", "Tiền đặt cọc", "Hệ thống", "Phí kết nối",
                "Phí liên kết ví", "Thanh toán bởi bên thứ 3", "Hoàn tiền Voucher",
                "Rút tiền thưởng trực tiếp", "Phí hủy đơn", "Phí xử phạt đơn dịch vụ",
                "Tiền rút ký quỹ", "Hoàn tiền từ chiến dịch", "Tiền nạp từ doanh nghiệp",
                "Tiền nạp vào ví ký quỹ", "Tiền rút từ ví chi phí",
                "Doanh nghiệp thanh toán đơn dịch vụ", "Tiền rút ngưng hợp tác",
                "Phí bảo hành đơn dịch vụ", "Trừ phí VT Care hàng ngày/tháng",
                "Hoàn phí VT Care khi hủy gói", "Cọc đơn thợ phụ", "Hoàn cọc thợ phụ",
                "Trả công thợ phụ", "Phí nền tảng thợ phụ", "Tiền phạt thợ phụ",
                "Tiền giữ lại thợ phụ", "Phí chia sẻ vật tư",
                "Nạp tiền vào số dư nền tảng qua chuyển khoản ngân hàng",
                "Rút tiền từ số dư nền tảng về tài khoản ngân hàng",
                "Thu bảo hành đơn dịch vụ", "Chi bảo hành đơn dịch vụ"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_009)
    public void statusOptionsAreComplete() {
        Assert.assertEquals(transactionPage.filterOptions(TransactionHistoryPage.Filter.STATUS),
                List.of("Đang chờ", "Thành công", "Thất bại"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_010)
    public void gatewayOptionsAreComplete() {
        Assert.assertEquals(transactionPage.filterOptions(TransactionHistoryPage.Filter.GATEWAY),
                List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_011)
    public void filtersByTransactionType() {
        var result = transactionPage.filterByFirstRow(TransactionHistoryPage.Filter.TYPE);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.type().equals(result.value())));
        Assert.assertTrue(result.selectedText().contains(result.value()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_012)
    public void filtersByStatus() {
        var result = transactionPage.filterByFirstRow(TransactionHistoryPage.Filter.STATUS);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals(result.value())));
        Assert.assertTrue(result.selectedText().contains(result.value()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_013)
    public void filtersByPaymentGateway() {
        var result = transactionPage.filterByFirstRow(TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.gateway().equals(result.value())));
        Assert.assertTrue(result.selectedText().contains(result.value()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_014)
    public void combinesTypeStatusAndGateway() {
        var result = transactionPage.combineFiltersFromOneRow();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.type().equals(result.source().type())
                        && row.status().equals(result.source().status())
                        && row.gateway().equals(result.source().gateway())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_015)
    public void futureDatesCannotBeSelected() {
        var result = transactionPage.futureDatesAreDisabled();
        Assert.assertTrue(result.disabled());
        Assert.assertTrue(result.disabledCount() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_016)
    public void filtersInclusiveDateRange() {
        var result = transactionPage.filterRecentRange();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var date = row.createdAt().toLocalDate();
            return !date.isBefore(result.start()) && !date.isAfter(result.end());
        }));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_017)
    public void unmatchedCombinationShowsEmptyState() {
        var result = transactionPage.findEmptyCombination();
        Assert.assertTrue(result.empty());
        Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
        Assert.assertTrue(result.pageText().contains("Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_018)
    public void resetClearsFiltersAndKeepsAllTab() {
        var result = transactionPage.resetAfterFilters();
        assertReset(result);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_019)
    public void resetFromEmptyStateRestoresData() {
        var result = transactionPage.resetEmptyState();
        assertReset(result);
        Assert.assertTrue(result.rows() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_047)
    public void changingStatusUpdatesFilterAndRows() {
        var result = transactionPage.changeStatusFilter();
        Assert.assertNotEquals(result.secondValue(), result.firstValue());
        Assert.assertTrue(result.selectedText().contains(result.secondValue()));
        Assert.assertFalse(result.secondRows().isEmpty());
        Assert.assertTrue(result.secondRows().stream()
                .allMatch(row -> row.status().equals(result.secondValue())));
        Assert.assertNotEquals(result.secondRows().stream().map(
                        TransactionHistoryPage.TransactionRow::signature).toList(),
                result.firstRows().stream().map(
                        TransactionHistoryPage.TransactionRow::signature).toList());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_048)
    public void filtersByEveryStatusOption() {
        var result = transactionPage.applyEveryFilterOption(TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(result.options(), List.of("Đang chờ", "Thành công", "Thất bại"));
        Assert.assertEquals(result.results().size(), result.options().size());
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.status().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_049)
    public void filtersByEveryGatewayOption() {
        var result = transactionPage.applyEveryFilterOption(TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertEquals(result.options(), List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
        Assert.assertEquals(result.results().size(), result.options().size());
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.gateway().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_093)
    public void filtersByEveryTransactionTypeOption() {
        var result = transactionPage.applyEveryFilterOption(TransactionHistoryPage.Filter.TYPE);
        Assert.assertEquals(result.options().size(), 36);
        Assert.assertEquals(result.results().size(), result.options().size());
        result.results().forEach(option -> {
            Assert.assertTrue(option.selectedText().contains(option.value()));
            Assert.assertTrue(option.rows().stream()
                    .allMatch(row -> row.type().equals(option.value())));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_050)
    public void closingFilterWithoutSelectionKeepsRows() {
        var result = transactionPage.dismissFilterWithoutSelection(
                TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_051)
    public void datePickerUsesFullDayDefaults() {
        var result = transactionPage.dateControlDefaults();
        Assert.assertEquals(result.startTime(), "00:00");
        Assert.assertEquals(result.endTime(), "23:59");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_052)
    public void applyIsDisabledBeforeSelectingDate() {
        var result = transactionPage.dateApplyRequiresSelection();
        Assert.assertTrue(result.initiallyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_053)
    public void filtersTransactionsWithinOneDay() {
        var result = transactionPage.filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
        String expectedDate = result.startDate().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Assert.assertTrue(result.selectedText().contains(expectedDate));
        Assert.assertTrue(result.selectedText().contains("00:00"));
        Assert.assertTrue(result.selectedText().contains("23:59"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_054)
    public void filtersTransactionsWithinTimeRange() {
        var result = transactionPage.filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> {
            var date = row.createdAt().toLocalDate();
            var time = row.createdAt().toLocalTime();
            return date.equals(result.startDate())
                    && !time.isBefore(result.startTime())
                    && !time.isAfter(result.endTime());
        }));
        Assert.assertTrue(result.selectedText().contains(result.startTime().toString()));
        Assert.assertTrue(result.selectedText().contains(result.endTime().toString()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_055)
    public void dismissingDatePickerWithoutApplyKeepsRows() {
        var result = transactionPage.dismissDateWithoutApply();
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_056)
    public void combinesDateTypeStatusAndGateway() {
        var result = transactionPage.combineDateAndSelectFilters();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.date())
                        && row.type().equals(result.source().type())
                        && row.status().equals(result.source().status())
                        && row.gateway().equals(result.source().gateway())));
        Assert.assertTrue(result.type().contains(result.source().type()));
        Assert.assertTrue(result.status().contains(result.source().status()));
        Assert.assertTrue(result.gateway().contains(result.source().gateway()));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_057)
    public void totalAndPaginationUpdateAfterFiltering() {
        var result = transactionPage.totalAndPaginationAfterStatusFilter();
        Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
        Assert.assertTrue(result.selectedStatus().contains("Thành công"));
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals("Thành công")));
        Assert.assertEquals(result.afterPagination(), result.afterTotal() > 20);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_058)
    public void filterPersistsAfterOpeningAndClosingDetail() {
        var result = transactionPage.filterPersistsAfterDetail();
        Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
        Assert.assertTrue(result.url().contains("tab=all"));
        Assert.assertFalse(result.url().contains("id="));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_059)
    public void resetClearsCombinedFiltersAndDate() {
        var result = transactionPage.resetAllCombinedFiltersAndDate();
        assertReset(result);
        Assert.assertTrue(result.rows() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_060)
    public void clearingOnlyDateRestoresAllRows() {
        var result = transactionPage.clearAppliedDateFilter();
        Assert.assertTrue(result.filteredTotal() <= result.originalTotal());
        Assert.assertEquals(result.restoredTotal(), result.originalTotal());
        Assert.assertTrue(result.selectedDate().contains("Chọn khoảng ngày giờ"));
        Assert.assertTrue(result.rows() > 0);
        Assert.assertTrue(result.clearControlGone());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_061)
    public void unrelatedFiltersRemainHiddenOnAllTab() {
        var result = transactionPage.hiddenAllTabFilters();
        Assert.assertTrue(result.searchHidden());
        Assert.assertTrue(result.invoiceHidden());
        Assert.assertTrue(result.warrantyHidden());
    }

    private void assertReset(TransactionHistoryPage.ResetSnapshot result) {
        Assert.assertTrue(result.allSelected());
        Assert.assertEquals(result.type(), "Chọn loại giao dịch");
        Assert.assertEquals(result.status(), "Chọn trạng thái");
        Assert.assertEquals(result.gateway(), "Chọn cổng thanh toán");
        Assert.assertTrue(result.date().contains("Chọn khoảng ngày giờ"));
        Assert.assertEquals(result.page(), 1);
        Assert.assertTrue(result.url().contains("tab=all"));
    }
}
