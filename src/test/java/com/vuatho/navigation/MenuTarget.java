package com.vuatho.navigation;

public record MenuTarget(String parent, String name) {
    public static MenuTarget topLevel(String name) {
        return new MenuTarget(null, name);
    }

    public static MenuTarget childOf(String parent, String name) {
        return new MenuTarget(parent, name);
    }

    public boolean hasParent() {
        return parent != null && !parent.isBlank();
    }

    @Override
    public String toString() {
        return hasParent() ? parent + " > " + name : name;
    }
}
