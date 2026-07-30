package com.vuatho.testcases;

/**
 * Readable, compile-time testcase list for this business module.
 * Each constant is used directly by exactly one business {@code @Test}.
 */
public final class WorkerPostTestCases {
    private WorkerPostTestCases() {
    }

    public static final String WORKER_POST_001 = "WORKER-POST-001 - Dialog Từ chối bắt buộc lý do và Hủy được";
    public static final String WORKER_POST_002 = "WORKER-POST-002 - Duyệt bài chuyển bài sang Đã duyệt";
    public static final String WORKER_POST_003 = "WORKER-POST-003 - Từ chối bài lưu lý do và chuyển tab";
    public static final String WORKER_POST_004 = "WORKER-POST-004 - Click thumbnail mở modal đủ thông tin và đóng được";
    public static final String WORKER_POST_005 = "WORKER-POST-005 - Nút next/previous thay đổi và phục hồi media";
    public static final String WORKER_POST_006 = "WORKER-POST-006 - Zoom và xoay ảnh cập nhật đúng transform";
    public static final String WORKER_POST_007 = "WORKER-POST-007 - Xem ảnh thành công ở từng tab trạng thái";
    public static final String WORKER_POST_008 = "WORKER-POST-008 - Xem video thành công ở từng tab trạng thái";
    public static final String WORKER_POST_009 = "WORKER-POST-009 - Nút X đóng modal media";
    public static final String WORKER_POST_010 = "WORKER-POST-010 - Thumbnail +N mở viewer media";
    public static final String WORKER_POST_011 = "WORKER-POST-011 - Chuyển trạng thái cập nhật tab và query URL";
    public static final String WORKER_POST_012 = "WORKER-POST-012 - Tên thợ điều hướng đến đúng hồ sơ";
    public static final String WORKER_POST_013 = "WORKER-POST-013 - Trang có đủ bốn tab trạng thái và mặc định Chờ duyệt";
    public static final String WORKER_POST_014 = "WORKER-POST-014 - Card Chờ duyệt hiển thị đủ thông tin bài đăng";
    public static final String WORKER_POST_015 = "WORKER-POST-015 - Mỗi bài Chờ duyệt có nút Duyệt bài và Từ chối";
    public static final String WORKER_POST_016 = "WORKER-POST-016 - Bài Đã duyệt hiển thị người và ngày duyệt";
    public static final String WORKER_POST_017 = "WORKER-POST-017 - Bài Từ chối hiển thị audit và lý do từ chối";
    public static final String WORKER_POST_018 = "WORKER-POST-018 - Bài Đã xóa là trạng thái chỉ đọc";
    public static final String WORKER_POST_019 = "WORKER-POST-019 - Card có timestamp và tổng media hợp lệ";
    public static final String WORKER_POST_020 = "WORKER-POST-020 - Phân trang đổi dữ liệu và Reset quay về trang đầu";
    public static final String WORKER_POST_021 = "WORKER-POST-021 - Next và Previous đổi đúng trang";
    public static final String WORKER_POST_022 = "WORKER-POST-022 - Tab và card vẫn dùng được ở viewport tablet";
}
