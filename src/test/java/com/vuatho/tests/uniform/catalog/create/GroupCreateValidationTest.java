package com.vuatho.tests.uniform.catalog.create;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.GroupCreateSubmissionSnapshot;
import com.vuatho.support.UniformCatalogCrudTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Kiểm tra validation riêng của biểu mẫu Tạo mới nhóm đồng phục.
 *
 * <p>Mỗi testcase chỉ bỏ trống hoặc nhập sai một điều kiện để xác định chính xác
 * trường nào không được hệ thống kiểm soát.</p>
 */
public class GroupCreateValidationTest extends UniformCatalogCrudTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";

    public static void main(String[] args) {
        TestNgRunner.run(GroupCreateValidationTest.class,
                "Đồng phục", "Validation tạo nhóm đồng phục");
    }

    /** Xác nhận form rỗng phải hiển thị đồng thời lỗi của ba trường bắt buộc. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_072)
    public void emptyGroupCreateFormShowsRequiredValidation() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        String validation = catalogPage.submitEmptyCreateForm();
        for (String message : new String[]{
                "Nhập tên nhóm", "Nhập giá bán", "Chọn tài khoản thanh toán"}) {
            Assert.assertTrue(validation.contains(message),
                    "Form tạo nhóm thiếu validation " + message);
        }
    }

    /** Điền giá và tài khoản nhưng bỏ trống tên phải giữ drawer và báo lỗi tên. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_076)
    public void groupCannotBeCreatedWithoutName() {
        GroupCreateSubmissionSnapshot result =
                catalogPage.submitGroupCreateDraft(null, "125000", true);
        assertRequiredValidation(result, "Nhập tên nhóm");
    }

    /** Điền tên và tài khoản nhưng bỏ trống giá phải giữ drawer và báo lỗi giá. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_077)
    public void groupCannotBeCreatedWithoutPrice() {
        String name = uniqueCatalogName("AUTO-GROUP-MISSING-PRICE");
        GroupCreateSubmissionSnapshot result =
                catalogPage.submitGroupCreateDraft(name, null, true);
        assertRequiredValidation(result, "Nhập giá bán");
    }

    /** Điền tên và giá nhưng không chọn tài khoản phải giữ drawer và báo lỗi tài khoản. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_078)
    public void groupCannotBeCreatedWithoutPaymentAccount() {
        String name = uniqueCatalogName("AUTO-GROUP-MISSING-ACCOUNT");
        GroupCreateSubmissionSnapshot result =
                catalogPage.submitGroupCreateDraft(name, "125000", false);
        assertRequiredValidation(result, "Chọn tài khoản thanh toán");
    }

    /** Giá âm phải bị từ chối và không tạo bản ghi thật. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_079)
    public void negativePriceCannotCreateGroup() {
        assertInvalidPriceRejected("-125000", "AUTO-GROUP-NEGATIVE-PRICE");
    }

    /** Giá chứa chữ phải bị từ chối và không tạo bản ghi thật. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_080)
    public void alphabeticPriceCannotCreateGroup() {
        assertInvalidPriceRejected("abc", "AUTO-GROUP-TEXT-PRICE");
    }

    /** Giá bằng 0 phải bị từ chối và không tạo bản ghi thật. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_029)
    public void zeroPriceCannotCreateGroup() {
        assertInvalidPriceRejected("0", "AUTO-GROUP-ZERO-PRICE");
    }

    /** Tên chỉ chứa khoảng trắng phải được xem như chưa nhập. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_085)
    public void whitespaceOnlyNameCannotCreateGroup() {
        GroupCreateSubmissionSnapshot result =
                catalogPage.submitGroupCreateDraft("   ", "125000", true);
        assertRequiredValidation(result, "Nhập tên nhóm");
    }

    /** Giá chỉ chứa khoảng trắng phải được xem như chưa nhập. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_086)
    public void whitespaceOnlyPriceCannotCreateGroup() {
        String name = uniqueCatalogName("AUTO-GROUP-WHITESPACE-PRICE");
        GroupCreateSubmissionSnapshot result =
                catalogPage.submitGroupCreateDraft(name, "   ", true);
        assertRequiredValidation(result, "Nhập giá bán");
    }

    /** Đơn vị đồng Việt Nam không chấp nhận giá có phần thập phân. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_087)
    public void decimalPriceCannotCreateGroup() {
        assertInvalidPriceRejected("125000.5", "AUTO-GROUP-DECIMAL-PRICE");
    }

    /** Chuỗi số vượt xa giới hạn số nguyên phải bị từ chối an toàn. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_088)
    public void oversizedPriceCannotCreateGroup() {
        assertInvalidPriceRejected(
                "999999999999999999999999999999999999",
                "AUTO-GROUP-OVERSIZED-PRICE");
    }

    /** Input ảnh không được tạo preview hợp lệ từ file văn bản. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_089)
    public void nonImageFileDoesNotCreatePreview() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        Path file = createInvalidUploadFile("uniform-not-image.txt", "not an image");
        catalogPage.uploadCreateImages(List.of(file));
        Assert.assertEquals(catalogPage.loadedImagePreviewCount(), 0,
                "Form vẫn hiển thị preview hợp lệ cho file không phải hình ảnh.");
    }

    /** File mang đuôi PNG nhưng nội dung hỏng không được tạo preview hợp lệ. */
    @Test(groups = {"uniform", "catalog", "group-create", "validation",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_090)
    public void corruptedImageFileDoesNotCreatePreview() {
        catalogPage.open().selectTab(GROUP_TAB).openCreateDrawer();
        Path file = createInvalidUploadFile("uniform-corrupted.png", "broken png");
        catalogPage.uploadCreateImages(List.of(file));
        Assert.assertEquals(catalogPage.loadedImagePreviewCount(), 0,
                "Form vẫn hiển thị preview hợp lệ cho file hình ảnh bị hỏng.");
    }

    /** Assertion dùng chung cho từng trường bắt buộc, không gộp nhiều lỗi vào một testcase. */
    private void assertRequiredValidation(
            GroupCreateSubmissionSnapshot result, String expectedMessage) {
        Assert.assertTrue(result.drawerOpen(),
                "Form đã đóng và có thể đã tạo dữ liệu dù thiếu trường bắt buộc.");
        Assert.assertFalse(result.created(),
                "Dữ liệu vẫn được tạo dù thiếu trường bắt buộc.");
        Assert.assertTrue(result.content().contains(expectedMessage),
                "Form thiếu validation: " + expectedMessage);
    }

    /** Gửi từng giá không hợp lệ và xác nhận form không đóng, dữ liệu không được tạo. */
    private void assertInvalidPriceRejected(String price, String prefix) {
        String name = uniqueCatalogName(prefix);
        GroupCreateSubmissionSnapshot result =
                catalogPage.submitGroupCreateDraft(name, price, true);
        Assert.assertTrue(result.drawerOpen(),
                "Form đã đóng sau khi nhập giá không hợp lệ: " + price);
        Assert.assertFalse(result.created(),
                "Nhóm vẫn được tạo với giá không hợp lệ: " + price);
    }

    /** Tạo file upload không hợp lệ trong target để testcase không phụ thuộc máy chạy. */
    private Path createInvalidUploadFile(String fileName, String content) {
        Path directory = Path.of("target", "test-generated");
        Path file = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            Files.writeString(file, content);
            return file;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được file upload kiểm tra validation.", exception);
        }
    }
}
