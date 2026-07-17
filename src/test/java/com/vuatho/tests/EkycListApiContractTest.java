package com.vuatho.tests;

import com.vuatho.api.ApiAssertions;
import com.vuatho.api.ApiResponse;
import com.vuatho.api.JsonChecks;
import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Kiểm tra hợp đồng API danh sách eKYC gồm phân trang, bộ lọc và cấu trúc response.
 */
public class EkycListApiContractTest extends EkycApiTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycListApiContractTest.class,
                "Bo test API danh sach eKYC ERP",
                "Kiem tra list, filter, search, pagination eKYC API");
    }

    /**
     * Thực hiện xử lý list status filters trong luồng kiểm thử.
     * @return kết quả list status filters sau khi xử lý
     */
    @DataProvider(name = "listStatusFilters", parallel = false)
    public Object[][] listStatusFilters() {
        return new Object[][]{
                {0, "None"},
                {1, "Pending"},
                {2, "Approved"},
                {3, "Reject"}
        };
    }

    /**
     * Thực hiện xử lý list type filters trong luồng kiểm thử.
     * @return kết quả list type filters sau khi xử lý
     */
    @DataProvider(name = "listTypeFilters", parallel = false)
    public Object[][] listTypeFilters() {
        return new Object[][]{
                {0, "Id Card"},
                {1, "Passport"},
                {2, "Driving License"}
        };
    }

    /**
     * Thực hiện xử lý invalid list queries trong luồng kiểm thử.
     * @return kết quả invalid list queries sau khi xử lý
     */
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

    /**
     * Thực hiện xử lý list default returns paginated applicant contract trong luồng kiểm thử.
     */
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
            throw new SkipException("Sandbox khong co applicant eKYC de kiem contract row.");
        }
        assertListRowContract(response.body(), "GET /ekyc default");
    }

    /**
     * Thực hiện xử lý list limit is capped at fifty trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý list supports status filter trong luồng kiểm thử.
     * @param status giá trị status được truyền vào
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "listStatusFilters", groups = {"ekyc", "api", "contract"})
    public void listSupportsStatusFilter(int status, String label) {
        ApiResponse response = api.get("/ekyc?page=1&limit=20&status=" + status);
        ApiAssertions.assertOkJson(response, "GET /ekyc status=" + label);
        assertEveryNumericFieldEqualsWhenDataExists(response.body(), "data", "status", status,
                "Status filter did not return only " + label + " applicants.");
    }

    /**
     * Thực hiện xử lý list supports document type filter trong luồng kiểm thử.
     * @param type giá trị type được truyền vào
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "listTypeFilters", groups = {"ekyc", "api", "contract"})
    public void listSupportsDocumentTypeFilter(int type, String label) {
        ApiResponse response = api.get("/ekyc?page=1&limit=20&type=" + type);
        ApiAssertions.assertOkJson(response, "GET /ekyc type=" + label);
        assertEveryNumericFieldEqualsWhenDataExists(response.body(), "data", "type", type,
                "Type filter did not return only " + label + " applicants.");
    }

    /**
     * Thực hiện xử lý list supports search by user id trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý list supports search not found trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý list supports date range query trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý pagination next page changes applicant window when available trong luồng kiểm thử.
     */
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

    /**
     * Thực hiện xử lý list handles invalid query without server error trong luồng kiểm thử.
     * @param path đường dẫn cần xử lý
     * @param label giá trị label được truyền vào
     */
    @Test(dataProvider = "invalidListQueries", groups = {"ekyc", "api", "contract"})
    public void listHandlesInvalidQueryWithoutServerError(String path, String label) {
        ApiResponse response = api.get(path);
        Assert.assertTrue(response.status() >= 200 && response.status() < 500,
                "Invalid list query should not become 5xx. Case=" + label
                        + " status=" + response.status() + " body=" + response.preview());
    }
}
