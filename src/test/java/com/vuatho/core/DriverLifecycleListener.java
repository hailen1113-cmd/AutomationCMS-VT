package com.vuatho.core;

import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public final class DriverLifecycleListener implements IExecutionListener, IInvokedMethodListener {
    private int startedTestMethods;
    private int finishedTestMethods;

    @Override
    public void onExecutionStart() {
        startedTestMethods = 0;
        finishedTestMethods = 0;
        DriverSession.beginExecution();
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            startedTestMethods++;
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod()) {
            return;
        }
        finishedTestMethods++;
        try {
            DriverSession.assertBrowserAliveIfAcquired();
        } catch (IllegalStateException lifecycleFailure) {
            testResult.setStatus(ITestResult.FAILURE);
            testResult.setThrowable(lifecycleFailure);
        }
    }

    /**
     * Thực hiện xử lý on execution finish trong luồng kiểm thử.
     */
    @Override
    public void onExecutionFinish() {
        // Chỉ đóng sau khi toàn bộ TestNG execution kết thúc. Nếu một execution chứa
        // nhiều suite, tất cả suite vẫn dùng cùng browser và cùng session đăng nhập.
        System.out.println("[BROWSER LIFECYCLE] Test methods started=" + startedTestMethods
                + ", finished=" + finishedTestMethods + ".");
        DriverSession.finishExecution();
    }
}
