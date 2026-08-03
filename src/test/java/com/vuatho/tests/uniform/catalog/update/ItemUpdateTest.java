package com.vuatho.tests.uniform.catalog.update;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformItemUpdateTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Testcase cập nhật hợp lệ của Đồng Phục trên dữ liệu sandbox hiện có.
 *
 * <p>Không tạo dữ liệu chuẩn bị và không xóa dữ liệu sau khi cập nhật.</p>
 */
public class ItemUpdateTest extends UniformItemUpdateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(ItemUpdateTest.class,
                "Đồng phục", "Cập nhật Đồng Phục");
    }

    /** Đổi tên đồng phục thật và xác minh danh sách trả tên mới. */
    @Test(groups = {"uniform", "catalog", "item-update", "edit", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_026)
    public void renameUniformPersists() {
        String oldName = requireExistingItem();
        String newName = uniqueCatalogName("AUTO-UNIFORM-EDIT");
        Assert.assertTrue(catalogPage.renameItem(ITEM_TAB, oldName, newName),
                "Đổi tên đồng phục nhưng danh sách không trả tên mới.");
    }

    /** Drawer phải có đủ trường cơ bản, upload và ba nút nghiệp vụ. */
    @Test(groups = {"uniform", "catalog", "item-update", "form", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_130)
    public void itemUpdateFormContainsRequiredControls() {
        var form = catalogPage.itemUpdateFormSnapshot(requireExistingItem());
        Assert.assertEquals(form.businessTextInputCount(), 2,
                "Drawer phải có đúng ô Tên đồng phục và Giá bán.");
        Assert.assertTrue(form.imageUpload() && form.multipleUpload(),
                "Drawer thiếu input ảnh hợp lệ hoặc thuộc tính multiple.");
        Assert.assertTrue(form.fiveImageHint(),
                "Drawer thiếu hướng dẫn tối đa 5 ảnh.");
        Assert.assertTrue(form.deleteButton() && form.cancelButton()
                        && form.confirmButton(),
                "Drawer thiếu nút Xóa, Hủy hoặc Xác nhận.");
    }

    /** Đổi giá bán và mở lại drawer để xác minh dữ liệu được lưu. */
    @Test(groups = {"uniform", "catalog", "item-update", "price", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_131)
    public void updateItemPricePersists() {
        Assert.assertTrue(catalogPage.updateItemPricePersists(
                        requireExistingItem(), "285000"),
                "Đổi giá đồng phục nhưng mở lại không giữ giá mới.");
    }

    /** Upload một ảnh PNG thật và mở lại chi tiết kiểm tra ảnh tải thành công. */
    @Test(groups = {"uniform", "catalog", "item-update", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_132)
    public void updateOneItemImagePersists() {
        var result = catalogPage.updateItemImagesPersist(
                requireExistingItem(),
                createRasterTestImages(1, "single"));
        Assert.assertTrue(result.previewCount() >= 1,
                "Ảnh mới không hiển thị preview hợp lệ.");
        Assert.assertTrue(result.submitted(),
                "Drawer không đóng sau khi xác nhận cập nhật ảnh.");
        Assert.assertTrue(result.persistedImageCount() >= 1,
                "Mở lại chi tiết nhưng ảnh đồng phục không tải được.");
    }

    /** Upload đúng giới hạn năm ảnh thật và xác minh không vượt quá giới hạn. */
    @Test(groups = {"uniform", "catalog", "item-update", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_133)
    public void updateFiveItemImagesPersists() {
        var result = catalogPage.updateItemImagesPersist(
                requireExistingItem(),
                createRasterTestImages(5, "maximum-five"));
        Assert.assertTrue(result.previewCount() >= 1
                        && result.previewCount() <= 5,
                "Số preview phải nằm trong giới hạn từ 1 đến 5.");
        Assert.assertTrue(result.submitted(),
                "Drawer không đóng sau khi xác nhận cập nhật năm ảnh.");
        Assert.assertTrue(result.persistedImageCount() >= 1
                        && result.persistedImageCount() <= 5,
                "Số ảnh đã lưu phải nằm trong giới hạn từ 1 đến 5.");
    }

    /** Nhập tên nháp rồi Hủy phải giữ nguyên tên cũ. */
    @Test(groups = {"uniform", "catalog", "item-update", "cancel", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_134)
    public void cancelItemUpdateDoesNotSaveDraft() {
        Assert.assertTrue(catalogPage.discardItemNameUpdate(
                        requireExistingItem(),
                        uniqueCatalogName("AUTO-ITEM-CANCEL-DRAFT"), false),
                "Bấm Hủy nhưng tên đồng phục nháp vẫn được lưu.");
    }

    /** Nhập tên nháp rồi bấm X phải giữ nguyên tên cũ. */
    @Test(groups = {"uniform", "catalog", "item-update", "close", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_135)
    public void closeItemUpdateDoesNotSaveDraft() {
        Assert.assertTrue(catalogPage.discardItemNameUpdate(
                        requireExistingItem(),
                        uniqueCatalogName("AUTO-ITEM-CLOSE-DRAFT"), true),
                "Bấm X nhưng tên đồng phục nháp vẫn được lưu.");
    }

    /** Đổi đồng thời tên và giá rồi đọc lại cả hai trường. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "multiple-fields", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_136)
    public void updateItemNameAndPricePersists() {
        String newName = uniqueCatalogName("AUTO-ITEM-MULTI-EDIT");
        var result = catalogPage.updateItemNameAndPricePersists(
                requireExistingItem(), newName, "315000");
        Assert.assertTrue(result.namePersisted(),
                "Tên đồng phục mới không xuất hiện trong danh sách.");
        Assert.assertTrue(result.pricePersisted(),
                "Giá mới không được lưu trong drawer chi tiết.");
    }

    /** Hai lựa chọn biến thể phải bị khóa và chỉ một lựa chọn hiện tại được chọn. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_137)
    public void variantTypeIsReadOnlyAndKeepsCurrentSelection() {
        var form = catalogPage.itemUpdateFormSnapshot(requireExistingItem());
        Assert.assertEquals(form.variantChoiceCount(), 2,
                "Drawer phải hiển thị hai loại Có/Không có biến thể.");
        Assert.assertEquals(form.selectedVariantChoiceCount(), 1,
                "Drawer phải giữ đúng một loại biến thể hiện tại.");
        Assert.assertTrue(form.allVariantChoicesDisabled(),
                "Loại biến thể trong drawer cập nhật phải ở trạng thái chỉ đọc.");
    }
}
