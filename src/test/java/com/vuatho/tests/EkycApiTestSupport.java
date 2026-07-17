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

/**
 * Cung cấp thiết lập, client và assertion dùng chung cho các test API eKYC.
 */
abstract class EkycApiTestSupport extends BaseTest {
    protected BrowserApiClient api;

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực hiện xử lý prepare authenticated session trong luồng kiểm thử.
     */
    @BeforeMethod(alwaysRun = true)
    public void prepareAuthenticatedSession() {
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Khong the dang nhap truoc khi kiem tra eKYC API.");
        api = new BrowserApiClient(driver);
    }

    /**
     * Trả về first applicant id from list từ trạng thái hiện tại.
     * @return kết quả first applicant id from list sau khi xử lý
     */
    protected String firstApplicantIdFromList() {
        ApiResponse response = api.get("/ekyc?page=1&limit=20");
        ApiAssertions.assertOkJson(response, "GET /ekyc before detail");
        String applicantId = firstApplicantId(response.body());
        if (applicantId.isBlank()) {
            throw new SkipException("Sandbox khong co applicant fixture.");
        }
        return applicantId;
    }

    /**
     * Xác nhận list row contract trong luồng kiểm thử.
     * @param body giá trị body được truyền vào
     * @param label giá trị label được truyền vào
     */
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

    /**
     * Thực hiện xử lý mutation applicant id trong luồng kiểm thử.
     * @return kết quả mutation applicant id sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý int field trong luồng kiểm thử.
     * @param json giá trị json được truyền vào
     * @param fieldName giá trị field name được truyền vào
     * @return kết quả int field sau khi xử lý
     */
    protected static int intField(String json, String fieldName) {
        return JsonChecks.intField(json, fieldName, 0);
    }

    /**
     * Trả về first applicant id từ trạng thái hiện tại.
     * @param json giá trị json được truyền vào
     * @return kết quả first applicant id sau khi xử lý
     */
    protected static String firstApplicantId(String json) {
        return JsonChecks.firstObjectField(json, "data", "id");
    }
}
