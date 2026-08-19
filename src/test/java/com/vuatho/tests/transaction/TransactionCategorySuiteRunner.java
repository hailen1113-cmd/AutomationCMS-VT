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
import com.vuatho.tests.transaction.fee.TransactionFeeDetailTest;
import com.vuatho.tests.transaction.fee.TransactionFeeDropdownTest;
import com.vuatho.tests.transaction.fee.TransactionFeeExportTest;
import com.vuatho.tests.transaction.fee.TransactionFeeFilterTest;
import com.vuatho.tests.transaction.fee.TransactionFeeNavigationTest;
import com.vuatho.tests.transaction.fee.TransactionFeeOverviewTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceDetailTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceDropdownTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceExportTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceFilterTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceNavigationTest;
import com.vuatho.tests.transaction.insurance.TransactionInsuranceOverviewTest;
import com.vuatho.tests.transaction.order.TransactionOrderDetailTest;
import com.vuatho.tests.transaction.order.TransactionOrderDropdownTest;
import com.vuatho.tests.transaction.order.TransactionOrderExportTest;
import com.vuatho.tests.transaction.order.TransactionOrderFilterTest;
import com.vuatho.tests.transaction.order.TransactionOrderNavigationTest;
import com.vuatho.tests.transaction.order.TransactionOrderOverviewTest;
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
                TransactionOrderDropdownTest.class,
                TransactionOrderOverviewTest.class,
                TransactionOrderFilterTest.class,
                TransactionOrderNavigationTest.class,
                TransactionOrderDetailTest.class,
                TransactionOrderExportTest.class,
                TransactionRewardTest.class,
                TransactionFeeDropdownTest.class,
                TransactionFeeOverviewTest.class,
                TransactionFeeFilterTest.class,
                TransactionFeeNavigationTest.class,
                TransactionFeeDetailTest.class,
                TransactionFeeExportTest.class,
                TransactionInsuranceDropdownTest.class,
                TransactionInsuranceOverviewTest.class,
                TransactionInsuranceFilterTest.class,
                TransactionInsuranceNavigationTest.class,
                TransactionInsuranceDetailTest.class,
                TransactionInsuranceExportTest.class,
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
