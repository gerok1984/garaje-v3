package eus.gerok.garajev4;

import android.content.Context;
import android.content.SharedPreferences;

public final class GaragePrefs {
    private static final String PREFS = "garage_v4";
    private GaragePrefs() {}
    public static String url(Context c) { return p(c).getString("url", ""); }
    public static String method(Context c) { return p(c).getString("method", "POST"); }
    public static String body(Context c) { return p(c).getString("body", ""); }
    public static void save(Context c, String url, String method, String body) {
        p(c).edit().putString("url", url.trim()).putString("method", method).putString("body", body).apply();
    }
    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
