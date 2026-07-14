package com.vuatho.testdata;

import java.util.List;
import java.util.Optional;

public final class UserManagementTestData {
    private static final List<UserManagementCase> CASES = List.of(
            viewCase(
                    "USER-001",
                    "Xem thong tin nguoi dung dau tien trong danh sach"),
            approveCase(
                    "USER-NAME-APPROVE-001",
                    "Chap nhan yeu cau cap nhat ho ten",
                    UserManagementFeature.NAME_UPDATE),
            rejectCase(
                    "USER-NAME-REJECT-001",
                    "Tu choi yeu cau cap nhat ho ten voi ly do co san",
                    UserManagementFeature.NAME_UPDATE,
                    UserManagementRejectReasonMode.DEFAULT),
            rejectCase(
                    "USER-NAME-REJECT-OTHER-001",
                    "Tu choi yeu cau cap nhat ho ten voi ly do khac",
                    UserManagementFeature.NAME_UPDATE,
                    UserManagementRejectReasonMode.OTHER),
            approveCase(
                    "USER-AVATAR-APPROVE-001",
                    "Chap nhan yeu cau cap nhat anh dai dien",
                    UserManagementFeature.AVATAR_UPDATE),
            rejectCase(
                    "USER-AVATAR-REJECT-001",
                    "Tu choi yeu cau cap nhat anh dai dien voi ly do co san",
                    UserManagementFeature.AVATAR_UPDATE,
                    UserManagementRejectReasonMode.DEFAULT),
            rejectCase(
                    "USER-AVATAR-REJECT-OTHER-001",
                    "Tu choi yeu cau cap nhat anh dai dien voi ly do khac",
                    UserManagementFeature.AVATAR_UPDATE,
                    UserManagementRejectReasonMode.OTHER));

    private UserManagementTestData() {
    }

    public static List<UserManagementCase> cases() {
        return CASES;
    }

    public static Optional<UserManagementCase> findById(String id) {
        return CASES.stream()
                .filter(testCase -> testCase.id().equalsIgnoreCase(id))
                .findFirst();
    }

    public static boolean containsId(String id) {
        return findById(id).isPresent();
    }

    private static UserManagementCase viewCase(String id, String scenario) {
        return new UserManagementCase(
                id,
                scenario,
                UserManagementFeature.USER_DETAIL,
                UserManagementDecision.VIEW,
                UserManagementRejectReasonMode.NONE);
    }

    private static UserManagementCase approveCase(
            String id,
            String scenario,
            UserManagementFeature feature) {
        return new UserManagementCase(
                id,
                scenario,
                feature,
                UserManagementDecision.APPROVE,
                UserManagementRejectReasonMode.NONE);
    }

    private static UserManagementCase rejectCase(
            String id,
            String scenario,
            UserManagementFeature feature,
            UserManagementRejectReasonMode rejectReasonMode) {
        return new UserManagementCase(
                id,
                scenario,
                feature,
                UserManagementDecision.REJECT,
                rejectReasonMode);
    }
}
