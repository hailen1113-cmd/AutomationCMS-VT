package com.vuatho.tests.uniform.inventory;

import com.vuatho.testcases.UniformInventoryTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra danh sách và từng loại phiếu của Kho tổng/Kho bán hàng. */
public class UniformInventoryReceiptTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformInventoryReceiptTest.class,
                "Đồng phục", "Phiếu kho");
    }

    /** Section Phiếu phải trả đúng cột và dữ liệu mã phiếu. */
    @Test(dataProvider = "warehouseNames",
            groups = {"uniform", "inventory", "receipt", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_005)
    public void receiptListReturnsData(String warehouse) {
        inventoryPage.open().selectWarehouse(warehouse).selectSection("Phiếu");
        String text = inventoryPage.mainText();
        Assert.assertTrue(text.contains("MÃ PHIẾU")
                        && text.contains("LOẠI")
                        && text.contains("CHI TIẾT")
                        && text.contains("NGÀY"),
                warehouse + " thiếu cột danh sách phiếu.");
        Assert.assertTrue(text.matches("(?s).*\\b(?:NK|CK|XD|XNS|DC)-\\d{4}-\\d+\\b.*"),
                warehouse + " không trả mã phiếu.");
    }

    /** Mỗi loại phiếu phải lọc mà không làm mất cấu trúc danh sách. */
    @Test(dataProvider = "receiptTypes",
            groups = {"uniform", "inventory", "receipt-filter", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_006)
    public void eachReceiptTypeCanBeFiltered(String warehouse, String type) {
        inventoryPage.open().selectWarehouse(warehouse).selectSection("Phiếu");
        inventoryPage.selectReceiptType(type);
        String text = inventoryPage.mainText();
        Assert.assertTrue(text.contains("MÃ PHIẾU") && text.contains(type),
                "Lọc " + type + " làm mất danh sách hoặc nhãn bộ lọc.");
    }

    @DataProvider(name = "warehouseNames")
    public Object[][] warehouseNames() {
        return new Object[][]{{"Kho tổng"}, {"Kho bán hàng"}};
    }

    @DataProvider(name = "receiptTypes")
    public Object[][] receiptTypes() {
        return new Object[][]{
                {"Kho tổng", "Nhập kho"},
                {"Kho tổng", "Chuyển sang bán"},
                {"Kho tổng", "Điều chỉnh tồn"},
                {"Kho bán hàng", "Nhập từ kho tổng"},
                {"Kho bán hàng", "Xuất đơn"},
                {"Kho bán hàng", "Xuất nhân sự"},
                {"Kho bán hàng", "Điều chỉnh tồn"}
        };
    }
}
