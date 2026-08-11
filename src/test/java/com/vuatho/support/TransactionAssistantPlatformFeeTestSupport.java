package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;

/** Support cố định loại Phí nền tảng của tab Thợ phụ. */
public abstract class TransactionAssistantPlatformFeeTestSupport
        extends TransactionAssistantTestSupport {
    protected final TransactionCategoryPage.Subtype subtype() {
        return category().subtypes().get(0);
    }
}
