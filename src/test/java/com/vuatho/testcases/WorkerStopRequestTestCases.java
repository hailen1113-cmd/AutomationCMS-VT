package com.vuatho.testcases;

/**
 * Readable, compile-time testcase list for this business module.
 * Each constant is used directly by exactly one business {@code @Test}.
 */
public final class WorkerStopRequestTestCases {
    private WorkerStopRequestTestCases() {
    }

    public static final String WORKER_STOP_001 = "WORKER-STOP-001 - Duyệt thật chuyển yêu cầu sang Đã duyệt";
    public static final String WORKER_STOP_002 = "WORKER-STOP-002 - Từ chối thật chuyển yêu cầu sang Đã từ chối";
    public static final String WORKER_STOP_003 = "WORKER-STOP-003 - Bỏ qua thật chuyển yêu cầu sang Đã bỏ qua";
    public static final String WORKER_STOP_004 = "WORKER-STOP-004 - Mở khóa thật loại bỏ action Quay lại làm việc";
    public static final String WORKER_STOP_005 = "WORKER-STOP-005 - Click dòng mở chi tiết đầy đủ dữ liệu";
    public static final String WORKER_STOP_006 = "WORKER-STOP-006 - Yêu cầu lặp lại hiển thị lịch sử";
    public static final String WORKER_STOP_007 = "WORKER-STOP-007 - Bài đã duyệt mở chi tiết đúng trạng thái";
    public static final String WORKER_STOP_008 = "WORKER-STOP-008 - Từ chối bắt buộc chọn lý do";
    public static final String WORKER_STOP_009 = "WORKER-STOP-009 - Thống kê và bảng trả về dữ liệu hợp lệ";
    public static final String WORKER_STOP_010 = "WORKER-STOP-010 - Sang trang 2 đổi dữ liệu và quay lại trang 1";
    public static final String WORKER_STOP_011 = "WORKER-STOP-011 - Tìm kiếm theo tên trả đúng dữ liệu và Reset phục hồi";
    public static final String WORKER_STOP_012 = "WORKER-STOP-012 - Mỗi bộ lọc chỉ trả đúng trạng thái";
}
