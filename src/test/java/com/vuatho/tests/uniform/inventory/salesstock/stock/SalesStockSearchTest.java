package com.vuatho.tests.uniform.inventory.salesstock.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.SalesStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Testcase tìm kiếm mã lô của Kho bán hàng. */
public class SalesStockSearchTest extends SalesStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(SalesStockSearchTest.class, "Kho bán hàng", "Tìm kiếm Tồn kho");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_008)
    public void searchesExistingLotExactly() {
        String code = firstCode();
        var result = salesStockPage.search(code);
        Assert.assertFalse(result.rows().isEmpty(), "Mã đang có nhưng không trả dữ liệu.");
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.code().equals(code)));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_009)
    public void searchesCaseInsensitively() {
        String code = firstCode();
        var result = salesStockPage.search(code.toLowerCase());
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.code().equalsIgnoreCase(code)));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "partial"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_010)
    public void searchesPartialLotCode() {
        List<String> codes = salesStockPage.openStock().stockRows().stream().map(row -> row.code()).toList();
        String prefix = sharedPrefix(codes);
        Assert.assertFalse(prefix.isBlank(), "Không có prefix chung của ít nhất hai mã lô.");
        var result = salesStockPage.search(prefix);
        Assert.assertTrue(result.rows().size() >= 2, "Tìm một phần không trả nhiều kết quả.");
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.code().startsWith(prefix)));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "empty"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_011)
    public void unknownCodeShowsEmptyState() {
        var result = salesStockPage.search("AUTOMATION-KHONG-TON-TAI");
        Assert.assertTrue(result.rows().isEmpty());
        Assert.assertTrue(result.emptyState(), "Thiếu trạng thái rỗng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "clear"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_012)
    public void clearRestoresRows() {
        var result = salesStockPage.searchThenClear();
        Assert.assertTrue(result.initialCount() > 0);
        Assert.assertEquals(result.restoredCount(), result.initialCount());
        Assert.assertTrue(result.inputValue().isBlank());
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "replace"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_013)
    public void replacingKeywordLoadsSecondResult() {
        var rows = salesStockPage.openStock().stockRows();
        Assert.assertTrue(rows.size() >= 2, "Không đủ hai lô để thay từ khóa.");
        String first = rows.get(0).code();
        String second = rows.stream().map(row -> row.code()).filter(code -> !code.equals(first)).findFirst().orElseThrow();
        var result = salesStockPage.replaceSearchKeyword(first, second);
        Assert.assertFalse(result.second().rows().isEmpty());
        Assert.assertTrue(result.second().rows().stream().allMatch(row -> row.code().equals(second)));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "view"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_014)
    public void searchesInListView() {
        String code = firstCode();
        var result = salesStockPage.searchInListView(code);
        Assert.assertFalse(result.rows().isEmpty());
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.code().equals(code)));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "view"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_015)
    public void switchingViewKeepsSearch() {
        String code = firstCode();
        var result = salesStockPage.searchAndSwitchToList(code);
        Assert.assertTrue(result.listSelected());
        Assert.assertEquals(result.inputValueAfterSwitch(), code);
        Assert.assertTrue(result.listRows().stream().allMatch(row -> row.code().equals(code)));
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "whitespace"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_047)
    public void doesNotTrimWhitespaceAroundLotCode() {
        String code = firstCode();
        String keyword = " " + code + " ";
        var result = salesStockPage.searchLiteral(keyword);
        Assert.assertEquals(result.inputValue(), keyword,
                "Ô tìm kiếm đã tự loại bỏ khoảng trắng đầu hoặc cuối.");
        Assert.assertFalse(result.rows().isEmpty(),
                "Bộ lọc không nhận diện được mã lô hợp lệ nằm giữa khoảng trắng.");
        Assert.assertTrue(result.rows().stream().allMatch(row -> row.code().equals(code)),
                "Bộ lọc trả dữ liệu không đúng mã lô khi từ khóa có khoảng trắng.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "scope"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_048)
    public void doesNotSearchByUniformName() {
        var rows = salesStockPage.salesGridRows();
        String productName = rows.stream()
                .map(row -> row.name().trim())
                .filter(name -> !name.isBlank())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Không có tên đồng phục để kiểm tra phạm vi tìm kiếm."));
        var result = salesStockPage.searchLiteral(productName);
        Assert.assertTrue(result.rows().isEmpty(),
                "Ô tìm kiếm trả dữ liệu theo tên đồng phục thay vì chỉ theo mã lô.");
        Assert.assertTrue(result.emptyState(), "Thiếu trạng thái rỗng khi tìm bằng tên đồng phục.");
    }

    @Test(groups = {"uniform", "inventory", "sales-stock", "search", "reload"},
            description = UniformInventoryTestCases.UNI_INV_SALES_STOCK_049)
    public void reloadClearsKeywordAndRestoresRows() {
        String code = firstCode();
        var result = salesStockPage.searchThenReload(code);
        Assert.assertTrue(result.filteredCount() > 0, "Mã lô chuẩn bị không trả dữ liệu trước khi reload.");
        Assert.assertTrue(result.inputValueAfterReload().isBlank(),
                "Từ khóa tìm kiếm vẫn còn sau khi tải lại trang.");
        Assert.assertEquals(result.restoredCount(), result.initialCount(),
                "Tải lại trang chưa khôi phục đầy đủ dữ liệu tồn kho.");
    }

    private String firstCode() {
        var rows = salesStockPage.openStock().stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có mã lô làm dữ liệu tìm kiếm.");
        return rows.get(0).code();
    }

    private String sharedPrefix(List<String> codes) {
        for (String code : codes) {
            for (int length = code.length() - 1; length >= 2; length--) {
                String prefix = code.substring(0, length);
                if (codes.stream().filter(candidate -> candidate.startsWith(prefix)).count() >= 2) {
                    return prefix;
                }
            }
        }
        return "";
    }
}
