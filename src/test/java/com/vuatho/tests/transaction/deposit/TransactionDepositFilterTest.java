package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Kiểm tra tìm kiếm và bộ lọc của từng loại Tiền nạp. */
public class TransactionDepositFilterTest extends TransactionDepositTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositFilterTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Bộ lọc");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_005)
    public void searchesUserAndRestoresRows() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            if (!transactionPage.hasSearchableUser()) {
                return;
            }
            verifySearchAndReset(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_006)
    public void filterOptionsKeepCurrentTab() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            verifyFilterOptions(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_033)
    public void everyStatusFiltersMatchingRows() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = advancedPage().applyEveryFilterOption(
                    com.vuatho.pages.TransactionHistoryPage.Filter.STATUS);
            Assert.assertEquals(result.options(), List.of("Đang chờ", "Thành công", "Đã hủy"));
            result.results().forEach(option -> {
                Assert.assertTrue(option.selectedText().contains(option.value()));
                Assert.assertTrue(option.rows().stream()
                        .allMatch(row -> row.status().equals(option.value())));
            });
            assertCurrentSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_034)
    public void everyGatewayFiltersMatchingRows() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = advancedPage().applyEveryFilterOption(
                    com.vuatho.pages.TransactionHistoryPage.Filter.GATEWAY);
            Assert.assertEquals(result.options(), List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
            result.results().forEach(option -> {
                Assert.assertTrue(option.selectedText().contains(option.value()));
                Assert.assertTrue(option.rows().stream()
                        .allMatch(row -> row.gateway().equals(option.value())));
            });
            assertCurrentSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_035)
    public void combinesEveryStatusWithEveryGateway() {
        // 033 và 034 đã kiểm tra riêng từng trạng thái/cổng trên cả năm loại.
        // Ma trận giao nhau chỉ cần chạy một lần trên loại đại diện để tránh
        // lặp 15 tổ hợp x 5 loại (75 lượt gọi dữ liệu).
        var subtype = initialSubtype();
        openDepositSubtype(subtype);
        var result = advancedPage().applyEveryStatusGatewayCombination();
        Assert.assertEquals(result.statuses(), List.of("Đang chờ", "Thành công", "Đã hủy"));
        Assert.assertEquals(result.gateways(),
                List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX"));
        Assert.assertEquals(result.results().size(), 15);
        result.results().forEach(combination -> {
            String name = combination.status() + " + " + combination.gateway();
            Assert.assertTrue(combination.selectedStatus().contains(combination.status()), name);
            Assert.assertTrue(combination.selectedGateway().contains(combination.gateway()), name);
            if (combination.empty()) {
                Assert.assertTrue(combination.pageText().contains("Chưa có dữ liệu"), name);
            } else {
                Assert.assertTrue(combination.rows().stream().allMatch(row ->
                        row.status().equals(combination.status())
                                && row.gateway().equals(combination.gateway())), name);
            }
        });
        assertCurrentSubtype(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_036)
    public void dismissingSelectFiltersKeepsRows() {
        var status = advancedPage().dismissFilterWithoutSelection(
                com.vuatho.pages.TransactionHistoryPage.Filter.STATUS);
        Assert.assertEquals(status.selectedAfter(), status.selectedBefore());
        Assert.assertEquals(status.rowsAfter(), status.rowsBefore());
        var gateway = advancedPage().dismissFilterWithoutSelection(
                com.vuatho.pages.TransactionHistoryPage.Filter.GATEWAY);
        Assert.assertEquals(gateway.selectedAfter(), gateway.selectedBefore());
        Assert.assertEquals(gateway.rowsAfter(), gateway.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_037)
    public void futureDatesCannotBeSelected() {
        var result = advancedPage().futureDatesAreDisabled();
        Assert.assertTrue(result.disabled());
        Assert.assertTrue(result.disabledCount() > 0);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_038)
    public void datePickerDefaultsAndRequiresDate() {
        var result = advancedPage().dateControlDefaults();
        Assert.assertEquals(result.startTime(), "00:00");
        Assert.assertEquals(result.endTime(), "23:59");
        Assert.assertTrue(result.applyDisabled());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_039)
    public void filtersRowsWithinSingleDay() {
        var result = advancedPage().filterSingleDay();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_040)
    public void filtersRowsWithinSourceMinute() {
        var result = advancedPage().filterSourceMinute();
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                row.createdAt().toLocalDate().equals(result.startDate())
                        && !row.createdAt().toLocalTime().isBefore(result.startTime())
                        && !row.createdAt().toLocalTime().isAfter(result.endTime())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_041)
    public void dismissingDatePickerKeepsRows() {
        var result = advancedPage().dismissDateWithoutApply();
        Assert.assertEquals(result.selectedAfter(), result.selectedBefore());
        Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_042)
    public void unmatchedSearchAndResetRestoreSubtype() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = transactionPage.unmatchedSearchAndReset(
                    "NO_DEPOSIT_TRANSACTION_987654321");
            Assert.assertTrue(result.empty());
            Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
            Assert.assertTrue(result.pageText().contains("Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm"));
            Assert.assertEquals(signatures(result.restored()), signatures(result.before()));
            assertCurrentSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_043)
    public void totalAndPaginationUpdateAfterStatusFilter() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = advancedPage().totalAndPaginationAfterStatusFilter();
            Assert.assertTrue(result.afterTotal() <= result.beforeTotal());
            Assert.assertTrue(result.selectedStatus().contains("Thành công"));
            Assert.assertTrue(result.rows().stream().allMatch(row -> row.status().equals("Thành công")));
            Assert.assertEquals(result.afterPagination(), result.afterTotal() > 20);
            assertCurrentSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_044)
    public void filterPersistsAfterOpeningDetail() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = advancedPage().filterPersistsAfterDetail("Thành công");
            Assert.assertTrue(result.selectedStatus().contains(result.expectedStatus()));
            Assert.assertEquals(result.rowsAfter(), result.rowsBefore());
            Assert.assertTrue(transactionPage.activeGroupText().contains("Tiền nạp"));
            Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                    "Sau khi đóng chi tiết không còn hiển thị loại " + subtype.label());
            assertSubtypeUrl(result.listUrlBefore(), subtype, "trước khi mở chi tiết");
            assertSubtypeUrl(result.openedUrl(), subtype, "khi đang mở chi tiết");
            Assert.assertEquals(result.browserLocationAfterClose(), result.closedUrl(),
                    "window.location.href khác WebDriver.getCurrentUrl() sau khi đóng chi tiết");
            assertSubtypeUrl(result.closedUrl(), subtype, "sau khi đóng chi tiết");
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_045)
    public void clearingDateRestoresData() {
        var result = advancedPage().clearAppliedDateFilter();
        Assert.assertTrue(result.filteredTotal() <= result.originalTotal());
        Assert.assertEquals(result.restoredTotal(), result.originalTotal());
        Assert.assertTrue(result.clearControlGone());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_046)
    public void combinesDateStatusAndGateway() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = advancedPage().combineStatusGatewayAndDate("Thành công", "MOMO");
            Assert.assertTrue(result.selectedStatus().contains("Thành công"));
            Assert.assertTrue(result.selectedGateway().contains("MOMO"));
            Assert.assertTrue(result.selectedDate().contains(result.date().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            if (result.empty()) {
                Assert.assertTrue(result.pageText().contains("Chưa có dữ liệu"));
            } else {
                Assert.assertFalse(result.rows().isEmpty());
                Assert.assertTrue(result.rows().stream().allMatch(row ->
                        row.status().equals("Thành công")
                                && row.gateway().equals("MOMO")
                                && row.createdAt().toLocalDate().equals(result.date())));
            }
            assertCurrentSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_047)
    public void resetCombinedFiltersKeepsSubtype() {
        forEachSubtype(subtype -> {
            openDepositSubtype(subtype);
            var result = advancedPage().resetStatusGatewayAndDate("Thành công", "MOMO");
            Assert.assertTrue(result.status().contains("Chọn trạng thái"));
            Assert.assertTrue(result.gateway().contains("Chọn cổng thanh toán"));
            Assert.assertTrue(result.date().contains("Chọn khoảng ngày giờ"));
            Assert.assertEquals(result.page(), 1);
            Assert.assertTrue(result.rows() > 0);
            assertCurrentSubtype(subtype);
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_048)
    public void irrelevantFiltersRemainHidden() {
        var result = advancedPage().specializedIrrelevantFiltersAreHidden();
        Assert.assertTrue(result.typeHidden());
        Assert.assertTrue(result.invoiceHidden());
        Assert.assertTrue(result.warrantyHidden());
    }

    private void assertCurrentSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        assertSubtypeUrl(url, subtype, "trên URL hiện tại");
        Assert.assertTrue(transactionPage.activeGroupText().contains("Tiền nạp"));
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()));
    }

    private void assertSubtypeUrl(String url, TransactionCategoryPage.Subtype subtype, String stage) {
        Assert.assertTrue(url.contains("/vuatho/transaction"), url);
        Assert.assertTrue(url.contains("tab=deposit"), url);
        Assert.assertTrue(url.contains("type=" + subtype.type()),
                stage + " bị mất type=" + subtype.type() + ": " + url);
    }

    private List<String> signatures(List<TransactionCategoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionCategoryPage.TransactionRow::signature).toList();
    }

    private void forEachSubtype(Consumer<TransactionCategoryPage.Subtype> action) {
        List<String> failures = new ArrayList<>();
        String requestedType = System.getProperty("deposit.type", "").trim();
        for (TransactionCategoryPage.Subtype subtype : category().subtypes().stream()
                .filter(candidate -> requestedType.isBlank()
                        || requestedType.equals(String.valueOf(candidate.type())))
                .toList()) {
            try {
                action.accept(subtype);
            } catch (AssertionError | RuntimeException exception) {
                failures.add(subtype.label() + " (type=" + subtype.type() + "): "
                        + rootMessage(exception));
            }
        }
        if (!failures.isEmpty()) {
            Assert.fail("Các loại Tiền nạp không đạt:\n- " + String.join("\n- ", failures));
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank()
                ? current.getClass().getSimpleName() : message;
    }
}
