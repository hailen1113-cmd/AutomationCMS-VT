package com.vuatho.tests.transaction.fee;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionFeeTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dropdown ba loại Phí & Doanh thu. */
public class TransactionFeeDropdownTest extends TransactionFeeTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionFeeDropdownTest.class,
                "Lịch sử giao dịch", "Phí & Doanh thu - Dropdown");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_001)
    public void showsEverySubtype() {
        verifyGroupOptions();
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_002,
            dataProvider = "feeSubtypes")
    public void opensEverySubtypeRoute(TransactionCategoryPage.Subtype subtype) {
        verifySubtypeRoute(subtype);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_011)
    public void dropdownHasCorrectKeysAndSingleSelection() {
        var result = transactionPage.dropdownSemantics();
        Assert.assertEquals(result.expandedBefore(), "false");
        Assert.assertEquals(result.expandedAfter(), "true");
        Assert.assertEquals(result.hasPopup(), "true");
        Assert.assertEquals(result.menuId(), result.controls());
        Assert.assertEquals(result.menuLabel(), "Phí & Doanh thu sub-types");
        Assert.assertEquals(result.options().stream()
                        .map(TransactionCategoryPage.DropdownOption::label).toList(),
                List.of("Phí kết nối", "Phí liên kết ví", "Phí chia sẻ vật tư"));
        Assert.assertEquals(result.options().stream()
                        .map(TransactionCategoryPage.DropdownOption::key).toList(),
                List.of("8", "9", "33"));
        Assert.assertEquals(result.options().stream()
                .filter(option -> "true".equals(option.checked())).count(), 1L);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_012,
            dataProvider = "feeSubtypes")
    public void selectedSubtypeIsMarkedAndMenuCloses(TransactionCategoryPage.Subtype subtype) {
        var result = transactionPage.selectSubtypeFromDropdown(subtype);
        Assert.assertTrue(result.url().contains("tab=fee&type=" + subtype.type()), result.url());
        Assert.assertTrue(result.triggerText().contains(subtype.label()), result.triggerText());
        Assert.assertEquals(result.optionText(), subtype.label());
        Assert.assertEquals(result.checked(), "true");
        Assert.assertEquals(result.selected(), "true");
        Assert.assertEquals(result.selectedCount(), 1L);
        Assert.assertTrue(result.menuClosed());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_013)
    public void triggerClosesDropdownAndKeepsFeeConnection() {
        var result = transactionPage.closeDropdownWithTrigger();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"), result.url());
        Assert.assertTrue(result.activeText().contains("Phí kết nối"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_014)
    public void escapeClosesDropdownAndKeepsFeeConnection() {
        var result = transactionPage.closeDropdownWithEscape();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=fee&type=8"), result.url());
        Assert.assertTrue(result.activeText().contains("Phí kết nối"));
    }
}
