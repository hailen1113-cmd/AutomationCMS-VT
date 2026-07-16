package com.vuatho.tests;

import com.vuatho.testdata.PartnerWorkerCase;
import com.vuatho.testdata.PartnerWorkerTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

public class WorkerMenuTestDataTest {

    @Test
    public void workerMenuTestDataHasUniqueIdsAndWorkerProfileCoverage() {
        var cases = PartnerWorkerTestData.cases();
        Set<String> ids = cases.stream()
                .map(PartnerWorkerCase::id)
                .collect(Collectors.toSet());

        Assert.assertEquals(ids.size(), cases.size(),
                "Partner-worker testcase IDs must be unique.");
        Assert.assertTrue(cases.stream()
                        .anyMatch(testCase -> testCase.page().equals(PartnerWorkerTestData.WORKER_PROFILE)),
                "Partner-worker testdata must include worker profile management coverage.");
    }
}
