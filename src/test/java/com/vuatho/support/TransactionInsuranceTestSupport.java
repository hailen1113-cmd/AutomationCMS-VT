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

    @DataProvider(name = "insuranceSubtypes")
    public final Object[][] insuranceSubtypes() {
        return subtypeRows();
    }

    @DataProvider(name = "insuranceSelectFilters")
    public final Object[][] insuranceSelectFilters() {
        return new Object[][]{
                {"trạng thái-filter", java.util.List.of("Đang chờ", "Thành công", "Thất bại")},
                {"cổng thanh toán-filter", java.util.List.of("MOMO", "PAYPAL", "ONEPAY", "BANKING", "NEOX")}
        };
    }

    @DataProvider(name = "insuranceFilterSelections")
    public final Object[][] insuranceFilterSelections() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (TransactionCategoryPage.Subtype subtype : category().subtypes()) {
            for (Object[] filter : insuranceSelectFilters()) {
                String aria = String.valueOf(filter[0]);
                @SuppressWarnings("unchecked")
                java.util.List<String> options = (java.util.List<String>) filter[1];
                for (String option : options) {
                    rows.add(new Object[]{subtype, aria, option});
                }
            }
        }
        return rows.toArray(Object[][]::new);
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
