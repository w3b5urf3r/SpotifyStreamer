package com.androidnanodegree.mlopez.spotifystreamer.topTrack;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.androidnanodegree.mlopez.spotifystreamer.StreamerApp;
import com.androidnanodegree.mlopez.spotifystreamer.model.TrackSpotifyPlayer;
import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer.PlayerActivity;
import com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer.PlayerFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by marioromano on 05/07/2015.
 */
public class TopTracksFragment extends ListFragment implements AdapterView.OnItemClickListener {
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String ARTIST_ID = "ARTIST ID";
    public static final String PLAYER_DIALOG_TAG = "PLAYER DIALOG FRAGMENT";
    private ArrayList<Track> mTracks = new ArrayList<>();
    private AsyncTask<String, Void, ArrayList<Track>> mSearchTopTracksTask;
    private TopTrackListAdapter mTrackAdapter;
    private String mArtistId;
    private String mArtistName;
    private View inflatedView;
    private Tracks tracks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_toptracks_listview, null);
        mTrackAdapter = new TopTrackListAdapter(getActivity(), R.layout.toptrack_list_item, mTracks);
        setListAdapter(mTrackAdapter);

        return inflatedView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setOnItemClickListener(this);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            mArtistId = getArguments().getString(ARTIST_ID);
            mArtistName = getArguments().getString(ARTIST_NAME);
            searchArtist();
        } else {
            inflatedView.findViewById(android.R.id.empty).setVisibility(View.GONE);
        }
    }

    private void searchArtist() {
        mSearchTopTracksTask = new AsyncTask<String, Void, ArrayList<Track>>()

        {
            @Override
            protected ArrayList<Track> doInBackground(String... params) {
                tracks = null;
                try {
                    SpotifyApi api = new SpotifyApi();
                    SpotifyService spotify = api.getService();
                    Map<String, Object> paramaters = new HashMap<>();
                    paramaters.put("country", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("iso_code_list", Locale.getDefault().getCountry()));
                    tracks = spotify.getArtistTopTrack(params[0], paramaters);
                } catch (Exception e) {
                    return null;
                } finally {
                    if (tracks == null)
                        return null;

                    return (ArrayList<Track>) tracks.tracks;
                }


            }

            @Override
            protected void onPostExecute(ArrayList<Track> result) {
                super.onPostExecute(result);

                if (result != null && result.size() != 0) {
                    mTracks.clear();
                    mTracks.addAll(result);
                    mTrackAdapter.notifyDataSetChanged();
                } else {
                    showToastNoResults();
                }

            }

            private void showToastNoResults() {
                Toast.makeText(getActivity(), getString(R.string.no_results_found), Toast.LENGTH_LONG).show();
            }
        };
        mSearchTopTracksTask.execute(mArtistId);
    }

    @Override
    public void onDestroyView() {
        if (mSearchTopTracksTask != null)
            mSearchTopTracksTask.cancel(true);
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<TrackSpotifyPlayer> tracksSpotify = TrackSpotifyPlayer.getParcelableTracks(tracks);
        startPlayer(position, tracksSpotify);
    }

    private void startPlayer(int position, ArrayList<TrackSpotifyPlayer> tracksSpotify) {
        if (!StreamerApp.app.isTablet) {
            Intent playerIntent = getIntent(getActivity(),position, tracksSpotify);

            startActivity(playerIntent);
            getActivity().finish();
        } else {
            DialogFragment playerDialogFragment = PlayerFragment.newInstance(PlayerFragment.buildArgumentsBundle(tracksSpotify, position));
            playerDialogFragment.setShowsDialog(true);
            playerDialogFragment.show(getFragmentManager(), PLAYER_DIALOG_TAG);
        }
    }

    @NonNull
    public static Intent getIntent(Context context,int position, ArrayList<TrackSpotifyPlayer> tracksSpotify) {
        Intent playerIntent = new Intent(context, PlayerActivity.class);
        playerIntent.putExtra(PlayerFragment.TRACK_INDEX_SELECTED, position);
        playerIntent.putParcelableArrayListExtra(PlayerFragment.TRACKS_PARCELABLE, tracksSpotify);
        return playerIntent;
    }

    public void searchByIsoCode() {
        searchArtist();
    }

    public void showPlayer(ArrayList<TrackSpotifyPlayer> tracksBeingPlayed, int trackIndexBeingPlayed) {
        startPlayer(trackIndexBeingPlayed,tracksBeingPlayed);
    }
}
