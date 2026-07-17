package com.vuatho.testdata;

import com.vuatho.navigation.MenuTarget;

/**
 * Mô tả một menu có bộ lọc cùng các locator và giá trị dùng để kiểm tra.
 */
public record FilterTarget(MenuTarget page, String placeholder) {
    @Override
    public String toString() {
        return page + " [" + placeholder + "]";
    }
}
