package com.vuatho.testdata;

public record UserProfileCase(
        String id,
        String scenario,
        UserProfileFeature feature,
        UserProfileDecision decision,
        UserProfileRejectReasonMode rejectReasonMode) {

    public boolean viewsInformation() {
        return feature == UserProfileFeature.USER_DETAIL
                && decision == UserProfileDecision.VIEW;
    }

    public boolean approvesUpdate() {
        return decision == UserProfileDecision.APPROVE;
    }

    public boolean rejectsUpdate() {
        return decision == UserProfileDecision.REJECT;
    }

    public boolean usesDefaultRejectReason() {
        return rejectReasonMode == UserProfileRejectReasonMode.DEFAULT;
    }

    public boolean usesOtherRejectReason() {
        return rejectReasonMode == UserProfileRejectReasonMode.OTHER;
    }

    @Override
    public String toString() {
        return id + " - " + scenario;
    }
}
