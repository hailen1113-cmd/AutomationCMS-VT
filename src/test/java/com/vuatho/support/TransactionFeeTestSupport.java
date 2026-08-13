package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import org.testng.annotations.DataProvider;

/** Support dùng chung cho toàn bộ testcase của nhóm Phí & Doanh thu. */
public abstract class TransactionFeeTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.FEE;
    }

    @DataProvider(name = "feeSubtypes")
    public final Object[][] feeSubtypes() {
        String requestedType = System.getProperty("fee.type", "").trim();
        return category().subtypes().stream()
                .filter(subtype -> requestedType.isBlank()
                        || requestedType.equals(String.valueOf(subtype.type())))
                .map(subtype -> new Object[]{subtype})
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "feeOrderSubtypes")
    public final Object[][] feeOrderSubtypes() {
        return category().subtypes().stream()
                .filter(subtype -> subtype.type() == 8 || subtype.type() == 33)
                .map(subtype -> new Object[]{subtype})
                .toArray(Object[][]::new);
    }

    @DataProvider(name = "feeOrderInvoiceStates")
    public final Object[][] feeOrderInvoiceStates() {
        String requestedType = System.getProperty("fee.type", "").trim();
        return category().subtypes().stream()
                .filter(subtype -> subtype.type() == 8 || subtype.type() == 33)
                .filter(subtype -> requestedType.isBlank()
                        || requestedType.equals(String.valueOf(subtype.type())))
                .flatMap(subtype -> java.util.stream.Stream.of(
                        new Object[]{subtype, "Có"}, new Object[]{subtype, "Không"}))
                .toArray(Object[][]::new);
    }

    protected final void openFeeSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        if (!url.contains("tab=" + subtype.tab()) || !url.contains("type=" + subtype.type())) {
            transactionPage.open(subtype);
        }
    }

    protected final TransactionHistoryPage advancedPage() {
        return new TransactionHistoryPage(driver);
    }
}
