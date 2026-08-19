package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import org.testng.annotations.DataProvider;

import java.util.List;

/** Support dùng chung cho các suite chuyên biệt của nhóm Đơn dịch vụ. */
public abstract class TransactionOrderTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.ORDER;
    }

    @DataProvider(name = "orderSelectFilters")
    public final Object[][] orderSelectFilters() {
        return new Object[][]{
                {"trạng thái-filter", List.of("Đang chờ", "Thành công", "Thất bại")},
                {"cổng thanh toán-filter", List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX")}
        };
    }

    protected final void openOrderSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        if (!url.contains("tab=order") || !url.contains("type=" + subtype.type())
                || !transactionPage.activeGroupText().contains(subtype.label())) {
            transactionPage.open(subtype);
        }
    }

    protected final TransactionHistoryPage advancedPage() {
        return new TransactionHistoryPage(driver);
    }
}
