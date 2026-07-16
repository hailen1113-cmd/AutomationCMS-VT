package com.vuatho.testdata;

import java.util.List;
import java.util.Optional;

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

    private UserProfileTestData() {
    }

    public static List<UserProfileCase> cases() {
        return CASES;
    }

    public static Optional<UserProfileCase> findById(String id) {
        return CASES.stream()
                .filter(testCase -> testCase.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public static boolean containsId(String id) {
        return findById(id).isPresent();
    }

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
