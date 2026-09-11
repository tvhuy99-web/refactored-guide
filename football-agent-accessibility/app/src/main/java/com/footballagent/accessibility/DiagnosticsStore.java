package com.footballagent.accessibility;

import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

final class DiagnosticsStore {
    private static final long MAX_BYTES = 4L * 1024L * 1024L;
    private final File file;
    private final Set<String> sessionFingerprints = new HashSet<>();

    DiagnosticsStore(Context context) {
        file = new File(context.getFilesDir(), "football-agent-accessibility.jsonl");
    }

    File file() { return file; }

    synchronized void logUnlabeled(AccessibilityNodeInfo node, String nearbyText) {
        try {
            Rect r = new Rect();
            node.getBoundsInScreen(r);
            String fingerprint = r.flattenToString() + "|" + node.getClassName() + "|" + nearbyText;
            if (!sessionFingerprints.add(fingerprint)) return;

            JSONObject o = new JSONObject();
            o.put("type", "unlabeled_clickable");
            o.put("elapsedMs", SystemClock.elapsedRealtime());
            o.put("class", String.valueOf(node.getClassName()));
            o.put("bounds", r.flattenToString());
            o.put("clickable", node.isClickable());
            o.put("longClickable", node.isLongClickable());
            o.put("enabled", node.isEnabled());
            o.put("actions", node.getActionList().toString());
            o.put("nearby", nearbyText == null ? "" : nearbyText);
            append(o.toString());
        } catch (Exception ignored) {
        }
    }

    synchronized void logRow(String focused, String rowText, int cells) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", "row_candidate");
            o.put("elapsedMs", SystemClock.elapsedRealtime());
            o.put("focus", focused);
            o.put("row", rowText);
            o.put("cells", cells);
            append(o.toString());
        } catch (Exception ignored) {
        }
    }

    private void append(String line) {
        try {
            if (file.exists() && file.length() > MAX_BYTES) {
                File old = new File(file.getParentFile(), file.getName() + ".old");
                if (old.exists()) old.delete();
                file.renameTo(old);
            }
            try (FileOutputStream out = new FileOutputStream(file, true)) {
                out.write(line.getBytes(StandardCharsets.UTF_8));
                out.write('\n');
            }
        } catch (Exception ignored) {
        }
    }
}
