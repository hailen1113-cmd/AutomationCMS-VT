package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy bộ regression cho chức năng tìm kiếm và bộ lọc trên các màn hình ERP.
 *
 * <p>Suite bắt đầu bằng kiểm tra đăng nhập/điều hướng, sau đó xác nhận tìm kiếm
 * hồ sơ người dùng, tìm kiếm và lọc hồ sơ thợ, cuối cùng kiểm tra thao tác reset
 * bộ lọc trên nhiều menu.</p>
 */
public final class ErpSearchFilterSuiteRunner {
    /** Không cho khởi tạo vì lớp này chỉ là điểm chạy suite. */
    private ErpSearchFilterSuiteRunner() {
    }

    /**
     * Chạy toàn bộ nhóm test tìm kiếm và bộ lọc bằng TestNG.
     *
     * @param args tham số dòng lệnh; hiện chưa được sử dụng
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "ERP Filter Automation Suite",
                "All Search Filter Menus",
                // Xác nhận có thể đăng nhập và truy cập màn hình gốc trước các test chức năng.
                LoginDashboardSourceAccessTest.class,
                // Xác nhận các menu đích có thể được mở từ sidebar.
                CrossMenuSidebarNavigationTest.class,
                // Kiểm tra tìm kiếm trên hai miền dữ liệu người dùng và thợ.
                UserProfileSearchTest.class,
                WorkerProfileSearchTest.class,
                // Kiểm tra từng tiêu chí lọc của danh sách hồ sơ thợ.
                WorkerProfileFilterTest.class,
                // Xác nhận reset đưa bộ lọc về trạng thái ban đầu trên nhiều menu.
                CrossMenuSearchFilterResetTest.class);
    }
}
