package com.vuatho.tests.uniform.inventory;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra KPI, tồn kho, tìm mã lô và hai chế độ xem của Kho Đồng phục. */
public class UniformInventoryOverviewTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformInventoryOverviewTest.class,
                "Đồng phục", "Tổng quan kho");
    }

    /** Mỗi kho phải trả KPI, cảnh báo tồn và đúng nhóm action. */
    @Test(dataProvider = "warehouses",
            groups = {"uniform", "inventory", "data-interaction"},
            description = "UNIFORM-INVENTORY-001: Hai kho trả KPI và action phù hợp")
    public void eachWarehouseReturnsOverviewAndActions(
            String warehouse, String[] actions) {
        inventoryPage.open().selectWarehouse(warehouse);
        String text = inventoryPage.mainText();
        String normalizedText = text.toUpperCase();
        Assert.assertEquals(inventoryPage.selectedWarehouse(), warehouse);
        Assert.assertTrue(normalizedText.contains("TỔNG TỒN KHO"),
                warehouse + " thiếu KPI tổng tồn.");
        Assert.assertTrue(normalizedText.contains("LÔ SẮP HẾT"),
                warehouse + " thiếu cảnh báo lô sắp hết.");
        for (String action : actions) {
            Assert.assertTrue(inventoryPage.hasAction(action),
                    warehouse + " thiếu action " + action);
        }
    }

    /** Lưới tháng và Danh sách phải trả hai cấu trúc dữ liệu khác nhau. */
    @Test(dataProvider = "warehouseNames",
            groups = {"uniform", "inventory", "view-mode", "data-interaction"},
            description = "UNIFORM-INVENTORY-002: Lưới tháng và Danh sách trả dữ liệu")
    public void bothInventoryViewModesReturnData(String warehouse) {
        inventoryPage.open().selectWarehouse(warehouse)
                .selectSection("Tồn kho")
                .selectViewMode("Lưới tháng");
        Assert.assertTrue(inventoryPage.mainText().contains("Lưới tháng"));
        Assert.assertFalse(inventoryPage.firstLotCode().isBlank(),
                "Lưới tháng không trả mã lô.");

        inventoryPage.selectViewMode("Danh sách");
        String list = inventoryPage.mainText();
        Assert.assertTrue(list.contains("SẢN PHẨM")
                        && list.contains("TỒN")
                        && list.contains("NGÀY NHẬP"),
                "Danh sách thiếu cột dữ liệu tồn kho.");
    }

    /** Tìm mã lô lấy động từ bảng phải giữ lại đúng lô trong kết quả. */
    @Test(dataProvider = "warehouseNames",
            groups = {"uniform", "inventory", "search", "data-interaction"},
            description = "UNIFORM-INVENTORY-003: Tìm mã lô trả đúng dữ liệu")
    public void searchLotReturnsMatchingData(String warehouse) {
        inventoryPage.open().selectWarehouse(warehouse)
                .selectSection("Tồn kho");
        String code = inventoryPage.firstLotCode();
        Assert.assertFalse(code.isBlank(), "Không lấy được mã lô để tìm.");
        inventoryPage.searchLot(code);
        Assert.assertTrue(inventoryPage.mainText().contains(code),
                "Kết quả không chứa mã lô " + code);
    }

    @DataProvider(name = "warehouses")
    public Object[][] warehouses() {
        return new Object[][]{
                {"Kho tổng", new String[]{"Tồn kho", "Phiếu",
                        "Điều chỉnh tồn", "Nhập kho"}},
                {"Kho bán hàng", new String[]{"Tồn kho", "Phiếu",
                        "Điều chỉnh tồn", "Xuất hàng", "Nhập hàng"}}
        };
    }

    @DataProvider(name = "warehouseNames")
    public Object[][] warehouseNames() {
        return new Object[][]{{"Kho tổng"}, {"Kho bán hàng"}};
    }
}
