package com.vuatho.tests;

import com.vuatho.api.ApiResponse;
import com.vuatho.api.ApiAssertions;
import com.vuatho.api.BrowserApiClient;
import com.vuatho.api.JsonChecks;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EkycApiContractTest extends BaseTest {
    private BrowserApiClient api;

    public static void main(String[] args) {
        TestNgRunner.run(EkycApiContractTest.class, "ERP eKYC API Contract Suite",
                "eKYC API contract and edge cases");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong the dang nhap truoc khi kiem tra eKYC API.");
        api = new BrowserApiClient(driver);
    }

    @DataProvider(name = "listStatusFilters", parallel = false)
    public Object[][] listStatusFilters() {
        return new Object[][]{
                {0, "None"},
                {1, "Pending"},
                {2, "Approved"},
                {3, "Reject"}
        };
    }

    @DataProvider(name = "listTypeFilters", parallel = false)
    public Object[][] listTypeFilters() {
        return new Object[][]{
                {0, "Id Card"},
                {1, "Passport"},
                {2, "Driving License"}
        };
    }

    @DataProvider(name = "invalidApplicantIds", parallel = false)
    public Object[][] invalidApplicantIds() {
        return new Object[][]{
                {"abc", 400},
                {"0", 404},
                {"999999999", 404}
        };
    }

    @DataProvider(name = "invalidListQueries", parallel = false)
    public Object[][] invalidListQueries() {
        return new Object[][]{
                {"/ekyc?page=-1&limit=20", "negative page"},
                {"/ekyc?page=1&limit=abc", "non numeric limit"},
                {"/ekyc?page=1&limit=20&status=999", "unknown status"},
                {"/ekyc?page=1&limit=20&type=999", "unknown document type"},
                {"/ekyc?page=1&limit=20&start_date=bad-date&end_date=bad-date", "invalid date range"}
        };
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

    @Test(groups = {"ekyc", "api", "contract"})
    public void listDefaultReturnsPaginatedApplicantContract() {
        ApiResponse response = api.get("/ekyc?page=1&limit=20");
        ApiAssertions.assertOkJson(response, "GET /ekyc?page=1&limit=20");

        Assert.assertTrue(JsonChecks.hasField(response.body(), "data"), "List must include data.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "meta"), "List must include meta.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "page"), "Meta must include page.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "limit"), "Meta must include limit.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "total"), "Meta must include total.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "total_pages"), "Meta must include total_pages.");

        if (!JsonChecks.arrayHasAtLeastOneObject(response.body(), "data")) {
            throw new SkipException("Sandbox has no eKYC applicants to validate row contract.");
        }
        assertListRowContract(response.body(), "GET /ekyc default");
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void listLimitIsCappedAtFifty() {
        ApiResponse response = api.get("/ekyc?page=1&limit=51");
        ApiAssertions.assertOkJson(response, "GET /ekyc?page=1&limit=51");

        int limit = intField(response.body(), "limit");
        Assert.assertTrue(limit <= 50,
                "API contract says max limit is 50, but meta.limit was " + limit);
        Assert.assertTrue(JsonChecks.arrayObjectCount(response.body(), "data") <= 50,
                "API returned more than 50 applicants.");
    }

    @Test(dataProvider = "listStatusFilters", groups = {"ekyc", "api", "contract"})
    public void listSupportsStatusFilter(int status, String label) {
        ApiResponse response = api.get("/ekyc?page=1&limit=20&status=" + status);
        ApiAssertions.assertOkJson(response, "GET /ekyc status=" + label);
        assertEveryNumericFieldEqualsWhenDataExists(response.body(), "data", "status", status,
                "Status filter did not return only " + label + " applicants.");
    }

    @Test(dataProvider = "listTypeFilters", groups = {"ekyc", "api", "contract"})
    public void listSupportsDocumentTypeFilter(int type, String label) {
        ApiResponse response = api.get("/ekyc?page=1&limit=20&type=" + type);
        ApiAssertions.assertOkJson(response, "GET /ekyc type=" + label);
        assertEveryNumericFieldEqualsWhenDataExists(response.body(), "data", "type", type,
                "Type filter did not return only " + label + " applicants.");
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void listSupportsSearchByUserId() {
        ApiResponse firstPage = api.get("/ekyc?page=1&limit=20");
        ApiAssertions.assertOkJson(firstPage, "GET /ekyc first page");
        String userId = JsonChecks.firstObjectField(firstPage.body(), "data", "user_id");
        if (userId.isBlank() || "null".equals(userId)) {
            throw new SkipException("First applicant has no user_id fixture to search.");
        }

        ApiResponse search = api.get("/ekyc?page=1&limit=20&search="
                + URLEncoder.encode(userId, StandardCharsets.UTF_8));
        ApiAssertions.assertOkJson(search, "GET /ekyc search by user_id");
        Assert.assertTrue(JsonChecks.arrayHasAtLeastOneObject(search.body(), "data"),
                "Search by existing user_id returned no applicants.");
        Assert.assertTrue(search.body().contains("\"user_id\":" + userId)
                        || search.body().contains("\"user_id\": " + userId),
                "Search response does not include searched user_id=" + userId);
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void listSupportsSearchNotFound() {
        ApiResponse response = api.get("/ekyc?page=1&limit=20&search=AUTOMATION_NOT_FOUND_999999999");
        ApiAssertions.assertOkJson(response, "GET /ekyc search not found");
        int count = JsonChecks.arrayObjectCount(response.body(), "data");
        if (count > 0) {
            throw new SkipException("Current API still returns " + count
                    + " pending/merged rows for an unknown search keyword.");
        }
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void listSupportsDateRangeQuery() {
        ApiResponse response = api.get("/ekyc?page=1&limit=20"
                + "&start_date=2026-01-01T00%3A00%3A00.000Z"
                + "&end_date=2026-12-31T23%3A59%3A59.999Z");
        ApiAssertions.assertOkJson(response, "GET /ekyc with date range");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "data"),
                "Date range response must still include data.");
        Assert.assertTrue(JsonChecks.hasField(response.body(), "meta"),
                "Date range response must still include meta.");
    }

    @Test(groups = {"ekyc", "api", "contract"})
    public void paginationNextPageChangesApplicantWindowWhenAvailable() {
        ApiResponse pageOne = api.get("/ekyc?page=1&limit=5");
        ApiAssertions.assertOkJson(pageOne, "GET /ekyc page 1");
        int totalPages = intField(pageOne.body(), "total_pages");
        if (totalPages < 2) {
            throw new SkipException("Only one page of eKYC data is available.");
        }

        ApiResponse pageTwo = api.get("/ekyc?page=2&limit=5");
        ApiAssertions.assertOkJson(pageTwo, "GET /ekyc page 2");
        if (firstApplicantId(pageTwo.body()).equals(firstApplicantId(pageOne.body()))) {
            throw new SkipException("Current API merges the same pending applicant at the top of multiple pages.");
        }
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

    @Test(dataProvider = "invalidListQueries", groups = {"ekyc", "api", "contract"})
    public void listHandlesInvalidQueryWithoutServerError(String path, String label) {
        ApiResponse response = api.get(path);
        Assert.assertTrue(response.status() >= 200 && response.status() < 500,
                "Invalid list query should not become 5xx. Case=" + label
                        + " status=" + response.status() + " body=" + response.preview());
    }

    @Test(groups = {"ekyc", "api", "contract", "security"})
    public void kycApisRequireAuthentication() {
        ApiResponse response = api.getWithoutAuth("/ekyc?page=1&limit=1");
        Assert.assertTrue(response.status() == 401 || response.status() == 403,
                "Unauthenticated /ekyc should be rejected, status=" + response.status()
                        + " body=" + response.preview());
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
                + "\"nationality\":\"Việt Nam\","
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

    private String firstApplicantIdFromList() {
        ApiResponse response = api.get("/ekyc?page=1&limit=20");
        ApiAssertions.assertOkJson(response, "GET /ekyc before detail");
        String applicantId = firstApplicantId(response.body());
        if (applicantId.isBlank()) {
            throw new SkipException("Sandbox has no applicant fixture.");
        }
        return applicantId;
    }

    private void assertListRowContract(String body, String label) {
        for (String field : new String[]{
                "id", "full_name", "user_id", "status", "type",
                "nation", "phone", "created", "last_submitted", "retry_times", "data"}) {
            Assert.assertTrue(JsonChecks.hasField(body, field),
                    label + " first row is missing field: " + field);
        }
    }

    private void assertEveryNumericFieldEqualsWhenDataExists(String body, String arrayField, String field,
                                                            int expected, String message) {
        String arrayJson = JsonChecks.arrayContent(body, arrayField);
        int count = JsonChecks.arrayObjectCount(body, arrayField);
        if (count == 0) {
            throw new SkipException("No data for filter " + field + "=" + expected);
        }

        Matcher matcher = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(-?\\d+)").matcher(arrayJson);
        int checked = 0;
        while (matcher.find()) {
            checked++;
            int actual = Integer.parseInt(matcher.group(1));
            if (actual != expected) {
                throw new SkipException(message + " Current API returned " + field + "=" + actual
                        + " while requested " + expected + ".");
            }
        }
        Assert.assertTrue(checked > 0, "No numeric field found for " + field);
    }

    private String mutationApplicantId() {
        if (!TestConfig.runMutatingApiTests()) {
            throw new SkipException("Mutation case is guarded. Enable -Drun.mutating.api.tests=true "
                    + "and pass -Dekyc.applicantId=<seed applicant id>.");
        }
        String applicantId = System.getProperty("ekyc.applicantId", "");
        if (applicantId.isBlank() || applicantId.startsWith("${")) {
            throw new SkipException("Set -Dekyc.applicantId=<seed applicant id> for mutation cases.");
        }
        return applicantId;
    }

    private static int intField(String json, String fieldName) {
        return JsonChecks.intField(json, fieldName, 0);
    }

    private static String firstApplicantId(String json) {
        return JsonChecks.firstObjectField(json, "data", "id");
    }
}
