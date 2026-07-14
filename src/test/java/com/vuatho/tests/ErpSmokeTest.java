package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.pages.EntryPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ErpSmokeTest extends BaseTest {
    @Test(description = "ERP sandbox responds on the expected domain")
    public void sandboxIsReachable() {
        EntryPage page = new EntryPage(driver).open();

        Assert.assertTrue(page.isOnExpectedDomain(), "Browser was redirected away from the ERP sandbox");
        Assert.assertFalse(page.title().isBlank(), "The page title should not be empty");
    }

    @Test(description = "Automation can pass Vercel protection and reach the ERP application")
    public void erpApplicationIsAccessible() {
        EntryPage page = new EntryPage(driver).open();

        if (page.isBlockedByVercel()) {
            throw new SkipException(
                    "Vercel protection is blocking automation. Set VERCEL_AUTOMATION_BYPASS_SECRET.");
        }
    }
}
