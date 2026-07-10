package com.vuatho.core;

import org.testng.IExecutionListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public final class DriverLifecycleListener implements ISuiteListener, IExecutionListener {
    @Override
    public void onFinish(ISuite suite) {
        // Điểm cuối của một TestNG suite: mọi testcase trong suite đã được lên lịch và thực thi/skip.
        DriverSession.releaseAfterSuite();
    }

    @Override
    public void onExecutionFinish() {
        // Idempotent fallback cho các runner không phát sự kiện suite như mong đợi.
        DriverSession.releaseAfterSuite();
    }
}
