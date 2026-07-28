package com.vuatho.tests.uniform;

import com.vuatho.core.TestNgRunner;
import com.vuatho.tests.uniform.catalog.UniformCatalogDrawerTest;
import com.vuatho.tests.uniform.catalog.UniformCatalogOverviewTest;
import com.vuatho.tests.uniform.catalog.UniformCatalogPaginationTest;
import com.vuatho.tests.uniform.catalog.UniformCatalogSearchFilterTest;
import com.vuatho.tests.uniform.inventory.UniformInventoryDialogTest;
import com.vuatho.tests.uniform.inventory.UniformInventoryOverviewTest;
import com.vuatho.tests.uniform.inventory.UniformInventoryReceiptTest;
import com.vuatho.tests.uniform.order.UniformOrderDrawerTest;
import com.vuatho.tests.uniform.order.UniformOrderOverviewTest;
import com.vuatho.tests.uniform.order.UniformOrderSearchFilterTest;

/**
 * Điểm chạy toàn bộ testcase của ba menu Đồng phục.
 *
 * <p>File này chỉ gom các class test; từng file test bên dưới vẫn có
 * {@code main()} riêng và chạy độc lập được từ IDE.</p>
 */
public final class UniformManagementSuiteRunner {
    private UniformManagementSuiteRunner() {
    }

    /** Chạy lần lượt danh mục, đơn hàng rồi kho Đồng phục. */
    public static void main(String[] args) {
        TestNgRunner.run(
                "Đồng phục",
                "Toàn bộ testcase dữ liệu và popup",
                UniformCatalogOverviewTest.class,
                UniformCatalogSearchFilterTest.class,
                UniformCatalogDrawerTest.class,
                UniformCatalogPaginationTest.class,
                UniformOrderOverviewTest.class,
                UniformOrderSearchFilterTest.class,
                UniformOrderDrawerTest.class,
                UniformInventoryOverviewTest.class,
                UniformInventoryReceiptTest.class,
                UniformInventoryDialogTest.class);
    }
}
