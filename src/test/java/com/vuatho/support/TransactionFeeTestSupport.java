package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;

/** Support dùng chung cho toàn bộ testcase của nhóm Phí & Doanh thu. */
public abstract class TransactionFeeTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.FEE;
    }

    protected final void openFeeSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        if (!url.contains("tab=" + subtype.tab()) || !url.contains("type=" + subtype.type())) {
            transactionPage.open(subtype);
        }
    }

    protected final TransactionHistoryPage advancedPage() {
        return new TransactionHistoryPage(driver);
    }
}
