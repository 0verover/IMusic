package com.realgear.samplemusicplayertest.ui.adapters.models;

import android.content.Context;

import com.realgear.samplemusicplayertest.utils.lyricsparser.Lyric;

public class LyricRecyclerViewItem extends BaseRecyclerViewItem {

    private long mTimeStamp;
    private String mContent;

    private boolean mEnabled;

    public LyricRecyclerViewItem(String content, long timeStamp) {
        super("", ItemType.LYRIC);

        this.mContent = content;
        this.mTimeStamp = timeStamp;
    }

    public String getContent() {
        return this.mContent;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public boolean isEnabled() { return this.mEnabled; }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    @Override
    public void onCache(Context context) {

    }

    @Override
    public int getHashCode() {
        int result = String.valueOf(this.mTimeStamp).hashCode();
        result = 31 * result * this.mContent.hashCode();

        return result;
    }

    @Override
    public int getId() {
        return (int)this.mTimeStamp;
    }
}
