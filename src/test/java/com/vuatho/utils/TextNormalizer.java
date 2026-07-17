package com.vuatho.utils;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Chuẩn hóa khoảng trắng, dấu và kiểu chữ để so sánh nội dung giao diện ổn định hơn.
 */
public final class TextNormalizer {
    private TextNormalizer() {
    }

    /**
     * Thực hiện xử lý normalize trong luồng kiểm thử.
     * @param value giá trị đầu vào
     * @return kết quả normalize sau khi xử lý
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D")
                .replace("Ä‘", "d")
                .replace("Ä", "D")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
