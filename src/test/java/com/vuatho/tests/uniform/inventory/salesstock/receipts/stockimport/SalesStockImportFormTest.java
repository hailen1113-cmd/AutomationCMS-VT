package com.vuatho.tests.uniform.inventory.salesstock.receipts.stockimport;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockImportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra cấu trúc và thao tác trên form Nhập hàng từ Kho tổng. */
public class SalesStockImportFormTest extends SalesStockImportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockImportFormTest.class, "Kho bán hàng", "Form Nhập hàng");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_122)
    public void showsInitialImportForm() {
        var result = importPage.formSnapshot();
        Assert.assertFalse(result.date().isBlank());
        Assert.assertTrue(result.dateRequired());
        Assert.assertEquals(result.note(), "");
        Assert.assertTrue(result.lotCombobox());
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_123)
    public void showsMainToSalesWarehouseScope() {
        var result = importPage.formSnapshot();
        Assert.assertTrue(result.text().contains("Kho tổng"));
        Assert.assertTrue(result.text().contains("Kho bán hàng"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_124)
    public void cancelsEmptyImportForm() {
        Assert.assertTrue(importPage.cancelEmptyForm().dialogClosed());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_125)
    public void closesEmptyImportFormByX() {
        Assert.assertTrue(importPage.closeEmptyForm().dialogClosed());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_126)
    public void searchesExactLotCode() {
        var result = importPage.searchLots("VT20");
        Assert.assertFalse(result.options().isEmpty());
        Assert.assertTrue(result.options().stream().allMatch(option -> option.contains("VT20")));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_127)
    public void searchesLotsByProductName() {
        var result = importPage.searchLots("Đồ bảo hộ");
        Assert.assertTrue(result.options().size() > 1);
        Assert.assertTrue(result.options().stream().allMatch(option -> option.contains("Đồ bảo hộ")));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_128)
    public void unknownLotReturnsNoOptions() {
        var result = importPage.searchLots("LOT-KHONG-TON-TAI-908172");
        Assert.assertTrue(result.options().isEmpty());
        Assert.assertTrue(result.formText().contains("No results found")
                || result.formText().contains("Không có"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_129)
    public void selectingLotShowsStockAndZeroCounters() {
        var result = importPage.selectAvailableLot();
        Assert.assertFalse(result.code().isBlank());
        Assert.assertTrue(result.stock() > 0);
        Assert.assertTrue(result.rowText().contains(result.code()));
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_130)
    public void enteringQuantityUpdatesCountersAndSubmission() {
        var result = importPage.selectLotAndSetOne();
        Assert.assertEquals(result.quantity(), "1");
        Assert.assertEquals(result.selectedLots(), 1);
        Assert.assertEquals(result.totalQuantity(), 1);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_131)
    public void selectedLotCannotBeAddedTwice() {
        var result = importPage.selectedLotIsExcludedFromSuggestions();
        Assert.assertFalse(result.duplicateOptionVisible());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_132)
    public void selectsAndGroupsMultipleLotsInOneDropdownSession() {
        var result = importPage.selectsTwoLotsInOneDropdownSession();
        Assert.assertNotEquals(result.firstCode(), result.secondCode());
        Assert.assertEquals(result.productLotCount(), 2);
        Assert.assertTrue(result.dropdownClosed());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_133)
    public void removesOneSelectedLot() {
        var result = importPage.addAndRemoveLot();
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_134)
    public void removesAllLotsOfAProduct() {
        var result = importPage.removesProductAndAllLots();
        Assert.assertTrue(result.lotsBefore() > 1);
        Assert.assertTrue(result.removed());
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_135)
    public void scrollsLongSuggestionListDownAndBack() {
        var result = importPage.scrollSuggestionsDownAndBack();
        Assert.assertTrue(result.optionCount() > 1);
        Assert.assertTrue(result.reachedBottom());
        Assert.assertTrue(result.returnedTop());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_136)
    public void preservesUnlimitedUnicodeNote() {
        var result = importPage.entersLongUnicodeNote();
        Assert.assertEquals(result.actual(), result.expected());
        Assert.assertTrue(result.maxlength() == null || result.maxlength().isBlank());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_155)
    public void clearingSearchRestoresAllSuggestions() {
        var result = importPage.clearingKeywordRestoresSuggestions();
        Assert.assertEquals(result.keyword(), "");
        Assert.assertTrue(result.restoredCount() > result.filteredCount());
        Assert.assertTrue(result.options().size() > 1);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_156)
    public void replacingKeywordRefreshesSuggestionResults() {
        var result = importPage.replacingKeywordRefreshesResults();
        Assert.assertEquals(result.keyword(), "VT01");
        Assert.assertTrue(result.firstOptions().stream().allMatch(option -> option.contains("VT20")));
        Assert.assertTrue(result.secondOptions().stream().allMatch(option -> option.contains("VT01")));
        Assert.assertNotEquals(result.firstOptions(), result.secondOptions());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_157)
    public void selectsLotWithKeyboardAndClosesDropdown() {
        var result = importPage.selectsLotUsingKeyboard();
        Assert.assertTrue(result.selected());
        Assert.assertTrue(result.dropdownClosed());
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_158)
    public void selectingDifferentProductsCreatesSeparateCards() {
        var result = importPage.selectsLotsFromDifferentProducts();
        Assert.assertNotEquals(result.firstProduct(), result.secondProduct());
        Assert.assertEquals(result.productCards(), 2);
        Assert.assertTrue(result.firstVisible());
        Assert.assertTrue(result.secondVisible());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_159)
    public void removingOneLotPreservesSiblingLot() {
        var result = importPage.removingOneLotPreservesSibling();
        Assert.assertTrue(result.removed());
        Assert.assertTrue(result.siblingVisible());
        Assert.assertEquals(result.remainingLots(), 1);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_160)
    public void removingOneProductPreservesOtherProduct() {
        var result = importPage.removingProductPreservesOtherProduct();
        Assert.assertEquals(result.cardsBefore(), 2);
        Assert.assertEquals(result.cardsAfter(), 1);
        Assert.assertTrue(result.removedProductAbsent());
        Assert.assertTrue(result.otherProductVisible());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_161)
    public void reopeningFormDoesNotKeepPreviousDraft() {
        var result = importPage.reopeningFormClearsDraft();
        Assert.assertEquals(result.note(), "");
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertEquals(result.quantityInputs(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_162)
    public void scrollsLongSelectedLotListWithoutLosingData() {
        var result = importPage.scrollLongSelectedLotList();
        Assert.assertTrue(result.lotCount() >= 8);
        Assert.assertTrue(result.reachedBottom());
        Assert.assertTrue(result.returnedTop());
        Assert.assertTrue(result.allLotsPreserved());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_163)
    public void differentQuantitiesProduceCorrectLotCountAndTotal() {
        var result = importPage.totalsDifferentLotQuantities();
        Assert.assertEquals(result.quantities(), java.util.List.of("1", "2", "3"));
        Assert.assertEquals(result.selectedLots(), 3);
        Assert.assertEquals(result.totalQuantity(), 6);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_164)
    public void closingDropdownPreservesSelectedLot() {
        var result = importPage.closesDropdownWithoutLosingSelection();
        Assert.assertTrue(result.opened());
        Assert.assertTrue(result.closed());
        Assert.assertTrue(result.selectedLotPreserved());
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_152)
    public void cancellingPreparedImportDoesNotChangeStock() {
        var result = importPage.cancelPreparedImport(false);
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.mainAfter(), result.mainBefore());
        Assert.assertEquals(result.salesAfter(), result.salesBefore());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_153)
    public void closingPreparedImportDoesNotChangeStock() {
        var result = importPage.cancelPreparedImport(true);
        Assert.assertTrue(result.dialogClosed());
        Assert.assertEquals(result.mainAfter(), result.mainBefore());
        Assert.assertEquals(result.salesAfter(), result.salesBefore());
    }
}
