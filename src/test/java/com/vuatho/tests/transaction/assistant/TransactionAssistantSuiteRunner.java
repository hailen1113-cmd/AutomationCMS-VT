package com.vuatho.tests.transaction.assistant;

import com.vuatho.core.TestNgRunner;
import com.vuatho.tests.transaction.assistant.common.TransactionAssistantDropdownTest;
import com.vuatho.tests.transaction.assistant.common.TransactionAssistantSubtypeContractTest;
import com.vuatho.tests.transaction.assistant.penalty.TransactionAssistantPenaltyDetailTest;
import com.vuatho.tests.transaction.assistant.penalty.TransactionAssistantPenaltyExportTest;
import com.vuatho.tests.transaction.assistant.penalty.TransactionAssistantPenaltyFilterTest;
import com.vuatho.tests.transaction.assistant.penalty.TransactionAssistantPenaltyNavigationTest;
import com.vuatho.tests.transaction.assistant.penalty.TransactionAssistantPenaltyOverviewTest;
import com.vuatho.tests.transaction.assistant.platformfee.TransactionAssistantPlatformFeeDetailTest;
import com.vuatho.tests.transaction.assistant.platformfee.TransactionAssistantPlatformFeeExportTest;
import com.vuatho.tests.transaction.assistant.platformfee.TransactionAssistantPlatformFeeFilterTest;
import com.vuatho.tests.transaction.assistant.platformfee.TransactionAssistantPlatformFeeNavigationTest;
import com.vuatho.tests.transaction.assistant.platformfee.TransactionAssistantPlatformFeeOverviewTest;

/** Chạy toàn bộ suite chuyên biệt của tab Thợ phụ. */
public final class TransactionAssistantSuiteRunner {
    private TransactionAssistantSuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Thợ phụ",
                TransactionAssistantDropdownTest.class,
                TransactionAssistantSubtypeContractTest.class,
                TransactionAssistantPlatformFeeOverviewTest.class,
                TransactionAssistantPlatformFeeFilterTest.class,
                TransactionAssistantPlatformFeeNavigationTest.class,
                TransactionAssistantPlatformFeeDetailTest.class,
                TransactionAssistantPlatformFeeExportTest.class,
                TransactionAssistantPenaltyOverviewTest.class,
                TransactionAssistantPenaltyFilterTest.class,
                TransactionAssistantPenaltyNavigationTest.class,
                TransactionAssistantPenaltyDetailTest.class,
                TransactionAssistantPenaltyExportTest.class);
    }
}
