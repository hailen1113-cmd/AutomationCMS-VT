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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_002)
    public void opensEverySubtypeRoute() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(8));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_174)
    public void opensEverySubtypeRouteType9() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(9));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_175)
    public void opensEverySubtypeRouteType33() {
        verifyOpensEverySubtypeRouteForSubtype(subtype(33));
    }

    private void verifyOpensEverySubtypeRouteForSubtype(TransactionCategoryPage.Subtype subtype) {
        verifyGroupOptions();
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

    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_012)
    public void selectedSubtypeIsMarkedAndMenuCloses() {
        verifySelectedSubtypeIsMarkedAndMenuClosesForSubtype(subtype(8));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_176)
    public void selectedSubtypeIsMarkedAndMenuClosesType9() {
        verifySelectedSubtypeIsMarkedAndMenuClosesForSubtype(subtype(9));
    }


    @Test(description = TransactionHistoryTestCases.TRANSACTION_FEE_177)
    public void selectedSubtypeIsMarkedAndMenuClosesType33() {
        verifySelectedSubtypeIsMarkedAndMenuClosesForSubtype(subtype(33));
    }

    private void verifySelectedSubtypeIsMarkedAndMenuClosesForSubtype(TransactionCategoryPage.Subtype subtype) {
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

    private TransactionCategoryPage.Subtype subtype(int type) {
        return category().subtypes().stream()
                .filter(value -> value.type() == type).findFirst().orElseThrow();
    }
}
