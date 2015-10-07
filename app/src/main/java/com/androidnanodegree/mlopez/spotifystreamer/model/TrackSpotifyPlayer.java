package com.androidnanodegree.mlopez.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by marioromano on 16/08/2015.
 */
public class TrackSpotifyPlayer implements Parcelable {
    public final String externalUrlSpotify;
    public String previewUrl;
    public String name;
    public String albumName;
    public String albumImageUrl;
    public String artistNAme;

    public TrackSpotifyPlayer(String artistiNAme, String preview_url, String trackName, String albumName, String imageUrl, String externalUrlSpotify) {
        artistNAme =artistiNAme;
        this.previewUrl = preview_url;
        this.name = trackName;
        this.albumName = albumName;
        this.albumImageUrl = imageUrl;
        this.externalUrlSpotify=externalUrlSpotify;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.artistNAme);
        dest.writeString(this.previewUrl);
        dest.writeString(this.name);
        dest.writeString(this.albumName);
        dest.writeString(this.albumImageUrl);
        dest.writeString(this.externalUrlSpotify);

    }


    public static final Parcelable.Creator<TrackSpotifyPlayer> CREATOR
            = new Parcelable.Creator<TrackSpotifyPlayer>() {
        public TrackSpotifyPlayer createFromParcel(Parcel in) {
            return new TrackSpotifyPlayer(in);
        }

        public TrackSpotifyPlayer[] newArray(int size) {
            return new TrackSpotifyPlayer[size];
        }
    };

    private TrackSpotifyPlayer(Parcel in) {
        this.artistNAme=in.readString();
        this.previewUrl = in.readString();
        this.name = in.readString();
        this.albumName = in.readString();
        this.albumImageUrl = in.readString();
        this.externalUrlSpotify = in.readString();
    }

    public static ArrayList<TrackSpotifyPlayer> getParcelableTracks(Tracks tracks) {
        ArrayList<TrackSpotifyPlayer> tracksSpotifyStreamerParcelables = new ArrayList<>();
        for (Track track : tracks.tracks) {
            tracksSpotifyStreamerParcelables.add((new TrackSpotifyPlayer(track.artists.get(0).name,track.preview_url, track.name, track.album.name, track.album.images.get(0).url,track.external_urls.get("spotify"))));
        }
        return tracksSpotifyStreamerParcelables;
    }
}
