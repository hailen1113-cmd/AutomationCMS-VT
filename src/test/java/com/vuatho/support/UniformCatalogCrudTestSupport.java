package com.vuatho.support;

/**
 * Helper dùng chung cho testcase CRUD danh mục Đồng phục.
 *
 * <p>Lớp này không chứa annotation testcase; chỉ tạo tên dữ liệu độc lập và dọn bản ghi
 * AUTO trong {@code finally} để các file Create/Update/Delete chạy riêng được.</p>
 */
public abstract class UniformCatalogCrudTestSupport extends UniformModuleTestSupport {
    /** Sinh tên dữ liệu test không phụ thuộc ID cố định trên sandbox. */
    protected String uniqueCatalogName(String prefix) {
        return prefix + "-" + Long.toString(
                System.nanoTime(), 36).toUpperCase();
    }

    /** Cố gắng dọn dữ liệu AUTO nhưng không che kết quả assertion chính. */
    protected void safeDeleteCatalogItem(String tab, String name) {
        try {
            catalogPage.deleteItem(tab, name);
        } catch (RuntimeException ignored) {
            // Test sau mở lại route và dữ liệu AUTO có tên duy nhất.
        }
    }
}
