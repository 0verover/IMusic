package com.realgear.mediaplayer.statics;

import android.content.Intent;

public class IntentFields {
    // Media Notification Constants
    public static final String ACTION_NEXT = "com.realgear.samplemusicplayertest.mediaplayer.NEXT";
    public static final String ACTION_PAUSE = "com.realgear.samplemusicplayertest.mediaplayer.PAUSE";
    public static final String ACTION_PLAY = "com.realgear.samplemusicplayertest.mediaplayer.PLAY";
    public static final String ACTION_PREV = "com.realgear.samplemusicplayertest.mediaplayer.PREV";
    public static final String CHANNEL_ID = "com.realgear.samplemusicplayertest";

    // Activities Constants
    public static final String EXTRA_REPEAT_STATE = "RepeatState";
    public static final String EXTRA_TRACKS_LIST = "TracksList";
    public static final String EXTRA_TRACK_ID = "TrackId";
    public static final String EXTRA_TRACK_INDEX = "TrackIndex";
    public static final String EXTRA_TRACK_POSITION = "TrackPosition";

    // MediaPlayerService Constants
    public static final String INTENT_ADD_TRACK_NEXT = "com.realgear.samplemusicplayertest.service.MediaPlayerService.AddTrackNext";
    public static final String INTENT_CHANGE_REPEAT = "com.realgear.samplemusicplayertest.service.MediaPlayerService.ChangeRepeat";
    public static final String INTENT_PLAY = "com.realgear.samplemusicplayertest.service.MediaPlayerService.Play";
    public static final String INTENT_STOP = "com.realgear.samplemusicplayertest.service.MediaPlayerService.Stop";
    public static final String INTENT_PLAY_INDEX = "com.realgear.samplemusicplayertest.service.MediaPlayerService.PlayIndex";
    public static final String INTENT_PLAY_NEXT = "com.realgear.samplemusicplayertest.service.MediaPlayerService.PlayNext";
    public static final String INTENT_PLAY_PAUSE = "com.realgear.samplemusicplayertest.service.MediaPlayerService.PlayPause";
    public static final String INTENT_PLAY_PREV = "com.realgear.samplemusicplayertest.service.MediaPlayerService.PlayPrev";
    public static final String INTENT_SET_SEEKBAR = "com.realgear.samplemusicplayertest.service.MediaPlayerService.SetSeekbar";
    public static final String INTENT_UPDATE_QUEUE = "com.realgear.samplemusicplayertest.service.MediaPlayerService.UpdateQueue";

}
