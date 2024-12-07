package com.realgear.samplemusicplayertest.ui.fragments.bottomview;

import android.content.Context;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.realgear.samplemusicplayertest.R;
import com.realgear.samplemusicplayertest.ui.adapters.LyricsRecyclerViewAdapter;
import com.realgear.samplemusicplayertest.utils.lyricsparser.Lyric;
import com.realgear.samplemusicplayertest.utils.lyricsparser.LyricsUtil;

import java.io.InputStream;
import java.util.HashMap;

public class FragmentLyrics extends Fragment {

    public class CenterLayoutManager extends LinearLayoutManager {

        public CenterLayoutManager(Context context) {
            super(context);
        }

        public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public CenterLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class CenterSmoothScroller extends LinearSmoothScroller {

            private final float MILLISECONDS_PER_INCH = 15F;

            CenterSmoothScroller(Context context) {
                super(context);
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

            @Override
            public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            }
        }
    }

    private RecyclerView mLyricsView;
    private LyricsRecyclerViewAdapter mAdapter;
    private RecyclerView.SmoothScroller mSmoothScoller;

    private HashMap<Long, TextView> m_vSyncedLyrics = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lyrics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mLyricsView = findViewById(R.id.recycler_view_lyrics);

        initLyrics();
    }

    public void initLyrics() {
        InputStream stream = this.getView().getResources().openRawResource(R.raw.witt_lowry);

        Lyric lyric = LyricsUtil.parseLyric(stream, "UTF-8");

        var layoutManager = new CenterLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false);
        this.mAdapter = new LyricsRecyclerViewAdapter(this.mLyricsView, layoutManager);
        this.mAdapter.setLyrics(lyric);
        this.mAdapter.setHasStableIds(false);



        this.mLyricsView.setLayoutManager(layoutManager);
        this.mLyricsView.setAdapter(this.mAdapter);
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return this.getView().findViewById(id);
    }

    public void onPlaybackStateChanged(PlaybackState state) {
        this.mAdapter.onUpdateLyricsState(state.getPosition());
    }
}
