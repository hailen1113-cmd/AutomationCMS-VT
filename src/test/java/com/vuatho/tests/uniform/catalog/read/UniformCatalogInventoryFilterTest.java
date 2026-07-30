package com.vuatho.tests.uniform.catalog.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Kiểm tra bộ lọc tồn kho theo bốn tổ hợp tab/trạng thái.
 *
 * <p>Mỗi flow chính kiểm tra trong một lần lọc: trạng thái radio, dữ liệu card
 * và thao tác Reset. Các ID cũ từng tách riêng hai assertion này được giữ lại
 * ở trạng thái disabled để bảo toàn lịch sử nhưng không chạy lặp.</p>
 */
public class UniformCatalogInventoryFilterTest extends UniformModuleTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";
    private static final String ITEM_TAB = "Đồng Phục";
    private static final String IN_STOCK = "Còn hàng";
    private static final String OUT_OF_STOCK = "Hết hàng";

    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogInventoryFilterTest.class,
                "Đồng phục", "Bộ lọc tồn kho danh mục");
    }

    /** Lọc Còn hàng, kiểm tra radio/card rồi Reset ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "filter", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_008)
    public void filterInStockGroupsAndReset() {
        verifyFilterScenario(GROUP_TAB, IN_STOCK);
    }

    /** Lọc Hết hàng, kiểm tra radio/card rồi Reset ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "filter", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_046)
    public void filterOutOfStockGroupsAndReset() {
        verifyFilterScenario(GROUP_TAB, OUT_OF_STOCK);
    }

    /** Lọc Còn hàng, kiểm tra radio/card rồi Reset ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "filter", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_047)
    public void filterInStockItemsAndReset() {
        verifyFilterScenario(ITEM_TAB, IN_STOCK);
    }

    /** Lọc Hết hàng, kiểm tra radio/card rồi Reset ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "filter", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_048)
    public void filterOutOfStockItemsAndReset() {
        verifyFilterScenario(ITEM_TAB, OUT_OF_STOCK);
    }

    /** Đã gộp kiểm tra card vào UNI-CAT-008. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_011)
    public void filteredGroupCardsAreInStock() {
        // Giữ ID lịch sử; không chạy để tránh lọc Nhóm Đồng Phục/Còn hàng lần hai.
    }

    /** Đã gộp kiểm tra card vào UNI-CAT-046. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_049)
    public void filteredGroupCardsAreOutOfStock() {
        // Giữ ID lịch sử; không chạy để tránh lọc Nhóm Đồng Phục/Hết hàng lần hai.
    }

    /** Đã gộp kiểm tra card vào UNI-CAT-047. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_050)
    public void filteredItemCardsAreInStock() {
        // Giữ ID lịch sử; không chạy để tránh lọc Đồng Phục/Còn hàng lần hai.
    }

    /** Đã gộp kiểm tra card vào UNI-CAT-048. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_051)
    public void filteredItemCardsAreOutOfStock() {
        // Giữ ID lịch sử; không chạy để tránh lọc Đồng Phục/Hết hàng lần hai.
    }

    /** Đã gộp kiểm tra radio vào UNI-CAT-008. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "selection", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_036)
    public void groupInStockRadioIsSelected() {
        // Giữ ID lịch sử; không chạy để tránh lọc Nhóm Đồng Phục/Còn hàng lần ba.
    }

    /** Đã gộp kiểm tra radio vào UNI-CAT-046. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "selection", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_052)
    public void groupOutOfStockRadioIsSelected() {
        // Giữ ID lịch sử; không chạy để tránh lọc Nhóm Đồng Phục/Hết hàng lần ba.
    }

    /** Đã gộp kiểm tra radio vào UNI-CAT-047. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "selection", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_053)
    public void itemInStockRadioIsSelected() {
        // Giữ ID lịch sử; không chạy để tránh lọc Đồng Phục/Còn hàng lần ba.
    }

    /** Đã gộp kiểm tra radio vào UNI-CAT-048. */
    @Test(enabled = false,
            groups = {"uniform", "catalog", "filter", "selection", "retired"},
            description = UniformCatalogTestCases.UNI_CAT_054)
    public void itemOutOfStockRadioIsSelected() {
        // Giữ ID lịch sử; không chạy để tránh lọc Đồng Phục/Hết hàng lần ba.
    }

    /** Chuyển trực tiếp Còn hàng sang Hết hàng ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "filter", "selection", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_037)
    public void switchGroupFilterFromInStockToOutOfStock() {
        switchInventoryStatuses(GROUP_TAB);
    }

    /** Chuyển trực tiếp Còn hàng sang Hết hàng ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "filter", "selection", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_055)
    public void switchItemFilterFromInStockToOutOfStock() {
        switchInventoryStatuses(ITEM_TAB);
    }

    /** Chạy trọn bộ assertion của một tổ hợp tab/trạng thái trong đúng một lần lọc. */
    private void verifyFilterScenario(String tab, String status) {
        String other = status.equals(IN_STOCK) ? OUT_OF_STOCK : IN_STOCK;
        catalogPage.open().selectTab(tab);
        int before = catalogPage.totalDisplayed();
        String popup = catalogPage.openFilter();
        Assert.assertTrue(popup.contains(IN_STOCK) && popup.contains(OUT_OF_STOCK),
                "Bộ lọc " + tab + " thiếu trạng thái tồn kho.");
        catalogPage.chooseFilter(status);
        Assert.assertTrue(catalogPage.inventoryFilterSelected(status),
                "Popup không đánh dấu trạng thái vừa chọn: " + status);
        Assert.assertFalse(catalogPage.inventoryFilterSelected(other),
                "Popup đang chọn đồng thời " + status + " và " + other);
        Assert.assertTrue(catalogPage.totalDisplayed() <= before,
                "Lọc " + status + " làm tăng tổng bản ghi bất thường.");
        requireFilteredData(tab, status);
        Assert.assertTrue(catalogPage.displayedCardsMatchInventoryStatus(status),
                "Có card không đúng trạng thái " + status + " ở tab " + tab
                        + ": " + catalogPage.displayedCards());
        catalogPage.reset();
        Assert.assertTrue(catalogPage.inventoryFilterCleared(),
                "Reset nhưng radio tồn kho vẫn còn được chọn.");
        Assert.assertEquals(catalogPage.totalDisplayed(), before,
                "Reset không phục hồi tổng dữ liệu ban đầu.");
    }

    private void switchInventoryStatuses(String tab) {
        catalogPage.open().selectTab(tab).openFilter();
        catalogPage.chooseFilter(IN_STOCK);
        Assert.assertTrue(catalogPage.inventoryFilterSelected(IN_STOCK),
                "Không chọn được Còn hàng ở tab " + tab);
        catalogPage.chooseFilter(OUT_OF_STOCK);
        Assert.assertTrue(catalogPage.inventoryFilterSelected(OUT_OF_STOCK),
                "Không chuyển được sang Hết hàng ở tab " + tab);
        Assert.assertFalse(catalogPage.inventoryFilterSelected(IN_STOCK),
                "Còn hàng vẫn được chọn sau khi chuyển sang Hết hàng.");
        requireFilteredData(tab, OUT_OF_STOCK);
        Assert.assertTrue(catalogPage.displayedCardsMatchInventoryStatus(OUT_OF_STOCK),
                "Kết quả không cập nhật theo trạng thái Hết hàng.");
    }

    private void requireFilteredData(String tab, String status) {
        if (catalogPage.displayedItemNames().isEmpty()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] Tab " + tab
                    + " không có card trạng thái " + status + ".");
        }
    }
}
