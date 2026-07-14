package com.vuatho.tests;

import com.vuatho.testdata.UserManagementCase;
import com.vuatho.testdata.UserManagementDecision;
import com.vuatho.testdata.UserManagementFeature;
import com.vuatho.testdata.UserManagementRejectReasonMode;
import com.vuatho.testdata.UserManagementTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class UserManagementTestDataTest {

    @Test
    public void userManagementTestDataHasUniqueIdsAndRequiredCoverage() {
        var cases = UserManagementTestData.cases();
        Set<String> ids = cases.stream()
                .map(UserManagementCase::id)
                .collect(Collectors.toSet());
        Set<String> signatures = cases.stream()
                .map(testCase -> testCase.feature()
                        + "|" + testCase.decision()
                        + "|" + testCase.rejectReasonMode())
                .collect(Collectors.toSet());

        Assert.assertEquals(ids.size(), cases.size(), "User management testcase IDs must be unique.");
        Assert.assertEquals(signatures.size(), cases.size(),
                "User management testcase actions must not be duplicated.");
        Assert.assertTrue(cases.stream().anyMatch(UserManagementCase::viewsInformation),
                "User management testdata must include view information coverage.");

        assertModerationCoverage(cases, UserManagementFeature.NAME_UPDATE);
        assertModerationCoverage(cases, UserManagementFeature.AVATAR_UPDATE);
    }

    private void assertModerationCoverage(
            java.util.List<UserManagementCase> cases,
            UserManagementFeature feature) {
        Assert.assertTrue(cases.stream().anyMatch(testCase ->
                        testCase.feature() == feature
                                && testCase.decision() == UserManagementDecision.APPROVE
                                && testCase.rejectReasonMode() == UserManagementRejectReasonMode.NONE),
                "Missing approve coverage for feature: " + feature);
        Assert.assertTrue(cases.stream().anyMatch(testCase ->
                        testCase.feature() == feature
                                && testCase.decision() == UserManagementDecision.REJECT
                                && testCase.rejectReasonMode() == UserManagementRejectReasonMode.DEFAULT),
                "Missing default reject coverage for feature: " + feature);
        Assert.assertTrue(cases.stream().anyMatch(testCase ->
                        testCase.feature() == feature
                                && testCase.decision() == UserManagementDecision.REJECT
                                && testCase.rejectReasonMode() == UserManagementRejectReasonMode.OTHER),
                "Missing other-reason reject coverage for feature: " + feature);
    }
}
