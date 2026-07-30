package com.vuatho.testcases;

/**
 * Danh sách testcase cố định của module.
 * Mở file này để xem toàn bộ ID và mục tiêu testcase mà không cần chạy code.
 */
public final class EkycTestCases {
    public static final String EKYC_001 = "EKYC-001 - API Dashboard eKYC trả đúng cấu trúc thống kê";
    public static final String EKYC_002 = "EKYC-002 - API chi tiết trả thông tin cá nhân, hình ảnh và trạng thái xét duyệt";
    public static final String EKYC_003 = "EKYC-003 - API chi tiết từ chối ID hồ sơ sai hoặc không tồn tại";
    public static final String EKYC_004 = "EKYC-004 - Xóa trắng thông tin eKYC";
    public static final String EKYC_005 = "EKYC-005 - Sửa thông tin eKYC";
    public static final String EKYC_006 = "EKYC-006 - Dữ liệu test chỉnh sửa eKYC có ID duy nhất và đủ phạm vi";
    public static final String EKYC_007 = "EKYC-007 - Danh sách mặc định trả đúng cấu trúc hồ sơ và phân trang";
    public static final String EKYC_008 = "EKYC-008 - Giới hạn số bản ghi danh sách tối đa là 50";
    public static final String EKYC_009 = "EKYC-009 - Danh sách hỗ trợ lọc theo trạng thái";
    public static final String EKYC_010 = "EKYC-010 - Danh sách hỗ trợ lọc theo loại giấy tờ";
    public static final String EKYC_011 = "EKYC-011 - Danh sách hỗ trợ tìm theo ID người dùng";
    public static final String EKYC_012 = "EKYC-012 - Tìm kiếm không tồn tại trả kết quả rỗng";
    public static final String EKYC_013 = "EKYC-013 - Danh sách hỗ trợ truy vấn theo khoảng ngày";
    public static final String EKYC_014 = "EKYC-014 - Trang kế tiếp thay đổi tập hồ sơ khi còn dữ liệu";
    public static final String EKYC_015 = "EKYC-015 - Truy vấn không hợp lệ không gây lỗi máy chủ";
    public static final String EKYC_016 = "EKYC-016 - API cập nhật từ chối dữ liệu xét duyệt không hợp lệ";
    public static final String EKYC_017 = "EKYC-017 - API cập nhật duyệt tất cả mặt giấy tờ của hồ sơ mẫu";
    public static final String EKYC_018 = "EKYC-018 - API cập nhật từ chối hồ sơ mẫu kèm lý do giấy tờ";
    public static final String EKYC_019 = "EKYC-019 - API cập nhật được bảy trường thông tin có thể chỉnh sửa";
    public static final String EKYC_020 = "EKYC-020 - Chạy lại AI hoặc trả về lỗi nghiệp vụ phù hợp";
    public static final String EKYC_021 = "EKYC-021 - Duyệt và từ chối eKYC theo testcase trong workbook";
    public static final String EKYC_022 = "EKYC-022 - Các API eKYC bắt buộc xác thực";
    public static final String EKYC_023 = "EKYC-023 - Workbook eKYC cấp cao có 574 testcase không trùng";

    private EkycTestCases() {
    }
}
