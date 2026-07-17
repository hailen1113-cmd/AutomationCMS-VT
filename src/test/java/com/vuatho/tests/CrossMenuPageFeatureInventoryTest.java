package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.exploration.UiFeatureExplorer;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.navigation.MenuTarget;
import com.vuatho.pages.MenuDestinationPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.vuatho.navigation.MenuTarget.childOf;
import static com.vuatho.navigation.MenuTarget.topLevel;

/**
 * Quét từng trang menu để lập danh mục chức năng và control hiển thị trực tiếp.
 */
public class CrossMenuPageFeatureInventoryTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(CrossMenuPageFeatureInventoryTest.class,
                "ERP Cross-menu Feature Discovery", "Read-only cross-menu UI feature inventory");
    }

    /**
     * Cho biết có tái sử dụng cùng một WebDriver giữa các phương thức test hay không.
     * @return kết quả reuse driver between test methods sau khi xử lý
     */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Thực hiện xử lý representative pages trong luồng kiểm thử.
     * @return kết quả representative pages sau khi xử lý
     */
    @DataProvider(name = "representativePages")
    public Object[][] representativePages() {
        return new Object[][]{
                {topLevel("Tài chính")},
                {childOf("Người Dùng", "Quản Lí Người Dùng")},
                {childOf("Đơn Dịch Vụ", "Đơn Khách - Thợ")},
                {childOf("Giao Dịch", "Lịch Sử Giao Dịch")},
                {childOf("Website", "Quản Lí Bài Viết Nội Bộ")},
                {childOf("Marketing", "Thống Kê Thợ - Khách")}
        };
    }

    /**
     * Thu thập visible features trong luồng kiểm thử.
     * @param target giá trị target được truyền vào
     */
    @Test(dataProvider = "representativePages")
    public void inventoryVisibleFeatures(MenuTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target, false);
        new UiFeatureExplorer(driver).printInventory(target.toString());
    }
}
