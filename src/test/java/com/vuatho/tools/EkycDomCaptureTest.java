package com.vuatho.tools;

import com.vuatho.core.BaseTest;
import com.vuatho.flows.AuthenticationFlow;
import com.vuatho.pages.EkycPage;
import com.vuatho.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Thu thập bằng chứng kỹ thuật của màn hình eKYC ở nhiều trạng thái giao diện.
 *
 * <p>Công cụ lưu DOM trang chính, DOM khi mở bộ lọc, DOM drawer hồ sơ (nếu có dữ liệu),
 * danh sách tài nguyên mạng và khóa browser storage vào {@code target/analysis/ekyc}.</p>
 */
public class EkycDomCaptureTest extends BaseTest {
    // Locator tối thiểu chỉ dùng để thay đổi trạng thái UI trước khi chụp DOM.
    private static final By FILTER_BUTTON = By.cssSelector("button[title='Filter']");
    private static final By TABLE_ROWS = By.cssSelector(
            "table[aria-label='Table about eKYC Management'] tbody tr[role='row']");

    /**
     * Đăng nhập, mở trang eKYC và chụp lần lượt các trạng thái cùng dữ liệu chẩn đoán.
     *
     * @throws IOException khi không thể tạo hoặc ghi các artifact phân tích
     */
    @Test(description = "Capture live eKYC DOM from https://erp-sandbox.vuatho.com/vuatho/ekyc")
    public void captureEkycOuterHtml() throws IOException {
        // Xác thực trước khi điều hướng trực tiếp để tránh chụp nhầm trang đăng nhập.
        LoginPage loginPage = new AuthenticationFlow(driver).openApplicationAndLogin();
        Assert.assertTrue(loginPage.isDashboardVisible(Duration.ofSeconds(20)),
                "Cannot authenticate before opening eKYC page.");

        EkycPage ekycPage = new EkycPage(driver).openDirectly();
        Assert.assertTrue(driver.getCurrentUrl().contains("/vuatho/ekyc"),
                "eKYC route must be active before capturing DOM.");

        Path outputDirectory = Path.of("target", "analysis", "ekyc");
        Files.createDirectories(outputDirectory);

        // Ảnh chụp 1: trạng thái danh sách eKYC ban đầu.
        capture(outputDirectory.resolve("01-ekyc-page.html"));

        // Ảnh chụp 2: trạng thái bộ lọc mở, nếu môi trường cung cấp nút Filter.
        openFilterIfAvailable();
        capture(outputDirectory.resolve("02-ekyc-filter-open.html"));

        // Ảnh chụp 3 chỉ tồn tại khi danh sách có ít nhất một hồ sơ để mở drawer.
        if (!ekycPage.visibleRows().isEmpty()) {
            ekycPage.openFirstDrawer();
            capture(outputDirectory.resolve("03-ekyc-drawer-open.html"));
        }
        captureNetworkResources(outputDirectory.resolve("04-ekyc-network-resources.txt"));
        captureBrowserStorageKeys(outputDirectory.resolve("05-browser-storage-keys.txt"));

        System.out.println("[EKYC DOM CAPTURED] " + outputDirectory.toAbsolutePath());
    }

    /** Mở panel bộ lọc khi nút tồn tại và chờ tài liệu trình duyệt ổn định. */
    private void openFilterIfAvailable() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        if (driver.findElements(FILTER_BUTTON).stream().noneMatch(WebElement::isDisplayed)) {
            return;
        }
        wait.until(ExpectedConditions.elementToBeClickable(FILTER_BUTTON)).click();
        wait.until(webDriver -> "complete".equals(((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")));
        sleepBriefly();
    }

    /** Ghi source HTML hiện tại và xác nhận artifact không rỗng. */
    private void capture(Path output) throws IOException {
        Files.writeString(output, driver.getPageSource(), StandardCharsets.UTF_8);
        Assert.assertTrue(Files.size(output) > 0, "Captured DOM must not be empty: " + output);
        System.out.println("[DOM] " + output.toAbsolutePath() + " | bytes=" + Files.size(output));
    }

    /** Xuất URL của mọi tài nguyên mà Performance API đã ghi nhận. */
    private void captureNetworkResources(Path output) throws IOException {
        Object resources = ((JavascriptExecutor) driver).executeScript(
                "return performance.getEntriesByType('resource')"
                        + ".map(entry => entry.name)"
                        + ".filter(Boolean)"
                        + ".sort()"
                        + ".join('\\n');");
        Files.writeString(output, String.valueOf(resources), StandardCharsets.UTF_8);
        Assert.assertTrue(Files.size(output) > 0, "Captured resource list must not be empty: " + output);
        System.out.println("[RESOURCES] " + output.toAbsolutePath() + " | bytes=" + Files.size(output));
    }

    /**
     * Xuất tên khóa và tối đa 80 ký tự đầu của giá trị trong local/session storage.
     * Việc giới hạn độ dài giúp artifact đủ để nhận diện dữ liệu mà không quá lớn.
     */
    private void captureBrowserStorageKeys(Path output) throws IOException {
        Object keys = ((JavascriptExecutor) driver).executeScript(
                "const dump = store => Array.from({length: store.length}, (_, i) => {"
                        + " const key = store.key(i);"
                        + " const value = store.getItem(key) || '';"
                        + " return `${key}=${value.slice(0, 80)}`;"
                        + "});"
                        + "return ['[localStorage]', ...dump(localStorage), '[sessionStorage]',"
                        + " ...dump(sessionStorage)].join('\\n');");
        Files.writeString(output, String.valueOf(keys), StandardCharsets.UTF_8);
        System.out.println("[STORAGE] " + output.toAbsolutePath() + " | bytes=" + Files.size(output));
    }

    /** Chờ ngắn để animation hoặc render bất đồng bộ hoàn tất trước khi chụp. */
    private void sleepBriefly() {
        try {
            Thread.sleep(Duration.ofSeconds(2).toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for eKYC DOM state.", exception);
        }
    }
}
