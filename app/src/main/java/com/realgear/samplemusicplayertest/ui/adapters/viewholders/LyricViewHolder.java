package com.realgear.samplemusicplayertest.ui.adapters.viewholders;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.realgear.samplemusicplayertest.R;
import com.realgear.samplemusicplayertest.ui.adapters.BaseRecyclerViewAdapter;
import com.realgear.samplemusicplayertest.ui.adapters.models.BaseRecyclerViewItem;
import com.realgear.samplemusicplayertest.ui.adapters.models.LyricRecyclerViewItem;

public class LyricViewHolder extends BaseViewHolder {

    private TextView mLyricContent;

    public LyricViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setEnabled(boolean enabled) {
        if(enabled) {
            this.mLyricContent.setTextColor(Color.WHITE);
            this.mLyricContent.setTextSize(24);
        }
        else {
            this.mLyricContent.setTextColor(Color.parseColor("#A6A6A6"));
            this.mLyricContent.setTextSize(20);
        }
    }

    @Override
    public void onInitializeView(BaseRecyclerViewAdapter.ViewType viewType) {
        this.mLyricContent = itemView.findViewById(R.id.text_view_lyric_content);
    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewItem viewItem) {
        if (viewItem instanceof LyricRecyclerViewItem) {
            var item = (LyricRecyclerViewItem)viewItem;
            this.mLyricContent .setText(item.getContent());
        }
    }

    @Override
    public void onReloadData() {

    }
}
