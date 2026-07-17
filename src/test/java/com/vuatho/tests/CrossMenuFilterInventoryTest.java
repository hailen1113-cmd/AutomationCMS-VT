package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.exploration.UiControl;
import com.vuatho.exploration.UiFeatureExplorer;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.testdata.MenuCatalog;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Locale;

/**
 * Khám phá và ghi nhận input, dropdown cùng nút điều khiển của bộ lọc trên nhiều menu ERP.
 */
public class CrossMenuFilterInventoryTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(CrossMenuFilterInventoryTest.class,
                "ERP Cross-menu Filter Discovery", "Read-only cross-menu filter inventory");
    }

    /**
     * Thực hiện xử lý all menus trong luồng kiểm thử.
     * @return kết quả all menus sau khi xử lý
     */
    @DataProvider(name = "allMenus")
    public Object[][] allMenus() {
        return MenuCatalog.dataProviderRows();
    }

    /**
     * Thu thập filters across all menus trong luồng kiểm thử.
     * @param target giá trị target được truyền vào
     */
    @Test(dataProvider = "allMenus")
    public void inventoryFiltersAcrossAllMenus(MenuTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        UiFeatureExplorer explorer = new UiFeatureExplorer(driver);

        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);
        List<UiControl> filters = explorer.visibleControls().stream()
                .filter(this::isFilterControl)
                .toList();
        if (!filters.isEmpty()) {
            System.out.printf("%n[FILTER PAGE] %s | %s%n", target, driver.getCurrentUrl());
            filters.forEach(control -> System.out.println("  " + control));
        }
    }

    /**
     * Kiểm tra điều kiện is filter control.
     * @param control giá trị control được truyền vào
     * @return kết quả is filter control sau khi xử lý
     */
    private boolean isFilterControl(UiControl control) {
        String label = control.label().toLowerCase(Locale.ROOT);
        return control.tag().equals("input") || control.tag().equals("select")
                || label.contains("filter") || label.contains("lọc")
                || label.contains("reset") || label.contains("tìm kiếm")
                || label.contains("chọn trạng thái") || label.contains("chọn khoảng");
    }
}
