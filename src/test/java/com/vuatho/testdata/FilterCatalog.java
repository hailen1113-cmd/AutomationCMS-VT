package com.vuatho.testdata;

import java.util.List;
import java.util.Locale;

import static com.vuatho.navigation.MenuTarget.childOf;
import static com.vuatho.navigation.MenuTarget.topLevel;

/**
 * Khai báo tập trung các màn hình có bộ lọc và dữ liệu dùng để kiểm tra chéo.
 */
public final class FilterCatalog {
    private static final List<FilterTarget> GENERAL_SEARCH_FILTERS = List.of(
            filter("Người Dùng", "Quản Lí eKYC", "Tìm kiếm người dùng"),
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

    /**
     * Khởi tạo FilterCatalog với các phụ thuộc cần thiết.
     */
    private FilterCatalog() {
    }

    /**
     * Thực hiện xử lý search filter rows trong luồng kiểm thử.
     * @return kết quả search filter rows sau khi xử lý
     */
    public static Object[][] searchFilterRows() {
        String requested = System.getProperty("filter.filter", "").trim().toLowerCase(Locale.ROOT);
        Object[][] rows = GENERAL_SEARCH_FILTERS.stream()
                .filter(filter -> requested.isBlank()
                        || filter.toString().toLowerCase(Locale.ROOT).contains(requested))
                .map(filter -> new Object[] { filter })
                .toArray(Object[][]::new);
        System.out.printf("[FILTER DATA] %d search filter(s)%s%n",
                rows.length,
                requested.isBlank() ? "" : " matched filter: " + requested);
        return rows;
    }

    /**
     * Thực hiện xử lý filter trong luồng kiểm thử.
     * @param parent giá trị parent được truyền vào
     * @param page giá trị page được truyền vào
     * @param placeholder giá trị placeholder được truyền vào
     * @return kết quả filter sau khi xử lý
     */
    private static FilterTarget filter(String parent, String page, String placeholder) {
        return new FilterTarget(childOf(parent, page), placeholder);
    }

    /**
     * Thực hiện xử lý top level filter trong luồng kiểm thử.
     * @param page giá trị page được truyền vào
     * @param placeholder giá trị placeholder được truyền vào
     * @return kết quả top level filter sau khi xử lý
     */
    private static FilterTarget topLevelFilter(String page, String placeholder) {
        return new FilterTarget(topLevel(page), placeholder);
    }
}
