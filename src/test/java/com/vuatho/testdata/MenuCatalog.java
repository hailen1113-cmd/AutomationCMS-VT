package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

import java.util.List;
import java.util.Locale;

import static com.vuatho.navigation.MenuTarget.childOf;
import static com.vuatho.navigation.MenuTarget.topLevel;

public final class MenuCatalog {
    private static final List<MenuTarget> ALL = List.of(
            topLevel("Tài chính"),
            childOf("Người Dùng", "Quản Lí Người Dùng"),
            childOf("Người Dùng", "Quản Lí eKYC"),
            childOf("Đối Tác - Thợ", "Quản Lí Hồ Sơ Thợ"),
            childOf("Đối Tác - Thợ", "Quản Lí Thợ Vi Phạm"),
            childOf("Đối Tác - Thợ", "Quản Lí Bài Training"),
            childOf("Đối Tác - Thợ", "Quản Lí Bài Đăng"),
            childOf("Đối Tác - Thợ", "Yêu Cầu Ngưng Hợp Tác"),
            topLevel("Bài Kiểm Tra"),
            topLevel("Nghiệp Vụ"),
            childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ"),
            childOf("Đơn Dịch Vụ", "Đơn Thợ Phụ"),
            childOf("Đồng Phục", "Quản Lí Đồng Phục"),
            childOf("Đồng Phục", "Quản Lí Đơn Hàng Đồng Phục"),
            childOf("Đồng Phục", "Quản Lí Kho Đồng Phục"),
            childOf("Giao Dịch", "Lịch Sử Giao Dịch"),
            childOf("Website", "Quản Lí Danh Mục"),
            childOf("Website", "Quản Lí Bài Viết Nội Bộ"),
            childOf("Website", "Quản Lí Bài Viết Truyền Thông"),
            childOf("Website", "Quản Lí Hỗ Trợ Người Dùng"),
            childOf("Website", "Cấu Hình Website"),
            childOf("App", "Quản Lí Popup"),
            childOf("App", "Quản Lí Phương Thức Thanh Toán"),
            childOf("App", "Quản Lí Notification"),
            childOf("App", "Quản Lí Hình Thức Vi Phạm"),
            childOf("App", "Quản Lí Voucher"),
            childOf("App", "Quản Lí Chiến Dịch App"),
            childOf("App", "Cấu hình App"),
            childOf("System", "Quản Lí AI"),
            childOf("System", "Quản Lí Mạng Xã Hội"),
            childOf("Marketing", "Thống Kê Thợ - Khách"),
            childOf("Marketing", "Chương Trình Khuyến Mãi"),
            childOf("Marketing", "Quản Lí Chiến Dịch"),
            childOf("Marketing", "Quản Lí Cuộc Thi"),
            childOf("Marketing", "Tỏa Sáng Vua Thợ"),
            childOf("Marketing", "Quản Lí Vua Thợ Care"),
            childOf("Marketing", "Yêu Cầu Hỗ Trợ (SOS)"));

    private MenuCatalog() {
    }

    public static List<MenuTarget> all() {
        return ALL;
    }

    public static Object[][] dataProviderRows() {
        String filter = System.getProperty("menu.filter", "").trim().toLowerCase(Locale.ROOT);
        return ALL.stream()
                .filter(menu -> filter.isBlank()
                        || menu.toString().toLowerCase(Locale.ROOT).contains(filter))
                .map(menu -> new Object[]{menu})
                .toArray(Object[][]::new);
    }
}
