package com.vuatho.tests;

import com.vuatho.api.ApiAssertions;
import com.vuatho.api.ApiResponse;
import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EkycMutationApiContractTest extends EkycApiTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycMutationApiContractTest.class,
                "Bo test API mutation eKYC ERP",
                "Kiem tra update/rerun eKYC API");
    }

    @Test(groups = {"ekyc", "api", "contract", "mutation"})
    public void updateRejectsInvalidReviewPayload() {
        String applicantId = mutationApplicantId();
        ApiResponse response = api.put("/ekyc/" + applicantId,
                "{\"result\":{\"front_status\":9,\"back_status\":2,\"selfie_status\":2},\"note\":{}}");
        Assert.assertEquals(response.status(), 400,
                "Invalid review result should be rejected. Body: " + response.preview());
    }

    @Test(groups = {"ekyc", "api", "contract", "mutation"})
    public void updateCanApproveAllDocumentSidesForSeedApplicant() {
        String applicantId = mutationApplicantId();
        ApiResponse response = api.put("/ekyc/" + applicantId,
                "{\"result\":{\"front_status\":2,\"back_status\":2,\"selfie_status\":2},\"note\":{}}");
        ApiAssertions.assertOkJson(response, "PUT /ekyc approve all sides");
    }

    @Test(groups = {"ekyc", "api", "contract", "mutation"})
    public void updateCanRejectWithDocumentReasonForSeedApplicant() {
        String applicantId = mutationApplicantId();
        ApiResponse response = api.put("/ekyc/" + applicantId,
                "{\"result\":{\"front_status\":3,\"back_status\":2,\"selfie_status\":2},"
                        + "\"note\":{\"document\":\"BLURRED_IMAGE\",\"other\":\"automation reject check\"}}");
        ApiAssertions.assertOkJson(response, "PUT /ekyc reject document");
    }

    @Test(groups = {"ekyc", "api", "contract", "mutation"})
    public void updateInfoCanPatchSevenEditableFieldsForSeedApplicant() {
        String applicantId = mutationApplicantId();
        String body = "{"
                + "\"type\":\"UPDATE_INFO\","
                + "\"full_name\":\"Automation KYC Fixture\","
                + "\"gender\":1,"
                + "\"birthDate\":\"1990-01-01\","
                + "\"nationality\":\"Viá»‡t Nam\","
                + "\"number_card\":\"000000000001\","
                + "\"place_of_origin\":\"Automation Origin\","
                + "\"place_of_residence\":\"Automation Residence\""
                + "}";
        ApiResponse response = api.put("/ekyc/" + applicantId, body);
        ApiAssertions.assertOkJson(response, "PUT /ekyc UPDATE_INFO");
    }

    @Test(groups = {"ekyc", "api", "contract", "mutation"})
    public void rerunAiDispatchesOrReturnsBusinessValidationForSeedApplicant() {
        String applicantId = mutationApplicantId();
        ApiResponse response = api.post("/ekyc/" + applicantId + "/rerun-ai", null);
        Assert.assertTrue(response.status() == 200 || response.status() == 400,
                "Rerun AI should dispatch or return business validation. Status="
                        + response.status() + " body=" + response.preview());
    }
}
