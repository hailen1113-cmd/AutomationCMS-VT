package com.vuatho.hub;

import com.vuatho.hub.TestCaseHubModels.CatalogEntry;
import com.vuatho.hub.TestCaseHubModels.Implementation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Đọc catalog và mapping @Test từ mã nguồn, không sinh ID. */
final class TestCaseCatalogScanner {
    private static final Pattern CATALOG_CONSTANT = Pattern.compile(
            "public static final String \\w+\\s*=\\s*\"([^\"]+)\"");
    private static final Pattern TEST_METHOD = Pattern.compile(
            "@Test\\s*\\((.*?)\\)\\s*(?:/\\*\\*(.*?)\\*/\\s*)?(?:public|protected|private)?\\s+\\w+\\s+(\\w+)\\s*\\(",
            Pattern.DOTALL);
    private static final Pattern DESCRIPTION_CONST = Pattern.compile(
            "([A-Za-z0-9_]+TestCases)\\.([A-Z0-9_]+)");
    private static final Pattern DESCRIPTION_STRING = Pattern.compile(
            "description\\s*=\\s*\"([^\"]+)\"");

    private final Path sourceRoot;

    TestCaseCatalogScanner(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    List<CatalogEntry> catalog() {
        Path catalogDir = sourceRoot.resolve("com/vuatho/testcases");
        List<CatalogEntry> entries = new ArrayList<>();
        if (!Files.isDirectory(catalogDir)) {
            return entries;
        }
        try (Stream<Path> files = Files.list(catalogDir)) {
            files.filter(path -> path.getFileName().toString().endsWith("TestCases.java"))
                    .sorted()
                    .forEach(path -> entries.addAll(readCatalogFile(path)));
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được thư mục catalog testcase.", exception);
        }
        return entries;
    }

    Map<String, Implementation> implementations(Map<String, CatalogEntry> catalogByConst) {
        Map<String, Implementation> mapped = new LinkedHashMap<>();
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            files.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().endsWith("TestCases.java"))
                    .forEach(path -> readImplementations(path, catalogByConst, mapped));
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được các file test.", exception);
        }
        return mapped;
    }

    Map<String, CatalogEntry> catalogByConstantName() {
        Map<String, CatalogEntry> byConst = new LinkedHashMap<>();
        Path catalogDir = sourceRoot.resolve("com/vuatho/testcases");
        if (!Files.isDirectory(catalogDir)) {
            return byConst;
        }
        try (Stream<Path> files = Files.list(catalogDir)) {
            files.filter(path -> path.getFileName().toString().endsWith("TestCases.java"))
                    .forEach(path -> readConstants(path, byConst));
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được constant catalog.", exception);
        }
        return byConst;
    }

    private List<CatalogEntry> readCatalogFile(Path path) {
        List<CatalogEntry> entries = new ArrayList<>();
        Matcher matcher = CATALOG_CONSTANT.matcher(read(path));
        while (matcher.find()) {
            String value = matcher.group(1);
            int separator = value.indexOf(" - ");
            if (separator < 1) {
                continue;
            }
            String id = value.substring(0, separator).trim();
            String scenario = value.substring(separator + 3).trim();
            entries.add(new CatalogEntry(id, scenario, TestCaseModules.of(id),
                    path.getFileName().toString()));
        }
        return entries;
    }

    private void readConstants(Path path, Map<String, CatalogEntry> byConst) {
        Pattern named = Pattern.compile(
                "public static final String (\\w+)\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = named.matcher(read(path));
        while (matcher.find()) {
            String value = matcher.group(2);
            int separator = value.indexOf(" - ");
            if (separator < 1) {
                continue;
            }
            String id = value.substring(0, separator).trim();
            String scenario = value.substring(separator + 3).trim();
            byConst.put(matcher.group(1), new CatalogEntry(id, scenario,
                    TestCaseModules.of(id), path.getFileName().toString()));
        }
    }

    private void readImplementations(
            Path path,
            Map<String, CatalogEntry> catalogByConst,
            Map<String, Implementation> mapped) {
        String source = read(path);
        String className = path.getFileName().toString().replace(".java", "");
        Matcher matcher = TEST_METHOD.matcher(source);
        while (matcher.find()) {
            String annotation = matcher.group(1);
            String javadoc = matcher.group(2) == null ? "" : matcher.group(2)
                    .replace("*", " ").replaceAll("\\s+", " ").trim();
            String method = matcher.group(3);
            String id = idFromAnnotation(annotation, catalogByConst);
            if (id == null || id.isBlank()) {
                continue;
            }
            mapped.putIfAbsent(id, new Implementation(
                    id,
                    className,
                    method,
                    TestCaseModules.flowType(className),
                    javadoc));
        }
    }

    private String idFromAnnotation(String annotation, Map<String, CatalogEntry> catalogByConst) {
        Matcher constRef = DESCRIPTION_CONST.matcher(annotation);
        if (constRef.find()) {
            CatalogEntry entry = catalogByConst.get(constRef.group(2));
            if (entry != null) {
                return entry.id();
            }
        }
        Matcher literal = DESCRIPTION_STRING.matcher(annotation);
        if (literal.find()) {
            String value = literal.group(1);
            int separator = value.indexOf(" - ");
            return separator > 0 ? value.substring(0, separator).trim() : value.trim();
        }
        return null;
    }

    private String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Không đọc được " + path, exception);
        }
    }
}
