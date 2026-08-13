package com.vuatho.tests.transaction.insurance;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.support.TransactionInsuranceTestSupport;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Kiểm tra dropdown hai loại VT Care. */
public class TransactionInsuranceDropdownTest extends TransactionInsuranceTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionInsuranceDropdownTest.class,
                "Lịch sử giao dịch", "VT Care - Dropdown");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_001)
    public void showsEverySubtype() { verifyGroupOptions(); }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_002)
    public void opensEverySubtypeRoute() {
        for (TransactionCategoryPage.Subtype subtype : category().subtypes()) {
            verifySubtypeRoute(subtype);
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_011)
    public void dropdownHasCorrectKeysAndSingleSelection() {
        var result = transactionPage.dropdownSemantics();
        Assert.assertEquals(result.expandedBefore(), "false");
        Assert.assertEquals(result.expandedAfter(), "true");
        Assert.assertEquals(result.hasPopup(), "true");
        Assert.assertEquals(result.menuId(), result.controls());
        Assert.assertEquals(result.menuLabel(), "VT Care sub-types");
        Assert.assertEquals(result.options().stream().map(TransactionCategoryPage.DropdownOption::label).toList(),
                List.of("Trừ phí VT Care", "Hoàn phí VT Care"));
        Assert.assertEquals(result.options().stream().map(TransactionCategoryPage.DropdownOption::key).toList(),
                List.of("25", "26"));
        Assert.assertEquals(result.options().stream()
                .filter(option -> "true".equals(option.checked())).count(), 1L);
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_012)
    public void selectedSubtypeIsMarkedAndMenuCloses() {
        for (TransactionCategoryPage.Subtype subtype : category().subtypes()) {
            var result = transactionPage.selectSubtypeFromDropdown(subtype);
            Assert.assertTrue(result.url().contains("tab=insurance&type=" + subtype.type()), result.url());
            Assert.assertTrue(result.triggerText().contains(subtype.label()), result.triggerText());
            Assert.assertEquals(result.optionText(), subtype.label());
            Assert.assertEquals(result.checked(), "true");
            Assert.assertEquals(result.selected(), "true");
            Assert.assertEquals(result.selectedCount(), 1L);
            Assert.assertTrue(result.menuClosed());
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_013)
    public void triggerClosesDropdownAndKeepsSubtype() {
        var result = transactionPage.closeDropdownWithTrigger();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=insurance&type=25"), result.url());
        Assert.assertTrue(result.activeText().contains("Trừ phí VT Care"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_014)
    public void escapeClosesDropdownAndKeepsSubtype() {
        var result = transactionPage.closeDropdownWithEscape();
        Assert.assertEquals(result.expandedAfter(), "false");
        Assert.assertTrue(result.menuClosed());
        Assert.assertTrue(result.url().contains("tab=insurance&type=25"), result.url());
        Assert.assertTrue(result.activeText().contains("Trừ phí VT Care"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_034)
    public void clickingOutsideClosesWithoutChangingSubtype() {
        var result = transactionPage.closeDropdownByClickingOutside();
        Assert.assertTrue(result.menuClosed());
        Assert.assertEquals(result.afterUrl(), result.beforeUrl());
        Assert.assertEquals(result.afterText(), result.beforeText());
        Assert.assertTrue(result.afterText().contains("Trừ phí VT Care"));
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_035)
    public void keyboardSelectsRefundAndReturnsFocusToTrigger() {
        var result = transactionPage.selectNextSubtypeWithKeyboard();
        Assert.assertEquals(result.beforeKey(), "25");
        Assert.assertTrue(result.url().contains("tab=insurance&type=26"), result.url());
        Assert.assertTrue(result.activeText().contains("Hoàn phí VT Care"), result.activeText());
        Assert.assertTrue(result.menuClosed());
        Assert.assertEquals(result.focusedId(), result.triggerId());
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_036)
    public void switchingBothWaysUpdatesRouteLabelAndRows() {
        var result = transactionPage.switchAllSubtypesAndReturn();
        Assert.assertEquals(result.states().size(), 3);
        result.states().forEach(state -> {
            Assert.assertTrue(state.url().contains("tab=insurance&type=" + state.subtype().type()),
                    state.url());
            Assert.assertTrue(state.activeText().contains(state.subtype().label()), state.activeText());
            Assert.assertFalse(state.rowTypes().isEmpty(), "Không có dữ liệu " + state.subtype().label());
            state.rowTypes().forEach(type -> Assert.assertTrue(type.contains(state.subtype().label()),
                    state.subtype().label() + " <> " + type));
        });
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_037)
    public void refreshKeepsEachSubtypeSelected() {
        for (TransactionCategoryPage.Subtype subtype : category().subtypes()) {
            transactionPage.open(subtype);
            var result = transactionPage.refreshCurrentSubtype();
            Assert.assertEquals(result.afterUrl(), result.beforeUrl());
            Assert.assertEquals(result.afterText(), result.beforeText());
            Assert.assertTrue(result.afterText().contains(subtype.label()));
            Assert.assertEquals(result.options().stream()
                    .filter(option -> "true".equals(option.checked())).count(), 1L);
            Assert.assertTrue(result.options().stream().anyMatch(option ->
                    String.valueOf(subtype.type()).equals(option.key())
                            && "true".equals(option.checked())));
        }
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_INSURANCE_038)
    public void invalidTypeDoesNotSelectWrongSubtype() {
        var result = transactionPage.openInvalidSubtype(999999);
        Assert.assertTrue(result.url().contains("tab=insurance"), result.url());
        Assert.assertTrue(result.url().contains("type=999999"), result.url());
        Assert.assertTrue(result.options().stream().noneMatch(option ->
                String.valueOf(result.invalidType()).equals(option.key())));
        Assert.assertEquals(result.options().stream()
                .filter(option -> "true".equals(option.checked())).count(), 0L);
        Assert.assertEquals(result.activeText().trim(), "VT Care");
        Assert.assertTrue(category().subtypes().stream().noneMatch(subtype ->
                result.activeText().contains(subtype.label())), result.activeText());
    }
}
