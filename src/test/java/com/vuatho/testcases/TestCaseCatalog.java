package com.vuatho.testcases;

import com.vuatho.listeners.TestGroupConventionTransformer;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Central catalog for business TestNG testcases under {@code com.vuatho.tests}.
 *
 * <p>The readable {@code *TestCases.java} constants own each fixed ID and
 * scenario. Business {@code @Test} methods reference those constants directly.
 * This class reads the resulting annotation metadata only to generate reports;
 * synchronization is enforced by {@link TestCaseCatalogValidationTest}.</p>
 *
 * <p>Open this file and run {@link #main(String[])} from the IDE to view the
 * complete list. It also writes readable TXT and CSV reports under
 * {@code target/reports/}.</p>
 */
public final class TestCaseCatalog {
    private static final String BUSINESS_TEST_PACKAGE = "com.vuatho.tests";
    private static final Path REPORT_DIRECTORY = Path.of("target", "reports");
    private static final Path TXT_REPORT =
            REPORT_DIRECTORY.resolve("test-case-catalog.txt");
    private static final Path CSV_REPORT =
            REPORT_DIRECTORY.resolve("test-case-catalog.csv");
    private static final Pattern EXPLICIT_ID = Pattern.compile(
            "^\\s*([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*-\\d{3})\\b");
    private static final Pattern LEADING_ID = Pattern.compile(
            "^\\s*[A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*-\\d{3}\\s*[:\\-–—]?\\s*");

    private TestCaseCatalog() {
    }

    /**
     * Returns the whole catalog, sorted by module, feature, ID and mapping.
     */
    public static List<TestCaseDefinition> all() {
        return CatalogHolder.ALL;
    }

    private static List<TestCaseDefinition> buildCatalog() {
        return discoverTestMethods().stream()
                .map(TestCaseCatalog::definition)
                .sorted(Comparator.comparing(TestCaseDefinition::module)
                        .thenComparing(TestCaseDefinition::feature)
                        .thenComparing(TestCaseDefinition::id)
                        .thenComparing(TestCaseDefinition::mappingKey))
                .toList();
    }

    public static Optional<TestCaseDefinition> findById(String id) {
        return all().stream().filter(testCase -> testCase.id().equals(id)).findFirst();
    }

    public static Optional<TestCaseDefinition> findByMethod(
            Class<?> declaringClass, String methodName) {
        String mapping = declaringClass.getName() + "#" + methodName;
        return all().stream()
                .filter(testCase -> testCase.mappingKey().equals(mapping))
                .findFirst();
    }

    public static Map<String, Long> countByModule() {
        return all().stream().collect(Collectors.groupingBy(
                TestCaseDefinition::module,
                TreeMap::new,
                Collectors.counting()));
    }

    /**
     * Independent mapping view used by the validation test.
     */
    public static Set<String> discoveredAutomationMappings() {
        return discoverTestMethods().stream()
                .map(TestCaseCatalog::mappingKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static void writeReports() {
        try {
            Files.createDirectories(REPORT_DIRECTORY);
            Files.writeString(
                    TXT_REPORT,
                    textReport(all()),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(
                    CSV_REPORT,
                    csvReport(all()),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot write testcase catalog reports.", error);
        }
    }

    /**
     * Run this method from the IDE whenever you want to view the full catalog.
     */
    public static void main(String[] args) {
        List<TestCaseDefinition> catalog = all();
        writeReports();
        System.out.println(textReport(catalog));
        System.out.println("Catalog entries : " + catalog.size());
        System.out.println("By module       : " + countByModule());
        System.out.println("TXT report      : " + TXT_REPORT.toAbsolutePath());
        System.out.println("CSV report      : " + CSV_REPORT.toAbsolutePath());
    }

    private static List<Method> discoverTestMethods() {
        List<Method> methods = new ArrayList<>();
        for (Class<?> type : discoverProjectClasses()) {
            Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Test.class))
                    .forEach(methods::add);
        }
        methods.sort(Comparator.comparing(TestCaseCatalog::canonicalMethodKey));
        return List.copyOf(methods);
    }

    private static Set<Class<?>> discoverProjectClasses() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String resourceName = BUSINESS_TEST_PACKAGE.replace('.', '/');
        LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();
        try {
            Enumeration<URL> roots = loader.getResources(resourceName);
            while (roots.hasMoreElements()) {
                URL root = roots.nextElement();
                if (!"file".equalsIgnoreCase(root.getProtocol())) {
                    continue;
                }
                discoverClassesFromDirectory(Path.of(root.toURI()), loader, classes);
            }
        } catch (IOException | URISyntaxException error) {
            throw new IllegalStateException("Cannot scan compiled testcase classes.", error);
        }
        return Collections.unmodifiableSet(classes);
    }

    private static void discoverClassesFromDirectory(
            Path packageRoot,
            ClassLoader loader,
            Set<Class<?>> classes) throws IOException {
        try (var paths = Files.walk(packageRoot)) {
            for (Path classFile : paths
                    .filter(path -> path.toString().endsWith(".class"))
                    .filter(path -> !path.getFileName().toString().contains("$"))
                    .toList()) {
                String relative = packageRoot.relativize(classFile).toString()
                        .replace('\\', '.')
                        .replace('/', '.')
                        .replaceAll("\\.class$", "");
                String className = BUSINESS_TEST_PACKAGE + "." + relative;
                try {
                    classes.add(Class.forName(className, false, loader));
                } catch (ClassNotFoundException | LinkageError error) {
                    throw new IllegalStateException(
                            "Cannot load compiled class while building catalog: " + className,
                            error);
                }
            }
        }
    }

    private static TestCaseDefinition definition(Method method) {
        Test annotation = method.getAnnotation(Test.class);
        Set<String> effectiveGroups = TestGroupConventionTransformer.groupsFor(
                method.getDeclaringClass(), annotation.groups());
        Classification classification = classify(method);
        String scenario = scenario(annotation.description(), method.getName());
        return new TestCaseDefinition(
                requireExplicitId(method, annotation.description()),
                classification.module(),
                classification.feature(),
                scenario,
                severity(effectiveGroups),
                testType(effectiveGroups),
                method.getDeclaringClass().getName(),
                method.getName(),
                annotation.enabled() ? "Automated" : "Disabled",
                effectiveGroups.stream().sorted().toList());
    }

    private static Classification classify(Method method) {
        Class<?> type = method.getDeclaringClass();
        String className = type.getSimpleName();
        String packageName = type.getPackageName();
        if (!packageName.startsWith(BUSINESS_TEST_PACKAGE + ".")) {
            throw new IllegalArgumentException(
                    "Catalog only accepts business tests: " + type.getName());
        }
        String remainder = packageName.substring((BUSINESS_TEST_PACKAGE + ".").length());
        String[] parts = remainder.split("\\.");
        String moduleKey = parts[0];
        String featureKey = parts.length > 1
                ? parts[1]
                : inferredFeature(className, moduleKey);
        return classification(moduleKey, featureKey);
    }

    private static Classification classification(String moduleKey, String featureKey) {
        String module = switch (moduleKey) {
            case "customerworkerorder" -> "Customer Worker Order";
            case "crossmenu" -> "Cross Menu";
            case "dashboard" -> "Dashboard";
            case "ekyc" -> "eKYC";
            case "smoke" -> "Smoke";
            case "uniform" -> "Uniform";
            case "userprofile" -> "User Profile";
            case "workermenu" -> "Worker Menu";
            case "workerpost" -> "Worker Post";
            case "workerprofile" -> "Worker Profile";
            case "workerstoprequest" -> "Worker Stop Request";
            case "workertestmanagement" -> "Worker Test Management";
            case "workerviolation" -> "Worker Violation";
            default -> humanize(moduleKey);
        };
        String feature = humanize(featureKey);
        return new Classification(module, feature);
    }

    private static String inferredFeature(String className, String moduleKey) {
        String value = className.replaceFirst("Test$", "");
        Map<String, String> prefixes = new LinkedHashMap<>();
        prefixes.put("customerworkerorder", "CustomerWorkerOrder");
        prefixes.put("crossmenu", "CrossMenu");
        prefixes.put("dashboard", "Dashboard");
        prefixes.put("ekyc", "Ekyc");
        prefixes.put("uniform", "Uniform");
        prefixes.put("userprofile", "UserProfile");
        prefixes.put("workermenu", "WorkerMenu");
        prefixes.put("workerpost", "WorkerPost");
        prefixes.put("workerprofile", "WorkerProfile");
        prefixes.put("workerstoprequest", "WorkerStopRequest");
        prefixes.put("workertestmanagement", "WorkerTestManagement");
        prefixes.put("workerviolation", "WorkerViolation");
        String prefix = prefixes.getOrDefault(moduleKey, "");
        if (!prefix.isBlank() && value.startsWith(prefix)) {
            value = value.substring(prefix.length());
        }
        return value.isBlank() ? "General" : value;
    }

    private static String requireExplicitId(Method method, String description) {
        Matcher explicit = EXPLICIT_ID.matcher(description);
        if (explicit.find()) {
            return explicit.group(1);
        }
        throw new IllegalStateException(
                "Business @Test description must start with a fixed Test Case ID: "
                        + mappingKey(method));
    }

    private static String scenario(String description, String methodName) {
        String value = LEADING_ID.matcher(description == null ? "" : description)
                .replaceFirst("")
                .trim();
        return value.isBlank() ? humanize(methodName) : value;
    }

    private static String severity(Set<String> groups) {
        if (groups.contains("critical")) {
            return "Critical";
        }
        if (groups.contains("high") || groups.contains("smoke")
                || groups.contains("security") || groups.contains("mutation")) {
            return "High";
        }
        if (groups.contains("low")) {
            return "Low";
        }
        return "Medium";
    }

    private static String testType(Set<String> groups) {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        addType(groups, types, "smoke", "Smoke");
        addType(groups, types, "regression", "Regression");
        addType(groups, types, "api", "API");
        addType(groups, types, "mutation", "Mutation");
        addType(groups, types, "catalog", "Catalog");
        if (types.isEmpty()) {
            types.add("Functional");
        }
        return String.join(", ", types);
    }

    private static void addType(
            Set<String> groups, Set<String> types, String group, String label) {
        if (groups.contains(group)) {
            types.add(label);
        }
    }

    private static String mappingKey(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    private static String canonicalMethodKey(Method method) {
        String parameters = Arrays.stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(Collectors.joining(","));
        return mappingKey(method) + "(" + parameters + ")";
    }

    private static String humanize(String value) {
        if (value == null || value.isBlank()) {
            return "General";
        }
        String spaced = value.replace('_', ' ').replace('-', ' ')
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceAll("\\s+", " ")
                .trim();
        if (spaced.isBlank()) {
            return "General";
        }
        return Arrays.stream(spaced.split(" "))
                .map(word -> word.length() <= 3 && word.equals(word.toUpperCase(Locale.ROOT))
                        ? word
                        : Character.toUpperCase(word.charAt(0))
                                + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    private static String textReport(List<TestCaseDefinition> catalog) {
        StringBuilder report = new StringBuilder(
                "ID | Module | Feature | Scenario | Severity | Test Type | Class | Method | Status")
                .append(System.lineSeparator());
        for (TestCaseDefinition testCase : catalog) {
            report.append(reportValue(testCase.id())).append(" | ")
                    .append(reportValue(testCase.module())).append(" | ")
                    .append(reportValue(testCase.feature())).append(" | ")
                    .append(reportValue(testCase.scenario())).append(" | ")
                    .append(reportValue(testCase.severity())).append(" | ")
                    .append(reportValue(testCase.testType())).append(" | ")
                    .append(reportValue(testCase.className())).append(" | ")
                    .append(reportValue(testCase.methodName())).append(" | ")
                    .append(reportValue(testCase.automationStatus()))
                    .append(System.lineSeparator());
        }
        return report.toString();
    }

    private static String csvReport(List<TestCaseDefinition> catalog) {
        String header = "ID,Module,Feature,Scenario,Severity,Test Type,Class,"
                + "Method,Status,Groups";
        return catalog.stream()
                .map(testCase -> List.of(
                                testCase.id(),
                                testCase.module(),
                                testCase.feature(),
                                testCase.scenario(),
                                testCase.severity(),
                                testCase.testType(),
                                testCase.className(),
                                testCase.methodName(),
                                testCase.automationStatus(),
                                String.join("|", testCase.groups())).stream()
                        .map(TestCaseCatalog::csv)
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining(
                        System.lineSeparator(),
                        header + System.lineSeparator(),
                        System.lineSeparator()));
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String reportValue(String value) {
        return value.replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "/")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record Classification(String module, String feature) {
    }

    private static final class CatalogHolder {
        private static final List<TestCaseDefinition> ALL = buildCatalog();

        private CatalogHolder() {
        }
    }
}
