package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
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
        openSubtype(subtype);
    }
}
