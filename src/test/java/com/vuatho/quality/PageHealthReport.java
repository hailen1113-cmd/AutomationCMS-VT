package com.vuatho.quality;

import java.util.List;

public record PageHealthReport(String url, List<String> problems) {
    public boolean isHealthy() {
        return problems.isEmpty();
    }

    public String summary() {
        return isHealthy() ? "Page is healthy: " + url : String.join(System.lineSeparator(), problems);
    }
}
