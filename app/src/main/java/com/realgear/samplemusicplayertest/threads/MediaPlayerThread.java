package com.realgear.samplemusicplayertest.threads;

import android.app.Activity;
import android.media.session.MediaController;
import android.provider.Settings;
import android.util.Log;

import com.realgear.mediaplayer.CorePlayer;
import com.realgear.mediaplayer.interfaces.IPlayerCallback;
import com.realgear.mediaplayer.statics.ClassManager;
import com.realgear.samplemusicplayertest.MainActivity;

public class MediaPlayerThread {
    private final String TAG = getClass().getSimpleName();

    private static MediaPlayerThread instance;

    private CorePlayer m_vCorePlayer;
    private IPlayerCallback m_vCallback;

    public MediaPlayerThread(MainActivity activity, MediaController.Callback callback) {
        ClassManager.CreateInstance(MainActivity.class);
        this.m_vCorePlayer = new CorePlayer(activity, callback);
        this.m_vCallback = this.m_vCorePlayer.getCallback();

        setInstance(this);
    }

    private void setInstance(MediaPlayerThread instance) {
        if (MediaPlayerThread.instance == null) {
            MediaPlayerThread.instance = instance;
        }
    }

    public static MediaPlayerThread getInstance() {
        return instance;
    }

    public IPlayerCallback getCallback() {
        Log.d("corsor", "getCallback: jinru");
        return this.m_vCallback; }

    public void onStart() { this.m_vCorePlayer.onStart(); }

    public void onDestroy() { this.m_vCorePlayer.onDestroy(); }
}
