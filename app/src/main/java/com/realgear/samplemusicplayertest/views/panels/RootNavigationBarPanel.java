package com.realgear.samplemusicplayertest.views.panels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.realgear.multislidinguppanel.BasePanelView;
import com.realgear.multislidinguppanel.IPanel;
import com.realgear.multislidinguppanel.MultiSlidingUpPanelLayout;
import com.realgear.readable_bottom_bar.ReadableBottomBar;
import com.realgear.samplemusicplayertest.R;
import com.realgear.samplemusicplayertest.threads.UIThread;
import com.realgear.samplemusicplayertest.ui.adapters.StateFragmentAdapter;
import com.realgear.samplemusicplayertest.ui.fragments.FragmentHome;
import com.realgear.samplemusicplayertest.ui.fragments.FragmentLibrary;
import com.realgear.samplemusicplayertest.utils.BackEventHandler;

public class RootNavigationBarPanel extends BasePanelView {

    private ViewPager2 rootViewPager;

    private FrameLayout mParentView;
    private ReadableBottomBar rootNavigationBar;

    private FrameLayout mFrameView_Settings;

    private Runnable m_vBackEvent = () -> {
        rootNavigationBar.selectTabAt(0, true);
    };

    private Runnable m_vPanelBackEvent = this::collapsePanel;

    public RootNavigationBarPanel(@NonNull Context context, MultiSlidingUpPanelLayout panelLayout) {
        super(context, panelLayout);

        getContext().setTheme(R.style.Theme_SampleMusicPlayerTest);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_root_navigation_bar, this, true);
    }

    @Override
    public void onCreateView() {
        this.setPanelState(MultiSlidingUpPanelLayout.COLLAPSED);
        this.setSlideDirection(MultiSlidingUpPanelLayout.SLIDE_DOWN);

        this.setPeakHeight(getResources().getDimensionPixelSize(R.dimen.navigation_bar_height));
    }

    @Override
    public void onBindView() {
        rootViewPager = getMultiSlidingUpPanel().findViewById(R.id.root_view_pager);
        mParentView = findViewById(R.id.navigation_bar_parent_view);
        rootNavigationBar = findViewById(R.id.root_navigation_bar);
        mFrameView_Settings = findViewById(R.id.frame_view_settings);

        mFrameView_Settings.setVisibility(GONE);

        StateFragmentAdapter adapter = new StateFragmentAdapter(getSupportFragmentManager(), getLifecycle());

        adapter.addFragment(FragmentHome.class);
        adapter.addFragment(FragmentLibrary.class);

        rootViewPager.setAdapter(adapter);
        rootViewPager.setOffscreenPageLimit(2);
        rootNavigationBar.setupWithViewPager2(rootViewPager);

        rootViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position != 0) {
                    BackEventHandler.getInstance().addBackEvent(m_vBackEvent);
                }
                else {
                    BackEventHandler.getInstance().removeBackEvent(m_vBackEvent);
                }
            }
        });

        rootNavigationBar.setOnTabSelectListener(new ReadableBottomBar.OnTabSelectListener() {
            @Override
            public void onTabSelected(int prevIndex, @Nullable ReadableBottomBar.Tab prevTab, int newIndex, @NonNull ReadableBottomBar.Tab newTab) {
                if (newTab.getId() == R.id.tab_settings) {
                    expandPanel();
                }
                else {
                    if (getPanelState() == MultiSlidingUpPanelLayout.EXPANDED) {
                        collapsePanel();
                    }
                }
            }

            @Override
            public void onTabReselected(int i, @NonNull ReadableBottomBar.Tab tab) {

            }
        });
    }

    @Override
    public void onSliding(@NonNull IPanel<View> panel, int top, int dy, float slidingOffset) {
        super.onSliding(panel, top, dy, slidingOffset);

        if (UIThread.getInstance().isNoLimitFlag()) {
            int top_padding = Math.max(0, (int) (this.mParentSlidingPanel.getPaddingTop() * slidingOffset));
            this.mParentView.setPadding(0, top_padding, 0, 0);

            if (slidingOffset > 0.0F && panel == this) {
                this.rootNavigationBar.setVisibility(VISIBLE);
                this.mFrameView_Settings.setVisibility(VISIBLE);

                float fade_start = 0.8F;

                this.rootNavigationBar.setAlpha(Math.min(1.0F, 1.0F - (slidingOffset / fade_start)));
                this.mFrameView_Settings.setAlpha(Math.min(1.0F, (slidingOffset - fade_start) * (1F  / (1F - fade_start))));
            }
        }
    }

    @Override
    public void onPanelStateChanged(int panelSate) {
        UIThread.getInstance().onPanelStateChanged(this.getClass(), panelSate);

        if (panelSate == MultiSlidingUpPanelLayout.COLLAPSED) {
            this.setSlideDirection(MultiSlidingUpPanelLayout.SLIDE_DOWN);
            if (rootViewPager != null) {
                int index = rootViewPager.getCurrentItem();
                rootNavigationBar.selectTabAt(index, true);
            }
            BackEventHandler.getInstance().removeBackEvent(this.m_vPanelBackEvent);
            if (mFrameView_Settings != null) {
                mFrameView_Settings.setVisibility(GONE);
            }
            UIThread.getInstance().setStatusBarColor(false);
        }

        if (panelSate == MultiSlidingUpPanelLayout.EXPANDED) {
            this.setSlideDirection(MultiSlidingUpPanelLayout.SLIDE_VERTICAL);
            BackEventHandler.getInstance().addBackEvent(this.m_vPanelBackEvent);
            if (rootNavigationBar != null) {
                rootNavigationBar.setVisibility(GONE);
            }
            UIThread.getInstance().setStatusBarColor(true);
        }
    }
}
