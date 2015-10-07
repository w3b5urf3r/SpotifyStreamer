package com.androidnanodegree.mlopez.spotifystreamer;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.androidnanodegree.mlopez.spotifystreamer.service.StreamingService;
import com.androidnanodegree.mlopez.spotifystreamer.streamerPlayer.PlayerFragment;

/**
 * Created by marioromano on 22/08/2015.
 */
public class StreamerApp extends Application {
    private boolean mIsBound;
    public StreamingService streamingService;
    private ConnectionCallBack connectionCallback;
    public static StreamerApp app;
    public boolean isTablet;

    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
    }

    public ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            streamingService = ((StreamingService.LocalBinder) service).getService();
            connectionCallback.connected(streamingService);

        }

        public void onServiceDisconnected(ComponentName className) {
            streamingService = null;
        }
    };

    public void doBindService() {
        bindService(new Intent(this,
                StreamingService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void setConnectionListener(ConnectionCallBack callBack) {
        this.connectionCallback=callBack;
    }

    public void killMp() {
        if(streamingService!=null && streamingService.mMp!=null){
            streamingService.killMp();
        }
    }

    public interface ConnectionCallBack{
        void connected(StreamingService service);
    }
}
