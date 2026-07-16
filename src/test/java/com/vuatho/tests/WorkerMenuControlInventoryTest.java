package com.vuatho.tests;

import com.vuatho.core.BaseTest;
import com.vuatho.core.TestNgRunner;
import com.vuatho.exploration.UiFeatureExplorer;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.MenuDestinationPage;
import com.vuatho.testdata.PartnerWorkerCase;
import com.vuatho.testdata.PartnerWorkerTestData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkerMenuControlInventoryTest extends BaseTest {
    public static void main(String[] args) {
        TestNgRunner.run(WorkerMenuControlInventoryTest.class,
                "ERP Worker Menu Discovery",
                "Inventory control cho các màn liên quan đến thợ");
    }

    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    @DataProvider(name = "partnerWorkerCases", parallel = false)
    public Object[][] partnerWorkerCases() {
        return PartnerWorkerTestData.dataProviderRows();
    }

    @Test(dataProvider = "partnerWorkerCases",
            groups = {"partner-worker", "discovery"},
            description = "Khám phá control trên màn Đối Tác - Thợ")
    public void inventoryWorkerMenuControls(PartnerWorkerCase testCase) {
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(testCase.page(), false);
        new UiFeatureExplorer(driver).printInventory(testCase.toString());
    }
}
