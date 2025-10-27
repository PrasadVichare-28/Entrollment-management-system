package com.trainingapp.persistence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Very small CSV utility with safe quoting.
 * - Writes header if file doesn't exist.
 * - Quotes fields and escapes double-quotes by doubling them.
 * - Parses CSV lines with quoted fields.
 */
public class CsvStore {

    private static final String DATA_DIR = "data";

    public static void ensureDataDir() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- Write ----------

    public static void writeAll(String fileName, List<String> header, List<List<String>> rows) throws IOException {
        ensureDataDir();
        Path p = Paths.get(DATA_DIR, fileName);
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            if (header != null && !header.isEmpty()) {
                w.write(toCsvLine(header));
                w.newLine();
            }
            for (List<String> row : rows) {
                w.write(toCsvLine(row));
                w.newLine();
            }
        }
    }

    private static String toCsvLine(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(quote(fields.get(i)));
        }
        return sb.toString();
    }

    private static String quote(String s) {
        if (s == null) s = "";
        // Escape '"' by doubling it per RFC 4180, and wrap in quotes
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    // ---------- Read ----------

    public static List<Map<String, String>> readAll(String fileName) throws IOException {
        Path p = Paths.get(DATA_DIR, fileName);
        if (!Files.exists(p)) return Collections.emptyList();

        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return Collections.emptyList();

        List<String> header = parseCsvLine(lines.get(0));
        List<Map<String, String>> out = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> row = parseCsvLine(lines.get(i));
            Map<String, String> m = new LinkedHashMap<>();
            for (int c = 0; c < header.size(); c++) {
                String key = header.get(c);
                String val = c < row.size() ? row.get(c) : "";
                m.put(key, val);
            }
            out.add(m);
        }
        return out;
    }

    // Basic CSV parser (handles quoted fields and commas inside quotes)
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    // Escaped quote?
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(ch);
                }
            } else {
                if (ch == ',') {
                    fields.add(sb.toString());
                    sb.setLength(0);
                } else if (ch == '"') {
                    inQuotes = true;
                } else {
                    sb.append(ch);
                }
            }
        }
        fields.add(sb.toString());
        return fields;
    }
}
