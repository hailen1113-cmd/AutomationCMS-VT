package com.vuatho.tests.uniform.catalog;

import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog;
import com.vuatho.testdata.UniformCatalogTestCaseCatalog.Execution;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Kiểm tra form tạo và drawer chi tiết danh mục nhưng không ghi dữ liệu. */
public class UniformCatalogDrawerTest extends UniformModuleTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(UniformCatalogDrawerTest.class,
                "Đồng phục", "Form và chi tiết danh mục");
    }

    /** Form tạo nhóm phải có đủ thông tin, ảnh, trạng thái và packages. */
    @Test(dataProvider = "case004",
            groups = {"uniform", "catalog", "drawer", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_001)
    public void createGroupDrawerContainsRequiredControls(Execution testCase) {
        String form = catalogPage.open().openCreateDrawer();
        for (String field : testCase.expectedUiLabels()) {
            Assert.assertTrue(form.contains(field), "Form tạo nhóm thiếu " + field);
        }
        String validation = catalogPage.submitEmptyCreateForm();
        for (String message : testCase.expectedValidation()) {
            Assert.assertTrue(validation.contains(message),
                    "Form tạo nhóm thiếu validation " + message);
        }
    }

    /** Form tạo sản phẩm phải hỗ trợ ảnh và khai báo biến thể mới. */
    @Test(dataProvider = "case005",
            groups = {"uniform", "catalog", "drawer", "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_002)
    public void createProductDrawerSupportsVariants(Execution testCase) {
        String form = catalogPage.open().selectTab(testCase.tab()).openCreateDrawer();
        for (String field : testCase.expectedUiLabels()) {
            Assert.assertTrue(form.contains(field),
                    "Form tạo đồng phục thiếu " + field);
        }

        String validation = catalogPage.submitEmptyCreateForm();
        for (String message : testCase.expectedValidation()) {
            Assert.assertTrue(validation.contains(message),
                    "Form tạo đồng phục thiếu validation " + message);
        }

        String variant = catalogPage.addVariantDraft();
        for (String field : testCase.expectedVariantFields()) {
            Assert.assertTrue(variant.contains(field),
                    "Dòng biến thể thiếu " + field);
        }
    }

    /** Card có dữ liệu phải mở đúng drawer chi tiết tương ứng. */
    @Test(dataProvider = "case006",
            groups = {"uniform", "catalog", "detail", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_003)
    public void itemCardOpensDetail(Execution testCase) {
        String tab = testCase.tab();
        catalogPage.open().selectTab(tab);
        String name = catalogPage.firstItemName();
        String detail = catalogPage.openItemDetail(name);
        Assert.assertTrue(detail.contains(testCase.drawerTitle()),
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
                            && detail.contains("Tồn kho"),
                    "Chi tiết đồng phục thiếu biến thể hoặc tồn kho.");
        }
    }

    @DataProvider(name = "case004")
    public Object[][] case004() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-004");
    }

    @DataProvider(name = "case005")
    public Object[][] case005() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-005");
    }

    @DataProvider(name = "case006")
    public Object[][] case006() {
        return UniformCatalogTestCaseCatalog.dataProvider("UNIFORM-CATALOG-006");
    }
}
