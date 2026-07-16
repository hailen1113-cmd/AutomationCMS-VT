package com.vuatho.tests;

import com.vuatho.testdata.UserProfileCase;
import com.vuatho.testdata.UserProfileDecision;
import com.vuatho.testdata.UserProfileFeature;
import com.vuatho.testdata.UserProfileRejectReasonMode;
import com.vuatho.testdata.UserProfileTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class UserProfileTestDataTest {

    @Test
    public void userProfileTestDataHasUniqueIdsAndRequiredCoverage() {
        var cases = UserProfileTestData.cases();
        Set<String> ids = cases.stream()
                .map(UserProfileCase::id)
                .collect(Collectors.toSet());
        Set<String> signatures = cases.stream()
                .map(testCase -> testCase.feature()
                        + "|" + testCase.decision()
                        + "|" + testCase.rejectReasonMode())
                .collect(Collectors.toSet());

        Assert.assertEquals(ids.size(), cases.size(), "User profile testcase IDs must be unique.");
        Assert.assertEquals(signatures.size(), cases.size(),
                "User profile testcase actions must not be duplicated.");
        Assert.assertTrue(cases.stream().anyMatch(UserProfileCase::viewsInformation),
                "User profile testdata must include view information coverage.");

        assertModerationCoverage(cases, UserProfileFeature.NAME_UPDATE);
        assertModerationCoverage(cases, UserProfileFeature.AVATAR_UPDATE);
    }

    private void assertModerationCoverage(
            java.util.List<UserProfileCase> cases,
            UserProfileFeature feature) {
        Assert.assertTrue(cases.stream().anyMatch(testCase ->
                        testCase.feature() == feature
                                && testCase.decision() == UserProfileDecision.APPROVE
                                && testCase.rejectReasonMode() == UserProfileRejectReasonMode.NONE),
                "Missing approve coverage for feature: " + feature);
        Assert.assertTrue(cases.stream().anyMatch(testCase ->
                        testCase.feature() == feature
                                && testCase.decision() == UserProfileDecision.REJECT
                                && testCase.rejectReasonMode() == UserProfileRejectReasonMode.DEFAULT),
                "Missing default reject coverage for feature: " + feature);
        Assert.assertTrue(cases.stream().anyMatch(testCase ->
                        testCase.feature() == feature
                                && testCase.decision() == UserProfileDecision.REJECT
                                && testCase.rejectReasonMode() == UserProfileRejectReasonMode.OTHER),
                "Missing other-reason reject coverage for feature: " + feature);
    }
}
