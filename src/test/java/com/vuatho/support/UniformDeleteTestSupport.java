package com.vuatho.support;

/**
 * Chuẩn bị dữ liệu độc lập cho testcase xóa danh mục Đồng phục.
 *
 * <p>Mỗi testcase tạo đúng bản ghi AUTO của riêng nó, không xóa dữ liệu có sẵn
 * và không phụ thuộc ID cố định trên sandbox.</p>
 */
public abstract class UniformDeleteTestSupport
        extends UniformCatalogCrudTestSupport {
    protected static final String GROUP_TAB = "Nhóm Đồng Phục";
    protected static final String ITEM_TAB = "Đồng Phục";

    /** Tạo một nhóm riêng để testcase có thể Hủy hoặc Xóa thật an toàn. */
    protected String prepareGroupForDelete() {
        String name = uniqueCatalogName("AUTO-GROUP-DELETE");
        if (!catalogPage.createGroup(name, "145000")) {
            throw new IllegalStateException(
                    "Không tạo được nhóm chuẩn bị kiểm tra xóa.");
        }
        return name;
    }

    /** Tạo một đồng phục riêng để testcase có thể Hủy hoặc Xóa thật an toàn. */
    protected String prepareItemForDelete() {
        String name = uniqueCatalogName("AUTO-UNIFORM-DELETE");
        if (!catalogPage.createUniformWithoutVariant(name, "175000")) {
            throw new IllegalStateException(
                    "Không tạo được đồng phục chuẩn bị kiểm tra xóa.");
        }
        return name;
    }
}
