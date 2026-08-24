package com.vuatho.hub;

/** Gán module hiển thị từ tiền tố ID catalog cố định. */
final class TestCaseModules {
    private TestCaseModules() {}

    static String of(String id) {
        if (id.startsWith("TRANSACTION-ALL")) return "Lịch sử giao dịch · Tất cả";
        if (id.startsWith("TRANSACTION-DEPOSIT")) return "Lịch sử giao dịch · Tiền nạp";
        if (id.startsWith("TRANSACTION-WITHDRAW")) return "Lịch sử giao dịch · Tiền rút";
        if (id.startsWith("TRANSACTION-ORDER")) return "Lịch sử giao dịch · Đơn dịch vụ";
        if (id.startsWith("TRANSACTION-REWARD")) return "Lịch sử giao dịch · Thưởng & KM";
        if (id.startsWith("TRANSACTION-FEE")) return "Lịch sử giao dịch · Phí & Doanh thu";
        if (id.startsWith("TRANSACTION-INSURANCE")) return "Lịch sử giao dịch · VT Care";
        if (id.startsWith("TRANSACTION-ASSISTANT")) return "Lịch sử giao dịch · Thợ phụ";
        if (id.startsWith("TRANSACTION-SYSTEM")) return "Lịch sử giao dịch · Hệ thống";
        if (id.startsWith("CWO-")) return "Đơn Khách - Thợ";
        if (id.startsWith("DASH-")) return "Dashboard";
        if (id.startsWith("EKYC-")) return "eKYC";
        if (id.startsWith("SMOKE-")) return "Smoke";
        if (id.startsWith("DIAG-")) return "Diagnostic";
        if (id.startsWith("CROSS-MENU-")) return "Cross menu";
        if (id.startsWith("UNI-CAT-")) return "Đồng phục · Catalog";
        if (id.startsWith("UNI-INV-") || id.startsWith("UNI-STOCK-") || id.startsWith("SALES-STOCK-")) {
            return "Đồng phục · Kho";
        }
        if (id.startsWith("UNI-ORD-") || id.startsWith("UNI-ORDER-")) return "Đồng phục · Đơn hàng";
        if (id.startsWith("USER-PROFILE-") || id.startsWith("USER-")) return "Hồ sơ người dùng";
        if (id.startsWith("WORKER-PROFILE-")) return "Hồ sơ thợ";
        if (id.startsWith("WORKER-MENU-") || id.startsWith("WM-")) return "Menu thợ";
        if (id.startsWith("WP-") || id.startsWith("WORKER-POST-")) return "Quản lý bài đăng thợ";
        if (id.startsWith("WV-") || id.startsWith("WORKER-VIOLATION-")) return "Thợ vi phạm";
        if (id.startsWith("WSR-") || id.startsWith("WORKER-STOP-")) return "Yêu cầu ngưng hợp tác";
        if (id.startsWith("WTM-") || id.startsWith("WORKER-TEST-")) return "Quản lý bài kiểm tra thợ";
        return "Khác";
    }

    static String flowType(String className) {
        String name = className == null ? "" : className;
        if (contains(name, "Filter", "Search")) return "Bộ lọc & tìm kiếm";
        if (contains(name, "Navigation", "Pagination", "Responsive")) return "Điều hướng";
        if (contains(name, "Detail")) return "Chi tiết";
        if (contains(name, "Export")) return "Xuất Excel";
        if (contains(name, "Overview", "Summary")) return "Tổng quan";
        if (contains(name, "Dropdown")) return "Dropdown";
        if (contains(name, "Workflow", "Mutation", "Approval")) return "Workflow";
        if (contains(name, "Accessibility")) return "Accessibility";
        if (contains(name, "Catalog")) return "Catalog";
        return "Kiểm tra giao diện";
    }

    private static boolean contains(String name, String... tokens) {
        for (String token : tokens) {
            if (name.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
