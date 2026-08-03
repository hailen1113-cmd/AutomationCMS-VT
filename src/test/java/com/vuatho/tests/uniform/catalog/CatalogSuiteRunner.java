package com.vuatho.tests.uniform.catalog;

import com.vuatho.core.TestNgRunner;
import com.vuatho.tests.uniform.catalog.create.GroupCreateTest;
import com.vuatho.tests.uniform.catalog.create.GroupCreateValidationTest;
import com.vuatho.tests.uniform.catalog.create.ItemCreateTest;
import com.vuatho.tests.uniform.catalog.create.ItemCreateValidationTest;
import com.vuatho.tests.uniform.catalog.delete.GroupDeleteTest;
import com.vuatho.tests.uniform.catalog.delete.ItemDeleteTest;
import com.vuatho.tests.uniform.catalog.read.CatalogDetailTest;
import com.vuatho.tests.uniform.catalog.read.CatalogInventoryFilterTest;
import com.vuatho.tests.uniform.catalog.read.CatalogOverviewTest;
import com.vuatho.tests.uniform.catalog.read.CatalogPaginationTest;
import com.vuatho.tests.uniform.catalog.read.CatalogSearchFilterCombinationTest;
import com.vuatho.tests.uniform.catalog.read.CatalogSearchTest;
import com.vuatho.tests.uniform.catalog.update.GroupUpdateTest;
import com.vuatho.tests.uniform.catalog.update.GroupUpdateValidationTest;
import com.vuatho.tests.uniform.catalog.update.ItemUpdateTest;
import com.vuatho.tests.uniform.catalog.update.ItemUpdateValidationTest;

/**
 * Điểm chạy toàn bộ testcase của menu Quản lí Đồng Phục.
 *
 * <p>File này chỉ gom các class test; từng file test bên dưới vẫn có
 * {@code main()} riêng và chạy độc lập được từ IDE.</p>
 */
public final class CatalogSuiteRunner {
    private CatalogSuiteRunner() {
    }

    /** Chạy lần lượt Read, Create, Update và Delete của menu Quản lí Đồng Phục. */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Đồng phục",
                "Toàn bộ testcase dữ liệu và popup",
                CatalogOverviewTest.class,
                CatalogSearchTest.class,
                CatalogInventoryFilterTest.class,
                CatalogSearchFilterCombinationTest.class,
                CatalogDetailTest.class,
                GroupCreateTest.class,
                GroupCreateValidationTest.class,
                ItemCreateTest.class,
                ItemCreateValidationTest.class,
                GroupUpdateTest.class,
                GroupUpdateValidationTest.class,
                ItemUpdateTest.class,
                ItemUpdateValidationTest.class,
                GroupDeleteTest.class,
                ItemDeleteTest.class,
                CatalogPaginationTest.class);
    }
}
