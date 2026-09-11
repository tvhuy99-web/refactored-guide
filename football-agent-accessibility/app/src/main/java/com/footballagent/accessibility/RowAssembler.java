package com.footballagent.accessibility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class RowAssembler {
    private RowAssembler() {}

    static List<Cell> rowFor(Cell focus, List<Cell> all) {
        List<Cell> result = new ArrayList<>();
        for (Cell candidate : all) {
            if (candidate == focus || sameVisualRow(focus.box, candidate.box)) {
                result.add(candidate);
            }
        }
        result.sort(Comparator.comparingInt(c -> c.box.left));
        return dedupe(result);
    }

    static boolean sameVisualRow(Box a, Box b) {
        if (a.verticalOverlapRatio(b) >= 0.48) return true;
        int tolerance = Math.max(8, Math.min(a.height(), b.height()) * 2 / 3);
        return Math.abs(a.centerY() - b.centerY()) <= tolerance;
    }

    static String join(List<Cell> row) {
        StringBuilder out = new StringBuilder();
        for (Cell cell : row) {
            String value = normalize(cell.text);
            if (value.isEmpty()) continue;
            if (out.length() > 0) out.append(", ");
            out.append(value);
        }
        return out.toString();
    }

    private static List<Cell> dedupe(List<Cell> input) {
        Set<String> seen = new LinkedHashSet<>();
        List<Cell> result = new ArrayList<>();
        for (Cell cell : input) {
            String text = normalize(cell.text);
            if (text.isEmpty()) continue;
            if (seen.add(text)) result.add(cell);
        }
        return result;
    }

    static String normalize(CharSequence text) {
        if (text == null) return "";
        return text.toString().replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }
}
