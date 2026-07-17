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

/**
 * Khám phá và in danh mục control trên các màn hình thuộc miền Đối tác - Thợ.
 *
 * <p>Đây là test discovery: mỗi bộ dữ liệu trỏ đến một menu, sau đó
 * {@link UiFeatureExplorer} ghi nhận các input, button, link và control tương tác
 * để hỗ trợ thiết kế test chức năng.</p>
 */
public class WorkerMenuControlInventoryTest extends BaseTest {
    /** Cho phép chạy trực tiếp test discovery từ IDE. */
    public static void main(String[] args) {
        TestNgRunner.run(WorkerMenuControlInventoryTest.class,
                "ERP Worker Menu Discovery",
                "Inventory control cho các màn liên quan đến thợ");
    }

    /** Tái sử dụng trình duyệt để không phải đăng nhập lại bằng một WebDriver mới cho mỗi menu. */
    @Override
    protected boolean reuseDriverBetweenTestMethods() {
        return true;
    }

    /**
     * Chuyển catalog màn hình thợ thành các dòng dữ liệu TestNG.
     * Chạy tuần tự để nhiều test không điều khiển chung một trình duyệt đồng thời.
     */
    @DataProvider(name = "partnerWorkerCases", parallel = false)
    public Object[][] partnerWorkerCases() {
        return PartnerWorkerTestData.dataProviderRows();
    }

    /**
     * Đăng nhập, mở menu được chỉ định và in toàn bộ control có thể nhận diện.
     *
     * @param testCase mô tả menu/trang thợ cần khám phá
     */
    @Test(dataProvider = "partnerWorkerCases",
            groups = {"partner-worker", "discovery"},
            description = "Khám phá control trên màn Đối Tác - Thợ")
    public void inventoryWorkerMenuControls(PartnerWorkerCase testCase) {
        // Mỗi case chủ động khôi phục điểm bắt đầu hợp lệ trước khi điều hướng.
        new AuthenticationFlow(driver).openApplicationAndLogin();
        new MenuDestinationPage(driver).openAndWaitUntilLoaded(testCase.page(), false);
        // Kết quả inventory được in với tên case để dễ đối chiếu trong log TestNG.
        new UiFeatureExplorer(driver).printInventory(testCase.toString());
    }
}
