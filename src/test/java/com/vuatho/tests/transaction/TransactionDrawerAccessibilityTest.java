package com.vuatho.tests.transaction;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.pages.TransactionCategoryPage;
import com.vuatho.pages.TransactionHistoryPage;
import com.vuatho.testcases.TransactionHistoryTestCases;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/** Kiểm tra tên truy cập của nút đóng drawer trên toàn bộ menu giao dịch chuyên biệt. */
public class TransactionDrawerAccessibilityTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(TransactionDrawerAccessibilityTest.class,
                "Lịch sử giao dịch", "Accessibility drawer theo menu");
    }

    @BeforeMethod(alwaysRun = true)
    public void prepareTransactionMenu() {
        requireAuthenticatedSession("Lịch sử giao dịch");
    }

    @Test(description = TransactionHistoryTestCases.TRANSACTION_ALL_096)
    public void everySpecializedSubtypeCloseButtonHasAccessibleName() {
        List<String> violations = new ArrayList<>();
        for (TransactionCategoryPage.Category category : TransactionCategoryPage.Category.values()) {
            TransactionCategoryPage categoryPage = new TransactionCategoryPage(driver, category);
            for (TransactionCategoryPage.Subtype subtype : category.subtypes()) {
                categoryPage.open(subtype);
                try {
                    var result = new TransactionHistoryPage(driver)
                            .inspectDetailCloseAccessibility();
                    boolean named = !result.ariaLabel().isBlank()
                            || !result.title().isBlank() || !result.text().isBlank();
                    if (!named) {
                        violations.add(category.label() + " / " + subtype.label()
                                + ": nút đóng không có tên truy cập");
                    }
                } catch (AssertionError error) {
                    violations.add(category.label() + " / " + subtype.label()
                            + ": không có dữ liệu để mở drawer");
                }
            }
        }

        Assert.assertTrue(violations.isEmpty(), String.join(System.lineSeparator(), violations));
    }
}
