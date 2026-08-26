package eus.gerok.garajev3;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.GridItem;
import androidx.car.app.model.GridTemplate;
import androidx.car.app.model.ItemList;
import androidx.car.app.model.Template;
import androidx.car.app.CarToast;
import androidx.core.graphics.drawable.IconCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GarageScreen extends Screen {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private volatile boolean sending = false;

    public GarageScreen(@NonNull CarContext carContext) {
        super(carContext);
    }

    @NonNull
    @Override
    public Template onGetTemplate() {
        boolean configured = !GaragePrefs.getUrl(getCarContext()).trim().isEmpty();

        GridItem.Builder item = new GridItem.Builder();
        if (!configured) {
            item.setTitle("Configura la URL")
                    .setText("Abre Garaje v3 en el móvil")
                    .setImage(new CarIcon.Builder(
                            IconCompat.createWithResource(getCarContext(), R.drawable.ic_garage)
                    ).build(), GridItem.IMAGE_TYPE_ICON);
        } else if (sending) {
            item.setLoading(true);
        } else {
            item.setTitle("Abrir garaje")
                    .setImage(new CarIcon.Builder(
                            IconCompat.createWithResource(getCarContext(), R.drawable.ic_garage)
                    ).build(), GridItem.IMAGE_TYPE_ICON)
                    .setOnClickListener(this::sendCommand);
        }

        ItemList list = new ItemList.Builder().addItem(item.build()).build();
        return new GridTemplate.Builder()
                .setTitle("Garaje")
                .setHeaderAction(Action.APP_ICON)
                .setSingleList(list)
                .build();
    }

    private void sendCommand() {
        if (sending) return;
        sending = true;
        invalidate();

        EXECUTOR.execute(() -> {
            HttpCommand.Result result = HttpCommand.sendSaved(getCarContext().getApplicationContext());
            new Handler(Looper.getMainLooper()).post(() -> {
                sending = false;
                invalidate();
                String message = result.ok ? "Comando enviado" : "Error: " + result.message;
                CarToast.makeText(getCarContext(), message, CarToast.LENGTH_LONG).show();
            });
        });
    }
}
