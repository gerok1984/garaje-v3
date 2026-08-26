package eus.gerok.garajev3;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpCommand {
    public static final class Result {
        public final boolean ok;
        public final int statusCode;
        public final String message;

        Result(boolean ok, int statusCode, String message) {
            this.ok = ok;
            this.statusCode = statusCode;
            this.message = message;
        }
    }

    private HttpCommand() {}

    public static Result sendSaved(Context context) {
        return send(
                GaragePrefs.getUrl(context),
                GaragePrefs.getMethod(context),
                GaragePrefs.getBody(context)
        );
    }

    public static Result send(String urlText, String method, String body) {
        if (urlText == null || urlText.trim().isEmpty()) {
            return new Result(false, -1, "Configura la URL en el móvil");
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlText.trim());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestMethod(method == null ? "POST" : method);
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");

            if ("POST".equalsIgnoreCase(method)) {
                byte[] payload = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(payload.length);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(payload);
                }
            }

            int code = connection.getResponseCode();
            boolean ok = code >= 200 && code < 300;
            return new Result(ok, code, ok ? "Comando enviado" : "HTTP " + code);
        } catch (IOException e) {
            String msg = e.getMessage();
            return new Result(false, -1, msg == null || msg.isEmpty() ? "Error de conexión" : msg);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
