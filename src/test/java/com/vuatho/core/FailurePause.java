package com.vuatho.core;

import com.vuatho.config.TestConfig;

import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;

final class FailurePause {
    private FailurePause() {
    }

    static void awaitConfirmation() {
        if (!TestConfig.pauseOnFailure() || GraphicsEnvironment.isHeadless()) {
            return;
        }
        JOptionPane.showMessageDialog(
                null,
                "Test dang bi loi. Chrome se duoc giu mo cho toi khi ban bam OK.",
                "ERP Automation Test",
                JOptionPane.ERROR_MESSAGE);
    }
}
