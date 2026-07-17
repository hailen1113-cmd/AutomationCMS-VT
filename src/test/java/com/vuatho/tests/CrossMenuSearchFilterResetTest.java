package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.pages.ReadOnlyFeaturesPage;
import com.vuatho.testdata.FilterCatalog;
import com.vuatho.testdata.FilterTarget;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Xác nhận tìm kiếm, áp dụng bộ lọc và reset hoạt động nhất quán trên nhiều menu.
 */
public class CrossMenuSearchFilterResetTest extends BaseTest {
    private static final String NO_MATCH_QUERY = "__automation_no_match__";

    /**
     * Cho phép chạy trực tiếp lớp này từ IDE mà không cần cấu hình TestNG XML.
     * @param args các tham số dòng lệnh
     */
    public static void main(String[] args) {
        TestNgRunner.run(CrossMenuSearchFilterResetTest.class,
                "ERP Cross-menu Filter Suite", "Cross-menu Search Filter and Reset Tests");
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
     * Thực hiện xử lý search filters trong luồng kiểm thử.
     * @return kết quả search filters sau khi xử lý
     */
    @DataProvider(name = "searchFilters", parallel = false)
    public Object[][] searchFilters() {
        return FilterCatalog.searchFilterRows();
    }

    /**
     * Thực thi test “CMS-FILTER: Search filter waits for results and can be reset” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     * @param target giá trị target được truyền vào
     */
    @Test(dataProvider = "searchFilters",
            description = "CMS-FILTER: Search filter waits for results and can be reset")
    public void searchFilterCanBeAppliedAndReset(FilterTarget target) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(target.page(), false);

        ReadOnlyFeaturesPage features = new ReadOnlyFeaturesPage(driver);
        features.searchAndReset(target.placeholder(), NO_MATCH_QUERY);

        Assert.assertTrue(features.inputIsEmpty(target.placeholder()),
                "Reset không xóa filter: " + target);
        System.out.println("[FILTER PASS] " + target);
    }
}
