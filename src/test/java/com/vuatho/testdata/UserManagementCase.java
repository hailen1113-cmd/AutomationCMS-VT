package com.vuatho.testdata;

public record UserManagementCase(
        String id,
        String scenario,
        UserManagementFeature feature,
        UserManagementDecision decision,
        UserManagementRejectReasonMode rejectReasonMode) {

    public boolean viewsInformation() {
        return feature == UserManagementFeature.USER_DETAIL
                && decision == UserManagementDecision.VIEW;
    }

    public boolean approvesUpdate() {
        return decision == UserManagementDecision.APPROVE;
    }

    public boolean rejectsUpdate() {
        return decision == UserManagementDecision.REJECT;
    }

    public boolean usesDefaultRejectReason() {
        return rejectReasonMode == UserManagementRejectReasonMode.DEFAULT;
    }

    public boolean usesOtherRejectReason() {
        return rejectReasonMode == UserManagementRejectReasonMode.OTHER;
    }
}
