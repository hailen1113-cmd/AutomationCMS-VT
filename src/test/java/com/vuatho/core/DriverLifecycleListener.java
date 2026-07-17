package com.vuatho.core;

import org.testng.IExecutionListener;

public final class DriverLifecycleListener implements IExecutionListener {
    /**
     * Thực hiện xử lý on execution finish trong luồng kiểm thử.
     */
    @Override
    public void onExecutionFinish() {
        // Chỉ đóng sau khi toàn bộ TestNG execution kết thúc. Nếu một execution chứa
        // nhiều suite, tất cả suite vẫn dùng cùng browser và cùng session đăng nhập.
        DriverSession.releaseAfterSuite();
    }
}
