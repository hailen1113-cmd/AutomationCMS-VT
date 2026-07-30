package com.vuatho.testcases;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Prevents the centralized catalog from missing or duplicating business tests.
 */
public class TestCaseCatalogValidationTest {
    private static final java.util.regex.Pattern FIXED_ID =
            java.util.regex.Pattern.compile(
                    "^\\s*[A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*-\\d{3}\\s+-\\s+.+");
    private static final Pattern MANAGED_REFERENCE = Pattern.compile(
            "description\\s*=\\s*"
                    + "([A-Za-z][A-Za-z0-9]*TestCases\\.[A-Z][A-Z0-9_]*)");

    @Test(description = "Catalog contains every business TestNG @Test method")
    public void catalogContainsEveryAutomationTest() {
        Set<String> discovered = TestCaseCatalog.discoveredAutomationMappings();
        Set<String> catalogMappings = TestCaseCatalog.all().stream()
                .map(TestCaseDefinition::mappingKey)
                .collect(Collectors.toSet());

        Assert.assertEquals(catalogMappings, discovered,
                "Catalog mappings differ from business @Test methods.");
    }

    @Test(description = "Catalog IDs and class-method mappings are globally unique")
    public void catalogIdsAndMappingsAreUnique() {
        List<TestCaseDefinition> catalog = TestCaseCatalog.all();
        Assert.assertEquals(
                catalog.stream().map(TestCaseDefinition::id).distinct().count(),
                (long) catalog.size(),
                "Duplicate testcase ID found.");
        Assert.assertEquals(
                catalog.stream().map(TestCaseDefinition::mappingKey).distinct().count(),
                (long) catalog.size(),
                "Duplicate class + method mapping found.");
    }

    @Test(description = "Every catalog entry has complete required metadata")
    public void catalogMetadataIsComplete() {
        for (TestCaseDefinition testCase : TestCaseCatalog.all()) {
            Assert.assertFalse(testCase.id().isBlank(), "Blank ID: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.module().isBlank(), "Blank module: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.feature().isBlank(), "Blank feature: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.scenario().isBlank(), "Blank scenario: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.severity().isBlank(), "Blank severity: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.testType().isBlank(), "Blank test type: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.className().isBlank(), "Blank class: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.methodName().isBlank(), "Blank method: " + testCase.mappingKey());
            Assert.assertFalse(
                    testCase.automationStatus().isBlank(),
                    "Blank automation status: " + testCase.mappingKey());
            Assert.assertTrue(
                    testCase.className().startsWith("com.vuatho.tests."),
                    "Non-business test leaked into catalog: " + testCase.mappingKey());
        }
    }

    @Test(description = "Every business @Test declares its fixed ID in description")
    public void everyBusinessTestDeclaresFixedIdInDescription() throws Exception {
        for (TestCaseDefinition testCase : TestCaseCatalog.all()) {
            Class<?> type = Class.forName(testCase.className());
            Method method = java.util.Arrays.stream(type.getDeclaredMethods())
                    .filter(candidate -> candidate.getName().equals(testCase.methodName()))
                    .filter(candidate -> candidate.isAnnotationPresent(Test.class))
                    .findFirst()
                    .orElseThrow();
            String description = method.getAnnotation(Test.class).description();
            Assert.assertTrue(
                    FIXED_ID.matcher(description).matches(),
                    "Description must be 'ID - Scenario': " + testCase.mappingKey()
                            + " | actual=" + description);
        }
    }

    @Test(description = "Java TestCases lists and business @Test annotations are synchronized")
    public void managedJavaListsAndBusinessAnnotationsAreSynchronized() throws Exception {
        Map<String, String> managed = managedConstants();
        Map<String, Long> references = managedReferences();

        Assert.assertEquals(
                references.keySet(),
                managed.keySet(),
                "Every managed constant must be used by exactly one business @Test, "
                        + "and every business @Test must use a managed constant.");
        Assert.assertTrue(
                references.values().stream().allMatch(count -> count == 1L),
                "A managed testcase constant is referenced more than once: " + references);
        Assert.assertEquals(
                references.size(),
                TestCaseCatalog.all().size(),
                "Managed @Test reference count differs from the business catalog.");
        Assert.assertEquals(
                managed.values().stream().distinct().count(),
                (long) managed.size(),
                "Managed testcase descriptions must be unique.");

        Set<String> annotationDescriptions = TestCaseCatalog.all().stream()
                .map(this::annotationDescription)
                .collect(Collectors.toSet());
        Assert.assertEquals(
                annotationDescriptions,
                Set.copyOf(managed.values()),
                "Runtime @Test descriptions differ from the readable Java testcase lists.");
    }

    @Test(description = "Every catalog mapping resolves to an actual annotated method")
    public void catalogMappingsResolveToAnnotatedMethods() throws Exception {
        for (TestCaseDefinition testCase : TestCaseCatalog.all()) {
            Class<?> type = Class.forName(testCase.className());
            Method method = java.util.Arrays.stream(type.getDeclaredMethods())
                    .filter(candidate -> candidate.getName().equals(testCase.methodName()))
                    .filter(candidate -> candidate.isAnnotationPresent(Test.class))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(
                            "No @Test method for " + testCase.mappingKey()));
            Assert.assertNotNull(method.getAnnotation(Test.class));
        }
    }

    @Test(description = "Catalog reports can be generated as TXT and CSV")
    public void catalogReportsCanBeGenerated() {
        TestCaseCatalog.writeReports();
        java.nio.file.Path txt =
                java.nio.file.Path.of("target", "reports", "test-case-catalog.txt");
        java.nio.file.Path csv =
                java.nio.file.Path.of("target", "reports", "test-case-catalog.csv");
        Assert.assertTrue(java.nio.file.Files.isRegularFile(txt));
        Assert.assertTrue(java.nio.file.Files.isRegularFile(csv));
        try {
            Assert.assertEquals(
                    java.nio.file.Files.readAllLines(txt).get(0),
                    "ID | Module | Feature | Scenario | Severity | Test Type | Class | Method | Status");
            Assert.assertEquals(
                    java.nio.file.Files.readAllLines(csv).get(0),
                    "ID,Module,Feature,Scenario,Severity,Test Type,Class,Method,Status,Groups");
        } catch (java.io.IOException error) {
            throw new AssertionError("Cannot read generated catalog reports.", error);
        }
    }

    private Map<String, String> managedConstants() throws Exception {
        Path root = Path.of("src", "test", "java", "com", "vuatho", "testcases");
        Map<String, String> managed = new LinkedHashMap<>();
        try (var paths = Files.list(root)) {
            for (Path source : paths
                    .filter(path -> path.getFileName().toString().endsWith("TestCases.java"))
                    .sorted()
                    .toList()) {
                String simpleName = source.getFileName().toString().replaceFirst("\\.java$", "");
                Class<?> type = Class.forName("com.vuatho.testcases." + simpleName);
                for (Field field : type.getDeclaredFields()) {
                    if (field.getType() != String.class
                            || !Modifier.isPublic(field.getModifiers())
                            || !Modifier.isStatic(field.getModifiers())
                            || !Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    String reference = simpleName + "." + field.getName();
                    String previous = managed.put(reference, (String) field.get(null));
                    Assert.assertNull(previous, "Duplicate managed constant: " + reference);
                }
            }
        }
        Assert.assertFalse(managed.isEmpty(), "No readable *TestCases.java lists found.");
        return managed;
    }

    private Map<String, Long> managedReferences() throws IOException {
        Path root = Path.of("src", "test", "java", "com", "vuatho", "tests");
        Map<String, Long> references = new LinkedHashMap<>();
        try (var paths = Files.walk(root)) {
            for (Path source : paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList()) {
                Matcher matcher = MANAGED_REFERENCE.matcher(Files.readString(source));
                while (matcher.find()) {
                    references.merge(matcher.group(1), 1L, Long::sum);
                }
            }
        }
        return references;
    }

    private String annotationDescription(TestCaseDefinition testCase) {
        try {
            Class<?> type = Class.forName(testCase.className());
            return java.util.Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.getName().equals(testCase.methodName()))
                    .filter(method -> method.isAnnotationPresent(Test.class))
                    .findFirst()
                    .orElseThrow()
                    .getAnnotation(Test.class)
                    .description();
        } catch (ClassNotFoundException error) {
            throw new AssertionError("Cannot load " + testCase.mappingKey(), error);
        }
    }
}
