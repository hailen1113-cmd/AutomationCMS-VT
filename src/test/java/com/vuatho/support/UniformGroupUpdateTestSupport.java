package com.vuatho.support;

import org.testng.SkipException;

import java.util.List;

/**
 * Dùng chung cho testcase cập nhật Nhóm Đồng Phục.
 *
 * <p>Luôn ưu tiên dữ liệu AUTO hiện có, không tạo bản ghi chuẩn bị và không
 * xóa dữ liệu sau testcase.</p>
 */
public abstract class UniformGroupUpdateTestSupport
        extends UniformCatalogCrudTestSupport {
    protected static final String GROUP_TAB = "Nhóm Đồng Phục";

    /** Lấy động một nhóm hiện có hoặc SKIP nếu tab hoàn toàn không có dữ liệu. */
    protected String requireExistingGroup() {
        catalogPage.open().selectTab(GROUP_TAB);
        List<String> names = catalogPage.displayedItemNames();
        String name = names.stream()
                .filter(candidate -> candidate.toUpperCase().startsWith("AUTO-"))
                .findFirst()
                .orElseGet(() -> names.stream().findFirst().orElse(""));
        if (name.isBlank()) {
            throw new SkipException("[THIẾU DỮ LIỆU TEST] Tab "
                    + GROUP_TAB + " không có bản ghi để cập nhật.");
        }
        return name;
    }
}
