package com.realgear.samplemusicplayertest.utils.lyricsparser;

import java.util.HashMap;

public class Lyric {
    private static final String TAG = Lyric.class.getSimpleName();

    public String mTitle;
    public String mArtist;
    public String mAlbum;
    public String mBy;
    public String mAuthor;
    public int mOffset;
    public long mLength;

    private final HashMap<Long, String> mSyncedLyrics = new HashMap<>();

    public void addLine(Long time, String line) {
        this.mSyncedLyrics.put(time, line);
    }

    public HashMap<Long, String> getSyncedLyrics() {
        return this.mSyncedLyrics;
    }
}
