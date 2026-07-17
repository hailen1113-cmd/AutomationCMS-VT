package com.vuatho.tests;

import com.vuatho.core.TestNgRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Xác nhận các thẻ tổng quan Dashboard hiển thị đầy đủ nhãn và giá trị sau khi tải.
 */
public class DashboardSummaryCardsTest extends DashboardTestSupport {
    public static void main(String[] args) {
        TestNgRunner.run(DashboardSummaryCardsTest.class,
                "Bo test card tong quan Dashboard ERP",
                "Kiem tra card tong quan Dashboard");
    }

    /**
     * Thực thi test “CMS-DASH-CARDS-001: Card tong quan Dashboard dieu huong va tai thanh cong” và xác nhận kết quả theo yêu cầu nghiệp vụ.
     */
    @Test(description = "CMS-DASH-CARDS-001: Card tong quan Dashboard dieu huong va tai thanh cong")
    public void dashboardSummaryCardsLoadDestinations() {
        List<String> cards = dashboard.clickSummaryCardsAndWaitForDestinations();

        Assert.assertFalse(cards.isEmpty(), "Khong tai duoc card tong quan Dashboard nao.");
    }
}
