package com.vuatho.navigation;

/**
 * Mô tả một menu ERP cùng đường dẫn, nhãn hiển thị và thông tin dùng để điều hướng.
 */
public record MenuTarget(String parent, String name) {
    public static MenuTarget topLevel(String name) {
        return new MenuTarget(null, name);
    }

    /**
     * Thực hiện xử lý child of trong luồng kiểm thử.
     * @param parent giá trị parent được truyền vào
     * @param name giá trị name được truyền vào
     * @return kết quả child of sau khi xử lý
     */
    public static MenuTarget childOf(String parent, String name) {
        return new MenuTarget(parent, name);
    }

    /**
     * Kiểm tra điều kiện has parent.
     * @return kết quả has parent sau khi xử lý
     */
    public boolean hasParent() {
        return parent != null && !parent.isBlank();
    }

    /**
     * Thực hiện xử lý to string trong luồng kiểm thử.
     * @return kết quả to string sau khi xử lý
     */
    @Override
    public String toString() {
        return hasParent() ? parent + " > " + name : name;
    }
}
