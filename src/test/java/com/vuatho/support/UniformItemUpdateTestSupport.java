package com.vuatho.support;

import org.testng.SkipException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu và tiện ích dùng chung cho testcase cập nhật Đồng Phục.
 *
 * <p>Luồng luôn tìm động một đồng phục hiện có, ưu tiên dữ liệu AUTO và SKIP có
 * lý do khi tab không có bản ghi. Lớp không chứa {@code @Test}.</p>
 */
public abstract class UniformItemUpdateTestSupport
        extends UniformCatalogCrudTestSupport {
    protected static final String ITEM_TAB = "Đồng Phục";

    /** Chọn một đồng phục hiện có mà không phụ thuộc ID hoặc tên cố định. */
    protected String requireExistingItem() {
        catalogPage.open().selectTab(ITEM_TAB);
        List<String> names = catalogPage.displayedItemNames();
        String name = names.stream()
                .filter(candidate -> candidate.toUpperCase().startsWith("AUTO-"))
                .findFirst()
                .orElseGet(() -> names.stream().findFirst().orElse(""));
        if (name.isBlank()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] Tab "
                    + ITEM_TAB + " không có bản ghi để cập nhật.");
        }
        return name;
    }

    /** Tạo danh sách ảnh PNG thật trong target để upload bằng input file. */
    protected List<Path> createRasterTestImages(int count, String prefix) {
        Path directory = Path.of("target", "test-generated", "uniform-item-update");
        List<Path> paths = new ArrayList<>();
        try {
            Files.createDirectories(directory);
            for (int index = 1; index <= count; index++) {
                Path imagePath = directory.resolve(prefix + "-" + index + ".png");
                BufferedImage image = new BufferedImage(
                        120, 120, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.setColor(new Color(
                            (30 * index) % 255,
                            (80 * index) % 255,
                            (140 * index) % 255));
                    graphics.fillRect(0, 0, 120, 120);
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(25, 25, 70, 70);
                } finally {
                    graphics.dispose();
                }
                ImageIO.write(image, "png", imagePath.toFile());
                paths.add(imagePath);
            }
            return paths;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được ảnh PNG cập nhật đồng phục.", exception);
        }
    }

    /** Tạo file văn bản thật để kiểm tra input ảnh từ chối sai định dạng. */
    protected Path createNonImageFile() {
        Path path = Path.of(
                "target", "test-generated", "uniform-item-update", "not-image.txt");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "Đây không phải là dữ liệu hình ảnh.");
            return path;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được file sai định dạng.", exception);
        }
    }

    /** Tạo file có đuôi PNG nhưng nội dung hỏng để kiểm tra xác thực ảnh. */
    protected Path createCorruptedImageFile() {
        Path path = Path.of(
                "target", "test-generated", "uniform-item-update", "broken.png");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, "broken image payload");
            return path;
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Không tạo được file ảnh hỏng.", exception);
        }
    }
}
