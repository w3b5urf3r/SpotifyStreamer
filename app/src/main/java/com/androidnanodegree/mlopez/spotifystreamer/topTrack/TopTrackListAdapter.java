package com.androidnanodegree.mlopez.spotifystreamer.topTrack;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnanodegree.mlopez.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by marioromano on 05/07/2015.
 */
public class TopTrackListAdapter extends ArrayAdapter<Track> {

    private final Context mContext;
    private final ArrayList<Track> mTracks;
    private final int mListItem;

    public TopTrackListAdapter(Context context, int resource, ArrayList<Track> tracks) {
        super(context, resource, tracks);
        mContext = context;
        mTracks = tracks;
        mListItem = resource;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Track track = getItem(position);
        trackHolder h;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mListItem, null);
            h = new trackHolder();
            h.track = track;
            h.trackThumbImage = (ImageView) convertView.findViewById(R.id.toptrack_thumbnail);
            h.trackNameTextView = (TextView) convertView.findViewById(R.id.toptack_trackname_tv);
            h.albumNameTextView = (TextView) convertView.findViewById(R.id.toptack_albumname_tv);
            convertView.setTag(h);
        } else {

            h = (trackHolder) convertView.getTag();
        }
        if (track.album.images.size() > 0) {
            for (int i = 0; i < track.album.images.size(); i++) {
                String url = track.album.images.get(i).url;
                if (!TextUtils.isEmpty(url)) {
//                    picking the first available means pick 640px if available
                    Picasso.with(mContext).load(url).into(h.trackThumbImage);
                    break;
                }
            }
        }
        h.trackNameTextView.setText(track.name);
        h.albumNameTextView.setText(track.album.name);
        h.track = track;

        return convertView;
    }

    @Override
    public Track getItem(int position) {
        return mTracks.get(position);
    }

    @Override
    public int getCount() {
        return mTracks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    class trackHolder {
        Track track;
        ImageView trackThumbImage;
        TextView trackNameTextView;
        TextView albumNameTextView;
    }
}
