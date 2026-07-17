package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/**
 * Điểm chạy tập trung cho toàn bộ nhóm test workflow eKYC trên ERP.
 *
 * <p>Lớp này chỉ cấu hình danh sách test và chuyển việc thực thi cho
 * {@link TestNgRunner}; không chứa logic kiểm thử nghiệp vụ.</p>
 */
public final class EkycWorkflowSuiteRunner {
    /**
     * Ngăn khởi tạo vì đây là lớp tiện ích chỉ cung cấp hàm {@link #main(String[])}.
     */
    private EkycWorkflowSuiteRunner() {
    }

    /**
     * Chạy tuần tự các nhóm review, chỉnh sửa và xóa thông tin eKYC.
     * Có thể gọi trực tiếp phương thức này từ IDE để chạy cả suite.
     *
     * @param args tham số dòng lệnh; hiện tại bộ chạy không sử dụng
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test workflow eKYC ERP",
                "Chay tat ca nhom testcase workflow eKYC",
                // Kiểm tra các quyết định review hồ sơ eKYC.
                EkycReviewWorkflowTest.class,
                // Kiểm tra cập nhật và lưu lại thông tin eKYC.
                EkycInformationEditWorkflowTest.class,
                // Kiểm tra thao tác xóa dữ liệu khỏi các trường thông tin eKYC.
                EkycInformationClearWorkflowTest.class);
    }
}
