package com.vuatho.tests;

import com.vuatho.testdata.EkycInformationAction;
import com.vuatho.testdata.EkycInformationCase;
import com.vuatho.testdata.EkycInformationDataState;
import com.vuatho.testdata.EkycInformationField;
import com.vuatho.testdata.EkycInformationTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EkycInformationTestDataTest {

    @Test
    public void informationTestDataHasUniqueIdsAndRequiredCoverage() {
        var cases = EkycInformationTestData.cases();
        Set<String> ids = cases.stream()
                .map(EkycInformationCase::id)
                .collect(Collectors.toSet());

        Assert.assertEquals(ids.size(), cases.size(), "eKYC information testcase IDs must be unique.");
        Set<String> signatures = cases.stream()
                .map(testCase -> testCase.action()
                        + "|" + testCase.dataState()
                        + "|" + testCase.fields()
                        + "|" + testCase.saveChanges()
                        + "|" + testCase.refillAfterClear())
                .collect(Collectors.toSet());
        Assert.assertEquals(signatures.size(), cases.size(),
                "eKYC information testcase actions must not be duplicated.");
        Assert.assertTrue(cases.stream().anyMatch(testCase -> testCase.action() == EkycInformationAction.EDIT),
                "eKYC information testdata must include edit cases.");
        Assert.assertTrue(cases.stream().anyMatch(testCase -> testCase.action() == EkycInformationAction.CLEAR),
                "eKYC information testdata must include clear cases.");
        Assert.assertTrue(cases.stream().anyMatch(testCase -> testCase.dataState() == EkycInformationDataState.HAS_DATA),
                "eKYC information testdata must include existing-data cases.");
        Assert.assertTrue(cases.stream().anyMatch(testCase -> testCase.dataState() == EkycInformationDataState.NO_DATA),
                "eKYC information testdata must include no-data cases.");
        Assert.assertTrue(cases.stream()
                        .filter(testCase -> testCase.fields().containsAll(EnumSet.allOf(EkycInformationField.class)))
                        .count() >= 2,
                "eKYC information testdata must include full-form edit/clear coverage.");
        Assert.assertTrue(cases.stream().anyMatch(EkycInformationCase::cancelsChanges),
                "eKYC information testdata must include cancel cases.");
        Assert.assertTrue(cases.stream().anyMatch(testCase -> !testCase.cancelsChanges()),
                "eKYC information testdata must include save cases.");
        Assert.assertTrue(cases.stream().anyMatch(EkycInformationCase::clearsMultipleFields),
                "eKYC information testdata must include multi-field clear coverage.");
        Assert.assertTrue(cases.stream().anyMatch(EkycInformationCase::refillsAfterClear),
                "eKYC information testdata must include clear-then-refill coverage.");

        for (EkycInformationField field : EkycInformationField.values()) {
            Assert.assertTrue(cases.stream().anyMatch(testCase ->
                            testCase.action() == EkycInformationAction.EDIT
                                    && !testCase.cancelsChanges()
                                    && testCase.fields().contains(field)),
                    "Missing saved edit coverage for field: " + field);
            Assert.assertTrue(cases.stream().anyMatch(testCase ->
                            testCase.action() == EkycInformationAction.CLEAR
                                    && !testCase.cancelsChanges()
                                    && testCase.fields().contains(field)),
                    "Missing saved clear coverage for field: " + field);
        }
    }
}
