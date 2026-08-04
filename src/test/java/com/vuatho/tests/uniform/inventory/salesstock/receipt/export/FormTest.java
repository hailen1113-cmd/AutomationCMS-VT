
package com.vuatho.tests.uniform.inventory.salesstock.receipt.export;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockStaffExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra thao tác nhập liệu trên form Xuất hàng cho nhân sự. */
public class FormTest extends SalesStockStaffExportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(FormTest.class, "Kho bán hàng", "Form xuất hàng cho nhân sự");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_067)
    public void showsStaffExportFormWithInitialDisabledState() {
        var form = exportPage.formSnapshot();
        Assert.assertFalse(form.date().isBlank());
        Assert.assertTrue(form.lotCombobox());
        Assert.assertEquals(form.selectedLots(), 0);
        Assert.assertEquals(form.totalQuantity(), 0);
        Assert.assertFalse(form.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_068)
    public void searchesLotsByCodeAndProductName() {
        var byCode = exportPage.searchLots("VT");
        Assert.assertFalse(byCode.options().isEmpty());
        Assert.assertTrue(byCode.options().stream().allMatch(option -> option.contains("VT")));
        var byName = exportPage.searchLots("Áo thun");
        Assert.assertFalse(byName.options().isEmpty());
        Assert.assertTrue(byName.options().stream().anyMatch(option -> option.contains("Áo thun")));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_069)
    public void selectingLotAndQuantityUpdatesTotals() {
        var result = exportPage.addAvailableLotAndSetOne();
        Assert.assertFalse(result.code().isBlank());
        Assert.assertEquals(result.quantity(), "1");
        Assert.assertEquals(result.selectedLots(), 1);
        Assert.assertEquals(result.totalQuantity(), 1);
        Assert.assertTrue(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_070)
    public void removingSelectedLotRestoresInitialState() {
        var result = exportPage.addAndRemoveLot();
        Assert.assertFalse(result.code().isBlank());
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_071)
    public void scrollsLotSuggestionsDownAndBack() {
        var result = exportPage.scrollLotSuggestionsDownAndBack();
        Assert.assertTrue(result.optionCount() > 1);
        Assert.assertTrue(result.reachedLast());
        Assert.assertTrue(result.returnedFirst());
    }
}
