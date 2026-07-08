package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

public record FilterTarget(MenuTarget page, String placeholder) {
    @Override
    public String toString() {
        return page + " [" + placeholder + "]";
    }
}
