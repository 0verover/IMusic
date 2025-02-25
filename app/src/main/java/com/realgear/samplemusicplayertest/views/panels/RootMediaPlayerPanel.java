package com.realgear.samplemusicplayertest.views.panels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.realgear.extensions.bottomsheet.CustomBottomSheetBehavior;
import com.realgear.multislidinguppanel.BasePanelView;
import com.realgear.multislidinguppanel.IPanel;
import com.realgear.multislidinguppanel.MultiSlidingUpPanelLayout;
import com.realgear.samplemusicplayertest.R;
import com.realgear.samplemusicplayertest.theme.AsyncPaletteBuilder;
import com.realgear.samplemusicplayertest.theme.interfaces.PaletteStateListener;
import com.realgear.samplemusicplayertest.threads.MediaPlayerThread;
import com.realgear.samplemusicplayertest.threads.UIThread;
import com.realgear.samplemusicplayertest.utils.BackEventHandler;
import com.realgear.samplemusicplayertest.utils.ViewsUtil;
import com.realgear.samplemusicplayertest.views.mediaviews.MediaPlayerBarView;
import com.realgear.samplemusicplayertest.views.mediaviews.MediaPlayerView;

@SuppressLint("ViewConstructor")
public class RootMediaPlayerPanel extends BasePanelView implements PaletteStateListener {
    private String TAG = this.getClass().getName();

    private MediaPlayerView mMediaPlayerView;
    private MediaPlayerBarView mMediaPlayerBarView;

    private AsyncPaletteBuilder mAsyncPaletteBuilder;

    private View mParentView;

    public final Runnable m_vOnBackPressed = this::collapsePanel;

    private boolean mCanUpdateUI;

    /**
     * @param context
     * @param panelLayout
     * 实现效果为 评论翻出来 显示bar 隐藏播放器
     */
    public RootMediaPlayerPanel(@NonNull Context context, MultiSlidingUpPanelLayout panelLayout) {
        super(context, panelLayout);

        getContext().setTheme(R.style.Theme_SampleMusicPlayerTest);
        mParentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_root_media_player, this, true);
        // 这是播放界面 上方为bar 下方为控制界面
        this.mAsyncPaletteBuilder = new AsyncPaletteBuilder(this);
    }

    @Override
    public void onCreateView() {
        this.setPanelState(MultiSlidingUpPanelLayout.HIDDEN);
        this.setSlideDirection(MultiSlidingUpPanelLayout.SLIDE_VERTICAL);

        this.setPeakHeight(getResources().getDimensionPixelSize(R.dimen.media_player_bar_height));
        this.setUserHiddenMode(true);
    }

    @Override
    public void onBindView() {
        mMediaPlayerView = new MediaPlayerView(findViewById(R.id.media_player_view), getSupportFragmentManager(), getLifecycle());
        mMediaPlayerBarView = new MediaPlayerBarView(findViewById(R.id.media_player_bar_view));

        DisplayMetrics dm = getResources().getDisplayMetrics();
        FrameLayout layout = findViewById(R.id.media_player_bottom_sheet_behavior);

        int peek_height = ViewsUtil.dp2px(this.getContext(), R.dimen.bottom_bar_peek_height);

        int status_bar_height = ViewsUtil.getStatusBarHeight(this.getContext());
        int navigation_bar_height = ViewsUtil.getNavigationBarHeight(this.getContext());

        ViewGroup.LayoutParams params = layout.getLayoutParams();
        if (UIThread.getInstance().isNoLimitFlag()) {
            peek_height += navigation_bar_height;
            mMediaPlayerView.setPeakPadding(peek_height, navigation_bar_height);
            params.height = (dm.heightPixels + navigation_bar_height) - (mPeakHeight);
        }
        else {
            params.height = (dm.heightPixels - mPeakHeight);
        }
        layout.setLayoutParams(params);


        CustomBottomSheetBehavior<FrameLayout> bottomSheetBehavior = CustomBottomSheetBehavior.from(layout);
        bottomSheetBehavior.setSkipAnchored(false);
        bottomSheetBehavior.setAllowUserDragging(true);

        final Runnable onBottomSheetBehaviorBackPressed = () -> {
          bottomSheetBehavior.setState(CustomBottomSheetBehavior.STATE_COLLAPSED);
        };

        float anchor_offset = 0.80F;

        bottomSheetBehavior.setAnchorOffset((int)((dm.heightPixels - mPeakHeight) * anchor_offset));

        bottomSheetBehavior.setPeekHeight(peek_height);
        bottomSheetBehavior.setState(CustomBottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.addBottomSheetCallback(new CustomBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int oldState, int newState) {
                switch (newState) {
                    case CustomBottomSheetBehavior.STATE_COLLAPSED:
                        mMediaPlayerBarView.getRootView().setZ(0F);
                        getMultiSlidingUpPanel().setSlidingEnabled(true);
                        BackEventHandler.getInstance().removeBackEvent(onBottomSheetBehaviorBackPressed);

                        break;
                    case CustomBottomSheetBehavior.STATE_ANCHORED:
                        getMultiSlidingUpPanel().setSlidingEnabled(false);
                        mMediaPlayerBarView.getRootView().setZ(0F);
                        BackEventHandler.getInstance().addBackEvent(onBottomSheetBehaviorBackPressed);
                        break;
                    case CustomBottomSheetBehavior.STATE_EXPANDED:
                        mMediaPlayerBarView.getRootView().setZ(100F);
                        getMultiSlidingUpPanel().setSlidingEnabled(false);
                        BackEventHandler.getInstance().addBackEvent(onBottomSheetBehaviorBackPressed);
                        break;
                    case CustomBottomSheetBehavior.STATE_DRAGGING:
                        getMultiSlidingUpPanel().setSlidingEnabled(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float fadeStart = bottomSheetBehavior.getSlideOffsetByTop(bottomSheetBehavior.getTopByState(CustomBottomSheetBehavior.STATE_ANCHORED));
                float alpha = Math.max(0, slideOffset - fadeStart) / (1F - fadeStart) ;

                mMediaPlayerView.setBlur(slideOffset);
                mMediaPlayerView.onSliding(slideOffset, MediaPlayerView.STATE_PARTIAL);
                mMediaPlayerBarView.onSliding(alpha, MediaPlayerBarView.STATE_PARTIAL);
            }
        });
    }
    @Override
    public void onPanelStateChanged(int panelSate) {
        UIThread.getInstance().onPanelStateChanged(this.getClass(), panelSate);
        if (panelSate == MultiSlidingUpPanelLayout.HIDDEN) {
            mParentView.setVisibility(INVISIBLE);
        }            //  如果panel 完全不可见 也就是 为0了
        else {
            mParentView.setVisibility(VISIBLE);
        }
        if (this.mMediaPlayerView != null)
            this.mMediaPlayerView.onPanelStateChanged(panelSate);
        if (this.mMediaPlayerBarView != null)
            this.mMediaPlayerBarView.onPanelStateChanged(panelSate);

        if (panelSate == MultiSlidingUpPanelLayout.HIDDEN) {
            if (MediaPlayerThread.getInstance() != null && MediaPlayerThread.getInstance().getCallback() != null)
                MediaPlayerThread.getInstance().getCallback().onClickStop();
            BackEventHandler.getInstance().removeBackEvent(this.m_vOnBackPressed);
        }
        if (panelSate == MultiSlidingUpPanelLayout.COLLAPSED) {
            BackEventHandler.getInstance().removeBackEvent(this.m_vOnBackPressed);
        }
        if (panelSate == MultiSlidingUpPanelLayout.EXPANDED) {
            mParentView.setBackgroundColor(Color.parseColor("#1B1B1B"));
            BackEventHandler.getInstance().addBackEvent(this.m_vOnBackPressed);
        }
        else {
            mParentView.setBackgroundColor(Color.TRANSPARENT);
        }
    }
    public void onUpdateMetadata(MediaMetadata mediaMetadata) {
        if (mediaMetadata == null && getPanelState() == MultiSlidingUpPanelLayout.HIDDEN) {
            mParentView.setVisibility(INVISIBLE);
        }
        else {
            mParentView.setVisibility(VISIBLE);
        }

        this.mMediaPlayerBarView.onUpdateMetadata(mediaMetadata);
        this.mMediaPlayerView.onUpdateMetadata(mediaMetadata);

        Bitmap bitmap = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        this.mAsyncPaletteBuilder.onStartAnimation(bitmap);
    }
    public void onPlaybackStateChanged(PlaybackState state) {
        if (state.getState() == PlaybackState.STATE_PLAYING || state.getState() == PlaybackState.STATE_PAUSED) {
            if (this.getPanelState() == MultiSlidingUpPanelLayout.HIDDEN)
                this.collapsePanel();
        }

        if (UIThread.getInstance().canUpdatePanelUI()) {
            this.mMediaPlayerView.onPlaybackStateChanged(state);
            this.mMediaPlayerBarView.onPlaybackStateChanged(state);
        }
    }

    @Override
    public void onSliding(@NonNull IPanel<View> panel, int top, int dy, float slidingOffset) {
        super.onSliding(panel, top, dy, slidingOffset);
        mMediaPlayerView.onSliding(slidingOffset, MediaPlayerView.STATE_NORMAL);
        mMediaPlayerBarView.onSliding(slidingOffset, MediaPlayerBarView.STATE_NORMAL);

        if (UIThread.getInstance().isNoLimitFlag()) {
            int top_padding = Math.max(0, (int) (this.mParentSlidingPanel.getPaddingTop() * slidingOffset));
            this.mMediaPlayerView.getRootView().setPadding(0, top_padding, 0, 0);
            this.mMediaPlayerView.getRootView().setClipToOutline(false);
            ViewsUtil.setMargins(this.mMediaPlayerBarView.getRootView(), 0, top_padding, 0, 0, false);
        }
    }

    @Override
    public void onUpdateVibrantColor(int vibrantColor) {
        this.mMediaPlayerBarView.onUpdateVibrantColor(vibrantColor);
    }

    @Override
    public void onUpdateVibrantDarkColor(int vibrantDarkColor) {
        this.mMediaPlayerBarView.onUpdateVibrantDarkColor(vibrantDarkColor);
        this.mMediaPlayerView.onUpdateVibrantDarkColor(vibrantDarkColor);
    }

    @Override
    public void onUpdateVibrantLightColor(int vibrantLightColor) {
        this.mMediaPlayerView.onUpdateVibrantLightColor(vibrantLightColor);
    }

    @Override
    public void onUpdateMutedColor(int mutedColor) {
        this.mMediaPlayerBarView.onUpdateMutedColor(mutedColor);
    }

    @Override
    public void onUpdateMutedDarkColor(int mutedDarkColor) {
        this.mMediaPlayerBarView.onUpdateMutedDarkColor(mutedDarkColor);
        this.mMediaPlayerView.onUpdateMutedDarkColor(mutedDarkColor);
    }
}
