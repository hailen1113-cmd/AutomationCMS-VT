package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;

/**
 * Chạy tập trung các workflow tìm kiếm, xem chi tiết và cập nhật hồ sơ người dùng.
 */
public final class UserProfileWorkflowSuiteRunner {
    private UserProfileWorkflowSuiteRunner() {
    }

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Bo test ho so nguoi dung ERP",
                "Chay tat ca nhom testcase ho so nguoi dung",
                UserProfileSearchTest.class,
                UserProfileDetailInteractionTest.class,
                UserProfileNameUpdateWorkflowTest.class,
                UserProfileAvatarUpdateWorkflowTest.class);
    }
}
