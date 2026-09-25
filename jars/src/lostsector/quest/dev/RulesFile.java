package lostsector.quest.dev;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// A rules.csv file split into records and rows. Cells are split into lines as the 0.98a-RC8 Rules loader does
// (sources-obf/campaign.rules.java): carriage returns removed from Conditions, Script and Options, the cell trimmed,
// split at line breaks, empty lines and lines starting with '#' skipped.
final class RulesFile {

    static final List<String> HEADER = List.of("id", "trigger", "conditions", "script", "text", "options", "notes");

    final List<Record> records;
    final String error;

    record Record(int line, List<String> fields) {

        boolean isBlank() {
            for (String field : fields) {
                if (!field.isBlank()) return false;
            }
            return true;
        }
    }

    record Row(int line, String id, String trigger, String conditions, String script, String text, String options, String notes) {

        static Row of(Record record) {
            List<String> f = record.fields;
            return new Row(record.line, f.get(0), f.get(1), f.get(2), f.get(3), f.get(4), f.get(5), f.get(6));
        }

        // The loader skips rows with an empty id and rows whose id starts with '#'.
        boolean isLoaded() {
            return !id.isEmpty() && !id.startsWith("#");
        }
    }

    record Option(String line, float order, String id, String text, String error) {
    }

    private RulesFile(List<Record> records, String error) {
        this.records = records;
        this.error = error;
    }

    // RFC 4180 records: quoted fields may hold commas, line breaks and doubled quotes. Physical lines that hold
    // nothing are not records. Record line numbers are 1-based physical lines where the record starts.
    static RulesFile read(Path path) throws IOException {
        String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) text = text.substring(1);
        List<Record> records = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        int line = 1;
        int start = 1;
        boolean quoted = false;
        boolean any = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    if (c == '\n') line++;
                    field.append(c);
                }
                continue;
            }
            if (c == '"') {
                quoted = true;
                any = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
                any = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                if (any || field.length() > 0) {
                    fields.add(field.toString());
                    records.add(new Record(start, fields));
                }
                fields = new ArrayList<>();
                field.setLength(0);
                any = false;
                line++;
                start = line;
            } else {
                field.append(c);
            }
        }
        if (quoted) {
            return new RulesFile(records, "unterminated quote in the record starting at line " + start);
        }
        if (any || field.length() > 0) {
            fields.add(field.toString());
            records.add(new Record(start, fields));
        }
        return new RulesFile(records, null);
    }

    static List<String> lines(String cell) {
        List<String> lines = new ArrayList<>();
        for (String line : cell.replace("\r", "").trim().split("\n")) {
            if (!line.isEmpty() && !line.trim().startsWith("#")) lines.add(line);
        }
        return lines;
    }

    // Port of the loader's option parsing: two parts are id:text; otherwise the first part must parse as a float
    // order, and everything after a third colon is dropped.
    static List<Option> options(String cell) {
        List<Option> options = new ArrayList<>();
        for (String line : lines(cell)) {
            String[] parts = line.split(Pattern.quote(":"));
            if (parts.length == 2) {
                options.add(new Option(line, 0f, parts[0], parts[1], null));
                continue;
            }
            float order;
            try {
                if (parts.length == 0) throw new NumberFormatException();
                order = Float.parseFloat(parts[0]);
            } catch (NumberFormatException e) {
                String problem = parts.length > 2 ? "a colon in the label of an id:text option" : "not an id:text or order:id:text option";
                options.add(new Option(line, 0f, null, null, problem + "; the file fails to load"));
                continue;
            }
            if (parts.length < 3) {
                options.add(new Option(line, 0f, null, null, "not an id:text or order:id:text option; the file fails to load"));
            } else if (parts.length > 3) {
                options.add(new Option(line, order, parts[1], parts[2], "a colon in the label; the text after it is dropped"));
            } else {
                options.add(new Option(line, order, parts[1], parts[2], null));
            }
        }
        return options;
    }
}
