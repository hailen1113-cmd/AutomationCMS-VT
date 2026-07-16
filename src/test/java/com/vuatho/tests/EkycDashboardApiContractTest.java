package com.vuatho.tests;

import com.vuatho.api.ApiAssertions;
import com.vuatho.api.ApiResponse;
import com.vuatho.api.JsonChecks;
import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EkycDashboardApiContractTest extends EkycApiTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycDashboardApiContractTest.class,
                "Bo test API thong ke eKYC ERP",
                "Kiem tra contract API dashboard/kyc");
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void dashboardKycReturnsStatisticContract() {
        ApiResponse response = api.get("/dashboard/kyc");
        ApiAssertions.assertOkJson(response, "GET /dashboard/kyc");

        for (String field : new String[]{
                "total", "total_day", "total_pending", "total_approved", "total_rejected",
                "total_pending_day", "total_approved_day", "total_rejected_day", "statistic_total_week"}) {
            Assert.assertTrue(JsonChecks.hasField(response.body(), field),
                    "Missing dashboard field: " + field + ". Body: " + response.preview());
        }

        Assert.assertTrue(intField(response.body(), "total") >= 0, "total must be non-negative.");
        Assert.assertTrue(intField(response.body(), "total_pending") >= 0, "total_pending must be non-negative.");
        Assert.assertTrue(intField(response.body(), "total_approved") >= 0, "total_approved must be non-negative.");
        Assert.assertTrue(intField(response.body(), "total_rejected") >= 0, "total_rejected must be non-negative.");
        Assert.assertTrue(intField(response.body(), "total_day") <= intField(response.body(), "total"),
                "Today total must not exceed all-time total.");
        Assert.assertTrue(JsonChecks.arrayHasAtLeastOneObject(response.body(), "statistic_total_week"),
                "statistic_total_week must include chart points.");
    }
}
