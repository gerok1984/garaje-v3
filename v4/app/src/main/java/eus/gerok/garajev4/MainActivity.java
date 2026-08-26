package eus.gerok.garajev4;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Garaje v4");
        title.setTextSize(28);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Configura la llamada. En Android Auto aparecerá como fuente multimedia con un único botón: Abrir garaje.");
        info.setPadding(0, pad / 2, 0, pad);
        root.addView(info);

        EditText url = new EditText(this);
        url.setHint("http://... o https://...");
        url.setText(GaragePrefs.url(this));
        root.addView(url, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner method = new Spinner(this);
        String[] methods = {"POST", "GET"};
        method.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, methods));
        method.setSelection("GET".equalsIgnoreCase(GaragePrefs.method(this)) ? 1 : 0);
        root.addView(method);

        EditText body = new EditText(this);
        body.setHint("JSON opcional para POST");
        body.setMinLines(3);
        body.setText(GaragePrefs.body(this));
        root.addView(body, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button save = new Button(this);
        save.setText("Guardar");
        save.setOnClickListener(v -> {
            GaragePrefs.save(this, url.getText().toString(), method.getSelectedItem().toString(), body.getText().toString());
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
        });
        root.addView(save);

        Button test = new Button(this);
        test.setText("Probar llamada");
        test.setOnClickListener(v -> {
            GaragePrefs.save(this, url.getText().toString(), method.getSelectedItem().toString(), body.getText().toString());
            test.setEnabled(false);
            HttpCommand.send(this, (ok, msg) -> runOnUiThread(() -> {
                test.setEnabled(true);
                Toast.makeText(this, (ok ? "OK · " : "Error · ") + msg, Toast.LENGTH_LONG).show();
            }));
        });
        root.addView(test);

        setContentView(root);
    }
}
