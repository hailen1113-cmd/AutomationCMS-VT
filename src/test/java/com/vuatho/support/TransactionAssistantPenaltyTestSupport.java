package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;

/** Support cố định loại Tiền phạt của tab Thợ phụ. */
public abstract class TransactionAssistantPenaltyTestSupport
        extends TransactionAssistantTestSupport {
    protected final TransactionCategoryPage.Subtype subtype() {
        return category().subtypes().get(1);
    }

    @Override
    protected final TransactionCategoryPage.Subtype initialSubtype() {
        return subtype();
    }
}
