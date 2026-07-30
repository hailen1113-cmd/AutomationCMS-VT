package com.vuatho.tests.uniform.catalog;

import com.vuatho.core.TestNgRunner;
import com.vuatho.tests.uniform.catalog.create.UniformGroupCreateTest;
import com.vuatho.tests.uniform.catalog.create.UniformGroupCreateValidationTest;
import com.vuatho.tests.uniform.catalog.create.UniformItemCreateTest;
import com.vuatho.tests.uniform.catalog.create.UniformItemCreateValidationTest;
import com.vuatho.tests.uniform.catalog.delete.UniformCatalogDeleteTest;
import com.vuatho.tests.uniform.catalog.read.UniformCatalogDetailTest;
import com.vuatho.tests.uniform.catalog.read.UniformCatalogInventoryFilterTest;
import com.vuatho.tests.uniform.catalog.read.UniformCatalogOverviewTest;
import com.vuatho.tests.uniform.catalog.read.UniformCatalogPaginationTest;
import com.vuatho.tests.uniform.catalog.read.UniformCatalogSearchFilterCombinationTest;
import com.vuatho.tests.uniform.catalog.read.UniformCatalogSearchTest;
import com.vuatho.tests.uniform.catalog.update.UniformCatalogUpdateTest;

/**
 * Điểm chạy toàn bộ testcase của menu Quản lí Đồng Phục.
 *
 * <p>File này chỉ gom các class test; từng file test bên dưới vẫn có
 * {@code main()} riêng và chạy độc lập được từ IDE.</p>
 */
public final class UniformCatalogSuiteRunner {
    private UniformCatalogSuiteRunner() {
    }

    /** Chạy lần lượt Read, Create, Update và Delete của menu Quản lí Đồng Phục. */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Đồng phục",
                "Toàn bộ testcase dữ liệu và popup",
                UniformCatalogOverviewTest.class,
                UniformCatalogSearchTest.class,
                UniformCatalogInventoryFilterTest.class,
                UniformCatalogSearchFilterCombinationTest.class,
                UniformCatalogDetailTest.class,
                UniformGroupCreateTest.class,
                UniformGroupCreateValidationTest.class,
                UniformItemCreateTest.class,
                UniformItemCreateValidationTest.class,
                UniformCatalogUpdateTest.class,
                UniformCatalogDeleteTest.class,
                UniformCatalogPaginationTest.class);
    }
}
