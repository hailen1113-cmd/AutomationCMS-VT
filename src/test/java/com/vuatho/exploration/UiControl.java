package com.vuatho.exploration;

public record UiControl(String tag, String role, String label, String type) {
    @Override
    public String toString() {
        return String.format("%-8s %-10s %-18s %s", tag, role, type, label);
    }
}
