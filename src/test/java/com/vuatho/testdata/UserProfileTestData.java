package com.vuatho.testdata;

import java.util.List;
import java.util.Optional;

/**
 * Tạo các bộ dữ liệu đầu vào cho workflow xem, duyệt và cập nhật hồ sơ người dùng.
 */
public final class UserProfileTestData {
    private static final List<UserProfileCase> CASES = List.of(
            viewCase(
                    "USER-001",
                    "Xem thông tin người dùng đầu tiên trong danh sách"),
            approveCase(
                    "USER-NAME-APPROVE-001",
                    "Chấp nhận yêu cầu cập nhật họ tên",
                    UserProfileFeature.NAME_UPDATE),
            rejectCase(
                    "USER-NAME-REJECT-001",
                    "Từ chối yêu cầu cập nhật họ tên bằng lý do có sẵn",
                    UserProfileFeature.NAME_UPDATE,
                    UserProfileRejectReasonMode.DEFAULT),
            rejectCase(
                    "USER-NAME-REJECT-OTHER-001",
                    "Từ chối yêu cầu cập nhật họ tên bằng lý do khác",
                    UserProfileFeature.NAME_UPDATE,
                    UserProfileRejectReasonMode.OTHER),
            approveCase(
                    "USER-AVATAR-APPROVE-001",
                    "Chấp nhận yêu cầu cập nhật ảnh đại diện",
                    UserProfileFeature.AVATAR_UPDATE),
            rejectCase(
                    "USER-AVATAR-REJECT-001",
                    "Từ chối yêu cầu cập nhật ảnh đại diện bằng lý do có sẵn",
                    UserProfileFeature.AVATAR_UPDATE,
                    UserProfileRejectReasonMode.DEFAULT),
            rejectCase(
                    "USER-AVATAR-REJECT-OTHER-001",
                    "Từ chối yêu cầu cập nhật ảnh đại diện bằng lý do khác",
                    UserProfileFeature.AVATAR_UPDATE,
                    UserProfileRejectReasonMode.OTHER));

    /**
     * Khởi tạo UserProfileTestData với các phụ thuộc cần thiết.
     */
    private UserProfileTestData() {
    }

    /**
     * Thực hiện xử lý cases trong luồng kiểm thử.
     * @return kết quả cases sau khi xử lý
     */
    public static List<UserProfileCase> cases() {
        return CASES;
    }

    /**
     * Tìm by id trong luồng kiểm thử.
     * @param id giá trị id được truyền vào
     * @return kết quả find by id sau khi xử lý
     */
    public static Optional<UserProfileCase> findById(String id) {
        return CASES.stream()
                .filter(testCase -> testCase.id().equalsIgnoreCase(id))
                .findFirst();
    }

    /**
     * Thực hiện xử lý contains id trong luồng kiểm thử.
     * @param id giá trị id được truyền vào
     * @return kết quả contains id sau khi xử lý
     */
    public static boolean containsId(String id) {
        return findById(id).isPresent();
    }

    /**
     * Thực hiện xử lý view case trong luồng kiểm thử.
     * @param id giá trị id được truyền vào
     * @param scenario giá trị scenario được truyền vào
     * @return kết quả view case sau khi xử lý
     */
    private static UserProfileCase viewCase(String id, String scenario) {
        return new UserProfileCase(
                id,
                scenario,
                UserProfileFeature.USER_DETAIL,
                UserProfileDecision.VIEW,
                UserProfileRejectReasonMode.NONE);
    }

    private static UserProfileCase approveCase(
            String id,
            String scenario,
            UserProfileFeature feature) {
        return new UserProfileCase(
                id,
                scenario,
                feature,
                UserProfileDecision.APPROVE,
                UserProfileRejectReasonMode.NONE);
    }

    private static UserProfileCase rejectCase(
            String id,
            String scenario,
            UserProfileFeature feature,
            UserProfileRejectReasonMode rejectReasonMode) {
        return new UserProfileCase(
                id,
                scenario,
                feature,
                UserProfileDecision.REJECT,
                rejectReasonMode);
    }
}
