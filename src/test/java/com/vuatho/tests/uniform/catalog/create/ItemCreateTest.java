package com.vuatho.tests.uniform.catalog.create;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformCatalogCrudTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Toàn bộ testcase tạo Đồng Phục hợp lệ: form, ảnh, biến thể và lưu dữ liệu thật.
 */
public class ItemCreateTest extends UniformCatalogCrudTestSupport {
    private static final String ITEM_TAB = "Đồng Phục";
    private static final Path IMAGE_DIRECTORY = Path.of(
            "src", "test", "resources", "uniform-images");

    public static void main(String[] args) {
        TestNgRunner.run(ItemCreateTest.class,
                "Đồng phục", "Tạo đồng phục");
    }

    /** Kiểm tra đầy đủ trường cơ bản, upload và hai lựa chọn biến thể theo DOM drawer. */
    @Test(groups = {"uniform", "catalog", "item-create", "form", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_002)
    public void itemCreateFormContainsRequiredControls() {
        String form = catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        for (String field : new String[]{
                "Tên đồng phục", "Giá bán", "Hình ảnh sản phẩm",
                "Tối đa 5 ảnh", "Không có biến thể", "Có biến thể"}) {
            Assert.assertTrue(form.contains(field), "Form tạo đồng phục thiếu " + field);
        }

        var controls = catalogPage.uniformCreateFormSnapshot();
        Assert.assertEquals(controls.businessTextInputCount(), 2,
                "Form phải có đúng ô Tên đồng phục và Giá bán.");
        Assert.assertEquals(controls.variantChoiceCount(), 2,
                "Form phải có đúng hai lựa chọn Không có/Có biến thể.");
        Assert.assertEquals(controls.selectedVariantChoiceCount(), 0,
                "Form mới không được tự chọn loại biến thể.");
        Assert.assertTrue(controls.imageUpload() && controls.multipleUpload(),
                "Upload sản phẩm phải nhận tệp ảnh và hỗ trợ chọn nhiều tệp.");
        Assert.assertTrue(controls.fiveImageHint(),
                "Form thiếu hướng dẫn giới hạn tối đa năm ảnh.");
        Assert.assertTrue(controls.cancelButton() && controls.confirmButton(),
                "Form thiếu nút Hủy hoặc Xác nhận.");
    }

    /** Nhập tên nháp rồi Hủy phải đóng drawer và không tạo dữ liệu. */
    @Test(groups = {"uniform", "catalog", "item-create", "cancel", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_070)
    public void cancelClosesItemCreateFormWithoutSaving() {
        assertCloseDoesNotSave(false);
    }

    /** Nhập tên nháp rồi bấm X phải đóng drawer và không tạo dữ liệu. */
    @Test(groups = {"uniform", "catalog", "item-create", "close", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_071)
    public void closeIconClosesItemCreateFormWithoutSaving() {
        assertCloseDoesNotSave(true);
    }

    /** Upload sáu ảnh nhưng form chỉ được hiển thị tối đa năm ảnh xem trước. */
    @Test(groups = {"uniform", "catalog", "item-create", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_020)
    public void itemImageUploadHonorsFiveImageLimit() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        List<Path> images = IntStream.rangeClosed(1, 6)
                .mapToObj(index -> IMAGE_DIRECTORY.resolve(
                        "uniform-" + index + ".svg"))
                .toList();
        int previews = catalogPage.uploadCreateImages(images);
        Assert.assertTrue(previews > 0,
                "Upload ảnh đồng phục không hiển thị bản xem trước.");
        Assert.assertTrue(previews <= 5,
                "Form hiển thị " + previews + " ảnh, vượt giới hạn 5 ảnh.");
    }

    /** Có thể thêm dòng thứ hai rồi xóa đúng dòng cuối. */
    @Test(groups = {"uniform", "catalog", "item-create", "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_021)
    public void variantRowsCanBeAddedAndRemoved() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        catalogPage.addVariantDraft();
        Assert.assertEquals(catalogPage.variantRowCount(), 1,
                "Bật Có biến thể không tạo dòng đầu tiên.");
        Assert.assertEquals(catalogPage.addAnotherVariantRow(), 2,
                "Thêm biến thể không tạo dòng thứ hai.");
        Assert.assertEquals(catalogPage.removeLastVariantRow(), 1,
                "Xóa dòng cuối không đưa danh sách về một biến thể.");
    }

    /** Dòng biến thể phải chuyển được giữa kiểu Màu sắc và Văn bản. */
    @Test(groups = {"uniform", "catalog", "item-create", "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_022)
    public void variantSupportsColorAndTextTypes() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        catalogPage.addVariantDraft();
        String color = catalogPage.chooseFirstVariantType("Màu sắc");
        Assert.assertTrue(color.contains("Màu sắc"),
                "Chọn Màu sắc nhưng dòng biến thể không cập nhật.");

        String text = catalogPage.chooseFirstVariantType("Văn bản");
        Assert.assertTrue(text.contains("Văn bản"),
                "Chọn Văn bản nhưng dòng biến thể không cập nhật.");
        Assert.assertFalse(catalogPage.addFirstTextVariantValueDraft()
                        .contains("Chưa có giá trị"),
                "Bấm Thêm nhưng chưa tạo dòng giá trị biến thể.");
    }

    /** Tạo đồng phục không biến thể thật và tìm lại được trong danh sách. */
    @Test(groups = {"uniform", "catalog", "item-create", "mutation", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_024)
    public void createItemWithoutVariantPersistsInList() {
        String name = uniqueCatalogName("AUTO-UNIFORM");
        Assert.assertTrue(catalogPage.createUniformWithoutVariant(name, "150000"),
                "Tạo đồng phục xong nhưng không tìm thấy " + name);
    }

    /** Tạo đồng phục có biến thể Văn bản thật và tìm lại được trong danh sách. */
    @Test(groups = {"uniform", "catalog", "item-create", "mutation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_030)
    public void createItemWithTextVariantPersistsInList() {
        String name = uniqueCatalogName("AUTO-UNIFORM-VARIANT");
        Assert.assertTrue(catalogPage.createUniformWithTextVariant(name, "185000"),
                "Tạo đồng phục có biến thể nhưng không tìm thấy " + name);
    }

    /** Upload đúng năm ảnh phải hiển thị đủ năm preview, không thiếu hoặc vượt giới hạn. */
    @Test(groups = {"uniform", "catalog", "item-create", "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_091)
    public void exactlyFiveItemImagesShowFivePreviews() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        List<Path> images = IntStream.rangeClosed(1, 5)
                .mapToObj(index -> IMAGE_DIRECTORY.resolve(
                        "uniform-" + index + ".svg"))
                .toList();
        Assert.assertEquals(catalogPage.uploadCreateImages(images), 5,
                "Upload đúng năm ảnh nhưng số preview không bằng năm.");
    }

    /** Tạo đồng phục với ảnh PNG thật và kiểm tra ảnh tải được trong drawer chi tiết. */
    @Test(groups = {"uniform", "catalog", "item-create", "mutation",
            "media", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_092)
    public void createItemWithImagePersistsImage() {
        String name = uniqueCatalogName("AUTO-UNIFORM-IMAGE");
        var result = catalogPage.createUniformWithImages(
                name, "195000", List.of(createRasterTestImage()));
        Assert.assertTrue(result.created(),
                "Không tạo được đồng phục có ảnh.");
        Assert.assertTrue(result.previewCount() >= 1,
                "Ảnh đã chọn không hiển thị bản xem trước.");
        Assert.assertTrue(result.detailLoadedImageCount() >= 1,
                "Ảnh đồng phục không được tải lại trong chi tiết.");
    }

    /** Chuyển từ Có biến thể về Không có biến thể phải bỏ các dòng cấu hình nháp. */
    @Test(groups = {"uniform", "catalog", "item-create",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_093)
    public void switchingToNoVariantClearsVariantRows() {
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        catalogPage.addVariantDraft();
        Assert.assertEquals(catalogPage.variantRowCount(), 1,
                "Chọn Có biến thể nhưng không xuất hiện dòng cấu hình.");
        Assert.assertEquals(catalogPage.switchBackToNoVariantDraft(), 0,
                "Chuyển về Không có biến thể nhưng dòng cấu hình vẫn còn.");
    }

    /** Tạo một nhóm biến thể Văn bản có nhiều giá trị và đọc lại trong chi tiết. */
    @Test(groups = {"uniform", "catalog", "item-create", "mutation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_094)
    public void createItemWithMultipleTextValuesPersistsValues() {
        String name = uniqueCatalogName("AUTO-UNIFORM-MULTI-VALUE");
        var result = catalogPage.createUniformWithTextVariantData(
                name, "205000", List.of("S", "M", "L"), false);
        Assert.assertTrue(result.created(),
                "Không tạo được đồng phục có nhiều giá trị biến thể.");
        Assert.assertTrue(result.detailContainsExpectedValues(),
                "Chi tiết không hiển thị đủ tên và giá trị biến thể đã tạo.");
    }

    /** Tạo hai nhóm biến thể Văn bản và đọc lại toàn bộ dữ liệu trong chi tiết. */
    @Test(groups = {"uniform", "catalog", "item-create", "mutation",
            "variant", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_095)
    public void createItemWithTwoTextVariantsPersistsBothVariants() {
        String name = uniqueCatalogName("AUTO-UNIFORM-TWO-VARIANTS");
        var result = catalogPage.createUniformWithTextVariantData(
                name, "215000", List.of("M"), true);
        Assert.assertTrue(result.created(),
                "Không tạo được đồng phục có hai nhóm biến thể.");
        Assert.assertTrue(result.detailContainsExpectedValues(),
                "Chi tiết không hiển thị đủ hai nhóm biến thể đã tạo.");
    }

    /** Đồng phục vừa tạo phải còn tồn tại sau khi tải lại trang quản lí. */
    @Test(groups = {"uniform", "catalog", "item-create", "mutation",
            "reload", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_096)
    public void createdItemPersistsAfterPageReload() {
        String name = uniqueCatalogName("AUTO-UNIFORM-RELOAD");
        Assert.assertTrue(catalogPage.createUniformWithoutVariant(name, "225000"),
                "Không tạo được đồng phục chuẩn bị kiểm tra tải lại.");
        Assert.assertTrue(catalogPage.itemPersistsAfterRefresh(ITEM_TAB, name),
                "Đồng phục vừa tạo biến mất sau khi tải lại trang.");
    }

    /** Dùng chung kiểm tra đóng form đồng phục nhưng vẫn giữ hai testcase có ID độc lập. */
    private void assertCloseDoesNotSave(boolean byIcon) {
        String draftName = "AUTO-ITEM-DRAFT-" + Long.toString(
                System.nanoTime(), 36).toUpperCase();
        catalogPage.open().selectTab(ITEM_TAB).openCreateDrawer();
        catalogPage.fillCreateName(draftName);
        boolean closed = byIcon
                ? catalogPage.closeCreateDrawerByIcon()
                : catalogPage.cancelCreateDrawer();
        Assert.assertTrue(closed, "Không đóng được form tạo đồng phục.");
        Assert.assertFalse(catalogPage.itemExists(ITEM_TAB, draftName),
                "Đóng form nhưng vẫn tạo dữ liệu " + draftName);
    }

    /** Tạo ảnh PNG raster thật làm dữ liệu upload của testcase tạo có ảnh. */
    private Path createRasterTestImage() {
        Path directory = Path.of("target", "test-generated");
        Path imagePath = directory.resolve("uniform-item-create.png");
        try {
            Files.createDirectories(directory);
            BufferedImage image = new BufferedImage(
                    100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                graphics.setColor(new Color(37, 99, 235));
                graphics.fillRect(0, 0, 100, 100);
                graphics.setColor(Color.WHITE);
                graphics.fillOval(25, 25, 50, 50);
            } finally {
                graphics.dispose();
            }
            ImageIO.write(image, "png", imagePath.toFile());
            return imagePath;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được ảnh PNG dữ liệu test đồng phục.", exception);
        }
    }
}
