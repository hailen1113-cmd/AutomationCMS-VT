package com.vuatho.config;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.util.Arrays;

/**
 * Đọc mật khẩu Google từ nguồn cấu hình an toàn và báo lỗi rõ ràng khi chưa thiết lập.
 */
public final class GoogleCredentialProvider {
    private GoogleCredentialProvider() {
    }

    /**
     * Thực hiện xử lý password trong luồng kiểm thử.
     * @return kết quả password sau khi xử lý
     */
    public static String password() {
        if (!TestConfig.loginPassword().isBlank()) {
            return TestConfig.loginPassword();
        }
        if (!TestConfig.interactive()) {
            return "";
        }

        JPasswordField passwordField = new JPasswordField(24);
        int choice = JOptionPane.showConfirmDialog(
                null,
                passwordField,
                "Nhap mat khau Google cho " + TestConfig.loginEmail(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        char[] password = passwordField.getPassword();
        try {
            if (choice != JOptionPane.OK_OPTION || password.length == 0) {
                return "";
            }
            String value = new String(password);
            System.setProperty("google.password", value);
            return value;
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
