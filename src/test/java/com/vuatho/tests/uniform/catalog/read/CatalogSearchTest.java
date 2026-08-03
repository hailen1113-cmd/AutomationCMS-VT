package com.vuatho.tests.uniform.catalog.read;

import com.vuatho.core.TestNgRunner;
import com.vuatho.support.UniformModuleTestSupport;
import com.vuatho.testcases.UniformCatalogTestCases;
import com.vuatho.utils.TextNormalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Kiểm tra riêng chức năng tìm kiếm; mỗi tab có testcase và ID độc lập. */
public class CatalogSearchTest extends UniformModuleTestSupport {
    private static final String GROUP_TAB = "Nhóm Đồng Phục";
    private static final String ITEM_TAB = "Đồng Phục";

    public static void main(String[] args) {
        TestNgRunner.run(CatalogSearchTest.class,
                "Đồng phục", "Tìm kiếm danh mục");
    }

    /** Tìm chính xác tên card ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_007)
    public void exactGroupNameReturnsMatchingCards() {
        searchExactName(GROUP_TAB);
    }

    /** Tìm chính xác tên card ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_040)
    public void exactItemNameReturnsMatchingCards() {
        searchExactName(ITEM_TAB);
    }

    /** Từ khóa không tồn tại và Reset ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "reset", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_010)
    public void noMatchGroupSearchResetsData() {
        searchNoMatchThenReset(GROUP_TAB);
    }

    /** Từ khóa không tồn tại và Reset ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "reset", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_041)
    public void noMatchItemSearchResetsData() {
        searchNoMatchThenReset(ITEM_TAB);
    }

    /** Tìm bằng một phần tên ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_032)
    public void partialGroupNameReturnsMatchingCards() {
        searchPartialName(GROUP_TAB);
    }

    /** Tìm bằng một phần tên ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_042)
    public void partialItemNameReturnsMatchingCards() {
        searchPartialName(ITEM_TAB);
    }

    /** Tìm chữ hoa/chữ thường ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_033)
    public void groupSearchIsCaseInsensitive() {
        searchCaseInsensitive(GROUP_TAB);
    }

    /** Tìm chữ hoa/chữ thường ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_043)
    public void itemSearchIsCaseInsensitive() {
        searchCaseInsensitive(ITEM_TAB);
    }

    /** Xóa thủ công từ khóa ở tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "clear", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_034)
    public void clearingGroupSearchRestoresData() {
        clearSearchRestoresData(GROUP_TAB);
    }

    /** Xóa thủ công từ khóa ở tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "clear", "data-interaction"},
            description = UniformCatalogTestCases.UNI_CAT_044)
    public void clearingItemSearchRestoresData() {
        clearSearchRestoresData(ITEM_TAB);
    }

    /** Placeholder tìm kiếm của tab Nhóm Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "ui"},
            description = UniformCatalogTestCases.UNI_CAT_035)
    public void groupSearchPlaceholderIsCorrect() {
        assertSearchPlaceholder(GROUP_TAB, "Tìm kiếm nhóm đồng phục");
    }

    /** Placeholder tìm kiếm của tab Đồng Phục. */
    @Test(groups = {"uniform", "catalog", "search", "ui"},
            description = UniformCatalogTestCases.UNI_CAT_045)
    public void itemSearchPlaceholderIsCorrect() {
        assertSearchPlaceholder(ITEM_TAB, "Tìm kiếm đồng phục");
    }

    private void searchExactName(String tab) {
        catalogPage.open().selectTab(tab);
        String keyword = catalogPage.firstItemName();
        Assert.assertFalse(keyword.isBlank(), "Không lấy được tên item để tìm kiếm.");
        catalogPage.search(keyword);
        assertEveryNameContains(keyword, catalogPage.displayedItemNames());
    }

    private void searchNoMatchThenReset(String tab) {
        catalogPage.open().selectTab(tab);
        int before = catalogPage.totalDisplayed();
        String keyword = "__automation_uniform_no_match__";
        catalogPage.search(keyword);
        Assert.assertEquals(catalogPage.searchValue(), keyword,
                "Ô tìm kiếm không giữ đúng từ khóa đã nhập.");
        Assert.assertTrue(catalogPage.displayedItemNames().isEmpty(),
                "Tìm kiếm không khớp vẫn trả card ở tab " + tab);
        catalogPage.reset();
        Assert.assertTrue(catalogPage.searchValue().isBlank(),
                "Reset chưa xóa nội dung ô tìm kiếm.");
        Assert.assertEquals(catalogPage.totalDisplayed(), before,
                "Reset không phục hồi dữ liệu tab " + tab);
    }

    private void searchPartialName(String tab) {
        catalogPage.open().selectTab(tab);
        String keyword = partialKeyword(catalogPage.firstItemName());
        catalogPage.search(keyword);
        assertEveryNameContains(keyword, catalogPage.displayedItemNames());
    }

    private void searchCaseInsensitive(String tab) {
        catalogPage.open().selectTab(tab);
        String name = catalogPage.firstItemName();
        catalogPage.search(name.toUpperCase(Locale.forLanguageTag("vi")));
        assertEveryNameContains(name, catalogPage.displayedItemNames());
    }

    private void clearSearchRestoresData(String tab) {
        catalogPage.open().selectTab(tab);
        int before = catalogPage.totalDisplayed();
        catalogPage.search(catalogPage.firstItemName());
        catalogPage.clearSearchManually();
        Assert.assertTrue(catalogPage.searchValue().isBlank(),
                "Ô tìm kiếm vẫn còn dữ liệu sau khi xóa thủ công.");
        Assert.assertEquals(catalogPage.totalDisplayed(), before,
                "Xóa từ khóa thủ công không phục hồi danh sách.");
    }

    private void assertSearchPlaceholder(String tab, String expected) {
        catalogPage.open().selectTab(tab);
        Assert.assertEquals(catalogPage.searchPlaceholder(), expected,
                "Placeholder tìm kiếm không đúng tab " + tab);
    }

    private void assertEveryNameContains(String keyword, List<String> result) {
        Assert.assertFalse(result.isEmpty(),
                "Tìm kiếm không trả card dữ liệu cho từ khóa " + keyword);
        String normalizedKeyword = TextNormalizer.normalize(keyword);
        Assert.assertTrue(result.stream().allMatch(name ->
                        TextNormalizer.normalize(name).contains(normalizedKeyword)),
                "Kết quả có item không khớp từ khóa " + keyword + ": " + result);
    }

    private String partialKeyword(String name) {
        String longestWord = List.of(name.split("\\s+")).stream()
                .filter(word -> !word.isBlank())
                .max(Comparator.comparingInt(String::length))
                .orElse(name);
        int length = Math.min(longestWord.length(), Math.max(2, longestWord.length() / 2));
        return longestWord.substring(0, length);
    }
}
