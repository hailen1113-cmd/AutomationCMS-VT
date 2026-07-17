package com.vuatho.tests;

import com.vuatho.api.ApiResponse;
import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Xác nhận API eKYC từ chối request thiếu hoặc có thông tin xác thực không hợp lệ.
 */
public class EkycSecurityApiContractTest extends EkycApiTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(EkycSecurityApiContractTest.class,
                "Bo test API security eKYC ERP",
                "Kiem tra authentication eKYC API");
    }

    /**
     * Thực hiện xử lý kyc apis require authentication trong luồng kiểm thử.
     */
    @Test(groups = {"ekyc", "api", "contract", "security"})
    public void kycApisRequireAuthentication() {
        ApiResponse response = api.getWithoutAuth("/ekyc?page=1&limit=1");
        Assert.assertTrue(response.status() == 401 || response.status() == 403,
                "Unauthenticated /ekyc should be rejected, status=" + response.status()
                        + " body=" + response.preview());
    }
}
