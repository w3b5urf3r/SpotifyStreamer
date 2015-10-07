package com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer;

import android.app.DialogFragment;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnanodegree.mlopez.spotifystreamer.StreamerApp;
import com.androidnanodegree.mlopez.spotifystreamer.model.TrackSpotifyPlayer;
import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.androidnanodegree.mlopez.spotifystreamer.service.StreamingService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by marioromano on 16/08/2015.
 */
public class PlayerFragment extends DialogFragment implements View.OnClickListener, StreamerApp.ConnectionCallBack,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, StreamingService.PlayerPauseListener, SeekBar.OnSeekBarChangeListener, StreamingService.SelectedIndexListener {
    public static final String TRACKS = "TRACKS";
    public static final String TRACK_INDEX_SELECTED = "TRACK_INDEX_SELECTED";
    public static final String TRACKS_PARCELABLE = "track List Parcelable for spotify";
    public static final String TOP_TRACK_PLAYED_FILTER = "TopTrackPlayed";
    private View mInflatedView;
    private View playB;
    private View forwardB;
    private View backwardB;
    private ArrayList<TrackSpotifyPlayer> tracks;
    private boolean mMpIsPlaying;
    private TrackSpotifyPlayer trackSelected;
    private ImageView albumImage;
    private TextView trackTitleTv;
    private TextView artistNameTv;
    private TextView albumNameTv;
    private StreamerApp app;
    private SeekBar mSeekBar;
    private TextView durationTv;
    private TextView progressTv;
    private CountDownTimer countDownTimer;
    private int seekTo;
    private ShareActionProvider mShareActionProvider;
    private boolean tracksCharged = false;
    private int trackSelectedIndex;
    private MenuItem nowPlayingItemBar;

    public static PlayerFragment newInstance(Bundle args) {
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflatedView = inflater.inflate(R.layout.fragment_player, null);

        app = (StreamerApp) getActivity().getApplication();
        app.setConnectionListener(this);
        app.doBindService();
        if (app.streamingService != null) {
            setServiceListeners(app.streamingService);
        }
        mSeekBar = (SeekBar) mInflatedView.findViewById(R.id.player_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        durationTv = (TextView) mInflatedView.findViewById(R.id.track_duration);
        progressTv = (TextView) mInflatedView.findViewById(R.id.player_progress);
        resetProgress();

        albumImage = (ImageView) mInflatedView.findViewById(R.id.player_album_image);
        albumNameTv = (TextView) mInflatedView.findViewById(R.id.player_album_name);
        artistNameTv = (TextView) mInflatedView.findViewById(R.id.player_artist_name_tv);
        trackTitleTv = (TextView) mInflatedView.findViewById(R.id.player_song_title);

        playB = mInflatedView.findViewById(R.id.play_pause_button);
        backwardB = mInflatedView.findViewById(R.id.backward_button);
        forwardB = mInflatedView.findViewById(R.id.forward_button);
        playB.setOnClickListener(this);
        backwardB.setOnClickListener(this);
        forwardB.setOnClickListener(this);
        if (!StreamerApp.app.isTablet) {
            setHasOptionsMenu(true);
        }
        return mInflatedView;

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tracks = getArguments().getParcelableArrayList(TRACKS);
        trackSelectedIndex = getArguments().getInt(TRACK_INDEX_SELECTED);
        trackSelected = tracks.get(trackSelectedIndex);
        updateUI(trackSelected);
        if (app.streamingService != null)
            goAutoPlay();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (StreamerApp.app.isTablet) {
            int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
            int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    private void updateUI(TrackSpotifyPlayer trackSelected) {
        albumNameTv.setText(trackSelected.albumName);
        artistNameTv.setText(trackSelected.artistNAme);
        trackTitleTv.setText(trackSelected.name);
        Picasso.with(getActivity()).load(trackSelected.albumImageUrl).into(albumImage);
        if (isThisTrackPlaying()) {
            paused(false);
        }

    }

    private boolean isThisTrackPlaying() {
        return StreamerApp.app.streamingService != null
                && StreamerApp.app.streamingService.mMp != null
                && StreamerApp.app.streamingService.mMp.isPlaying()
                && TextUtils.equals(StreamerApp.app.streamingService.playingUrl, trackSelected.previewUrl);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.player_menu, menu);
        MenuItem shareMenuItem = menu.findItem(R.id.menu_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
        if (StreamerApp.app.isTablet) {
            nowPlayingItemBar = menu.findItem(R.id.now_playing);
            checkPlaying();
        }
        updateSharing();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.now_playing && !StreamerApp.app.isTablet) {
//            todo

            Toast.makeText(getActivity(), "nowPlaying", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void checkPlaying() {
        if (StreamerApp.app.streamingService != null) {
            nowPlayingItemBar.setVisible(true);

        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelCountDown();
    }

    @Override
    public void onClick(View v) {

        Intent intent = getStreamingServiceIntent();
        if (app.streamingService != null) {
            chargeTrack();
            switch (v.getId()) {
                case (R.id.backward_button): {
                    if (app.streamingService.hasTrack(false) && app.streamingService.mpIsDone) {
                        resetProgress();
                        startServiceSong(intent, StreamingService.ACTION_BACKWARD);
                    }
                    break;
                }
                case (R.id.play_pause_button): {
                    startServiceSong(intent, StreamingService.ACTION_PLAY);
                    break;
                }
                case (R.id.forward_button): {
                    if (app.streamingService.hasTrack(true) && app.streamingService.mpIsDone) {
                        resetProgress();
                        startServiceSong(intent, StreamingService.ACTION_FORWARD);
                    }

                    break;
                }
            }
        }
    }

    private void chargeTrack() {
        if (!tracksCharged) {
            tracksCharged = true;
            app.streamingService.setTracks(tracks);
            app.streamingService.setSelectedIndex(trackSelectedIndex);
        }
    }

    private void startServiceSong(Intent intent, String action) {
        updateSharing();
        intent.setAction(action);
        app.startService(intent);
    }

    private void updateSharing() {
        if (StreamerApp.app.isTablet) {
            // not specified
        } else {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, trackSelected.externalUrlSpotify);
            shareIntent.setType("text/plain");
            if (mShareActionProvider != null)
                mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void resetProgress() {
        mSeekBar.setMax(0);
        mSeekBar.setProgress(0);
        resetDurationTextView();
        cancelCountDown();
    }

    private void resetDurationTextView() {
        durationTv.setText(getHumanDuration(0));
    }


    private Intent getStreamingServiceIntent() {
        Intent intentTrack = new Intent(getActivity(), StreamingService.class);
        return intentTrack;
    }


    @Override
    public void connected(StreamingService service) {
        setServiceListeners(service);
        goAutoPlay();
    }

    private void goAutoPlay() {
        if (!isThisTrackPlaying()) {
            Intent intent = getStreamingServiceIntent();
            chargeTrack();
            startServiceSong(intent, StreamingService.ACTION_PLAY);
        }
    }

    private void setServiceListeners(StreamingService service) {
        service.setMediaPlayerListener(this, this, this, this);
        service.setSelectedIndexListener(this);
    }

    @Override
    public void onSelectedIndex(int selectedIndex) {
        updateUI(tracks.get(selectedIndex));
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        cancelCountDown();
        selectPlayPause(false);
        mMpIsPlaying = false;


    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        cancelCountDown();
        selectPlayPause(false);
        mMpIsPlaying = false;
        return false;
    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        mMpIsPlaying = true;
        selectPlayPause(true);
        final int duration = mp.getDuration();
        mSeekBar.setMax(duration);
        durationTv.setText(getHumanDuration(duration));
        cancelCountDown();
        startCountDown(mp, duration);

        Intent intent = new Intent(TOP_TRACK_PLAYED_FILTER);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void startCountDown(final MediaPlayer mp, final int duration) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int currentPosition = 0;
                try {
                    currentPosition = mp.getCurrentPosition();
                } catch (IllegalStateException e) {
                    cancelCountDown();
                    Log.e("Timer went wront", e.toString());
                }
                progressTv.setText(getHumanDuration(currentPosition));
                mSeekBar.setProgress(currentPosition);
            }

            @Override
            public void onFinish() {
                progressTv.setText(getHumanDuration(duration));

            }
        }.start();
    }

    private void cancelCountDown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private String getHumanDuration(int duration) {
        String humanString = "";
        if (duration >= 0) {
            humanString = String.format("%01d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration));
        }
        return humanString;
    }

    private void selectPlayPause(boolean selected) {
        mInflatedView.findViewById(R.id.play_pause_button).setSelected(selected);
    }


    @Override
    public void paused(boolean paused) {
        playB.setSelected(!paused);
        mMpIsPlaying = paused;

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progressTv.setText(getHumanDuration(progress));
        seekTo = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (app.streamingService != null && app.streamingService.mMp != null) {
            app.streamingService.mMp.seekTo(seekTo);
        }
    }

    public static Bundle buildArgumentsBundle(ArrayList<TrackSpotifyPlayer> tracks, int selectedIndex) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(PlayerFragment.TRACKS, tracks);
        args.putInt(PlayerFragment.TRACK_INDEX_SELECTED, selectedIndex);
        return args;
    }
}
