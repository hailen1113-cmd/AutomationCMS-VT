package com.vuatho.testdata;

/**
 * Mô tả một test case hồ sơ người dùng gồm tính năng, dữ liệu đầu vào và kỳ vọng.
 */
public record UserProfileCase(
        String id,
        String scenario,
        UserProfileFeature feature,
        UserProfileDecision decision,
        UserProfileRejectReasonMode rejectReasonMode) {

    /**
     * Thực hiện xử lý views information trong luồng kiểm thử.
     * @return kết quả views information sau khi xử lý
     */
    public boolean viewsInformation() {
        return feature == UserProfileFeature.USER_DETAIL
                && decision == UserProfileDecision.VIEW;
    }

    /**
     * Thực hiện xử lý approves update trong luồng kiểm thử.
     * @return kết quả approves update sau khi xử lý
     */
    public boolean approvesUpdate() {
        return decision == UserProfileDecision.APPROVE;
    }

    /**
     * Thực hiện xử lý rejects update trong luồng kiểm thử.
     * @return kết quả rejects update sau khi xử lý
     */
    public boolean rejectsUpdate() {
        return decision == UserProfileDecision.REJECT;
    }

    /**
     * Thực hiện xử lý uses default reject reason trong luồng kiểm thử.
     * @return kết quả uses default reject reason sau khi xử lý
     */
    public boolean usesDefaultRejectReason() {
        return rejectReasonMode == UserProfileRejectReasonMode.DEFAULT;
    }

    /**
     * Thực hiện xử lý uses other reject reason trong luồng kiểm thử.
     * @return kết quả uses other reject reason sau khi xử lý
     */
    public boolean usesOtherRejectReason() {
        return rejectReasonMode == UserProfileRejectReasonMode.OTHER;
    }

    /**
     * Thực hiện xử lý to string trong luồng kiểm thử.
     * @return kết quả to string sau khi xử lý
     */
    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
