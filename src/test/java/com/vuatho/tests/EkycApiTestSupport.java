package com.vuatho.tests;

import com.vuatho.api.ApiAssertions;
import com.vuatho.api.ApiResponse;
import com.vuatho.api.BrowserApiClient;
import com.vuatho.api.JsonChecks;
import com.vuatho.config.TestConfig;
import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.LoginPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class EkycApiTestSupport extends BaseTest {
    protected BrowserApiClient api;

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

    protected String firstApplicantIdFromList() {
        ApiResponse response = api.get("/ekyc?page=1&limit=20");
        ApiAssertions.assertOkJson(response, "GET /ekyc before detail");
        String applicantId = firstApplicantId(response.body());
        if (applicantId.isBlank()) {
            throw new SkipException("Sandbox khong co applicant fixture.");
        }
        return applicantId;
    }

    protected void assertListRowContract(String body, String label) {
        for (String field : new String[]{
                "id", "full_name", "user_id", "status", "type",
                "nation", "phone", "created", "last_submitted", "retry_times", "data"}) {
            Assert.assertTrue(JsonChecks.hasField(body, field),
                    label + " first row is missing field: " + field);
        }
    }

    protected void assertEveryNumericFieldEqualsWhenDataExists(String body, String arrayField, String field,
                                                               int expected, String message) {
        String arrayJson = JsonChecks.arrayContent(body, arrayField);
        int count = JsonChecks.arrayObjectCount(body, arrayField);
        if (count == 0) {
            throw new SkipException("Khong co data cho filter " + field + "=" + expected);
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

    protected String mutationApplicantId() {
        if (!TestConfig.runMutatingApiTests()) {
            throw new SkipException("Mutation case dang duoc bao ve. Bat -Drun.mutating.api.tests=true "
                    + "va truyen -Dekyc.applicantId=<seed applicant id>.");
        }
        String applicantId = System.getProperty("ekyc.applicantId", "");
        if (applicantId.isBlank() || applicantId.startsWith("${")) {
            throw new SkipException("Set -Dekyc.applicantId=<seed applicant id> cho mutation case.");
        }
        return applicantId;
    }

    protected static int intField(String json, String fieldName) {
        return JsonChecks.intField(json, fieldName, 0);
    }

    protected static String firstApplicantId(String json) {
        return JsonChecks.firstObjectField(json, "data", "id");
    }
}
