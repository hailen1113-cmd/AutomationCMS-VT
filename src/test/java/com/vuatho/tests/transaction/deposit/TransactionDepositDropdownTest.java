package com.vuatho.tests.transaction.deposit;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionDepositTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dropdown và route của năm loại Tiền nạp. */
public class TransactionDepositDropdownTest extends TransactionDepositTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDepositDropdownTest.class,
                "Lịch sử giao dịch", "Tiền nạp - Dropdown");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_002)
    public void opensEverySubtypeRoute() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_088)
    public void opensEverySubtypeRouteType10() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(10));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_089)
    public void opensEverySubtypeRouteType19() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(19));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_090)
    public void opensEverySubtypeRouteType20() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(20));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_091)
    public void opensEverySubtypeRouteType34() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(34));
    }

    private void verifyOpensEverySubtypeRouteForSubtype(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.chooseSubtypeFromDropdown(subtype);
        Assert.assertTrue(result.url().contains("tab=deposit"), result.url());
        Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
        Assert.assertTrue(result.triggerText().contains(subtype.label()), result.triggerText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_011)
    public void triggerOpensItsControlledMenu() {
        var result = transactionPage.dropdownSemantics();
        Assert.assertEquals(result.expandedBefore(), "false");
        Assert.assertEquals(result.expandedAfter(), "true");
        Assert.assertEquals(result.hasPopup(), "true");
        Assert.assertFalse(result.controls().isBlank());
        Assert.assertEquals(result.menuId(), result.controls());
        Assert.assertEquals(result.menuLabel(), "Tiền nạp sub-types");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_012)
    public void optionsHaveCorrectKeysAndSelectionState() {
        var options = transactionPage.dropdownSemantics().options();
        Assert.assertEquals(options.stream().map(TransactionCategoryPage.DropdownOption::label).toList(),
                List.of("Nạp thường", "Bên thứ 3", "Nạp từ DN", "Nạp vào ký quỹ", "Nạp ký quỹ qua NH"));
        Assert.assertEquals(options.stream().map(TransactionCategoryPage.DropdownOption::key).toList(),
                List.of("0", "10", "19", "20", "34"));
        Assert.assertEquals(options.stream().filter(option -> "true".equals(option.checked())).count(), 1L);
        Assert.assertEquals(options.get(0).checked(), "true");
        Assert.assertEquals(options.get(0).selected(), "true");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_013)
    public void switchingSubtypeReloadsMatchingRows() {
        var first = category().subtypes().get(0);
        var last = category().subtypes().get(category().subtypes().size() - 1);
        assertDepositRows();
        List<String> firstRows = transactionPage.rows().stream()
                .map(TransactionCategoryPage.TransactionRow::signature).toList();

        var selectedLast = transactionPage.chooseSubtypeFromDropdown(last);
        Assert.assertTrue(selectedLast.url().contains("type=" + last.type()), selectedLast.url());
        Assert.assertTrue(selectedLast.triggerText().contains(last.label()), selectedLast.triggerText());
        List<String> lastRows = waitForRowsToChange(firstRows);
        assertDepositRows();
        Assert.assertNotEquals(lastRows, firstRows);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_014)
    public void selectingSubtypeUpdatesOnlySelectedOption() {
        var subtype = category().subtypes().get(category().subtypes().size() - 1);
        var result = transactionPage.selectSubtypeFromDropdown(subtype);

        Assert.assertTrue(result.url().contains("tab=deposit"), result.url());
        Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
        Assert.assertTrue(result.triggerText().contains(subtype.label()), result.triggerText());
        Assert.assertEquals(result.optionText(), subtype.label());
        Assert.assertEquals(result.checked(), "true");
        Assert.assertEquals(result.selected(), "true");
        Assert.assertEquals(result.selectedCount(), 1L);
        Assert.assertTrue(result.menuClosed(), "Dropdown không tự đóng sau khi chọn loại");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_015)
    public void triggerClosesDropdownAndKeepsSubtype() {
        var subtype = initialSubtype();
        var result = transactionPage.closeDropdownWithTrigger();

        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=deposit"), result.url());
        Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_DEPOSIT_016)
    public void escapeClosesDropdownAndKeepsSubtype() {
        var subtype = initialSubtype();
        var result = transactionPage.closeDropdownWithEscape();

        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=deposit"), result.url());
        Assert.assertTrue(result.url().contains("type=" + subtype.type()), result.url());
        Assert.assertTrue(result.activeText().contains(subtype.label()), result.activeText());
    }

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
