package com.vuatho.tests.uniform.catalog.create;

import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.UniformCatalogPage.ItemCreateSubmissionSnapshot;
import com.vuatho.support.UniformCatalogCrudTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Testcase dữ liệu không hợp lệ của thao tác tạo Đồng Phục. */
public class UniformItemCreateValidationTest extends UniformCatalogCrudTestSupport {
    private static final String ITEM_TAB = "Đồng Phục";

    public static void main(String[] args) {
        TestNgRunner.run(UniformItemCreateValidationTest.class,
                "Đồng phục", "Validation tạo đồng phục");
    }

    /** Form rỗng phải hiển thị đủ lỗi tên, giá và loại biến thể. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_073)
    public void emptyItemCreateFormShowsRequiredValidation() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        String validation = catalogPage.submitEmptyCreateForm();
        for (String message : new String[]{
                "Nhập tên đồng phục", "Nhập giá bán",
                "Vui lòng chọn loại biến thể"}) {
            Assert.assertTrue(validation.contains(message),
                    "Form tạo đồng phục thiếu validation " + message);
        }
    }

    /** Giá bán bằng 0 phải bị từ chối và không xuất hiện trong danh sách. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_069)
    public void zeroPriceCannotCreateItem() {
        assertInvalidPriceRejected("0", "AUTO-ITEM-ZERO-PRICE");
    }

    /** Điền giá và loại biến thể nhưng bỏ trống tên đồng phục. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_097)
    public void itemCannotBeCreatedWithoutName() {
        ItemCreateSubmissionSnapshot result =
                catalogPage.submitItemCreateDraft(
                        null, "150000", "Không có biến thể");
        assertRequiredValidation(result, "Nhập tên đồng phục");
    }

    /** Điền tên và loại biến thể nhưng bỏ trống giá bán. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_098)
    public void itemCannotBeCreatedWithoutPrice() {
        String name = uniqueCatalogName("AUTO-ITEM-MISSING-PRICE");
        ItemCreateSubmissionSnapshot result =
                catalogPage.submitItemCreateDraft(
                        name, null, "Không có biến thể");
        assertRequiredValidation(result, "Nhập giá bán");
    }

    /** Điền tên và giá nhưng không chọn Có/Không có biến thể. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_099)
    public void itemCannotBeCreatedWithoutVariantSelection() {
        String name = uniqueCatalogName("AUTO-ITEM-MISSING-VARIANT-TYPE");
        ItemCreateSubmissionSnapshot result =
                catalogPage.submitItemCreateDraft(name, "150000", null);
        assertRequiredValidation(result, "Vui lòng chọn loại biến thể");
    }

    /** Tên chỉ chứa khoảng trắng phải được xem như chưa nhập. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_100)
    public void whitespaceOnlyNameCannotCreateItem() {
        ItemCreateSubmissionSnapshot result =
                catalogPage.submitItemCreateDraft(
                        "   ", "150000", "Không có biến thể");
        assertRequiredValidation(result, "Nhập tên đồng phục");
    }

    /** Giá chỉ chứa khoảng trắng phải được xem như chưa nhập. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_101)
    public void whitespaceOnlyPriceCannotCreateItem() {
        String name = uniqueCatalogName("AUTO-ITEM-WHITESPACE-PRICE");
        ItemCreateSubmissionSnapshot result =
                catalogPage.submitItemCreateDraft(
                        name, "   ", "Không có biến thể");
        assertRequiredValidation(result, "Nhập giá bán");
    }

    /** Giá âm phải bị từ chối và không tạo đồng phục. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_102)
    public void negativePriceCannotCreateItem() {
        assertInvalidPriceRejected("-150000", "AUTO-ITEM-NEGATIVE-PRICE");
    }

    /** Giá chứa chữ phải bị từ chối và không tạo đồng phục. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_103)
    public void alphabeticPriceCannotCreateItem() {
        assertInvalidPriceRejected("abc", "AUTO-ITEM-TEXT-PRICE");
    }

    /** Giá thập phân không hợp lệ với đơn vị đồng Việt Nam. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_104)
    public void decimalPriceCannotCreateItem() {
        assertInvalidPriceRejected("150000.5", "AUTO-ITEM-DECIMAL-PRICE");
    }

    /** Chuỗi số vượt giới hạn phải bị từ chối an toàn. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_105)
    public void oversizedPriceCannotCreateItem() {
        assertInvalidPriceRejected(
                "999999999999999999999999999999999999",
                "AUTO-ITEM-OVERSIZED-PRICE");
    }

    /** File văn bản không được tạo preview ảnh hợp lệ trên form đồng phục. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_106)
    public void nonImageFileDoesNotCreateItemPreview() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        Path file = createInvalidUploadFile("uniform-item-not-image.txt", "not an image");
        catalogPage.uploadCreateImages(List.of(file));
        Assert.assertEquals(catalogPage.loadedImagePreviewCount(), 0,
                "Form đồng phục hiển thị preview hợp lệ cho file không phải ảnh.");
    }

    /** File mang đuôi PNG nhưng nội dung hỏng không được tạo preview hợp lệ. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_107)
    public void corruptedImageFileDoesNotCreateItemPreview() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        Path file = createInvalidUploadFile(
                "uniform-item-corrupted.png", "broken png");
        catalogPage.uploadCreateImages(List.of(file));
        Assert.assertEquals(catalogPage.loadedImagePreviewCount(), 0,
                "Form đồng phục hiển thị preview hợp lệ cho ảnh bị hỏng.");
    }

    /** Dòng biến thể thiếu tên tiếng Việt không được phép tạo sản phẩm. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_108)
    public void variantCannotBeCreatedWithoutVietnameseName() {
        assertVariantSubmissionRejected(catalogPage.submitIncompleteTextVariant(
                uniqueCatalogName("AUTO-ITEM-VARIANT-NO-VI"),
                false, true, true, true));
    }

    /** Dòng biến thể thiếu tên tiếng Anh không được phép tạo sản phẩm. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_109)
    public void variantCannotBeCreatedWithoutEnglishName() {
        assertVariantSubmissionRejected(catalogPage.submitIncompleteTextVariant(
                uniqueCatalogName("AUTO-ITEM-VARIANT-NO-EN"),
                true, false, true, true));
    }

    /** Biến thể Văn bản chưa có dòng giá trị không được phép tạo sản phẩm. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_110)
    public void textVariantCannotBeCreatedWithoutValueRow() {
        assertVariantSubmissionRejected(catalogPage.submitIncompleteTextVariant(
                uniqueCatalogName("AUTO-ITEM-VARIANT-NO-VALUE"),
                true, true, false, false));
    }

    /** Đã thêm dòng giá trị nhưng để trống vẫn phải bị từ chối. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_111)
    public void textVariantCannotBeCreatedWithEmptyValue() {
        assertVariantSubmissionRejected(catalogPage.submitIncompleteTextVariant(
                uniqueCatalogName("AUTO-ITEM-VARIANT-EMPTY-VALUE"),
                true, true, true, false));
    }

    /** Dòng biến thể chưa chọn Màu sắc hoặc Văn bản không được phép tạo. */
    @Test(groups = {"uniform", "catalog", "item-create", "validation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_112)
    public void variantCannotBeCreatedWithoutValueType() {
        assertVariantSubmissionRejected(catalogPage.submitVariantWithoutType(
                uniqueCatalogName("AUTO-ITEM-VARIANT-NO-TYPE")));
    }

    /** Assertion chung cho từng trường bắt buộc của form đồng phục. */
    private void assertRequiredValidation(
            ItemCreateSubmissionSnapshot result, String expectedMessage) {
        Assert.assertTrue(result.drawerOpen(),
                "Form đã đóng dù thiếu trường bắt buộc.");
        Assert.assertFalse(result.created(),
                "Dữ liệu vẫn được tạo dù thiếu trường bắt buộc.");
        Assert.assertTrue(result.content().contains(expectedMessage),
                "Form thiếu validation: " + expectedMessage);
    }

    /** Gửi giá sai định dạng và xác nhận form không đóng, dữ liệu không được tạo. */
    private void assertInvalidPriceRejected(String price, String prefix) {
        String name = uniqueCatalogName(prefix);
        ItemCreateSubmissionSnapshot result =
                catalogPage.submitItemCreateDraft(
                        name, price, "Không có biến thể");
        Assert.assertTrue(result.drawerOpen(),
                "Form đã đóng sau khi nhập giá không hợp lệ: " + price);
        Assert.assertFalse(result.created(),
                "Đồng phục vẫn được tạo với giá không hợp lệ: " + price);
    }

    /** Assertion chung cho form có dòng biến thể chưa đủ dữ liệu. */
    private void assertVariantSubmissionRejected(
            ItemCreateSubmissionSnapshot result) {
        Assert.assertTrue(result.drawerOpen(),
                "Form đã đóng dù dữ liệu biến thể chưa hợp lệ.");
        Assert.assertFalse(result.created(),
                "Đồng phục vẫn được tạo dù dữ liệu biến thể chưa hợp lệ.");
    }

    /** Tạo file upload sai trong target để testcase không phụ thuộc máy chạy. */
    private Path createInvalidUploadFile(String fileName, String content) {
        Path directory = Path.of("target", "test-generated");
        Path file = directory.resolve(fileName);
        try {
            Files.createDirectories(directory);
            Files.writeString(file, content);
            return file;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được file upload validation đồng phục.", exception);
        }
    }
}
