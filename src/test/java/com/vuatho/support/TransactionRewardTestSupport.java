package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;

import java.util.List;

/** Cung cấp route và subtype dùng chung cho nhóm Thưởng & KM. */
public abstract class TransactionRewardTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.REWARD;
    }

    protected final TransactionCategoryPage.Subtype rewardSubtype(int type) {
        return subtypeByType(type);
    }

    protected final List<TransactionCategoryPage.Subtype> rewardSubtypes() {
        return category().subtypes();
    }

    protected final void openRewardSubtype(TransactionCategoryPage.Subtype subtype) {
        openSubtype(subtype);
    }
}
