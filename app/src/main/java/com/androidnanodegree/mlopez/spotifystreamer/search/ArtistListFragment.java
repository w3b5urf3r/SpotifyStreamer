package com.androidnanodegree.mlopez.spotifystreamer.search;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.androidnanodegree.mlopez.spotifystreamer.MainActivity;
import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.androidnanodegree.mlopez.spotifystreamer.topTrack.TopTracksActivity;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by marioromano on 04/07/2015.
 */
public class ArtistListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String SEARCH_RESULT = "SEARCH RESULT";
    private ArrayList<Artist> mArtists;
    private ArtistListAdapter mAdapter;
    private SearchView mSearchView;
    private AsyncTask<String, Void, ArrayList<Artist>> mSearchTask;
    private String mLastQuery;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_artist_listview, null);
        ListView listView = (ListView) inflatedView.findViewById(R.id.artist_listview);

        mArtists = new ArrayList<>();
        mAdapter = new ArtistListAdapter(getActivity(), R.layout.artist_list_item, mArtists);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        mSearchView = (SearchView) inflatedView.findViewById(R.id.artist_search_view);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                destroyTask();
                if (!TextUtils.equals(mLastQuery, query)) {
                    mSearchTask = new AsyncTask<String, Void, ArrayList<Artist>>() {
                        @Override
                        protected ArrayList<Artist> doInBackground(String... params) {
                            ArtistsPager results = null;
                            try {
                                SpotifyApi api = new SpotifyApi();
                                SpotifyService spotify = api.getService();
                                results = spotify.searchArtists(params[0]);

                            } catch (Exception e) {
                                return null;
                            } finally {
                                if (results == null)
                                    return null;
                                mLastQuery = query;

                                return (ArrayList<Artist>) results.artists.items;
                            }

                        }

                        @Override
                        protected void onPostExecute(ArrayList<Artist> result) {
                            super.onPostExecute(result);

                            if (result != null && result.size() != 0) {
                                mArtists.clear();
                                mArtists.addAll(result);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                showToastNoResults();
                            }

                        }

                        private void showToastNoResults() {
                            if (mActivity != null)
                                Toast.makeText(mActivity, getString(R.string.no_results_found), Toast.LENGTH_LONG).show();
                        }
                    };
                    mSearchTask.execute(query);
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return inflatedView;
    }

    private void destroyTask() {
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState == null)
            outState = new Bundle();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        destroyTask();
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Artist artist = ((ArtistListAdapter.ArtistHolder) view.getTag()).artist;
        ((MainActivity) getActivity()).onClickArtist(artist);

    }

    public interface callBackItem {
        public void onClickArtist(Artist artist);
    }
}
