package com.vuatho.tests.uniform.catalog;

import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.PaginationSnapshot;
import com.vuatho.pages.UniformCatalogPage.UploadConstraint;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

/**
 * Bao phủ các tương tác an toàn của danh mục Đồng phục.
 *
 * <p>Các testcase trong class này không xác nhận tạo, sửa hoặc xóa dữ liệu.</p>
 */
public class UniformCatalogSafeInteractionTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogSafeInteractionTest.class,
                "Đồng phục", "Tương tác an toàn danh mục");
    }

    @Test(dataProvider = "case010",
            groups = {"uniform", "catalog", "tab", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_009)
    public void tabSwitchUpdatesSelectedStateAndSearchContext(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);

        Assert.assertEquals(catalogPage.selectedTab(), tab);
        Assert.assertEquals(catalogPage.selectedTabCount(), 1,
                "DOM phải chỉ đánh dấu đúng một tab đang chọn.");
        Assert.assertEquals(
                catalogPage.searchPlaceholder(), testCase.searchPlaceholder());
        Assert.assertTrue(catalogPage.searchValue().isBlank(),
                "Ô tìm kiếm phải rỗng khi mở mới route.");
    }

    @Test(dataProvider = "case011",
            groups = {"uniform", "catalog", "search", "negative", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_010)
    public void unknownSearchReturnsEmptyStateAndResetRestoresData(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        int originalTotal = catalogPage.totalDisplayed();
        String impossibleKeyword = "AUTOMATION-NOT-FOUND-" + System.nanoTime();

        catalogPage.search(impossibleKeyword);
        Assert.assertEquals(catalogPage.totalDisplayed(), 0,
                "Từ khóa không tồn tại vẫn trả dữ liệu.");
        Assert.assertTrue(catalogPage.displayedCards().isEmpty(),
                "Từ khóa không tồn tại vẫn trả card trong vùng kết quả.");
        Assert.assertEquals(catalogPage.searchValue(), impossibleKeyword);

        catalogPage.reset();
        Assert.assertTrue(catalogPage.searchValue().isBlank(),
                "Reset không xóa nội dung tìm kiếm.");
        Assert.assertEquals(catalogPage.totalDisplayed(), originalTotal,
                "Reset không phục hồi tổng dữ liệu ban đầu.");
    }

    @Test(dataProvider = "case012",
            groups = {"uniform", "catalog", "filter", "accessibility",
                    "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_011)
    public void inventoryFilterExposesAccessibleRadioOptions(Execution testCase) {
        String tab = testCase.tab();
        String popup = catalogPage.open().selectTab(tab).openFilter();

        Assert.assertTrue(popup.contains("Trạng thái tồn kho"));
        Assert.assertTrue(popup.contains("Còn hàng"));
        Assert.assertTrue(popup.contains("Hết hàng"));
        Assert.assertTrue(popup.contains("Đặt lại"));
        Assert.assertEquals(catalogPage.visibleInventoryFilterRadioCount(), 2,
                "Popup phải có đúng hai radio trạng thái tồn kho.");
        catalogPage.closeFilter();
    }

    @Test(dataProvider = "case013",
            groups = {"uniform", "catalog", "drawer", "cancel", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_012)
    public void cancellingCreateDrawerDoesNotChangeCatalog(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        int originalTotal = catalogPage.totalDisplayed();
        String drawerText = catalogPage.openCreateDrawer();

        Assert.assertFalse(drawerText.isBlank(), "Drawer tạo mới không có nội dung.");
        Assert.assertTrue(catalogPage.visibleCreateDrawerLabel().startsWith("drawer-Tạo mới"));
        catalogPage.cancelCreateDrawer();

        Assert.assertFalse(catalogPage.hasVisibleDrawer(),
                "Drawer vẫn hiển thị sau khi bấm Hủy.");
        Assert.assertEquals(catalogPage.totalDisplayed(), originalTotal,
                "Hủy form tạo làm thay đổi tổng dữ liệu.");
    }

    @Test(dataProvider = "case014",
            groups = {"uniform", "catalog", "drawer", "upload", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_013)
    public void createDrawerDeclaresSafeImageUploadContract(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab).openCreateDrawer();
        List<UploadConstraint> uploads = catalogPage.createDrawerUploadConstraints();

        Assert.assertFalse(uploads.isEmpty(),
                "Form " + tab + " thiếu input upload file.");
        for (UploadConstraint upload : uploads) {
            String accept = upload.accept() == null
                    ? "" : upload.accept().toLowerCase(Locale.ROOT);
            Assert.assertTrue(accept.contains("image")
                            || accept.contains(".png")
                            || accept.contains(".jpg")
                            || accept.contains(".jpeg"),
                    "Input upload không giới hạn định dạng ảnh: " + upload.accept());
        }
        if (tab.equals("Đồng Phục")) {
            Assert.assertTrue(uploads.stream().anyMatch(UploadConstraint::multiple),
                    "Form Đồng Phục công bố tối đa 5 ảnh nhưng input không cho chọn nhiều file.");
        }
        catalogPage.cancelCreateDrawer();
    }

    @Test(dataProvider = "case015",
            groups = {"uniform", "catalog", "detail", "cancel", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_014)
    public void detailDrawerCanCloseWithoutChangingCatalog(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        int originalTotal = catalogPage.totalDisplayed();
        String itemName = catalogPage.firstItemName();

        Assert.assertFalse(itemName.isBlank(), "Không có item để mở chi tiết.");
        Assert.assertFalse(catalogPage.openItemDetail(itemName).isBlank());
        Assert.assertTrue(catalogPage.hasVisibleDrawer());
        catalogPage.closeDrawer();

        Assert.assertFalse(catalogPage.hasVisibleDrawer());
        Assert.assertEquals(catalogPage.totalDisplayed(), originalTotal);
    }

    @Test(dataProvider = "case016",
            groups = {"uniform", "catalog", "pagination", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_015)
    public void paginationSupportsSafeRoundTripWhenMorePagesExist(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        PaginationSnapshot firstPage = catalogPage.pagination();
        List<String> firstPageNames = catalogPage.displayedItemNames();

        if (firstPage.nextDisabled()) {
            Assert.assertEquals(firstPage.pageCount(), 1,
                    "Nút Next bị khóa dù DOM công bố nhiều trang.");
            return;
        }

        catalogPage.nextPage();
        Assert.assertEquals(catalogPage.pagination().activePage(), 2);
        Assert.assertNotEquals(catalogPage.displayedItemNames(), firstPageNames,
                "Sang trang 2 nhưng tập card không thay đổi.");

        catalogPage.previousPage();
        Assert.assertEquals(catalogPage.pagination().activePage(), 1);
        Assert.assertEquals(catalogPage.displayedItemNames(), firstPageNames,
                "Quay lại trang 1 không phục hồi tập card ban đầu.");
    }

    @DataProvider(name = "case010")
    public Object[][] case010() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-010");
    }

    @DataProvider(name = "case011")
    public Object[][] case011() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-011");
    }

    @DataProvider(name = "case012")
    public Object[][] case012() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-012");
    }

    @DataProvider(name = "case013")
    public Object[][] case013() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-013");
    }

    @DataProvider(name = "case014")
    public Object[][] case014() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-014");
    }

    @DataProvider(name = "case015")
    public Object[][] case015() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-015");
    }

    @DataProvider(name = "case016")
    public Object[][] case016() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-016");
    }
}
