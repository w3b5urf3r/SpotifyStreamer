package com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.androidnanodegree.mlopez.spotifystreamer.StreamerApp;
import com.androidnanodegree.mlopez.spotifystreamer.model.TrackSpotifyPlayer;
import com.androidnanodegree.mlopez.spotifystreamer.R;

import java.util.ArrayList;

/**
 * Created by marioromano on 16/08/2015.
 */
public class PlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ArrayList<TrackSpotifyPlayer> tracks = getIntent().getParcelableArrayListExtra(PlayerFragment.TRACKS_PARCELABLE);
        int selectedIndex = getIntent().getIntExtra(PlayerFragment.TRACK_INDEX_SELECTED, 0);

        Bundle args=PlayerFragment.buildArgumentsBundle(tracks,selectedIndex);
        PlayerFragment playerFragment = PlayerFragment.newInstance(args);
        playerFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(R.id.player_activity_fragment_container, playerFragment).commit();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
