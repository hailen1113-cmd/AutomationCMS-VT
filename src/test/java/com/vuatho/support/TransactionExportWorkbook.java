package com.vuatho.support;

import com.vuatho.utils.TextNormalizer;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Đọc file giao dịch đã xuất và ánh xạ dữ liệu theo tên cột. */
public final class TransactionExportWorkbook {
    private TransactionExportWorkbook() {}

    public static Snapshot read(Path file) {
        List<List<String>> raw = file.getFileName().toString().toLowerCase().endsWith(".csv")
                ? readCsv(file) : readExcel(file);
        int headerIndex = findHeader(raw);
        List<String> headers = raw.get(headerIndex).stream().map(String::trim).toList();
        List<Map<String, String>> rows = new ArrayList<>();
        for (int index = headerIndex + 1; index < raw.size(); index++) {
            List<String> values = raw.get(index);
            if (values.stream().allMatch(String::isBlank)) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            for (int column = 0; column < headers.size(); column++) {
                String key = TextNormalizer.normalize(headers.get(column));
                String value = column < values.size() ? values.get(column).trim() : "";
                row.put(key, value);
            }
            rows.add(row);
        }
        return new Snapshot(file, headers, rows);
    }

    private static List<List<String>> readExcel(Path file) {
        try (var input = Files.newInputStream(file);
             var workbook = WorkbookFactory.create(input)) {
            DataFormatter formatter = new DataFormatter();
            List<List<String>> rows = new ArrayList<>();
            workbook.getSheetAt(0).forEach(row -> {
                List<String> values = new ArrayList<>();
                int last = Math.max(row.getLastCellNum(), 0);
                for (int column = 0; column < last; column++) {
                    values.add(formatter.formatCellValue(row.getCell(column,
                            org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                }
                rows.add(values);
            });
            return rows;
        } catch (IOException exception) {
            throw new AssertionError("Không đọc được Excel " + file, exception);
        }
    }

    private static List<List<String>> readCsv(Path file) {
        try {
            return Files.readAllLines(file).stream().map(TransactionExportWorkbook::parseCsvLine).toList();
        } catch (IOException exception) {
            throw new AssertionError("Không đọc được CSV " + file, exception);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        values.add(current.toString());
        return values;
    }

    private static int findHeader(List<List<String>> rows) {
        for (int index = 0; index < rows.size(); index++) {
            List<String> normalized = rows.get(index).stream().map(TextNormalizer::normalize).toList();
            boolean hasAmount = normalized.stream().anyMatch(value -> value.contains("so tien"));
            boolean hasDate = normalized.stream().anyMatch(value -> value.contains("ngay tao"));
            if (hasAmount && hasDate) {
                return index;
            }
        }
        throw new AssertionError("Không tìm thấy dòng tiêu đề giao dịch trong file xuất.");
    }

    public record Snapshot(Path file, List<String> headers, List<Map<String, String>> rows) {
        public boolean hasHeader(String header) {
            String expected = TextNormalizer.normalize(header);
            return headers.stream().map(TextNormalizer::normalize)
                    .anyMatch(expected::equals);
        }

        public List<String> values(String header) {
            String key = TextNormalizer.normalize(header);
            return rows.stream().map(row -> row.getOrDefault(key, "")).toList();
        }
    }
}
