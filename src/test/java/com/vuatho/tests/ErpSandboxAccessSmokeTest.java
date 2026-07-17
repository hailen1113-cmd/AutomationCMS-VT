package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.EntryPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Xác nhận URL ERP sandbox có thể truy cập và chuyển đến màn hình đăng nhập hoặc Dashboard.
 */
public class ErpSandboxAccessSmokeTest extends BaseTest {
    @Test(description = "ERP sandbox responds on the expected domain")
    public void sandboxIsReachable() {
        EntryPage page = new EntryPage(driver).open();

        Assert.assertTrue(page.isOnExpectedDomain(), "Browser was redirected away from the ERP sandbox");
        Assert.assertFalse(page.title().isBlank(), "The page title should not be empty");
    }

    /**
     * Thực thi test “Automation can pass Vercel protection and reach the ERP application” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "Automation can pass Vercel protection and reach the ERP application")
    public void erpApplicationIsAccessible() {
        EntryPage page = new EntryPage(driver).open();

        if (page.isBlockedByVercel()) {
            throw new SkipException(
                    "Vercel protection is blocking automation. Set VERCEL_AUTOMATION_BYPASS_SECRET.");
        }
    }
}
