package com.vuatho.tests.uniform.catalog;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
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
    @Test(groups = {"uniform", "catalog", "drawer", "data-interaction"},
            description = "UNIFORM-CATALOG-004: Form tạo nhóm có đủ trường nghiệp vụ")
    public void createGroupDrawerContainsRequiredControls() {
        String form = catalogPage.open().openCreateDrawer();
        for (String field : new String[]{
                "Tên nhóm", "Giá bán", "Tài khoản thanh toán",
                "Ảnh đại diện", "Trạng thái hết hàng", "Packages",
                "Chọn đồng phục"}) {
            Assert.assertTrue(form.contains(field), "Form tạo nhóm thiếu " + field);
        }
        String validation = catalogPage.submitEmptyCreateForm();
        for (String message : new String[]{
                "Nhập tên nhóm", "Nhập giá bán", "Chọn tài khoản thanh toán"}) {
            Assert.assertTrue(validation.contains(message),
                    "Form tạo nhóm thiếu validation " + message);
        }
    }

    /** Form tạo sản phẩm phải hỗ trợ ảnh và khai báo biến thể mới. */
    @Test(groups = {"uniform", "catalog", "drawer", "variant", "data-interaction"},
            description = "UNIFORM-CATALOG-005: Form tạo sản phẩm hỗ trợ ảnh và biến thể")
    public void createProductDrawerSupportsVariants() {
        String form = catalogPage.open().selectTab("Đồng Phục").openCreateDrawer();
        Assert.assertTrue(form.contains("Tên đồng phục"));
        Assert.assertTrue(form.contains("Hình ảnh sản phẩm"));
        Assert.assertTrue(form.contains("Tối đa 5 ảnh"));
        Assert.assertTrue(form.contains("Không có biến thể")
                && form.contains("Có biến thể"));

        String validation = catalogPage.submitEmptyCreateForm();
        for (String message : new String[]{
                "Nhập tên đồng phục", "Nhập giá bán",
                "Vui lòng chọn loại biến thể"}) {
            Assert.assertTrue(validation.contains(message),
                    "Form tạo đồng phục thiếu validation " + message);
        }

        String variant = catalogPage.addVariantDraft();
        for (String field : new String[]{
                "Tên biến thể (VI)", "Tên biến thể (EN)",
                "Màu sắc", "Văn bản"}) {
            Assert.assertTrue(variant.contains(field),
                    "Dòng biến thể thiếu " + field);
        }
    }

    /** Card có dữ liệu phải mở đúng drawer chi tiết tương ứng. */
    @Test(dataProvider = "tabs",
            groups = {"uniform", "catalog", "detail", "data-interaction"},
            description = "UNIFORM-CATALOG-006: Card mở drawer chi tiết có dữ liệu")
    public void itemCardOpensDetail(String tab, String expectedTitle) {
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
                            && detail.contains("Tồn kho"),
                    "Chi tiết đồng phục thiếu biến thể hoặc tồn kho.");
        }
    }

    @DataProvider(name = "tabs")
    public Object[][] tabs() {
        return new Object[][]{
                {"Nhóm Đồng Phục", "Chi tiết nhóm đồng phục"},
                {"Đồng Phục", "Chi tiết đồng phục"}
        };
    }
}
