package com.footballagent.accessibility;

import android.content.Context;
import android.content.SharedPreferences;

final class Prefs {
    private static final String NAME = "football_agent_accessibility";
    private static final String KEY_DIAGNOSTICS = "diagnostics";
    private static final String KEY_ROW_ANNOUNCE = "row_announce";

    private Prefs() {}

    static SharedPreferences store(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    static boolean diagnostics(Context context) {
        return store(context).getBoolean(KEY_DIAGNOSTICS, true);
    }

    static boolean rowAnnounce(Context context) {
        return store(context).getBoolean(KEY_ROW_ANNOUNCE, true);
    }

    static void setDiagnostics(Context context, boolean value) {
        store(context).edit().putBoolean(KEY_DIAGNOSTICS, value).apply();
    }

    static void setRowAnnounce(Context context, boolean value) {
        store(context).edit().putBoolean(KEY_ROW_ANNOUNCE, value).apply();
    }
}
