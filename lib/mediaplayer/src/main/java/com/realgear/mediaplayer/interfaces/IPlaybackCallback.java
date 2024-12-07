package com.realgear.mediaplayer.interfaces;

import android.media.session.PlaybackState;

import com.realgear.mediaplayer.model.Song;

public interface IPlaybackCallback {
    void onPlaybackStateChanged(PlaybackState playbackState);
    void onUpdateMetadata(Song song);
}
