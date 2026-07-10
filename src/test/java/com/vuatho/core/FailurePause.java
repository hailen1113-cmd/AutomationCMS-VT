package com.vuatho.core;

import com.vuatho.config.TestConfig;

import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;

final class FailurePause {
    private FailurePause() {
    }

    static void awaitConfirmation() {
        // Chỉ pause khi được bật rõ ràng; chạy bình thường không được đứng ở popup này.
        if (!TestConfig.pauseOnFailure() || GraphicsEnvironment.isHeadless()) {
            return;
        }
        // Đây chỉ là hỗ trợ debug local, không phải một bước trong flow automation.
        JOptionPane.showMessageDialog(
                null,
                "Test dang bi loi. Chrome se duoc giu mo cho toi khi ban bam OK.",
                "ERP Automation Test",
                JOptionPane.ERROR_MESSAGE);
    }
}
