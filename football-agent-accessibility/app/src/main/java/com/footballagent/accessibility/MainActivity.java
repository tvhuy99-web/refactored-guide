package com.footballagent.accessibility;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public final class MainActivity extends Activity {
    private static final int CREATE_LOG_FILE = 61;
    private TextView diagnosticsState;
    private TextView rowsState;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildUi());
        refreshStates();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Football Agent Trợ năng");
        title.setTextSize(24f);
        root.addView(title);

        Button settings = button("Mở cài đặt trợ năng");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(settings);

        diagnosticsState = new TextView(this);
        root.addView(diagnosticsState);
        Button diagnostics = button("Bật/tắt chẩn đoán nút không tên");
        diagnostics.setOnClickListener(v -> {
            Prefs.setDiagnostics(this, !Prefs.diagnostics(this));
            refreshStates();
        });
        root.addView(diagnostics);

        rowsState = new TextView(this);
        root.addView(rowsState);
        Button rows = button("Bật/tắt đọc gộp hàng cầu thủ/đội");
        rows.setOnClickListener(v -> {
            Prefs.setRowAnnounce(this, !Prefs.rowAnnounce(this));
            refreshStates();
        });
        root.addView(rows);

        Button export = button("Xuất nhật ký");
        export.setOnClickListener(v -> exportLog());
        root.addView(export);
        return root;
    }

    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        return b;
    }

    private void refreshStates() {
        if (diagnosticsState != null) {
            diagnosticsState.setText("Chẩn đoán: " + (Prefs.diagnostics(this) ? "Bật" : "Tắt"));
        }
        if (rowsState != null) {
            rowsState.setText("Đọc gộp hàng: " + (Prefs.rowAnnounce(this) ? "Bật" : "Tắt"));
        }
    }

    private void exportLog() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-ndjson");
        intent.putExtra(Intent.EXTRA_TITLE, "football-agent-accessibility.jsonl");
        startActivityForResult(intent, CREATE_LOG_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != CREATE_LOG_FILE || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        File source = new File(getFilesDir(), "football-agent-accessibility.jsonl");
        try (FileInputStream in = new FileInputStream(source);
             OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
            if (out == null) throw new IllegalStateException("Không mở được tệp đích");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) >= 0) out.write(buffer, 0, n);
            Toast.makeText(this, "Đã xuất nhật ký", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Chưa có nhật ký để xuất", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
