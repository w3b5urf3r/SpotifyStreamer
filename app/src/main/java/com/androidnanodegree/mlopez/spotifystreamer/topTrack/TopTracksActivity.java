package com.androidnanodegree.mlopez.spotifystreamer.topTrack;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.androidnanodegree.mlopez.spotifystreamer.model.TrackSpotifyPlayer;
import com.androidnanodegree.mlopez.spotifystreamer.setting.SettingsActivity;
import com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer.PlayerFragment;

import java.util.ArrayList;

/**
 * Created by marioromano on 05/07/2015.
 */
public class TopTracksActivity extends AppCompatActivity {

    private static final String TOP_TRACK_FRAGMENT = "top_fragment";
    private MenuItem nowPlayingItemBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10tracks);
        String artistName = getIntent().getStringExtra(TopTracksFragment.ARTIST_NAME);
        String artistID = getIntent().getStringExtra(TopTracksFragment.ARTIST_ID);
        Bundle artistBundle= new Bundle();
        artistBundle.putString(TopTracksFragment.ARTIST_NAME, artistName);
        artistBundle.putString(TopTracksFragment.ARTIST_ID, artistID);
        TopTracksFragment topTracksFragment = new TopTracksFragment();
        topTracksFragment.setArguments(artistBundle);

        getFragmentManager().beginTransaction()
                .replace(R.id.top_tracks_frag_container, topTracksFragment,TOP_TRACK_FRAGMENT)
                .commit();
        getSupportActionBar().setSubtitle(artistName);
        LocalBroadcastManager.getInstance(this).registerReceiver(tracksPlaying_receiver, new IntentFilter(PlayerFragment.TOP_TRACK_PLAYED_FILTER));


    }
    private ArrayList<TrackSpotifyPlayer> tracksBeingPlayed;
    private int trackIndexBeingPlayed;
    BroadcastReceiver tracksPlaying_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            initialize toolbar
            nowPlayingItemBar.setVisible(true);
            tracksBeingPlayed = intent.getParcelableArrayListExtra(PlayerFragment.TRACKS);
            trackIndexBeingPlayed = intent.getIntExtra(PlayerFragment.TRACK_INDEX_SELECTED, 0);
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        nowPlayingItemBar = menu.findItem(R.id.now_playing);
        return true;
    }

    @Override
    protected void onDestroy() {
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
                ((TopTracksFragment)getFragmentManager().findFragmentByTag(TOP_TRACK_FRAGMENT)).showPlayer(tracksBeingPlayed,trackIndexBeingPlayed);
                DialogFragment playerDialogFragment = PlayerFragment.newInstance(PlayerFragment.buildArgumentsBundle(tracksBeingPlayed, trackIndexBeingPlayed));
                playerDialogFragment.setShowsDialog(true);
                playerDialogFragment.show(getFragmentManager(), TopTracksFragment.PLAYER_DIALOG_TAG);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

}
