package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

/**
 * Mô tả một màn hình Đối tác - Thợ cần kiểm tra cùng menu đích tương ứng.
 */
public record PartnerWorkerCase(
        String id,
        String scenario,
        MenuTarget page,
        String searchPlaceholder) {

    /**
     * Kiểm tra điều kiện has search filter.
     * @return kết quả has search filter sau khi xử lý
     */
    public boolean hasSearchFilter() {
        return searchPlaceholder != null && !searchPlaceholder.isBlank();
    }

    /**
     * Thực hiện xử lý to string trong luồng kiểm thử.
     * @return kết quả to string sau khi xử lý
     */
    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
