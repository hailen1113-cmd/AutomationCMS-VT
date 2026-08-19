package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import org.testng.annotations.DataProvider;

/** Support dùng chung cho toàn bộ testcase của nhóm VT Care. */
public abstract class TransactionInsuranceTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.INSURANCE;
    }

    @DataProvider(name = "insuranceSelectFilters")
    public final Object[][] insuranceSelectFilters() {
        return new Object[][]{
                {"trạng thái-filter", java.util.List.of("Đang chờ", "Thành công", "Thất bại")},
                {"cổng thanh toán-filter", java.util.List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX")}
        };
    }

    protected final void openInsuranceSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        if (!url.contains("tab=insurance") || !url.contains("type=" + subtype.type())) {
            transactionPage.open(subtype);
        }
    }

    protected final TransactionHistoryPage advancedPage() {
        return new TransactionHistoryPage(driver);
    }
}
