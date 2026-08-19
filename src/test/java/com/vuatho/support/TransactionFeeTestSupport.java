package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;

/** Support dùng chung cho toàn bộ testcase của nhóm Phí & Doanh thu. */
public abstract class TransactionFeeTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.FEE;
    }

    protected final void openFeeSubtype(TransactionCategoryPage.Subtype subtype) {
        openSubtype(subtype);
    }
}
