package eus.gerok.garajev4;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
import java.util.List;

public class GarageMediaService extends MediaBrowserServiceCompat {
    private static final String ROOT = "garage_root";
    private static final String OPEN = "open_garage";
    private MediaSessionCompat session;

    @Override
    public void onCreate() {
        super.onCreate();
        session = new MediaSessionCompat(this, "GarajeV4");
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                if (!OPEN.equals(mediaId)) return;
                showStatus("Enviando comando…");
                HttpCommand.send(GarageMediaService.this, (ok, message) ->
                        showStatus(ok ? "Comando enviado" : "Error: " + message));
            }

            @Override
            public void onPlay() {
                onPlayFromMediaId(OPEN, null);
            }
        });
        showStatus("Pulsa Abrir garaje");
        session.setActive(true);
        setSessionToken(session.getSessionToken());
    }

    private void showStatus(String text) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, text)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Garaje v4")
                .build();
        session.setMetadata(metadata);
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
                .build();
        session.setPlaybackState(state);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> items = new ArrayList<>();
        if (ROOT.equals(parentId)) {
            MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                    .setMediaId(OPEN)
                    .setTitle("Abrir garaje")
                    .setSubtitle("Enviar comando HTTP")
                    .build();
            items.add(new MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        result.sendResult(items);
    }

    @Override
    public void onDestroy() {
        if (session != null) session.release();
        super.onDestroy();
    }
}
