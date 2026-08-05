package com.vuatho.tests.uniform.inventory.uniformstock.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformInventoryStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Testcase chuyển đổi hai cách xem dữ liệu của tab Tồn kho. */
public class StockViewTest
        extends UniformInventoryStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockViewTest.class,
                "Kho Đồng phục", "Chế độ xem Tồn kho");
    }

    /** Danh sách phải hiển thị các trường nghiệp vụ và dữ liệu lô thật. */
    @Test(groups = {"uniform", "inventory", "stock", "view", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_007)
    public void listViewShowsColumnsAndRows() {
        var list = inventoryPage.switchToListView();
        Assert.assertTrue(list.selected(), "Nút Danh sách chưa được chọn.");
        for (String header : List.of(
                "san pham", "gia nhap", "ton", "ngay nhap", "xuat gan nhat")) {
            Assert.assertTrue(list.normalizedContent().contains(header),
                    "Danh sách thiếu cột: " + header);
        }
        Assert.assertFalse(list.rows().isEmpty(),
                "Chế độ Danh sách không có dữ liệu.");
        Assert.assertTrue(list.rows().stream().allMatch(row -> row.parts().size() >= 5),
                "Có dòng Danh sách thiếu trường dữ liệu.");
    }

    /** Hai nút phải chuyển trạng thái và khôi phục lại Lưới tháng có dữ liệu. */
    @Test(groups = {"uniform", "inventory", "stock", "view", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_008)
    public void switchesListAndBackToMonthlyGrid() {
        var result = inventoryPage.switchListAndBackToGrid();
        Assert.assertTrue(result.listSelected(),
                "Không chuyển được sang Danh sách.");
        Assert.assertTrue(result.gridSelectedAfterBack(),
                "Không quay lại được Lưới tháng.");
        Assert.assertTrue(result.gridHasData(),
                "Lưới tháng không khôi phục dữ liệu sau khi quay lại.");
    }

    /** Khi mới mở tab, chỉ Lưới tháng được chọn để tránh hai view cùng active. */
    @Test(groups = {"uniform", "inventory", "stock", "view", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_024)
    public void monthlyGridIsTheOnlyDefaultSelectedView() {
        var selection = inventoryPage.defaultViewSelection();
        Assert.assertTrue(selection.gridSelected(),
                "Lưới tháng không được chọn mặc định.");
        Assert.assertFalse(selection.listSelected(),
                "Danh sách bị chọn đồng thời với Lưới tháng.");
    }

    /** Cùng mã lô phải có cùng số tồn ở Lưới tháng và Danh sách. */
    @Test(groups = {"uniform", "inventory", "stock", "view", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_025)
    public void bothViewsShowConsistentStockData() {
        var snapshot = inventoryPage.viewDataConsistency();
        Assert.assertFalse(snapshot.gridRows().isEmpty(),
                "Lưới tháng không có dữ liệu để đối chiếu.");
        Assert.assertFalse(snapshot.listRows().isEmpty(),
                "Danh sách không có dữ liệu để đối chiếu.");
        Assert.assertTrue(snapshot.selection().listSelected()
                        && !snapshot.selection().gridSelected(),
                "Hai nút chế độ xem không chuyển trạng thái loại trừ nhau.");

        Map<String, com.vuatho.pages.UniformInventoryPage.StockRow> gridByCode =
                snapshot.gridRows().stream().collect(Collectors.toMap(
                        row -> row.code(), Function.identity()));
        Assert.assertEquals(
                snapshot.listRows().stream().map(row -> row.code()).collect(Collectors.toSet()),
                gridByCode.keySet(),
                "Hai chế độ xem không hiển thị cùng tập mã lô.");
        for (var row : snapshot.listRows()) {
            Assert.assertEquals(row.stock(), gridByCode.get(row.code()).stock(),
                    "Số tồn thay đổi khi chuyển chế độ ở mã " + row.code());
        }
    }

    /** Mỗi dòng Danh sách phải chứa dữ liệu nghiệp vụ hợp lệ, không chỉ đủ số cột. */
    @Test(groups = {"uniform", "inventory", "stock", "view", "validation",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_026)
    public void listViewRowsContainValidBusinessData() {
        var list = inventoryPage.switchToListView();
        Assert.assertFalse(list.rows().isEmpty(),
                "Danh sách không có dòng dữ liệu để kiểm tra.");
        for (var row : list.rows()) {
            Assert.assertTrue(row.parts().get(0).contains(row.code()),
                    "Cột sản phẩm thiếu mã lô " + row.code());
            Assert.assertTrue(row.parts().get(1).matches("(?s).*\\d.*"),
                    "Giá nhập không có giá trị số ở mã " + row.code());
            Assert.assertTrue(row.stock() >= 0,
                    "Số tồn không hợp lệ ở mã " + row.code());
            Assert.assertFalse(row.importDate().isBlank(),
                    "Ngày nhập không hợp lệ ở mã " + row.code());
            Assert.assertFalse(row.latestExport().isBlank(),
                    "Cột xuất gần nhất bị trống ở mã " + row.code());
        }
    }

    /** Chuyển ba vòng không được làm mất, nhân đôi hoặc thay đổi tập mã lô. */
    @Test(groups = {"uniform", "inventory", "stock", "view", "stability",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_027)
    public void repeatedViewSwitchKeepsDataStable() {
        var result = inventoryPage.switchViewsRepeatedly();
        Assert.assertFalse(result.originalCodes().isEmpty(),
                "Không có dữ liệu gốc để kiểm tra chuyển view.");
        Assert.assertFalse(result.listCodeSnapshots().isEmpty(),
                "Không có dữ liệu Danh sách sau khi chuyển view.");
        Assert.assertTrue(result.exclusiveSelection(),
                "Có lần hai chế độ cùng được chọn hoặc không có chế độ nào được chọn.");
        List<String> expectedListOrder = result.listCodeSnapshots().get(0);
        for (List<String> codes : result.listCodeSnapshots()) {
            Assert.assertEquals(codes, expectedListOrder,
                    "Danh sách thay đổi thứ tự hoặc dữ liệu giữa các lần chuyển view.");
            Assert.assertEquals(codes.stream().collect(Collectors.toSet()),
                    result.originalCodes().stream().collect(Collectors.toSet()),
                    "Danh sách bị mất hoặc thêm mã lô sau khi chuyển view.");
            Assert.assertEquals(codes.stream().distinct().count(), (long) codes.size(),
                    "Danh sách xuất hiện mã lô trùng lặp sau khi chuyển view.");
        }
        for (List<String> codes : result.gridCodeSnapshots()) {
            Assert.assertEquals(codes, result.originalCodes(),
                    "Lưới tháng thay đổi dữ liệu sau khi chuyển view.");
            Assert.assertEquals(codes.stream().distinct().count(), (long) codes.size(),
                    "Lưới tháng xuất hiện mã lô trùng lặp sau khi chuyển view.");
        }
    }
}
