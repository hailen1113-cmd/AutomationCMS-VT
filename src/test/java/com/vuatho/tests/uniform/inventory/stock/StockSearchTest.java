package com.vuatho.tests.uniform.inventory.stock;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformInventoryStockTestSupport;
import com.vuatho.testcases.UniformInventoryTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Testcase tìm kiếm theo mã lô bằng dữ liệu động trên tab Tồn kho. */
public class StockSearchTest
        extends UniformInventoryStockTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(StockSearchTest.class,
                "Kho Đồng phục", "Tìm kiếm Tồn kho");
    }

    /** Lấy mã đầu tiên đang có, không phụ thuộc ID lô cố định. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_009)
    public void searchesExistingLotCode() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có mã lô làm dữ liệu tìm kiếm.");
        String code = rows.get(0).code();
        var result = inventoryPage.search(code);
        Assert.assertEquals(result.inputValue(), code);
        Assert.assertFalse(result.rows().isEmpty(),
                "Tìm mã đang tồn tại nhưng không trả dữ liệu.");
        Assert.assertTrue(result.rows().stream()
                        .allMatch(row -> row.code().equals(code)),
                "Kết quả có mã lô khác từ khóa chính xác.");
    }

    /** Nhập mã bằng chữ thường vẫn phải tìm được mã lô viết hoa. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_010)
    public void searchesLotCodeCaseInsensitively() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có mã lô làm dữ liệu tìm kiếm.");
        String keyword = rows.get(0).code().toLowerCase();
        var result = inventoryPage.search(keyword);
        Assert.assertFalse(result.rows().isEmpty(),
                "Tìm mã chữ thường nhưng không trả dữ liệu.");
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                        row.code().equalsIgnoreCase(keyword)),
                "Kết quả không khớp mã lô khi bỏ qua hoa thường.");
    }

    /** Từ khóa chắc chắn không tồn tại phải trả empty-state rõ ràng. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "empty",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_011)
    public void unknownLotCodeShowsEmptyState() {
        var result = inventoryPage.search("AUTOMATION-KHONG-TON-TAI");
        Assert.assertTrue(result.rows().isEmpty(),
                "Từ khóa không tồn tại vẫn trả dòng dữ liệu.");
        Assert.assertTrue(result.emptyState(),
                "Không hiển thị trạng thái rỗng sau khi tìm không có kết quả.");
    }

    /** Nút xóa từ khóa phải khôi phục đúng số dòng ban đầu. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "clear",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_012)
    public void clearSearchRestoresAllRows() {
        var result = inventoryPage.searchThenClear();
        Assert.assertTrue(result.initialCount() > 0,
                "Không có dữ liệu ban đầu để kiểm tra xóa tìm kiếm.");
        Assert.assertTrue(result.filteredCount() > 0
                        && result.filteredCount() <= result.initialCount(),
                "Tìm kiếm chuẩn bị trước khi xóa không hợp lệ.");
        Assert.assertEquals(result.restoredCount(), result.initialCount(),
                "Xóa từ khóa chưa khôi phục đủ dữ liệu.");
        Assert.assertTrue(result.inputValue().isBlank(),
                "Ô tìm kiếm vẫn còn nội dung sau khi xóa.");
    }

    /** Tự tìm một prefix chung của ít nhất hai mã để kiểm tra partial search có dữ liệu. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "partial",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_018)
    public void partialLotCodeReturnsMatchingRows() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertTrue(rows.size() >= 2,
                "Không đủ hai mã lô để kiểm tra tìm một phần.");
        String prefix = sharedPrefix(rows.stream().map(row -> row.code()).toList());
        Assert.assertFalse(prefix.isBlank(),
                "Không tìm được prefix chung của nhiều mã lô.");
        var result = inventoryPage.search(prefix.toLowerCase());
        Assert.assertTrue(result.rows().size() >= 2,
                "Tìm một phần mã không trả nhiều kết quả.");
        Assert.assertTrue(result.rows().stream().allMatch(row ->
                        row.code().startsWith(prefix)),
                "Có kết quả không bắt đầu bằng prefix yêu cầu.");
    }

    /** Không bấm xóa giữa hai lần nhập; kết quả cuối phải thuộc mã thứ hai. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "replace",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_019)
    public void replacingKeywordUpdatesToSecondLot() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertTrue(rows.size() >= 2,
                "Không đủ hai mã lô để kiểm tra thay từ khóa.");
        String firstCode = rows.get(0).code();
        String secondCode = rows.stream()
                .map(row -> row.code())
                .filter(code -> !code.equals(firstCode))
                .findFirst().orElseThrow();
        var result = inventoryPage.replaceSearchKeyword(firstCode, secondCode);
        Assert.assertTrue(result.first().rows().stream()
                .allMatch(row -> row.code().equals(firstCode)));
        Assert.assertFalse(result.second().rows().isEmpty(),
                "Thay từ khóa nhưng không tải kết quả mới.");
        Assert.assertTrue(result.second().rows().stream()
                .allMatch(row -> row.code().equals(secondCode)),
                "Kết quả cũ còn sót lại sau khi thay từ khóa.");
    }

    /** Ô search phải hoạt động cả khi người dùng đang xem dạng Danh sách. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "view",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_020)
    public void searchesLotCodeInListView() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có mã lô làm dữ liệu tìm kiếm.");
        String code = rows.get(0).code();
        var result = inventoryPage.searchInListView(code);
        Assert.assertEquals(result.inputValue(), code);
        Assert.assertFalse(result.rows().isEmpty(),
                "Tìm trong Danh sách nhưng không trả dữ liệu.");
        Assert.assertTrue(result.rows().stream()
                .allMatch(row -> row.code().equals(code)));
    }

    /** Sau empty-state, nút clear phải phục hồi đúng toàn bộ bảng. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "empty",
            "clear", "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_021)
    public void clearAfterEmptyStateRestoresRows() {
        var result = inventoryPage.clearSearchAfterEmpty(
                "AUTOMATION-KHONG-TON-TAI");
        Assert.assertTrue(result.initialCount() > 0,
                "Không có dữ liệu ban đầu để kiểm tra.");
        Assert.assertEquals(result.filteredCount(), 0,
                "Từ khóa chuẩn bị không tạo empty-state.");
        Assert.assertEquals(result.restoredCount(), result.initialCount(),
                "Clear từ empty-state chưa khôi phục đủ dữ liệu.");
        Assert.assertTrue(result.inputValue().isBlank());
    }

    /** Khoảng trắng đầu/cuối không được làm mất kết quả của mã hợp lệ. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "whitespace",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_022)
    public void trimsWhitespaceAroundLotCode() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có mã lô làm dữ liệu tìm kiếm.");
        String code = rows.get(0).code();
        var result = inventoryPage.search("  " + code.toLowerCase() + "  ");
        Assert.assertFalse(result.rows().isEmpty(),
                "Mã hợp lệ có khoảng trắng nhưng không trả dữ liệu.");
        Assert.assertTrue(result.rows().stream()
                .allMatch(row -> row.code().equals(code)));
    }

    /** Từ khóa và tập kết quả phải được giữ khi đổi Lưới tháng sang Danh sách. */
    @Test(groups = {"uniform", "inventory", "stock", "search", "view",
            "data-interaction"},
            description = UniformInventoryTestCases.UNI_INV_STOCK_023)
    public void switchingViewKeepsSearchResult() {
        var rows = inventoryPage.openStock().stockRows();
        Assert.assertFalse(rows.isEmpty(), "Không có mã lô làm dữ liệu tìm kiếm.");
        String code = rows.get(0).code();
        var result = inventoryPage.searchAndSwitchToList(code);
        Assert.assertTrue(result.listSelected(),
                "Chưa chuyển được sang Danh sách.");
        Assert.assertEquals(result.inputValueAfterSwitch(), code,
                "Chuyển view làm mất từ khóa.");
        Assert.assertFalse(result.listRows().isEmpty(),
                "Chuyển view làm mất dữ liệu tìm kiếm.");
        Assert.assertTrue(result.listRows().stream()
                .allMatch(row -> row.code().equals(code)));
    }

    private String sharedPrefix(List<String> codes) {
        for (String code : codes) {
            for (int length = code.length() - 1; length >= 2; length--) {
                String prefix = code.substring(0, length);
                long matches = codes.stream()
                        .filter(candidate -> candidate.startsWith(prefix))
                        .count();
                if (matches >= 2) {
                    return prefix;
                }
            }
        }
        return "";
    }
}
