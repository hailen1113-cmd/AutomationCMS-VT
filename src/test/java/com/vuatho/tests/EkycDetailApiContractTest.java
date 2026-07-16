package com.vuatho.tests;

import com.vuatho.api.ApiAssertions;
import com.vuatho.api.ApiResponse;
import com.vuatho.api.JsonChecks;
import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EkycDetailApiContractTest extends EkycApiTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycDetailApiContractTest.class,
                "Bo test API chi tiet eKYC ERP",
                "Kiem tra detail eKYC API");
    }

    @DataProvider(name = "invalidApplicantIds", parallel = false)
    public Object[][] invalidApplicantIds() {
        return new Object[][]{
                {"abc", 400},
                {"0", 404},
                {"999999999", 404}
        };
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void detailReturnsApplicantPersonalInfoImagesAndDecisionState() {
        String applicantId = firstApplicantIdFromList();
        ApiResponse response = api.get("/ekyc/" + applicantId);
        ApiAssertions.assertOkJson(response, "GET /ekyc/" + applicantId);

        Assert.assertTrue(JsonChecks.hasField(response.body(), "applicant"), "Detail must include applicant.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "personal_info"), "Detail must include personal_info.");
        for (String field : new String[]{
                "id", "user_id", "status", "type", "full_name", "gender", "birthDate",
                "data", "note", "retry_times", "result", "front", "back", "selfie", "nation"}) {
            Assert.assertTrue(JsonChecks.hasField(response.body(), field),
                    "Detail response should expose field: " + field);
        }
        Assert.assertTrue(JsonChecks.hasField(response.body(), "phone"),
                "Detail personal_info should expose phone.");
    }

    @Test(dataProvider = "invalidApplicantIds", groups = {"ekyc", "api", "contract"})
    public void detailRejectsInvalidOrUnknownApplicantId(String applicantId, int expectedStatus) {
        ApiResponse response = api.get("/ekyc/" + applicantId);
        Assert.assertEquals(response.status(), expectedStatus,
                "Unexpected detail error for applicant id=" + applicantId + ". Body: " + response.preview());
    }
}
