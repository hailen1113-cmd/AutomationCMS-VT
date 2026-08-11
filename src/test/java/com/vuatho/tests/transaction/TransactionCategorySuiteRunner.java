package com.vuatho.tests.transaction;

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
import com.vuatho.tests.transaction.deposit.TransactionDepositDetailTest;
import com.vuatho.tests.transaction.deposit.TransactionDepositDropdownTest;
import com.vuatho.tests.transaction.deposit.TransactionDepositExportTest;
import com.vuatho.tests.transaction.deposit.TransactionDepositFilterTest;
import com.vuatho.tests.transaction.deposit.TransactionDepositNavigationTest;
import com.vuatho.tests.transaction.deposit.TransactionDepositOverviewTest;
import com.vuatho.tests.transaction.fee.TransactionFeeTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceTest;
import com.vuatho.tests.transaction.order.TransactionOrderTest;
import com.vuatho.tests.transaction.reward.TransactionRewardTest;
import com.vuatho.tests.transaction.system.TransactionSystemTest;
import com.vuatho.tests.transaction.withdraw.TransactionWithdrawTest;

/** Chạy toàn bộ tám nhóm chuyên biệt ngoài tab Tất cả. */
public final class TransactionCategorySuiteRunner {
    private TransactionCategorySuiteRunner() {}

    public static void main(String[] args) {
        TestNgRunner.run("Lịch sử giao dịch", "Tám nhóm giao dịch chuyên biệt",
                TransactionDepositDropdownTest.class,
                TransactionDepositOverviewTest.class,
                TransactionDepositFilterTest.class,
                TransactionDepositNavigationTest.class,
                TransactionDepositDetailTest.class,
                TransactionDepositExportTest.class,
                TransactionWithdrawTest.class,
                TransactionOrderTest.class,
                TransactionRewardTest.class,
                TransactionFeeTest.class,
                TransactionInsuranceTest.class,
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
                TransactionAssistantPenaltyExportTest.class,
                TransactionSystemTest.class);
    }
}
