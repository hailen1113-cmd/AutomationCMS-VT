package com.vuatho.tests;

import com.vuatho.testdata.EkycWorkbookCatalog;
import com.vuatho.testdata.EkycWorkbookCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EkycWorkbookCatalogTest {
    @Test(description = "EKYC-CATALOG-001: Senior eKYC workbook has 574 unique testcases")
    public void seniorWorkbookHasExpectedCaseCountAndUniqueIds() {
        List<EkycWorkbookCase> cases = EkycWorkbookCatalog.load();
        Set<String> ids = cases.stream()
                .map(EkycWorkbookCase::id)
                .collect(Collectors.toSet());

        Assert.assertEquals(cases.size(), 574,
                "Senior eKYC workbook testcase count changed.");
        Assert.assertEquals(ids.size(), cases.size(),
                "Senior eKYC workbook contains duplicate testcase IDs.");
    }
}
