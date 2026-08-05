package com.vuatho.tests.uniform.inventory.salesstock.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Testcase chuyển đổi Lưới tháng và Danh sách của Kho bán hàng. */
public class SalesStockViewTest extends SalesStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockViewTest.class, "Kho bán hàng", "Chế độ xem Tồn kho");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "view"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_016)
    public void monthlyGridIsOnlyDefaultView() {
        var selection = salesStockPage.defaultViewSelection();
        Assert.assertTrue(selection.gridSelected());
        Assert.assertFalse(selection.listSelected(), "Hai chế độ xem cùng được chọn.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "view", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_017)
    public void listShowsColumnsAndRows() {
        var list = salesStockPage.switchToListView();
        Assert.assertTrue(list.selected());
        for (String header : List.of("san pham", "gia nhap", "ton", "ngay nhap", "xuat gan nhat")) {
            Assert.assertTrue(list.normalizedContent().contains(header), "Thiếu cột: " + header);
        }
        Assert.assertFalse(list.rows().isEmpty(), "Danh sách không có dữ liệu.");
        Assert.assertTrue(list.rows().stream().allMatch(row -> row.parts().size() >= 5));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "view", "scroll"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_018)
    public void switchesListAndBackToGrid() {
        var result = salesStockPage.switchListAndBackToGrid();
        Assert.assertTrue(result.listSelected());
        Assert.assertTrue(result.gridSelectedAfterBack());
        Assert.assertTrue(result.gridHasData());
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "view", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_019)
    public void viewsShowConsistentStock() {
        var snapshot = salesStockPage.viewDataConsistency();
        Assert.assertFalse(snapshot.gridRows().isEmpty());
        Assert.assertFalse(snapshot.listRows().isEmpty());
        Map<String, com.vuatho.pages.UniformInventoryPage.StockRow> byCode =
                snapshot.gridRows().stream().collect(Collectors.toMap(row -> row.code(), Function.identity()));
        for (var row : snapshot.listRows()) {
            Assert.assertTrue(byCode.containsKey(row.code()), "Danh sách có mã không thuộc Lưới tháng: " + row.code());
            Assert.assertEquals(row.stock(), byCode.get(row.code()).stock(),
                    "Số tồn không nhất quán ở mã " + row.code());
        }
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "view", "stability"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_020)
    public void repeatedSwitchKeepsDataStable() {
        var result = salesStockPage.switchViewsRepeatedly();
        Assert.assertFalse(result.originalCodes().isEmpty());
        Assert.assertTrue(result.exclusiveSelection(), "Hai nút không duy trì trạng thái loại trừ.");
        for (List<String> codes : result.gridCodeSnapshots()) {
            Assert.assertEquals(codes, result.originalCodes(), "Lưới tháng thay đổi sau khi chuyển view.");
        }
        List<String> expectedList = result.listCodeSnapshots().get(0);
        for (List<String> codes : result.listCodeSnapshots()) {
            Assert.assertEquals(codes, expectedList, "Danh sách thay đổi sau khi chuyển view.");
        }
    }
}
