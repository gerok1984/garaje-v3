package eus.gerok.garajev4;

import android.content.Context;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HttpCommand {
    public interface Callback { void done(boolean ok, String message); }
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private HttpCommand() {}

    public static void send(Context context, Callback callback) {
        String target = GaragePrefs.url(context);
        String method = GaragePrefs.method(context);
        String body = GaragePrefs.body(context);
        if (target == null || target.trim().isEmpty()) {
            callback.done(false, "Configura primero la URL en el móvil");
            return;
        }
        EXECUTOR.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(target).openConnection();
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(7000);
                conn.setRequestMethod(method);
                conn.setRequestProperty("Accept", "*/*");
                if ("POST".equalsIgnoreCase(method)) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    byte[] bytes = (body == null ? "" : body).getBytes(StandardCharsets.UTF_8);
                    conn.setFixedLengthStreamingMode(bytes.length);
                    try (OutputStream os = conn.getOutputStream()) { os.write(bytes); }
                }
                int code = conn.getResponseCode();
                callback.done(code >= 200 && code < 400, "HTTP " + code);
            } catch (Exception e) {
                callback.done(false, e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }
}
