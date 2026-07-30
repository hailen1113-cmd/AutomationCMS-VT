package com.vuatho.tests.uniform.inventory;

import com.vuatho.testcases.UniformInventoryTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformInventoryPage.DialogSnapshot;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra popup nghiệp vụ kho và validation khi chưa chọn lô. */
public class UniformInventoryDialogTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformInventoryDialogTest.class,
                "Đồng phục", "Popup nghiệp vụ kho");
    }

    /** Các popup phải hiển thị đúng nội dung và khóa submit khi chưa có lô. */
    @Test(dataProvider = "warehouseActions",
            groups = {"uniform", "inventory", "dialog", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_001)
    public void warehouseActionDialogHasRequiredFields(
            String warehouse, String action, String[] expectedText) {
        inventoryPage.open().selectWarehouse(warehouse).selectSection("Tồn kho");
        DialogSnapshot dialog = inventoryPage.openActionDialog(action);
        for (String expected : expectedText) {
            Assert.assertTrue(dialog.text().contains(expected),
                    action + " tại " + warehouse + " thiếu " + expected
                            + ". Nội dung: " + dialog.text());
        }
        Assert.assertTrue(dialog.confirmDisabled()
                        || inventoryPage.submitEmptyDialogKeepsFormOpen(),
                "Form " + action
                        + " cho submit dù chưa chọn/nhập lô hợp lệ.");
    }

    @DataProvider(name = "warehouseActions")
    public Object[][] warehouseActions() {
        return new Object[][]{
                {"Kho tổng", "Điều chỉnh tồn", new String[]{
                        "Ngày điều chỉnh", "Lý do điều chỉnh",
                        "Thêm lô cần điều chỉnh", "Lô thay đổi"}},
                {"Kho tổng", "Nhập kho", new String[]{
                        "Ngày", "Ghi chú", "Thêm sản phẩm"}},
                {"Kho bán hàng", "Điều chỉnh tồn", new String[]{
                        "Kiểm kê Kho bán hàng", "Ngày điều chỉnh",
                        "Lý do điều chỉnh", "Thêm lô cần điều chỉnh"}},
                {"Kho bán hàng", "Xuất hàng", new String[]{
                        "Xuất hàng cho nhân sự", "Ngày", "Ghi chú",
                        "Thêm lô"}},
                {"Kho bán hàng", "Nhập hàng", new String[]{
                        "Kho tổng", "Kho bán hàng", "Ngày",
                        "Ghi chú", "Thêm lô"}}
        };
    }
}
