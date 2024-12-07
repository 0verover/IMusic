package com.realgear.samplemusicplayertest.views;

import android.content.res.ColorStateList;
import android.media.session.PlaybackState;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.realgear.readable_bottom_bar.ReadableBottomBar;
import com.realgear.samplemusicplayertest.R;
import com.realgear.samplemusicplayertest.ui.adapters.StateFragmentAdapter;
import com.realgear.samplemusicplayertest.ui.fragments.bottomview.FragmentLyrics;
import com.realgear.samplemusicplayertest.ui.fragments.bottomview.FragmentQueue;
import com.realgear.samplemusicplayertest.ui.fragments.bottomview.FragmentRelated;

public class MediaPlayerBottomView {
    private View m_vRootView;
    private MaterialCardView mBottomViewCard;

    private ReadableBottomBar m_vNavigationBar;
    private ViewPager2 m_vViewPager;

    private StateFragmentAdapter mAdapter;

    public MediaPlayerBottomView(View rootView, FragmentManager fragmentManager, Lifecycle lifecycle) {
        this.m_vRootView = rootView;

        this.mBottomViewCard = findViewById(R.id.bottom_view_material_card);
        this.m_vNavigationBar = findViewById(R.id.bottom_view_navigation_bar);
        this.m_vViewPager = findViewById(R.id.bottom_view_viewpager);


        this.mAdapter = new StateFragmentAdapter(fragmentManager, lifecycle);

        this.mAdapter.addFragment(FragmentLyrics.class);
        this.mAdapter.addFragment(FragmentQueue.class);
        this.mAdapter.addFragment(FragmentRelated.class);

        this.m_vViewPager.setAdapter(this.mAdapter);
        this.m_vViewPager.setOffscreenPageLimit(3);
        this.m_vNavigationBar.setupWithViewPager2(this.m_vViewPager);
    }


    public <T extends View> T findViewById(@IdRes int id) {
        return this.m_vRootView.findViewById(id);
    }

    public void setPeakPadding(int peak, int padding) {
        ViewGroup.LayoutParams params = this.m_vNavigationBar.getLayoutParams();
        params.height = peak;
        this.m_vNavigationBar.setLayoutParams(params);
        this.m_vNavigationBar.setPadding(0, 0, 0, padding);
    }

    public void onPlaybackStateChanged(PlaybackState state) {
        this.mAdapter.getFragment(FragmentLyrics.class).onPlaybackStateChanged(state);
    }

    public void onUpdateVibrantDarkColor(int vibrantDarkColor) {
    }

    public void onUpdateVibrantLightColor(int vibrantLightColor) {
        this.m_vNavigationBar.setIndicatorColor(vibrantLightColor);
    }

    public void onUpdateMutedDarkColor(int mutedDarkColor) {
        this.mBottomViewCard.setBackgroundTintList(ColorStateList.valueOf(mutedDarkColor));
    }
}
