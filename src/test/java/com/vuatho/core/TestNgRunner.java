package com.vuatho.core;

import com.vuatho.reporting.ConsoleTestListener;
import com.vuatho.tests.conditions.LogoutTestsEnabled;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.util.Arrays;

public final class TestNgRunner {
    private TestNgRunner() {
    }

    public static void run(Class<?> testClass, String suiteName, String testName) {
        run(suiteName, testName, testClass);
    }

    public static void run(String suiteName, String testName, Class<?>... testClasses) {
        // Mặc định mở Chrome thật để dễ quan sát, nhưng vẫn quit sau test cuối cùng.
        // Có thể ghi đè từ Maven/IDE bằng -Dheadless, -Dinteractive hoặc -Dkeep.browser.open.
        System.setProperty("headless", System.getProperty("headless", "false"));
        System.setProperty("interactive", System.getProperty("interactive", "true"));
        System.setProperty("pause.on.failure", System.getProperty("pause.on.failure", "false"));
        System.setProperty("capture.screenshots", System.getProperty("capture.screenshots", "false"));
        System.setProperty("keep.browser.open", System.getProperty("keep.browser.open", "false"));

        TestNG testNG = new TestNG();
        testNG.setDefaultSuiteName(suiteName);
        testNG.setDefaultTestName(testName);
        testNG.setTestClasses(testClasses);
        // Giữ đúng thứ tự class được truyền vào để login/setup chạy trước các flow load page.
        testNG.setPreserveOrder(true);
        // Nếu một @BeforeMethod/@AfterMethod lỗi ở một testcase, vẫn tiếp tục chạy các testcase còn lại.
        testNG.setConfigFailurePolicy(XmlSuite.FailurePolicy.CONTINUE);
        // Test logout mặc định bị tắt vì nó sẽ làm mất session đăng nhập của các test phía sau.
        testNG.addListener(new LogoutTestsEnabled());
        // Listener này chỉ quit browser khi toàn bộ TestNG run đã kết thúc.
        testNG.addListener(new DriverLifecycleListener());
        // Listener này in tiến trình và kết quả từng test method ra console cho dễ kiểm tra.
        testNG.addListener(new ConsoleTestListener());
        System.out.println("Running TestNG classes: " + Arrays.toString(testClasses));
        testNG.run();

        // Nếu TestNG có test fail thì main process cũng phải fail để IDE/CI báo lỗi thật.
        if (testNG.hasFailure() || testNG.hasFailureWithinSuccessPercentage()) {
            throw new IllegalStateException("TestNG execution failed.");
        }
    }
}
