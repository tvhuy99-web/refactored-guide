package com.footballagent.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

final class NodeCollector {
    static final int MAX_NODES = 700;
    static final int MAX_DEPTH = 45;

    static final class Snapshot {
        final List<AccessibilityNodeInfo> nodes = new ArrayList<>();
        final List<Cell> cells = new ArrayList<>();
    }

    private NodeCollector() {}

    static Snapshot collect(AccessibilityNodeInfo root) {
        Snapshot out = new Snapshot();
        walk(root, 0, out);
        return out;
    }

    private static void walk(AccessibilityNodeInfo node, int depth, Snapshot out) {
        if (node == null || depth > MAX_DEPTH || out.nodes.size() >= MAX_NODES) return;
        out.nodes.add(node);

        String label = labelOf(node);
        if (!label.isEmpty() && node.isVisibleToUser()) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            if (!rect.isEmpty()) {
                out.cells.add(new Cell(label, new Box(rect.left, rect.top, rect.right, rect.bottom)));
            }
        }

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount && out.nodes.size() < MAX_NODES; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) walk(child, depth + 1, out);
        }
    }

    static String labelOf(AccessibilityNodeInfo node) {
        String text = RowAssembler.normalize(node.getText());
        if (!text.isEmpty()) return text;
        String desc = RowAssembler.normalize(node.getContentDescription());
        if (!desc.isEmpty()) return desc;
        String hint = RowAssembler.normalize(node.getHintText());
        if (!hint.isEmpty()) return hint;
        String state = RowAssembler.normalize(node.getStateDescription());
        if (!state.isEmpty()) return state;
        return "";
    }

    static boolean isActionableUnlabeled(AccessibilityNodeInfo node) {
        if (!labelOf(node).isEmpty()) return false;
        return node.isVisibleToUser()
                && node.isEnabled()
                && (node.isClickable()
                    || node.isLongClickable()
                    || node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK));
    }

    static String nearbyText(AccessibilityNodeInfo target, List<AccessibilityNodeInfo> all) {
        Rect tr = new Rect();
        target.getBoundsInScreen(tr);
        StringBuilder result = new StringBuilder();
        int found = 0;
        for (AccessibilityNodeInfo node : all) {
            if (node == target) continue;
            String label = labelOf(node);
            if (label.isEmpty()) continue;
            Rect nr = new Rect();
            node.getBoundsInScreen(nr);
            int dx = Math.abs(nr.centerX() - tr.centerX());
            int dy = Math.abs(nr.centerY() - tr.centerY());
            if (dx <= 500 && dy <= 260) {
                if (result.length() > 0) result.append(" | ");
                result.append(label);
                if (++found >= 5) break;
            }
        }
        return result.toString();
    }
}
