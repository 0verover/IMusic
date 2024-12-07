package com.realgear.samplemusicplayertest.ui.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.realgear.samplemusicplayertest.ui.adapters.helpers.BaseViewHelper;
import com.realgear.samplemusicplayertest.ui.adapters.models.BaseRecyclerViewItem;
import com.realgear.samplemusicplayertest.ui.adapters.models.LyricRecyclerViewItem;
import com.realgear.samplemusicplayertest.ui.adapters.viewholders.BaseViewHolder;
import com.realgear.samplemusicplayertest.ui.adapters.viewholders.LyricViewHolder;
import com.realgear.samplemusicplayertest.ui.fragments.bottomview.FragmentLyrics;
import com.realgear.samplemusicplayertest.utils.lyricsparser.Lyric;

import java.util.TreeMap;

public class LyricsRecyclerViewAdapter extends SimpleBaseRecyclerViewAdapter {

    private RecyclerView mRecyclerView;
    private FragmentLyrics.CenterLayoutManager mLayoutManager;

    public LyricsRecyclerViewAdapter(RecyclerView recyclerView, FragmentLyrics.CenterLayoutManager layoutManager) {
        this.mRecyclerView = recyclerView;
        this.mLayoutManager = layoutManager;
    }

    public void setLyrics(Lyric lyric) {

        this.m_vItems.clear();

        TreeMap<Long, String> lyrics = new TreeMap<>(lyric.getSyncedLyrics());

        lyrics.forEach((timestamp, content) -> {
            this.m_vItems.add(new LyricRecyclerViewItem(content, timestamp));
        });
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseRecyclerViewItem.ItemType itemType = BaseRecyclerViewItem.ItemType.values()[viewType];

        switch (itemType) {
            case LYRIC:
                var viewHolder = BaseViewHelper.onCreateViewHolder(LyricViewHolder.class, parent);
                viewHolder.onInitializeView(BaseRecyclerViewAdapter.ViewType.LIST);
                return viewHolder;
        }

        return null;
    }

    public void onUpdateLyricsState(long duration) {
        boolean prev_has_changed = false;
        boolean prev_is_enabled = false;
        for (int i = 0; i < this.getItemCount(); i++) {
            var item = (LyricRecyclerViewItem)this.m_vItems.get(i);
            boolean has_changed = false;


            if (item.getTimeStamp() <= duration) {
                if (!item.isEnabled()) { has_changed = true;}

                item.setEnabled(true);
                prev_is_enabled = true;
            }
            else {
                if (item.isEnabled()) { has_changed = true;}

                item.setEnabled(false);

                if (prev_has_changed && prev_is_enabled) {
                    if (((LyricRecyclerViewItem) this.m_vItems.get(i - 1)).isEnabled()) {
                        mRecyclerView.smoothScrollToPosition(i - 1);
                    }
                }

                prev_is_enabled = false;
            }


            prev_has_changed = has_changed;
            if (has_changed) {
                this.notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBindViewHolder(this.m_vItems.get(position));
        ((LyricViewHolder)holder).setEnabled(((LyricRecyclerViewItem)this.m_vItems.get(position)).isEnabled());
    }

}
