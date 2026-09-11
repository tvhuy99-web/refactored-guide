package com.footballagent.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public final class FootballAgentAccessibilityService extends AccessibilityService {
    private static final String TARGET_PACKAGE = "com.footballagent61";
    private static final long SNAPSHOT_DEBOUNCE_MS = 130;
    private static final long ANNOUNCE_DEBOUNCE_MS = 900;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DiagnosticsStore diagnostics;
    private long lastSnapshotAt;
    private long lastAnnouncementAt;
    private String lastAnnouncement = "";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        diagnostics = new DiagnosticsStore(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (!TARGET_PACKAGE.contentEquals(event.getPackageName())) return;

        long now = android.os.SystemClock.elapsedRealtime();
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            handleFocusedNode(event.getSource(), now);
        }

        if (Prefs.diagnostics(this) && now - lastSnapshotAt >= SNAPSHOT_DEBOUNCE_MS) {
            lastSnapshotAt = now;
            handler.post(this::scanForUnlabeledControls);
        }
    }

    @Override
    public void onInterrupt() {
    }

    private void scanForUnlabeledControls() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || diagnostics == null) return;
        NodeCollector.Snapshot snapshot = NodeCollector.collect(root);
        for (AccessibilityNodeInfo node : snapshot.nodes) {
            if (NodeCollector.isActionableUnlabeled(node)) {
                diagnostics.logUnlabeled(node, NodeCollector.nearbyText(node, snapshot.nodes));
            }
        }
    }

    private void handleFocusedNode(AccessibilityNodeInfo source, long now) {
        if (source == null || !Prefs.rowAnnounce(this)) return;
        String focusText = NodeCollector.labelOf(source);
        if (focusText.isEmpty()) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        NodeCollector.Snapshot snapshot = NodeCollector.collect(root);

        Rect focusRect = new Rect();
        source.getBoundsInScreen(focusRect);
        Cell focus = new Cell(focusText,
                new Box(focusRect.left, focusRect.top, focusRect.right, focusRect.bottom));
        List<Cell> row = RowAssembler.rowFor(focus, snapshot.cells);

        if (row.size() < 3) return;
        String rowText = RowAssembler.join(row);
        if (rowText.length() < focusText.length() + 3) return;
        if (rowText.length() > 420) rowText = rowText.substring(0, 420);

        if (diagnostics != null) diagnostics.logRow(focusText, rowText, row.size());

        if (now - lastAnnouncementAt < ANNOUNCE_DEBOUNCE_MS && rowText.equals(lastAnnouncement)) {
            return;
        }
        lastAnnouncementAt = now;
        lastAnnouncement = rowText;
        announce(rowText);
    }

    private void announce(String text) {
        AccessibilityManager manager =
                (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null || !manager.isEnabled()) return;
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
        event.setPackageName(getPackageName());
        event.setClassName(getClass().getName());
        event.getText().add(text);
        manager.sendAccessibilityEvent(event);
    }
}
