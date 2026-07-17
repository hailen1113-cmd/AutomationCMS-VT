package com.vuatho.quality;

import java.util.List;

/**
 * Tổng hợp lỗi console, lỗi tài nguyên và trạng thái tải trang thành kết quả sức khỏe duy nhất.
 */
public record PageHealthReport(String url, List<String> problems) {
    public boolean isHealthy() {
        return problems.isEmpty();
    }

    /**
     * Thực hiện xử lý summary trong luồng kiểm thử.
     * @return kết quả summary sau khi xử lý
     */
    public String summary() {
        return isHealthy() ? "Page is healthy: " + url : String.join(System.lineSeparator(), problems);
    }
}
