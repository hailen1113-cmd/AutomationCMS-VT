package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.vuatho.navigation.MenuTarget.childOf;
import static com.vuatho.navigation.MenuTarget.topLevel;

public final class MenuCatalog {
    private static final List<MenuTarget> GENERAL = List.of(
            topLevel("Dashboard"),
            topLevel("Hiệu Quả Nguồn Thợ & Chi Phí"),
            topLevel("Tài chính"),
            childOf("Người Dùng", "Quản Lí Người Dùng"),
            childOf("Người Dùng", "Quản Lí eKYC"),
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
    private static final List<MenuTarget> ALL = Stream.concat(
                    GENERAL.stream(),
                    PartnerWorkerTestData.menuPages().stream())
            .toList();

    private MenuCatalog() {
    }

    public static List<MenuTarget> all() {
        return ALL;
    }

    public static Object[][] dataProviderRows() {
        String filter = System.getProperty("menu.filter", "").trim().toLowerCase(Locale.ROOT);
        Object[][] rows = ALL.stream()
                .map(MenuCatalog::normalize)
                .filter(menu -> filter.isBlank()
                        || menu.toString().toLowerCase(Locale.ROOT).contains(filter))
                .map(menu -> new Object[]{menu})
                .toArray(Object[][]::new);
        System.out.printf("[MENU DATA] %d menu page(s)%s%n",
                rows.length,
                filter.isBlank() ? "" : " matched filter: " + filter);
        return rows;
    }

    private static MenuTarget normalize(MenuTarget menu) {
        if ("Marketing".equals(menu.parent()) && menu.name().contains("-")) {
            return childOf("Marketing", "Hiệu suất Marketing");
        }
        return menu;
    }
}
