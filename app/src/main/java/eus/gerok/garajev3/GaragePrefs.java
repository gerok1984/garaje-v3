package eus.gerok.garajev3;

import android.content.Context;
import android.content.SharedPreferences;

public final class GaragePrefs {
    private static final String PREFS = "garage_v3";
    private static final String KEY_URL = "saved_url";
    private static final String KEY_METHOD = "saved_method";
    private static final String KEY_BODY = "saved_body";

    private GaragePrefs() {}

    public static String getUrl(Context context) {
        return prefs(context).getString(KEY_URL, "");
    }

    public static String getMethod(Context context) {
        return prefs(context).getString(KEY_METHOD, "POST");
    }

    public static String getBody(Context context) {
        return prefs(context).getString(KEY_BODY, "");
    }

    public static void save(Context context, String url, String method, String body) {
        prefs(context).edit()
                .putString(KEY_URL, url.trim())
                .putString(KEY_METHOD, method)
                .putString(KEY_BODY, body)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
