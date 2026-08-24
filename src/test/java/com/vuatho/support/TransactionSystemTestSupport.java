package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import org.testng.Assert;

/** Cung cấp route và subtype dùng chung cho nhóm Hệ thống. */
public abstract class TransactionSystemTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.SYSTEM;
    }

    protected final TransactionCategoryPage.Subtype systemSubtype() {
        return category().subtypes().get(0);
    }

    protected final void openSystemSubtype() {
        openSubtype(systemSubtype());
    }

    protected final void assertSystemRoute(String url, boolean hasId) {
        Assert.assertTrue(url.contains("tab=system"), url);
        Assert.assertTrue(url.contains("type=7"), url);
        Assert.assertEquals(url.contains("id="), hasId, url);
    }
}
