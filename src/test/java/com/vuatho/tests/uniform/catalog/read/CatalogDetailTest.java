package com.vuatho.tests.uniform.catalog.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Kiểm tra mở drawer chi tiết riêng cho từng tab danh mục Đồng phục. */
public class CatalogDetailTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(CatalogDetailTest.class,
                "Đồng phục", "Chi tiết nhóm và đồng phục");
    }

    /** Card nhóm có dữ liệu phải mở đúng drawer chi tiết nhóm. */
    @Test(groups = {"uniform", "catalog", "detail", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_003)
    public void groupCardOpensGroupDetail() {
        assertItemCardOpensDetail(
                "Nhóm Đồng Phục", "Chi tiết nhóm đồng phục");
    }

    /** Card đồng phục có dữ liệu phải mở đúng drawer chi tiết đồng phục. */
    @Test(groups = {"uniform", "catalog", "detail", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_075)
    public void itemCardOpensItemDetail() {
        assertItemCardOpensDetail(
                "Đồng Phục", "Chi tiết đồng phục");
    }

    /** Dùng chung assertion nhưng hai tab vẫn có testcase và ID độc lập trên Terminal. */
    private void assertItemCardOpensDetail(String tab, String expectedTitle) {
        catalogPage.open().selectTab(tab);
        String name = catalogPage.firstItemName();
        String detail = catalogPage.openItemDetail(name);
        Assert.assertTrue(detail.contains(expectedTitle),
                "Drawer mở sai loại: " + detail);
        Assert.assertTrue(detail.contains(name),
                "Drawer không chứa tên item " + name);
        Assert.assertTrue(detail.contains("Giá bán"),
                "Drawer chi tiết thiếu giá bán.");
        if (tab.equals("Nhóm Đồng Phục")) {
            Assert.assertTrue(detail.contains("Packages")
                            && detail.contains("Xóa nhóm đồng phục"),
                    "Chi tiết nhóm thiếu danh sách package hoặc thao tác xóa.");
        } else {
            Assert.assertTrue(detail.contains("Biến thể")
                            && detail.contains("Không có biến thể")
                            && detail.contains("Có biến thể"),
                    "Chi tiết đồng phục thiếu lựa chọn loại biến thể.");
        }
    }
}
