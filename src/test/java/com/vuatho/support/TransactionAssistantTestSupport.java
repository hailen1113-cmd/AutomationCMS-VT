package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;

import java.util.List;

/** Support dùng chung cho các suite chuyên biệt của tab Thợ phụ. */
public abstract class TransactionAssistantTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.ASSISTANT;
    }

    protected final TransactionHistoryPage advancedPage() {
        return new TransactionHistoryPage(driver);
    }

    protected final List<String> signatures(List<TransactionCategoryPage.TransactionRow> rows) {
        return rows.stream().map(TransactionCategoryPage.TransactionRow::signature).toList();
    }

    protected final String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^0-9A-Za-zÀ-ỹ]", "").toLowerCase();
    }
}
