package com.vuatho.testdata;

import java.util.List;
import java.util.Locale;

import static com.vuatho.navigation.MenuTarget.childOf;
import static com.vuatho.navigation.MenuTarget.topLevel;

public final class FilterCatalog {
    private static final List<FilterTarget> SEARCH_FILTERS = List.of(
            filter("Người Dùng", "Quản Lí Người Dùng", "Tìm kiếm người dùng"),
            filter("Người Dùng", "Quản Lí eKYC", "Tìm kiếm người dùng"),
            filter("Đối Tác - Thợ", "Quản Lí Hồ Sơ Thợ", "Tìm kiếm thợ"),
            filter("Đối Tác - Thợ", "Quản Lí Thợ Vi Phạm", "Tìm kiếm thợ"),
            filter("Đối Tác - Thợ", "Quản Lí Bài Training", "Tìm kiếm bài training"),
            filter("Đối Tác - Thợ", "Yêu Cầu Ngưng Hợp Tác", "Tìm kiếm thợ theo tên"),
            topLevelFilter("Bài Kiểm Tra", "Tìm kiếm bài kiểm tra"),
            topLevelFilter("Nghiệp Vụ", "Tìm kiếm nghiệp vụ"),
            filter("Đơn Dịch Vụ", "Đơn Khách - Thợ", "Tìm kiếm mã đơn dịch vụ"),
            filter("Đơn Dịch Vụ", "Đơn Thợ Phụ", "Tìm kiếm mã đơn, tiêu đề"),
            filter("Đồng Phục", "Quản Lí Đồng Phục", "Tìm kiếm nhóm đồng phục"),
            filter("Đồng Phục", "Quản Lí Đơn Hàng Đồng Phục", "Tìm kiếm thông tin khách"),
            filter("Website", "Quản Lí Danh Mục", "Tìm kiếm danh mục"),
            filter("Website", "Quản Lí Bài Viết Nội Bộ", "Tìm kiếm bài viết"),
            filter("Website", "Quản Lí Bài Viết Truyền Thông", "Tìm kiếm bài viết"),
            filter("Website", "Quản Lí Hỗ Trợ Người Dùng", "Tìm kiếm tên người dùng"),
            filter("App", "Quản Lí Popup", "Tìm kiếm Popup"),
            filter("App", "Quản Lí Phương Thức Thanh Toán", "Tìm kiếm phương thức"),
            filter("App", "Quản Lí Notification", "Tìm kiếm thông báo"),
            filter("App", "Quản Lí Hình Thức Vi Phạm", "Tìm kiếm hình thức vi phạm"),
            filter("App", "Quản Lí Voucher", "Tìm kiếm mã voucher"),
            filter("App", "Quản Lí Chiến Dịch App", "Tìm kiếm chiến dịch"),
            filter("System", "Quản Lí AI", "Tìm kiếm model"),
            filter("System", "Quản Lí Mạng Xã Hội", "Tìm kiếm mạng xã hội"),
            filter("Marketing", "Quản Lí Chiến Dịch", "Tìm kiếm chiến dịch"),
            filter("Marketing", "Quản Lí Cuộc Thi", "Tìm kiếm cuộc thi"),
            filter("Marketing", "Yêu Cầu Hỗ Trợ (SOS)", "Tìm kiếm thông tin thợ"));

    private FilterCatalog() {
    }

    public static Object[][] searchFilterRows() {
        String requested = System.getProperty("filter.filter", "").trim().toLowerCase(Locale.ROOT);
        Object[][] rows = SEARCH_FILTERS.stream()
                .filter(filter -> requested.isBlank()
                        || filter.toString().toLowerCase(Locale.ROOT).contains(requested))
                .map(filter -> new Object[]{filter})
                .toArray(Object[][]::new);
        System.out.printf("[FILTER DATA] %d search filter(s)%s%n",
                rows.length,
                requested.isBlank() ? "" : " matched filter: " + requested);
        return rows;
    }

    private static FilterTarget filter(String parent, String page, String placeholder) {
        return new FilterTarget(childOf(parent, page), placeholder);
    }

    private static FilterTarget topLevelFilter(String page, String placeholder) {
        return new FilterTarget(topLevel(page), placeholder);
    }
}
