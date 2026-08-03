package com.vuatho.tests.uniform.catalog.update;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.ItemUpdateSubmissionSnapshot;
import com.vuatho.support.UniformItemUpdateTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Testcase validation riêng của drawer cập nhật Đồng Phục. */
public class ItemUpdateValidationTest
        extends UniformItemUpdateTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(ItemUpdateValidationTest.class,
                "Đồng phục", "Validation cập nhật Đồng Phục");
    }

    /** Xóa rỗng tên đồng phục phải giữ drawer và báo lỗi bắt buộc. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_138)
    public void emptyNameCannotUpdateItem() {
        assertRequiredValidation(
                catalogPage.submitItemUpdateDraft(
                        requireExistingItem(), "", "285000"),
                "Nhập tên đồng phục");
    }

    /** Tên chỉ có khoảng trắng phải bị xem là chưa nhập. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_139)
    public void whitespaceNameCannotUpdateItem() {
        assertRequiredValidation(
                catalogPage.submitItemUpdateDraft(
                        requireExistingItem(), "   ", "285000"),
                "Nhập tên đồng phục");
    }

    /** Xóa rỗng giá bán phải giữ drawer và báo lỗi bắt buộc. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_140)
    public void emptyPriceCannotUpdateItem() {
        String name = requireExistingItem();
        assertRequiredValidation(
                catalogPage.submitItemUpdateDraft(name, name, ""),
                "Nhập giá bán");
    }

    /** Giá chỉ có khoảng trắng phải bị xem là chưa nhập. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_141)
    public void whitespacePriceCannotUpdateItem() {
        String name = requireExistingItem();
        assertRequiredValidation(
                catalogPage.submitItemUpdateDraft(name, name, "   "),
                "Nhập giá bán");
    }

    /** Giá bằng 0 không được phép lưu khi cập nhật. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_142)
    public void zeroPriceCannotUpdateItem() {
        assertInvalidPriceRejected("0");
    }

    /** Giá âm không được phép lưu khi cập nhật. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_143)
    public void negativePriceCannotUpdateItem() {
        assertInvalidPriceRejected("-285000");
    }

    /** Giá chứa chữ không được phép lưu khi cập nhật. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_144)
    public void alphabeticPriceCannotUpdateItem() {
        assertInvalidPriceRejected("abc");
    }

    /** Giá thập phân không được phép lưu với đơn vị đồng Việt Nam. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_145)
    public void decimalPriceCannotUpdateItem() {
        assertInvalidPriceRejected("285000.5");
    }

    /** Chuỗi số vượt giới hạn phải bị từ chối an toàn. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_146)
    public void oversizedPriceCannotUpdateItem() {
        assertInvalidPriceRejected(
                "999999999999999999999999999999999999");
    }

    /** Chọn sáu ảnh thật phải không làm số ảnh hợp lệ vượt giới hạn năm. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_147)
    public void moreThanFiveImagesCannotBeAccepted() {
        var result = catalogPage.itemImageDraftSnapshot(
                requireExistingItem(),
                createRasterTestImages(6, "over-limit"));
        Assert.assertTrue(result.afterLoadedImageCount() <= 5,
                "Form đã nhận quá 5 ảnh: "
                        + result.afterLoadedImageCount());
    }

    /** File văn bản không được tạo thêm preview ảnh hợp lệ. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_148)
    public void nonImageFileCannotBeAccepted() {
        var result = catalogPage.itemImageDraftSnapshot(
                requireExistingItem(), List.of(createNonImageFile()));
        Assert.assertEquals(
                result.afterLoadedImageCount(),
                result.beforeLoadedImageCount(),
                "File văn bản đã tạo thêm preview ảnh hợp lệ.");
    }

    /** File PNG có payload hỏng không được tạo thêm preview ảnh hợp lệ. */
    @Test(groups = {"uniform", "catalog", "item-update",
            "validation", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_149)
    public void corruptedImageCannotBeAccepted() {
        var result = catalogPage.itemImageDraftSnapshot(
                requireExistingItem(), List.of(createCorruptedImageFile()));
        Assert.assertEquals(
                result.afterLoadedImageCount(),
                result.beforeLoadedImageCount(),
                "File ảnh hỏng đã tạo thêm preview ảnh hợp lệ.");
    }

    /** Kiểm tra thông báo đúng và drawer không đóng khi thiếu trường bắt buộc. */
    private void assertRequiredValidation(
            ItemUpdateSubmissionSnapshot result, String expectedMessage) {
        Assert.assertTrue(result.drawerOpen(),
                "Drawer đã đóng dù thiếu trường bắt buộc.");
        Assert.assertTrue(result.content().contains(expectedMessage),
                "Drawer thiếu validation: " + expectedMessage);
    }

    /** Giá sai phải giữ drawer đang mở; nếu đóng nghĩa là hệ thống đã nhận sai. */
    private void assertInvalidPriceRejected(String price) {
        String name = requireExistingItem();
        ItemUpdateSubmissionSnapshot result =
                catalogPage.submitItemUpdateDraft(name, name, price);
        Assert.assertTrue(result.drawerOpen(),
                "Drawer đã đóng sau khi nhập giá không hợp lệ: " + price);
    }
}
