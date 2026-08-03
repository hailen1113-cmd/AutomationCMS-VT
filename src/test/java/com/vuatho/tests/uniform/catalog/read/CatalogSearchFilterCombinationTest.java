package com.vuatho.tests.uniform.catalog.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Kiểm tra tổ hợp tìm kiếm/bộ lọc theo từng tab và trạng thái.
 *
 * <p>Bốn flow tìm kiếm có dữ liệu kiểm tra luôn thao tác Reset ở cuối để tránh
 * mở lại trang và chọn lại cùng điều kiện. Các ID Reset cũ được giữ ở trạng
 * thái disabled nhằm bảo toàn lịch sử nhưng không chạy lặp.</p>
 */
public class CatalogSearchFilterCombinationTest extends UniformModuleTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";
    private static final String ITEM_TAB = "Đồng Phục";
    private static final String IN_STOCK = "Còn hàng";
    private static final String OUT_OF_STOCK = "Hết hàng";

    public static void main(String[] args) {
        TestNgRunner.run(CatalogSearchFilterCombinationTest.class,
                "Đồng phục", "Kết hợp tìm kiếm và bộ lọc");
    }

    /** Từ khóa không tồn tại + Còn hàng ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "negative", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_009)
    public void noMatchWithInStockGroupReturnsEmpty() {
        assertNoMatchCombination(GROUP_TAB, IN_STOCK);
    }

    /** Từ khóa không tồn tại + Hết hàng ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "negative", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_056)
    public void noMatchWithOutOfStockGroupReturnsEmpty() {
        assertNoMatchCombination(GROUP_TAB, OUT_OF_STOCK);
    }

    /** Từ khóa không tồn tại + Còn hàng ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "negative", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_057)
    public void noMatchWithInStockItemReturnsEmpty() {
        assertNoMatchCombination(ITEM_TAB, IN_STOCK);
    }

    /** Từ khóa không tồn tại + Hết hàng ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "negative", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_058)
    public void noMatchWithOutOfStockItemReturnsEmpty() {
        assertNoMatchCombination(ITEM_TAB, OUT_OF_STOCK);
    }

    /** Tìm kiếm + Còn hàng rồi Reset ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_012)
    public void searchWithInStockGroupReturnsMatchingCards() {
        assertPositiveCombination(GROUP_TAB, IN_STOCK);
    }

    /** Tìm kiếm + Hết hàng rồi Reset ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_059)
    public void searchWithOutOfStockGroupReturnsMatchingCards() {
        assertPositiveCombination(GROUP_TAB, OUT_OF_STOCK);
    }

    /** Tìm kiếm + Còn hàng rồi Reset ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_060)
    public void searchWithInStockItemReturnsMatchingCards() {
        assertPositiveCombination(ITEM_TAB, IN_STOCK);
    }

    /** Tìm kiếm + Hết hàng rồi Reset ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_061)
    public void searchWithOutOfStockItemReturnsMatchingCards() {
        assertPositiveCombination(ITEM_TAB, OUT_OF_STOCK);
    }

    /** Đã gộp kiểm tra Reset vào UNI-CAT-012. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "search", "filter-combination",
                    "reset", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_038)
    public void resetGroupSearchAndInStockFilter() {
        // Giữ ID lịch sử; không chạy lại Nhóm Đồng Phục/Còn hàng.
    }

    /** Đã gộp kiểm tra Reset vào UNI-CAT-059. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "search", "filter-combination",
                    "reset", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_062)
    public void resetGroupSearchAndOutOfStockFilter() {
        // Giữ ID lịch sử; không chạy lại Nhóm Đồng Phục/Hết hàng.
    }

    /** Đã gộp kiểm tra Reset vào UNI-CAT-060. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "search", "filter-combination",
                    "reset", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_063)
    public void resetItemSearchAndInStockFilter() {
        // Giữ ID lịch sử; không chạy lại Đồng Phục/Còn hàng.
    }

    /** Đã gộp kiểm tra Reset vào UNI-CAT-061. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "search", "filter-combination",
                    "reset", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_064)
    public void resetItemSearchAndOutOfStockFilter() {
        // Giữ ID lịch sử; không chạy lại Đồng Phục/Hết hàng.
    }

    /** Chuyển Nhóm Đồng Phục sang Đồng Phục không giữ sai điều kiện. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "tab-state", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_039)
    public void switchingGroupToItemDoesNotCarryCriteria() {
        assertTabStateIsolation(GROUP_TAB, ITEM_TAB);
    }

    /** Chuyển Đồng Phục sang Nhóm Đồng Phục không giữ sai điều kiện. */
    @Test(groups = {"uniform", "catalog", "search", "filter-combination",
            "tab-state", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_065)
    public void switchingItemToGroupDoesNotCarryCriteria() {
        assertTabStateIsolation(ITEM_TAB, GROUP_TAB);
    }

    private void assertNoMatchCombination(String tab, String status) {
        catalogPage.open().selectTab(tab)
                .search("__automation_uniform_no_match__")
                .openFilter();
        catalogPage.chooseFilter(status);
        Assert.assertTrue(catalogPage.displayedItemNames().isEmpty(),
                "Tổ hợp từ khóa không tồn tại + " + status
                        + " vẫn trả dữ liệu ở tab " + tab);
    }

    private void assertPositiveCombination(String tab, String status) {
        catalogPage.open().selectTab(tab);
        int before = catalogPage.totalDisplayed();
        catalogPage.openFilter();
        catalogPage.chooseFilter(status);
        if (catalogPage.displayedItemNames().isEmpty()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] Tab " + tab
                    + " không có card trạng thái " + status + ".");
        }
        String keyword = catalogPage.firstItemName();
        catalogPage.reset().search(keyword).openFilter();
        catalogPage.chooseFilter(status);
        List<String> result = catalogPage.displayedItemNames();
        Assert.assertFalse(result.isEmpty(),
                "Kết hợp tìm kiếm + " + status + " không trả dữ liệu.");
        String normalizedKeyword = TextNormalizer.normalize(keyword);
        Assert.assertTrue(result.stream().allMatch(name ->
                        TextNormalizer.normalize(name).contains(normalizedKeyword)),
                "Kết quả kết hợp không khớp từ khóa " + keyword + ": " + result);
        Assert.assertTrue(catalogPage.displayedCardsMatchInventoryStatus(status),
                "Kết quả kết hợp có card sai trạng thái " + status);
        catalogPage.reset();
        Assert.assertTrue(catalogPage.searchValue().isBlank(),
                "Reset chưa xóa từ khóa ở tab " + tab);
        Assert.assertTrue(catalogPage.inventoryFilterCleared(),
                "Reset chưa xóa trạng thái tồn kho ở tab " + tab);
        Assert.assertEquals(catalogPage.totalDisplayed(), before,
                "Reset không phục hồi toàn bộ dữ liệu ở tab " + tab);
    }

    private void assertTabStateIsolation(String sourceTab, String targetTab) {
        catalogPage.open().selectTab(sourceTab);
        catalogPage.search(catalogPage.firstItemName()).openFilter();
        catalogPage.chooseFilter(IN_STOCK);
        catalogPage.selectTab(targetTab);
        Assert.assertTrue(catalogPage.searchValue().isBlank(),
                "Tab " + targetTab + " giữ sai từ khóa của tab " + sourceTab);
        Assert.assertTrue(catalogPage.inventoryFilterCleared(),
                "Tab " + targetTab + " giữ sai bộ lọc của tab " + sourceTab);
    }
}
