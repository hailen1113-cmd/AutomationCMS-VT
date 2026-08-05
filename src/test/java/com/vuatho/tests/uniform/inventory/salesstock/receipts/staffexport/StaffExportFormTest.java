
package com.vuatho.tests.uniform.inventory.salesstock.receipts.staffexport;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockStaffExportTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra thao tác nhập liệu trên form Xuất hàng cho nhân sự. */
public class StaffExportFormTest extends SalesStockStaffExportTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StaffExportFormTest.class, "Kho bán hàng", "Form xuất hàng cho nhân sự");
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_067)
    public void showsStaffExportFormWithInitialDisabledState() {
        var form = exportPage.formSnapshot();
        Assert.assertFalse(form.date().isBlank());
        Assert.assertTrue(form.dateRequired());
        Assert.assertEquals(form.note(), "");
        Assert.assertTrue(form.lotCombobox());
        Assert.assertEquals(form.selectedLots(), 0);
        Assert.assertEquals(form.totalQuantity(), 0);
        Assert.assertFalse(form.submitEnabled());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_075)
    public void showsSalesWarehouseAndHumanResourcesContext() {
        var form = exportPage.formSnapshot();
        Assert.assertTrue(form.text().contains("Kho bán hàng"));
        Assert.assertTrue(form.text().contains("Nhân sự / HR"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_076)
    public void acceptsPastAndFutureExportDates() {
        var result = exportPage.acceptsPastAndFutureDates();
        Assert.assertEquals(result.actualPast(), result.expectedPast());
        Assert.assertEquals(result.actualFuture(), result.expectedFuture());
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_077)
    public void preservesLongUnicodeAndSpecialCharacterNote() {
        var result = exportPage.entersLongUnicodeNote();
        Assert.assertEquals(result.actual(), result.expected());
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

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_078)
    public void parsesThousandsSeparatedExportQuantity() {
        var result = exportPage.entersThousandsSeparatedQuantity();
        Assert.assertFalse(result.code().isBlank());
        Assert.assertEquals(result.quantity(), "1,111");
        Assert.assertEquals(result.totalQuantity(), 1_111);
        Assert.assertFalse(result.error().contains("Số lượng không hợp lệ"));
    }

    @Test(description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_079)
    public void removingProductRemovesAllOfItsLots() {
        var result = exportPage.removesProductAndAllItsLots();
        Assert.assertFalse(result.productName().isBlank());
        Assert.assertTrue(result.lotsBeforeRemoval() > 1,
                "Sản phẩm kiểm tra phải có nhiều lô trước khi gỡ.");
        Assert.assertTrue(result.productRemoved(), "Gỡ sản phẩm nhưng card vẫn còn hiển thị.");
        Assert.assertEquals(result.selectedLots(), 0);
        Assert.assertEquals(result.totalQuantity(), 0);
        Assert.assertFalse(result.submitEnabled());
    }
}
