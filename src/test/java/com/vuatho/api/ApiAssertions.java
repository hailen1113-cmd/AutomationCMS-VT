package com.vuatho.api;

import org.testng.Assert;

/**
 * Cung cấp assertion dùng chung cho status code, content type và nội dung JSON của API.
 */
public final class ApiAssertions {
    private ApiAssertions() {
    }

    /**
     * Xác nhận ok json trong luồng kiểm thử.
     * @param response phản hồi API cần kiểm tra
     * @param label giá trị label được truyền vào
     */
    public static void assertOkJson(ApiResponse response, String label) {
        Assert.assertEquals(response.status(), 200,
                label + " failed. Body: " + response.preview());
        Assert.assertTrue(response.isJson() || response.body().trim().startsWith("{"),
                label + " did not return JSON. Content-Type: " + response.contentType()
                        + " Body: " + response.preview());
    }
}
