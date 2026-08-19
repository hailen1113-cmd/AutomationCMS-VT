package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
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
        openSubtype(subtype);
    }
}
