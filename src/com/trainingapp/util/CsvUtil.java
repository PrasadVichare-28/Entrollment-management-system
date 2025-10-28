package com.trainingapp.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Lightweight CSV helper for simple persistence (no external library). */
public class CsvUtil {
    private static final String DATA_DIR = "data";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String filename, List<String> headers, List<List<String>> rows) {
        Path path = Paths.get(DATA_DIR, filename);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            if (headers != null) {
                writer.write(String.join(",", headers));
                writer.newLine();
            }
            for (List<String> row : rows) {
                writer.write(String.join(",", escape(row)));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, String>> read(String filename) {
        Path path = Paths.get(DATA_DIR, filename);
        if (!Files.exists(path)) return Collections.emptyList();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) return Collections.emptyList();

            List<String> headers = Arrays.asList(headerLine.split(","));
            List<Map<String, String>> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> values = Arrays.asList(line.split(","));
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), i < values.size() ? values.get(i) : "");
                }
                result.add(row);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<String> escape(List<String> fields) {
        List<String> out = new ArrayList<>();
        for (String f : fields) {
            if (f.contains(",") || f.contains("\"")) {
                f = "\"" + f.replace("\"", "\"\"") + "\"";
            }
            out.add(f);
        }
        return out;
    }
}
