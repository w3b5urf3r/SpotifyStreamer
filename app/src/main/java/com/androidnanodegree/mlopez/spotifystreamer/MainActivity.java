package com.androidnanodegree.mlopez.spotifystreamer;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.androidnanodegree.mlopez.spotifystreamer.model.TrackSpotifyPlayer;
import com.androidnanodegree.mlopez.spotifystreamer.search.ArtistListFragment;
import com.androidnanodegree.mlopez.spotifystreamer.service.StreamingService;
import com.androidnanodegree.mlopez.spotifystreamer.setting.SettingFragment;
import com.androidnanodegree.mlopez.spotifystreamer.setting.SettingsActivity;
import com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer.PlayerFragment;
import com.androidnanodegree.mlopez.spotifystreamer.topTrack.TopTracksActivity;
import com.androidnanodegree.mlopez.spotifystreamer.topTrack.TopTracksFragment;

import java.util.ArrayList;
import java.util.Locale;

import kaaes.spotify.webapi.android.models.Artist;


public class MainActivity extends AppCompatActivity implements ArtistListFragment.callBackItem, Preference.OnPreferenceChangeListener {

    public boolean mTwoPanel;
    private String TOP_TRACK_TAG = "TOP TRACK FRAGMENT TAG";
    private MenuItem nowPlayingItemBar;
    private Artist mArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.top_tracks_frag_container) != null) {
            mTwoPanel = true;
            StreamerApp.app.isTablet = true;
            if (savedInstanceState == null) {
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.top_tracks_frag_container, new TopTracksFragment(), TOP_TRACK_TAG)
                        .commit();
            }
        } else {
            mTwoPanel = false;
            StreamerApp.app.isTablet = false;
            getSupportActionBar().setElevation(7);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(tracksPlaying_receiver, new IntentFilter(PlayerFragment.TOP_TRACK_PLAYED_FILTER));
    }


    private void checkPlaying() {
        if (StreamerApp.app.streamingService != null ) {
            tracksBeingPlayed = StreamerApp.app.streamingService.tracks;
            trackIndexBeingPlayed = StreamerApp.app.streamingService.trackSelectedIndex;
            nowPlayingItemBar.setVisible(true);
        }

    }

    private ArrayList<TrackSpotifyPlayer> tracksBeingPlayed;
    private int trackIndexBeingPlayed;
    BroadcastReceiver tracksPlaying_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            initialize toolbar
            checkPlaying();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        nowPlayingItemBar = menu.findItem(R.id.now_playing);
        checkPlaying();
        return true;
    }

    @Override
    protected void onDestroy() {
        StreamerApp.app.doUnbindService();
        StreamerApp.app.killMp();
        stopService(       new Intent(this, StreamingService.class));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tracksPlaying_receiver);

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), 00, null);
                return true;
            case R.id.now_playing:
//                quick show dialog player instead of task builder
                if (mTwoPanel) {
                    DialogFragment playerDialogFragment = PlayerFragment.newInstance(PlayerFragment.buildArgumentsBundle(tracksBeingPlayed, trackIndexBeingPlayed));
                    playerDialogFragment.setShowsDialog(true);
                    playerDialogFragment.show(getFragmentManager(), TopTracksFragment.PLAYER_DIALOG_TAG);
                }else{
                    TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
                    taskStackBuilder.addParentStack(MainActivity.class);
                    taskStackBuilder.addNextIntent(getTopTrackActivityIntent(mArtist));
                    taskStackBuilder.addParentStack(TopTracksActivity.class);
                    taskStackBuilder.addNextIntent(TopTracksFragment.getIntent(this, StreamerApp.app.streamingService.trackSelectedIndex, StreamerApp.app.streamingService.tracks));
                    startActivities(taskStackBuilder.getIntents());
                }
                break;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        we just need to check the result code in this case
        if (resultCode == SettingFragment.ISO_CODE_PICKED) {
            Fragment frag = getFragmentManager().findFragmentByTag(TOP_TRACK_TAG);
//            trigget the tablet to search again (optional not required, but it makes sense to me)
            if (StreamerApp.app.isTablet && frag != null) {
                TopTracksFragment topTracksFragment = (TopTracksFragment) frag;
                topTracksFragment.searchByIsoCode();
            }
//            if is smartphone it will catch it automatically by the preferences
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onClickArtist(Artist artist) {
        if (mTwoPanel) {
            Bundle artistBundle = new Bundle();
            artistBundle.putString(TopTracksFragment.ARTIST_NAME, artist.name);
            artistBundle.putString(TopTracksFragment.ARTIST_ID, artist.id);
            TopTracksFragment topTracksFragment = new TopTracksFragment();
            topTracksFragment.setArguments(artistBundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_frag_container, topTracksFragment, TOP_TRACK_TAG)
                    .commit();
        } else {
            mArtist=artist;
            Intent topTrackIntent = getTopTrackActivityIntent(artist);
            MainActivity.this.startActivity(topTrackIntent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @NonNull
    private Intent getTopTrackActivityIntent(Artist artist) {
        Intent topTrackIntent = new Intent(this, TopTracksActivity.class);
        topTrackIntent.putExtra(TopTracksFragment.ARTIST_NAME, artist.name);
        topTrackIntent.putExtra(TopTracksFragment.ARTIST_ID, artist.id);
        topTrackIntent.putParcelableArrayListExtra(PlayerFragment.TRACKS, tracksBeingPlayed);
        topTrackIntent.putExtra(PlayerFragment.TRACK_INDEX_SELECTED, trackIndexBeingPlayed);
        return topTrackIntent;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;

    }
}
