package com.vuatho.support;

import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.support.ui.WebDriverWait;

/** Support dùng chung cho toàn bộ testcase của nhóm Tiền nạp. */
public abstract class TransactionDepositTestSupport extends TransactionCategoryTestSupport {
    @Override
    protected final TransactionCategoryPage.Category category() {
        return TransactionCategoryPage.Category.DEPOSIT;
    }

    protected final void openDepositSubtype(TransactionCategoryPage.Subtype subtype) {
        String url = transactionPage.currentUrl();
        if (!url.contains("tab=" + subtype.tab()) || !url.contains("type=" + subtype.type())) {
            transactionPage.open(subtype);
        }
    }

    protected final void assertDepositRows() {
        var rows = transactionPage.rows();
        Assert.assertFalse(rows.isEmpty(), "Không có dữ liệu Tiền nạp để kiểm tra");
        rows.forEach(row -> Assert.assertFalse(row.value("Loại giao dịch").isBlank(),
                "Dòng thiếu loại giao dịch: " + row.signature()));
    }

    protected final TransactionHistoryPage advancedPage() {
        return new TransactionHistoryPage(driver);
    }

    protected final List<String> waitForRowsToChange(List<String> before) {
        return new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            List<String> current = transactionPage.rows().stream()
                    .map(TransactionCategoryPage.TransactionRow::signature).toList();
            return current.equals(before) ? null : current;
        });
    }

}
