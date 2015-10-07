package com.androidnanodegree.mlopez.spotifystreamer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.androidnanodegree.mlopez.spotifystreamer.model.TrackSpotifyPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by marioromano on 22/08/2015.
 */
public class StreamingService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY = "com.spotifyStreamer.action.PLAY";
    public static final String ACTION_FORWARD = "com.spotifyStreamer.action.FORWARD";
    ;
    public static final String ACTION_BACKWARD = "com.spotifyStreamer.action.BACKWARD";
    private static final int NOTIFICATION_PLAYER = 311;
    ;
    public MediaPlayer mMp = null;
    private WifiManager.WifiLock wifiLock;
    private AudioManager audioManager;
    private String urlCharged;
    private MediaPlayer.OnCompletionListener playerOnCompletitionListener;
    private MediaPlayer.OnErrorListener playerOnErrorListener;
    private MediaPlayer.OnPreparedListener playerPreparedListner;
    private PlayerPauseListener playerPauseListener;
    public boolean mpIsDone = true;
    public String playingUrl;
    public ArrayList<TrackSpotifyPlayer> tracks;
    private boolean songCompleted;
    public int trackSelectedIndex;
    private TrackSpotifyPlayer trackSelected;
    private SelectedIndexListener selectedIndexListener;
    private int newSelectedIndex;


    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mMp == null) {
            initMp();
        }
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_PLAY)) {
                if (TextUtils.equals(urlCharged, trackSelected.previewUrl)) {
                    pauseMpOrResume();
                } else {
                    playTrack(trackSelected.previewUrl);
                }
            } else if (mpIsDone) {
                boolean hasTrack;
                if (mpIsDone) {
                    int newIndex = 0;
                    boolean isForward;
                    if (intent.getAction().equals(ACTION_BACKWARD)) {
                        isForward = false;
                        hasTrack = hasTrack(isForward);
                        newIndex = calculateNewIndex(isForward);


                    } else {
//                    FORWARD
                        isForward = true;
                        hasTrack = hasTrack(isForward);
                        newIndex = calculateNewIndex(isForward);
                    }

                    if (hasTrack) {
                        setNewSelectedIndex(newIndex);
                        selectedIndexListener.onSelectedIndex(newSelectedIndex);
                        trackSelected = tracks.get(newSelectedIndex);
                        playTrack(trackSelected.previewUrl);

                    } else if (!hasTrack && mpIsDone) {
                        showNoTrackToast();
                    }
                }
            }
        }
        return START_NOT_STICKY;
    }

    public boolean hasTrack(boolean forward) {
        boolean hasTrack;
        int newSelectedIndex = calculateNewIndex(forward);
        hasTrack = newSelectedIndex > 0 && newSelectedIndex < tracks.size();
        return hasTrack;
    }


    private void setNewSelectedIndex(int index) {
        newSelectedIndex = index;
    }

    private int calculateNewIndex(boolean forward) {
        if (forward) {
            return newSelectedIndex + 1;

        } else {
            return newSelectedIndex - 1;
        }

    }

    private void showNoTrackToast() {
        Toast.makeText(this, "no track", Toast.LENGTH_SHORT).show();
    }

    private void playTrack(String previewUrl) {
        try {
            mMp.stop();
            mMp.reset();
            if (playerPauseListener != null) {
                playerPauseListener.paused(true);
            }
            mpIsDone = false;
            mMp.setDataSource(previewUrl);
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            mMp.prepareAsync();
        } catch (IOException e) {
            showErrorToast();
        } catch (IllegalArgumentException e) {
            showErrorToast();
        } finally {
            playingUrl = previewUrl;
        }
    }

    private void pauseMpOrResume() {
        if (mMp.isPlaying()) {
            mMp.pause();
            if (playerPauseListener != null) {
                playerPauseListener.paused(true);
                showNotification(trackSelected, true);

            }
        } else if (!songCompleted) {
            mMp.start();
            if (playerPauseListener != null) {
                playerPauseListener.paused(false);
                showNotification(trackSelected, false);

            }
        } else {
            initMp();
            playTrack(trackSelected.previewUrl);
        }
    }

    public TrackSpotifyPlayer getTrackSelected() {
        return trackSelected;
    }

    private void initMp() {
        mMp = new MediaPlayer();
        //todo release wake lock
        mMp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();
        mMp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMp.setOnPreparedListener(this);
        mMp.setOnErrorListener(this);
        mMp.setOnCompletionListener(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void releaseLocks() {
        wifiLock.release();
    }

    private void showErrorToast() {
        Toast.makeText(this, R.string.player_error_msg, Toast.LENGTH_LONG).show();
    }

    public void setMediaPlayerListener(MediaPlayer.OnPreparedListener onPreparedListener,
                                       MediaPlayer.OnErrorListener onErrorListener,
                                       MediaPlayer.OnCompletionListener onCompletionListener,
                                       PlayerPauseListener pauseListener) {
        playerPreparedListner = onPreparedListener;
        playerOnErrorListener = onErrorListener;
        playerOnCompletitionListener = onCompletionListener;
        playerPauseListener = pauseListener;
    }

    public void killMp() {
        if (mMp != null) {
            if (mMp.isPlaying())
                mMp.stop();
            mMp.reset();
            mMp.release();
            mMp = null;
        }
    }

    public void setTracks(ArrayList<TrackSpotifyPlayer> tracks) {
        this.tracks = tracks;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.trackSelectedIndex = selectedIndex;
        this.newSelectedIndex = selectedIndex;
        trackSelected = tracks.get(selectedIndex);

    }

    public void setSelectedIndexListener(SelectedIndexListener selectedIndexListener) {
        this.selectedIndexListener = selectedIndexListener;
    }


    public interface PlayerPauseListener {
        //        false if resumed
        void paused(boolean paused);
    }

    public interface SelectedIndexListener {
        void onSelectedIndex(int selectedIndex);
    }

    /**
     * Called when MediaPlayer is ready
     */
    public class LocalBinder extends Binder {
        public StreamingService getService() {
            return StreamingService.this;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public void onPrepared(MediaPlayer player) {
        songCompleted = false;
        trackSelectedIndex = newSelectedIndex;
        setMpIsDone();
        urlCharged = trackSelected.previewUrl;
        if (playerPreparedListner != null)
            playerPreparedListner.onPrepared(player);
        showNotification(trackSelected, false);
        player.start();
    }


    private void setMpIsDone() {
        mpIsDone = true;
    }

    private void showNotification(TrackSpotifyPlayer track, boolean paused) {


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        RemoteViews playerNotView;
        if (paused)
            playerNotView = new RemoteViews(this.getPackageName(), R.layout.player_notification_layout);
        else
            playerNotView = new RemoteViews(this.getPackageName(), R.layout.player_notification_layout_paused);

        Intent intentPlayTrack = new Intent(getApplicationContext(), StreamingService.class);
        intentPlayTrack.setAction(ACTION_PLAY);

        Intent intentBackwardTrack = new Intent(this, StreamingService.class);
        intentBackwardTrack.setAction(ACTION_BACKWARD);

        Intent intentForwardTrack = new Intent(this, StreamingService.class);
        intentForwardTrack.setAction(ACTION_FORWARD);

        playerNotView.setOnClickPendingIntent(R.id.notification_backward, PendingIntent.getService(this.getApplicationContext(), 0, intentBackwardTrack, 0));
        playerNotView.setOnClickPendingIntent(R.id.notification_forward, PendingIntent.getService(this.getApplicationContext(), 0, intentForwardTrack, 0));
        playerNotView.setOnClickPendingIntent(R.id.notification_play_pause, PendingIntent.getService(this.getApplicationContext(), 0, intentPlayTrack, 0));

        playerNotView.setTextViewText(R.id.notification_track_name, track.name);
        builder.setContent(playerNotView);
        builder.setContentTitle("");
        builder.setSmallIcon(R.drawable.play_pause_selector);

        Notification notification = builder.build();
        notification.visibility = Notification.VISIBILITY_PUBLIC;
        getBitmapFromURL(builder, playerNotView, track.albumImageUrl);
        StreamingService.this.startForeground(NOTIFICATION_PLAYER, notification);
    }


    public void getBitmapFromURL(final NotificationCompat.Builder builder, final RemoteViews playerNotView, String strURL) {
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ;
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                playerNotView.setImageViewBitmap(R.id.notification_album_image, bitmap);
                StreamingService.this.startForeground(NOTIFICATION_PLAYER, builder.setContent(playerNotView).build());
            }
        }.execute(strURL);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        songCompleted = false;
        trackSelectedIndex = newSelectedIndex;
        showNotification(trackSelected, true);

        setMpIsDone();
        if (playerOnErrorListener != null)
            playerOnErrorListener.onError(mp, what, extra);
        showErrorToast();
        mp.reset();
        audioManager.abandonAudioFocus(this);
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        songCompleted = true;
        setMpIsDone();
        if (playerOnCompletitionListener != null)
            playerOnCompletitionListener.onCompletion(mp);
        showNotification(trackSelected, true);

        releaseMp();
        releaseLocks();
        audioManager.abandonAudioFocus(this);
    }


    @Override
    public void onDestroy() {
        killMp();
        stopForeground(true);
        stopSelf();
        super.onDestroy();


    }

    private void releaseMp() {
        mMp.release();
        mMp = null;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mMp == null)
                    initMp();
                else if (!mMp.isPlaying()) mMp.start();
                mMp.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                stopMp();
                releaseMp();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseMpOrResume();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMp.isPlaying()) mMp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void stopMp() {
        if (mMp != null && mMp.isPlaying())
            mMp.stop();
    }
}
