package com.vuatho.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonChecks {
    private JsonChecks() {
    }

    public static boolean hasField(String json, String fieldName) {
        return Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:").matcher(json).find();
    }

    public static int intField(String json, String fieldName, int defaultValue) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(-?\\d+)")
                .matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    public static String firstObjectField(String json, String arrayField, String fieldName) {
        int arrayIndex = json.indexOf("\"" + arrayField + "\"");
        if (arrayIndex < 0) {
            return "";
        }
        int arrayStart = json.indexOf('[', arrayIndex);
        int objectStart = json.indexOf('{', arrayStart);
        if (arrayStart < 0 || objectStart < 0) {
            return "";
        }
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(\"[^\"]*\"|-?\\d+|true|false|null)")
                .matcher(json.substring(objectStart));
        if (!matcher.find()) {
            return "";
        }
        String value = matcher.group(1);
        return value.startsWith("\"") && value.endsWith("\"")
                ? value.substring(1, value.length() - 1)
                : value;
    }

    public static boolean arrayHasAtLeastOneObject(String json, String arrayField) {
        return arrayObjectCount(json, arrayField) > 0;
    }

    public static int arrayObjectCount(String json, String fieldName) {
        String content = arrayContent(json, fieldName);
        int depth = 0;
        int count = 0;
        for (int index = 0; index < content.length(); index++) {
            char current = content.charAt(index);
            if (current == '{') {
                if (depth == 0) {
                    count++;
                }
                depth++;
            } else if (current == '}') {
                depth = Math.max(0, depth - 1);
            }
        }
        return count;
    }

    public static String arrayContent(String json, String fieldName) {
        int arrayIndex = json.indexOf("\"" + fieldName + "\"");
        if (arrayIndex < 0) {
            return "";
        }
        int arrayStart = json.indexOf('[', arrayIndex);
        if (arrayStart < 0) {
            return "";
        }
        int depth = 0;
        for (int index = arrayStart + 1; index < json.length(); index++) {
            char current = json.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth = Math.max(0, depth - 1);
            } else if (current == ']' && depth == 0) {
                return json.substring(arrayStart + 1, index);
            }
        }
        return "";
    }
}
