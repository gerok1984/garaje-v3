package eus.gerok.garajev3;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private EditText urlInput;
    private Spinner methodSpinner;
    private EditText bodyInput;
    private Button testButton;
    private ProgressBar progress;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Garaje v3");
        title.setTextSize(26);
        title.setPadding(0, 0, 0, dp(8));
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Configura la llamada que ejecutará el botón de Android Auto.");
        subtitle.setTextSize(16);
        subtitle.setPadding(0, 0, 0, dp(20));
        root.addView(subtitle);

        TextView urlLabel = new TextView(this);
        urlLabel.setText("URL");
        root.addView(urlLabel);

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setHint("https://… o http://…");
        urlInput.setText(GaragePrefs.getUrl(this));
        root.addView(urlInput, new LinearLayout.LayoutParams(-1, -2));

        TextView methodLabel = new TextView(this);
        methodLabel.setText("Método");
        methodLabel.setPadding(0, dp(14), 0, 0);
        root.addView(methodLabel);

        methodSpinner = new Spinner(this);
        String[] methods = {"POST", "GET"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods);
        methodSpinner.setAdapter(adapter);
        methodSpinner.setSelection("GET".equalsIgnoreCase(GaragePrefs.getMethod(this)) ? 1 : 0);
        root.addView(methodSpinner);

        TextView bodyLabel = new TextView(this);
        bodyLabel.setText("Cuerpo JSON (opcional, solo POST)");
        bodyLabel.setPadding(0, dp(14), 0, 0);
        root.addView(bodyLabel);

        bodyInput = new EditText(this);
        bodyInput.setMinLines(3);
        bodyInput.setGravity(android.view.Gravity.TOP);
        bodyInput.setHint("{}\n");
        bodyInput.setText(GaragePrefs.getBody(this));
        root.addView(bodyInput, new LinearLayout.LayoutParams(-1, -2));

        Button saveButton = new Button(this);
        saveButton.setText("Guardar");
        saveButton.setOnClickListener(v -> {
            savePrefs();
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        buttonParams.topMargin = dp(18);
        root.addView(saveButton, buttonParams);

        testButton = new Button(this);
        testButton.setText("Probar llamada ahora");
        testButton.setOnClickListener(v -> testCall());
        root.addView(testButton, new LinearLayout.LayoutParams(-1, -2));

        progress = new ProgressBar(this);
        progress.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        progressParams.topMargin = dp(12);
        root.addView(progress, progressParams);

        TextView note = new TextView(this);
        note.setText("En Android Auto aparecerá como una app IoT con un botón “Abrir garaje”.");
        note.setPadding(0, dp(18), 0, 0);
        root.addView(note);

        setContentView(root);
    }

    private void savePrefs() {
        String method = methodSpinner.getSelectedItem().toString();
        GaragePrefs.save(this, urlInput.getText().toString(), method, bodyInput.getText().toString());
    }

    private void testCall() {
        savePrefs();
        testButton.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            HttpCommand.Result result = HttpCommand.sendSaved(getApplicationContext());
            runOnUiThread(() -> {
                progress.setVisibility(View.GONE);
                testButton.setEnabled(true);
                String text = result.ok ? "Correcto: " + result.message : "Error: " + result.message;
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            });
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
