package com.vuatho.api;

public record ApiResponse(int status, boolean ok, String contentType, String body) {
    public boolean isJson() {
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    public String preview() {
        if (body == null) {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 500) + "...";
    }
}
