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

public class FilterBehaviorTest extends BaseTest {
    private static final String NO_MATCH_QUERY = "__automation_no_match__";

    public static void main(String[] args) {
        TestNgRunner.run(FilterBehaviorTest.class,
                "ERP Filter Suite", "Search Filter and Reset Tests");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @DataProvider(name = "searchFilters", parallel = false)
    public Object[][] searchFilters() {
        return FilterCatalog.searchFilterRows();
    }

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
