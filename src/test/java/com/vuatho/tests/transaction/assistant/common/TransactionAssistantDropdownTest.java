package com.vuatho.tests.transaction.assistant.common;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionAssistantTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dropdown loại giao dịch của tab Thợ phụ. */
public class TransactionAssistantDropdownTest extends TransactionAssistantTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionAssistantDropdownTest.class,
                "Lịch sử giao dịch", "Thợ phụ - Dropdown");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_002)
    public void opensPlatformFeeRoute() {
        verifyAssistantSubtypeRoute(category().subtypes().get(0));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_128)
    public void opensPenaltyRoute() {
        verifyAssistantSubtypeRoute(category().subtypes().get(1));
    }

    private void verifyAssistantSubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.selectSubtypeFromDropdown(subtype);
        Assert.assertTrue(result.url().contains("tab=assistant"));
        Assert.assertTrue(result.url().contains("type=" + subtype.type()));
        Assert.assertTrue(result.triggerText().contains(subtype.label()));
        Assert.assertEquals(result.optionText(), subtype.label());
        Assert.assertEquals(result.checked(), "true");
        Assert.assertEquals(result.selected(), "true");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_029)
    public void triggerOpensItsControlledMenu() {
        var result = transactionPage.dropdownSemantics();
        Assert.assertEquals(result.expandedBefore(), "false");
        Assert.assertEquals(result.expandedAfter(), "true");
        Assert.assertFalse(result.controls().isBlank());
        Assert.assertEquals(result.menuId(), result.controls());
        Assert.assertEquals(result.menuLabel(), "Thợ phụ sub-types");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_030)
    public void optionsHaveCorrectKeysAndSelectionState() {
        var options = transactionPage.dropdownSemantics().options();
        Assert.assertEquals(options.stream().map(TransactionCategoryPage.DropdownOption::label).toList(),
                List.of("Phí nền tảng", "Tiền phạt"));
        Assert.assertEquals(options.stream().map(TransactionCategoryPage.DropdownOption::key).toList(),
                List.of("30", "31"));
        Assert.assertEquals(options.get(0).checked(), "true");
        Assert.assertEquals(options.get(0).selected(), "true");
        Assert.assertEquals(options.get(1).checked(), "false");
        Assert.assertTrue(options.get(1).selected() == null
                || options.get(1).selected().equals("false"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_031)
    public void selectingPenaltyUpdatesRouteAndState() {
        var result = transactionPage.selectSubtypeFromDropdown(category().subtypes().get(1));
        Assert.assertTrue(result.url().contains("tab=assistant"));
        Assert.assertTrue(result.url().contains("type=31"));
        Assert.assertTrue(result.triggerText().contains("Tiền phạt"));
        Assert.assertEquals(result.optionText(), "Tiền phạt");
        Assert.assertEquals(result.checked(), "true");
        Assert.assertEquals(result.selected(), "true");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ASSISTANT_032)
    public void escapeClosesDropdownAndKeepsSubtype() {
        var result = transactionPage.closeDropdownWithEscape();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=assistant"));
        Assert.assertTrue(result.url().contains("type=30"));
        Assert.assertTrue(result.activeText().contains("Phí nền tảng"));
    }
}
