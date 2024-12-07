package com.realgear.mediaplayer;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.media.session.MediaController;
import android.util.Log;

import com.realgear.mediaplayer.interfaces.IPlayerCallback;
import com.realgear.mediaplayer.model.Song;
import com.realgear.mediaplayer.statics.IntentFields;

import java.util.ArrayList;
import java.util.List;

public class CorePlayer {
    private final Activity m_vActivity;
    private IPlayerCallback m_vCallback;
    private MediaPlayerController m_vMediaPlayerController;

    public CorePlayer(Activity activity, MediaController.Callback callback) {
        this.m_vActivity = activity;
        this.m_vCallback = getCallback();
        this.m_vMediaPlayerController = new MediaPlayerController(activity, callback);
    }

    public IPlayerCallback getCallback() {
        if(this.m_vCallback == null)
            this.m_vCallback = new IPlayerCallback() {
                @Override
                public void onClickPlay(int queueIndex, List<Integer> queue) {
                    Log.d("corsor", "onClickPlay: 进入 ");
                    ArrayList<Integer> arrayList = new ArrayList<>();
                    for (int i : queue) { arrayList.add(i); }
                    Log.d("corsor","onclick"+queue);

                    Intent intent = new Intent(IntentFields.INTENT_PLAY);
                    intent.putIntegerArrayListExtra(IntentFields.EXTRA_TRACKS_LIST, arrayList);
                    intent.putExtra(IntentFields.EXTRA_TRACK_INDEX, queueIndex);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                    Log.e("CorePlayer", "Trying to play track at : " + queueIndex);
                }

                @Override
                public void onClickPlayIndex(int queueIndex) {

                }

                @Override
                public void onClickPlayNext() {
                    Intent intent = new Intent(IntentFields.ACTION_NEXT);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                }

                @Override
                public void onClickPlayPause() {
                    Intent intent = new Intent(IntentFields.INTENT_PLAY_PAUSE);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                }

                @Override
                public void onClickStop() {
                    Intent intent = new Intent(IntentFields.INTENT_STOP);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                }

                @Override
                public void onClickPlayPrevious() {
                    Intent intent = new Intent(IntentFields.ACTION_PREV);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                }

                @Override
                public void onSetSeekbar(int position) {
                    Intent intent = new Intent(IntentFields.INTENT_SET_SEEKBAR);
                    intent.putExtra(IntentFields.EXTRA_TRACK_POSITION, position);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                }

                @Override
                public void onSetRepeatType(@PlaybackManager.RepeatType int repeatType) {
                    Intent intent = new Intent(IntentFields.INTENT_CHANGE_REPEAT);
                    intent.putExtra(IntentFields.EXTRA_REPEAT_STATE, repeatType);
                    CorePlayer.this.m_vActivity.sendBroadcast(intent);
                }

                @Override
                public void onUpdateQueue(List<Song> queue, int queueIndex) {

                }

                @Override
                public void onDestroy() {

                }
            };

        return this.m_vCallback;
    }

    public void onDestroy() {
        this.m_vMediaPlayerController.onDestroy();
    }

    public void onStart() {
        this.m_vMediaPlayerController.onStart();
    }
}
