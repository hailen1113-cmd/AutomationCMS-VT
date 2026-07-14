package com.vuatho.api;

import org.testng.Assert;

public final class ApiAssertions {
    private ApiAssertions() {
    }

    public static void assertOkJson(ApiResponse response, String label) {
        Assert.assertEquals(response.status(), 200,
                label + " failed. Body: " + response.preview());
        Assert.assertTrue(response.isJson() || response.body().trim().startsWith("{"),
                label + " did not return JSON. Content-Type: " + response.contentType()
                        + " Body: " + response.preview());
    }
}
