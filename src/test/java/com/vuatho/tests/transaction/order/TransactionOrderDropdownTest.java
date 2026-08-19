package com.vuatho.tests.transaction.order;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionOrderTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dropdown sáu loại giao dịch Đơn dịch vụ. */
public class TransactionOrderDropdownTest extends TransactionOrderTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionOrderDropdownTest.class,
                "Lịch sử giao dịch", "Đơn dịch vụ - Dropdown");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_002)
    public void opensEverySubtypeRouteAndMarksSelection() {
        verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_098)
    public void opensEverySubtypeRouteAndMarksSelectionType22() {
        verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_099)
    public void opensEverySubtypeRouteAndMarksSelectionType24() {
        verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_100)
    public void opensEverySubtypeRouteAndMarksSelectionType36() {
        verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_101)
    public void opensEverySubtypeRouteAndMarksSelectionType37() {
        verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_102)
    public void opensEverySubtypeRouteAndMarksSelectionType15() {
        verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(subtype(15));
    }

    private void verifyOpensEverySubtypeRouteAndMarksSelectionForSubtype(TransactionCategoryPage.Subtype subtype) {
        transactionPage.open(subtype);
        Assert.assertTrue(transactionPage.currentUrl().contains(
                "tab=order&type=" + subtype.type()), transactionPage.currentUrl());
        Assert.assertTrue(transactionPage.activeGroupText().contains(subtype.label()),
                transactionPage.activeGroupText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_031)
    public void dropdownHasCorrectKeysAndSingleSelection() {
        var result = transactionPage.dropdownSemantics();
        Assert.assertEquals(result.expandedBefore(), "false");
        Assert.assertEquals(result.expandedAfter(), "true");
        Assert.assertEquals(result.hasPopup(), "true");
        Assert.assertEquals(result.menuId(), result.controls());
        Assert.assertEquals(result.menuLabel(), "Đơn dịch vụ sub-types");
        List<String> actualLabels = result.options().stream()
                .map(TransactionCategoryPage.DropdownOption::label).toList();
        List<String> expectedLabels = category().subtypes().stream()
                .map(TransactionCategoryPage.Subtype::label).toList();
        Assert.assertEquals(actualLabels.size(), expectedLabels.size());
        for (int index = 0; index < expectedLabels.size(); index++) {
            Assert.assertTrue(actualLabels.get(index).startsWith(expectedLabels.get(index)),
                    actualLabels.get(index));
        }
        Assert.assertEquals(result.options().stream()
                        .map(TransactionCategoryPage.DropdownOption::key).toList(),
                List.of("2", "22", "24", "36", "37", "15"));
        Assert.assertEquals(result.options().stream()
                .filter(option -> "true".equals(option.checked())).count(), 1L);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_032)
    public void selectingEverySubtypeMarksOnlyItAndClosesMenu() {
        verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(subtype(2));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_103)
    public void selectingEverySubtypeMarksOnlyItAndClosesMenuType22() {
        verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(subtype(22));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_104)
    public void selectingEverySubtypeMarksOnlyItAndClosesMenuType24() {
        verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(subtype(24));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_105)
    public void selectingEverySubtypeMarksOnlyItAndClosesMenuType36() {
        verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(subtype(36));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_106)
    public void selectingEverySubtypeMarksOnlyItAndClosesMenuType37() {
        verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(subtype(37));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_107)
    public void selectingEverySubtypeMarksOnlyItAndClosesMenuType15() {
        verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(subtype(15));
    }

    private void verifySelectingEverySubtypeMarksOnlyItAndClosesMenuForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.selectSubtypeFromDropdown(subtype);
        Assert.assertTrue(result.url().contains("tab=order&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.triggerText().contains(subtype.label()), result.triggerText());
        Assert.assertTrue(result.optionText().startsWith(subtype.label()), result.optionText());
        Assert.assertEquals(result.checked(), "true");
        Assert.assertEquals(result.selected(), "true");
        Assert.assertEquals(result.selectedCount(), 1L);
        Assert.assertTrue(result.menuClosed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_033)
    public void triggerClosesDropdownAndKeepsDefaultSubtype() {
        var result = transactionPage.closeDropdownWithTrigger();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        assertDefaultSubtype(result.url(), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_034)
    public void escapeClosesDropdownAndKeepsDefaultSubtype() {
        var result = transactionPage.closeDropdownWithEscape();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        assertDefaultSubtype(result.url(), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_035)
    public void clickingOutsideClosesDropdownAndKeepsDefaultSubtype() {
        var result = transactionPage.closeDropdownByClickingOutside();
        Assert.assertTrue(result.menuClosed());
        Assert.assertEquals(result.afterUrl(), result.beforeUrl());
        Assert.assertEquals(result.afterText(), result.beforeText());
        assertDefaultSubtype(result.afterUrl(), result.afterText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_036)
    public void refreshKeepsWarrantyIncomeSubtypeSelected() {
        TransactionCategoryPage.Subtype subtype = category().subtypes().stream()
                .filter(value -> value.type() == 36).findFirst().orElseThrow();
        transactionPage.open(subtype);
        var result = transactionPage.refreshCurrentSubtype();
        Assert.assertEquals(result.afterUrl(), result.beforeUrl());
        Assert.assertTrue(result.afterText().contains(subtype.label()), result.afterText());
        Assert.assertEquals(result.options().stream()
                .filter(option -> "true".equals(option.checked())).count(), 1L);
        Assert.assertTrue(result.options().stream().anyMatch(option ->
                "36".equals(option.key()) && "true".equals(option.checked())));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ORDER_037)
    public void invalidTypeDoesNotSelectAnyValidSubtype() {
        var result = transactionPage.openInvalidSubtype(999999);
        Assert.assertTrue(result.url().contains("tab=order&type=999999"), result.url());
        Assert.assertEquals(result.options().stream()
                .filter(option -> "true".equals(option.checked())).count(), 0L);
        Assert.assertTrue(result.options().stream().noneMatch(option ->
                String.valueOf(result.invalidType()).equals(option.key())));
    }

    private void assertDefaultSubtype(String url, String activeText) {
        Assert.assertTrue(url.contains("tab=order&type=2"), url);
        Assert.assertTrue(activeText.contains("Đơn dịch vụ"), activeText);
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
