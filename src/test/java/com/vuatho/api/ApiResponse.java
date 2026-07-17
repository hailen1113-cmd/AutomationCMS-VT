package com.vuatho.api;

/**
 * Đóng gói status code, header và body trả về để assertion API sử dụng thống nhất.
 */
public record ApiResponse(int status, boolean ok, String contentType, String body) {
    public boolean isJson() {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    /**
     * Thực hiện xử lý preview trong luồng kiểm thử.
     * @return kết quả preview sau khi xử lý
     */
    public String preview() {
        if (body == null) {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 500) + "...";
    }
}
