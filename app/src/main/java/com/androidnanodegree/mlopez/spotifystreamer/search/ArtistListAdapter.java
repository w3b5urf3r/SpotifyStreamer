package com.androidnanodegree.mlopez.spotifystreamer.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by marioromano on 04/07/2015.
 */
public class ArtistListAdapter extends ArrayAdapter<Artist> {
    private final List<Artist> mArtists;
    private final Context mContext;
    private final int mListItem;

    public ArtistListAdapter(Context context, int resource, ArrayList<Artist> artists) {
        super(context, resource, artists);
        mContext = context;
        mArtists = artists;
        mListItem = resource;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Artist artist = getItem(position);
        ArtistHolder h;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mListItem, null);
            h = new ArtistHolder();
            h.artist = artist;
            h.artistThumbImage = (ImageView) convertView.findViewById(R.id.artist_thumbnail);
            h.artistNameTextView = (TextView) convertView.findViewById(R.id.artist_name_tv);
            convertView.setTag(h);
        } else {

            h = (ArtistHolder) convertView.getTag();
        }
        if (artist.images.size() > 0)
            Picasso.with(mContext).load(artist.images.get(0).url).into(h.artistThumbImage);
        h.artistNameTextView.setText(artist.name);
        h.artist = artist;

        return convertView;
    }

    @Override
    public Artist getItem(int position) {
        return mArtists.get(position);
    }

    @Override
    public int getCount() {
        return mArtists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class ArtistHolder {
        Artist artist;
        ImageView artistThumbImage;
        TextView artistNameTextView;
    }
}
