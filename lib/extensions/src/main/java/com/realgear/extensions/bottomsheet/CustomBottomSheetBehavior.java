package com.realgear.extensions.bottomsheet;

import static androidx.annotation.RestrictTo.Scope.GROUP_ID;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ScrollView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.os.ParcelableCompat;
import androidx.core.os.ParcelableCompatCreatorCallbacks;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.realgear.extensions.R;
import com.realgear.extensions.bottomsheet.utils.ViewPager2Utils;
import com.realgear.extensions.bottomsheet.utils.ViewPagerUtils;

import java.io.Console;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CustomBottomSheetBehavior <V extends View> extends CoordinatorLayout.Behavior<V> {
    public final String TAG = this.getClass().getSimpleName();

    /**
     * Callback for monitoring events about bottom sheets.
     */
    public abstract static class BottomSheetCallback {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param bottomSheet The bottom sheet view.
         * @param oldState    The old state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, {@link #STATE_ANCHORED} or
         *                    {@link #STATE_HIDDEN}.
         * @param newState    The new state. This will be one of {@link #STATE_DRAGGING},
         *                    {@link #STATE_SETTLING}, {@link #STATE_EXPANDED},
         *                    {@link #STATE_COLLAPSED}, {@link #STATE_ANCHORED} or
         *                    {@link #STATE_HIDDEN}.
         */
        public abstract void onStateChanged(@NonNull View bottomSheet, @State int oldState, @State int newState);

        /**
         * Called when the bottom sheet is being dragged.
         *
         * @param bottomSheet The bottom sheet view.
         * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset
         *                    increases as this bottom sheet is moving upward. From 0 to 1 the sheet
         *                    is between collapsed and expanded states and from -1 to 0 it is
         *                    between hidden and collapsed states.
         */
        public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);
    }

    /**
     * Stub/no-op implementations of all methods of {@link BottomSheetCallback}.
     * Override this if you only care about a few of the available callback methods.
     */
    public abstract static class SimpleBottomSheetCallback extends BottomSheetCallback {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, @State int oldState, @State int newState) {
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    }

    /**
     * The bottom sheet is dragging.
     */
    public static final int STATE_DRAGGING = 1;

    /**
     * The bottom sheet is settling.
     */
    public static final int STATE_SETTLING = 2;

    /**
     * The bottom sheet is expanded.
     */
    public static final int STATE_EXPANDED = 3;

    /**
     * The bottom sheet is collapsed.
     */
    public static final int STATE_COLLAPSED = 4;

    /**
     * The bottom sheet is hidden.
     */
    public static final int STATE_HIDDEN = 5;

    /**
     * The bottom sheet is anchored.
     */
    public static final int STATE_ANCHORED = 6;

    /**
     * @hide
     */
    @RestrictTo(GROUP_ID)
    @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_DRAGGING, STATE_SETTLING, STATE_HIDDEN, STATE_ANCHORED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    /**
     * Peek at the 16:9 ratio keyline of its parent.
     * <p>
     * <p>This can be used as a parameter for {@link #setPeekHeight(int)}.
     * {@link #getPeekHeight()} will return this when the value is set.</p>
     */
    public static final int PEEK_HEIGHT_AUTO = -1;

    private static final float HIDE_THRESHOLD = 0.5f;

    private static final float HIDE_FRICTION = 0.1f;

    private static final float EXPAND_FRICTION = 0.2f;

    private static final float COLLAPSE_FRICTION = 0.2f;

    private float mMinimumVelocity;

    private float mMaximumVelocity;

    private int mPeekHeight;

    private boolean mPeekHeightAuto;

    private int mPeekHeightMin;

    private int mAnchorOffset;

    private boolean mAllowUserDragging = true;

    int mMinOffset;

    int mMaxOffset;

    boolean mHideable;

    private boolean mSkipCollapsed;

    private boolean mSkipAnchored;

    private boolean mDisableExpanded;

    @State
    int mState = STATE_COLLAPSED;

    @State
    int mPrevState = STATE_COLLAPSED;

    ViewDragHelper mViewDragHelper;

    private boolean mIgnoreEvents;

    private boolean mNestedScrolled;

    int mParentHeight;

    int mParentTopPadding;

    WeakReference<V> mViewRef;

    WeakReference<View> mNestedScrollingChildRef;

    private List<BottomSheetCallback> mCallbacks = new ArrayList<>(2);

    private VelocityTracker mVelocityTracker;

    int mActivePointerId;

    private int mInitialY;

    boolean mTouchingScrollingChild;

    /**
     * Default constructor for instantiating AnchorBottomSheetBehaviors.
     */
    public CustomBottomSheetBehavior() {
    }

    /**
     * Default constructor for inflating AnchorBottomSheetBehaviors from layout.
     *
     * @param context The {@link Context}.
     * @param attrs   The {@link AttributeSet}.
     */
    public CustomBottomSheetBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.google.android.material.R.styleable.BottomSheetBehavior_Layout);
        TypedValue value = a.peekValue(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight);
        if (value != null && value.data == PEEK_HEIGHT_AUTO) {
            setPeekHeight(value.data);
        } else {
            setPeekHeight(a.getDimensionPixelSize(
                    com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, PEEK_HEIGHT_AUTO));
        }
        setHideable(a.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
        setSkipCollapsed(a.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed,
                false));
        setSkipAnchored(a.getBoolean(R.styleable.CustomBottomSheetBehavior_behavior_skipAnchored, false));
        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.CustomBottomSheetBehavior);
        mAnchorOffset = (int) a.getDimension(R.styleable.CustomBottomSheetBehavior_behavior_anchorOffset, 0);
        //noinspection WrongConstant
        mState = a.getInt(R.styleable.CustomBottomSheetBehavior_behavior_defaultState, mState);
        a.recycle();

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    void invalidateScrollingChild() {
        final View scrollingChild = findScrollingChild(mViewRef.get());
        mNestedScrollingChildRef = new WeakReference<>(scrollingChild);
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
        return (Parcelable) new SavedState(super.onSaveInstanceState(parent, child), mState);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, child, ss.getSuperState());
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED;
        } else {
            mState = ss.state;
        }
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            ViewCompat.setFitsSystemWindows(child, true);
        }
        int savedTop = child.getTop();
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection);
        // Offset the bottom sheet
        mParentTopPadding = parent.getPaddingTop();
        mParentHeight = parent.getHeight() - (parent.getPaddingTop() + parent.getPaddingBottom());
        int peekHeight;
        if (mPeekHeightAuto) {
            if (mPeekHeightMin == 0) {
                mPeekHeightMin = parent.getResources().getDimensionPixelSize(
                        com.google.android.material.R.dimen.design_bottom_sheet_peek_height_min);
            }
            peekHeight = Math.max(mPeekHeightMin, mParentHeight - parent.getWidth() * 9 / 16);
        } else {
            peekHeight = mPeekHeight;
        }
        mMinOffset = Math.max(0, mParentHeight - child.getHeight());
        if (mDisableExpanded) {
            mMinOffset = mAnchorOffset;
        }
        mMaxOffset = Math.max(mParentHeight - peekHeight, mMinOffset);
        if (mState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset);
        } else if (mHideable && mState == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, mParentHeight);
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset);
        } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.getTop());
        } else if (mState == STATE_ANCHORED) {
            if (mAnchorOffset > mMinOffset) {
                ViewCompat.offsetTopAndBottom(child, mAnchorOffset);
            } else {
                mState = STATE_EXPANDED;
                ViewCompat.offsetTopAndBottom(child, mMinOffset);
            }
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback);
        }
        mViewRef = new WeakReference<>(child);
        mNestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        if (!child.isShown() || !mAllowUserDragging) {
            mIgnoreEvents = true;
            return false;
        }

        int action = event.getActionMasked();
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            if (getState() != STATE_DRAGGING) {
                this.mPrevState = getState();
            }
            reset();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchingScrollingChild = false;
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                int initialX = (int) event.getX();
                mInitialY = (int) event.getY();
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                    mActivePointerId = event.getPointerId(event.getActionIndex());
                    mTouchingScrollingChild = true;
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID &&
                        !parent.isPointInChildBounds(child, initialX, mInitialY);
                break;
        }
        if (!mIgnoreEvents && mViewDragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.

        View scroll = findScrollingChild(child);


        if (scroll != null) {
            Boolean is_action_moving = (action == MotionEvent.ACTION_MOVE);
            Boolean is_in_child = parent.isPointInChildBounds(scroll, (int)event.getX(), (int)event.getY());

            if (is_in_child && this.mState != STATE_EXPANDED) {
                return true;
            }
            else {
                /*if (is_in_child) {
                    var diffY = mInitialY - event.getY();
                    if (Math.abs(diffY) > mViewDragHelper.getTouchSlop()) {
                        Log.d(TAG, "onInterceptTouchEvent: DiffY -> [" + diffY + "]");
                        if (diffY < 0) {
                            var recycleView = (RecyclerView)scroll;

                            //Log.d(TAG, "onInterceptTouchEvent: Can Scroll -> [" + recycleView.scroll + "]");
                            if (recycleView.computeVerticalScrollOffset() > 0) {
                                //return true;
                            }
                        }
                    }
                }*/

                return is_action_moving && !mIgnoreEvents && !is_in_child && Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop();
            }
        }
        else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        if (!child.isShown() || !mAllowUserDragging) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(getState() != STATE_DRAGGING) {
                this.mPrevState = getState();
                Log.i(TAG, "Set Prev State to : " + getState());
            }
        }

        int action = event.getActionMasked();
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            Log.i(TAG, "Dragging acton down : " + getState());
            return true;
        }
        if (mViewDragHelper != null) {
            mViewDragHelper.processTouchEvent(event);
        }
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            Log.i(TAG, "actually dragging acton : " + getState());
            reset();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (Math.abs(mInitialY - event.getY()) > mViewDragHelper.getTouchSlop()) {
                mViewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
            }
        }
        return !mIgnoreEvents;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        if (!mAllowUserDragging) {
            return false;
        }
        mNestedScrolled = false;
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx,
                                  int dy, int[] consumed) {
        if (!mAllowUserDragging) {
            return;
        }
        View scrollingChild = mNestedScrollingChildRef.get();
        if (target != scrollingChild) {
            return;
        }
        int currentTop = child.getTop();
        int newTop = currentTop - dy;
        if (dy > 0) { // Upward
            if (newTop < mMinOffset) {
                consumed[1] = currentTop - mMinOffset;
                ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                setStateInternal(STATE_EXPANDED);
            } else {
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom(child, -dy);
                setStateInternal(STATE_DRAGGING);
            }
        } else if (dy < 0) { // Downward
            if (!ViewCompat.canScrollVertically(target, -1)) {
                if (newTop <= mMaxOffset || mHideable) {
                    consumed[1] = dy;
                    ViewCompat.offsetTopAndBottom(child, -dy);
                    setStateInternal(STATE_DRAGGING);
                } else {
                    consumed[1] = currentTop - mMaxOffset;
                    ViewCompat.offsetTopAndBottom(child, -consumed[1]);
                    setStateInternal(STATE_COLLAPSED);
                }
            }
        }
        dispatchOnSlide(child.getTop());
        mNestedScrolled = true;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
        if (!mAllowUserDragging) {
            return;
        }
        if (child.getTop() == mMinOffset) {
            setStateInternal(STATE_EXPANDED);
            return;
        }
        if (target != mNestedScrollingChildRef.get() || !mNestedScrolled) {
            return;
        }

        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        float xvel = mVelocityTracker.getXVelocity(mActivePointerId);
        float yvel = mVelocityTracker.getYVelocity(mActivePointerId);

        int[] out = new int[2];
        calculateTopAndTargetState(child, xvel, yvel, out);
        int top = out[0];
        int targetState = out[1];

        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
        } else {
            setStateInternal(targetState);
        }
        mNestedScrolled = false;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target,
                                    float velocityX, float velocityY) {
        if (!mAllowUserDragging) {
            return false;
        }
        return target == mNestedScrollingChildRef.get() &&
                (mState != STATE_EXPANDED ||
                        super.onNestedPreFling(coordinatorLayout, child, target,
                                velocityX, velocityY));
    }

    /**
     * Sets the height of the bottom sheet when it is collapsed.
     *
     * @param peekHeight The height of the collapsed bottom sheet in pixels, or
     *                   {@link #PEEK_HEIGHT_AUTO} to configure the sheet to peek automatically
     *                   at 16:9 ratio keyline.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    public final void setPeekHeight(int peekHeight) {
        boolean layout = false;
        if (peekHeight == PEEK_HEIGHT_AUTO) {
            if (!mPeekHeightAuto) {
                mPeekHeightAuto = true;
                layout = true;
            }
        } else if (mPeekHeightAuto || mPeekHeight != peekHeight) {
            mPeekHeightAuto = false;
            mPeekHeight = Math.max(0, peekHeight);
            mMaxOffset = mParentHeight - peekHeight;
            layout = true;
        }
        if (layout && mState == STATE_COLLAPSED && mViewRef != null) {
            V view = mViewRef.get();
            if (view != null) {
                view.requestLayout();
            }
        }
    }

    /**
     * Gets the height of the bottom sheet when it is collapsed.
     *
     * @return The height of the collapsed bottom sheet in pixels, or {@link #PEEK_HEIGHT_AUTO}
     * if the sheet is configured to peek automatically at 16:9 ratio keyline
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
     */
    public final int getPeekHeight() {
        return mPeekHeightAuto ? PEEK_HEIGHT_AUTO : mPeekHeight;
    }

    /**
     * Sets the offset of the bottom sheet when it is anchored.
     *
     * @param anchorOffset The offset of the anchored bottom sheet in pixels.
     * @attr ref com.trafi.anchorbottomsheetbehavior.R.styleable#AnchorBottomSheetBehavior_Layout_behavior_anchorOffset
     */
    public final void setAnchorOffset(int anchorOffset) {
        if (mAnchorOffset != anchorOffset) {
            mAnchorOffset = anchorOffset;

            if (mDisableExpanded) {
                mMinOffset = mAnchorOffset;
            }

            if (mState == STATE_ANCHORED) {
                setStateInternal(STATE_SETTLING);
                setState(STATE_ANCHORED);
            }
        }
    }

    /**
     * Gets the offset of the bottom sheet when it is anchored.
     *
     * @return The offset of the anchored bottom sheet in pixels.
     * @attr ref com.trafi.anchorbottomsheetbehavior.R.styleable#AnchorBottomSheetBehavior_Layout_behavior_anchorOffset
     */
    public final int getAnchorOffset() {
        return mAnchorOffset;
    }

    /**
     * Sets whether this bottom sheet can hide when it is swiped down.
     *
     * @param hideable {@code true} to make this bottom sheet hideable.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    public void setHideable(boolean hideable) {
        mHideable = hideable;
    }

    /**
     * Gets whether this bottom sheet can hide when it is swiped down.
     *
     * @return {@code true} if this bottom sheet can hide.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
     */
    public boolean isHideable() {
        return mHideable;
    }

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once. Setting this to true has no effect unless the sheet is hideable.
     *
     * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public void setSkipCollapsed(boolean skipCollapsed) {
        mSkipCollapsed = skipCollapsed;
    }

    /**
     * Sets whether this bottom sheet should skip the collapsed state when it is being hidden
     * after it is expanded once.
     *
     * @return Whether the bottom sheet should skip the collapsed state.
     * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
     */
    public boolean getSkipCollapsed() {
        return mSkipCollapsed;
    }

    /**
     * Sets whether this bottom sheet should skip the anchored state when it is being collapsed from
     * an expanded state or when it is being expanded from a collapsed state.
     *
     * @param skipAnchored True if the bottom sheet should skip the anchored state.
     * @attr ref R.styleable#AnchorBottomSheetBehavior_Layout_behavior_skipAnchored
     */
    public void setSkipAnchored(boolean skipAnchored) {
        mSkipAnchored = skipAnchored;
    }

    /**
     * Sets whether this bottom sheet should skip the anchored state when it is being collapsed from
     * an expanded state or when it is being expanded from a collapsed state.
     *
     * @return Whether the bottom sheet should skip the anchored state.
     * @attr ref R.styleable#AnchorBottomSheetBehavior_Layout_behavior_skipAnchored
     */
    public boolean getSkipAnchored() {
        return mSkipAnchored;
    }

    public boolean isDisableExpanded() {
        return mDisableExpanded;
    }

    public void setDisableExpanded(boolean mDisableExpanded) {
        this.mDisableExpanded = mDisableExpanded;
    }

    /**
     * Registers a callback to be notified of bottom sheet events.
     *
     * @param callback The callback to notify when bottom sheet events occur.
     */
    public void addBottomSheetCallback(BottomSheetCallback callback) {
        mCallbacks.add(callback);
    }

    public void removeBottomSheetCallback(BottomSheetCallback callback) {
        mCallbacks.remove(callback);
    }

    public void setAllowUserDragging(boolean allowUserDragging) {
        mAllowUserDragging = allowUserDragging;
    }

    public boolean getAllowUserDragging() {
        return mAllowUserDragging;
    }

    /**
     * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
     * animation.
     *
     * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED},
     *              {@link #STATE_ANCHORED} or{@link #STATE_HIDDEN}.
     */
    public final void setState(final @State int state) {
        if (state == mState) {
            return;
        }
        if (mViewRef == null) {
            // The view is not laid out yet; modify mState and let onLayoutChild handle it later
            if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_ANCHORED ||
                    (mHideable && state == STATE_HIDDEN)) {
                mState = state;
            }
            return;
        }
        final V child = mViewRef.get();
        if (child == null) {
            return;
        }
        // Start the animation; wait until a pending layout if there is one.
        ViewParent parent = child.getParent();
        if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child)) {
            child.post(new Runnable() {
                @Override
                public void run() {
                    startSettlingAnimation(child, state);
                }
            });
        } else {
            startSettlingAnimation(child, state);
        }
    }

    /**
     * Gets the current state of the bottom sheet.
     *
     * @return One of {@link #STATE_EXPANDED}, {@link #STATE_ANCHORED}, {@link #STATE_COLLAPSED},
     * {@link #STATE_DRAGGING}, and {@link #STATE_SETTLING}.
     */
    @State
    public final int getState() {
        return mState;
    }

    void setStateInternal(@State int state) {
        if (mState == state) {
            return;
        }
        int oldState = mState;
        mState = state;
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null) {
            for (int i = 0; i < mCallbacks.size(); i++) {
                mCallbacks.get(i).onStateChanged(bottomSheet, oldState, state);
            }
        }
    }

    private void reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void calculateTopAndTargetState(View child, float xvel, float yvel, int[] out) {
        int top;
        @State int targetState;

        if (yvel < 0 && Math.abs(yvel) > mMinimumVelocity && Math.abs(yvel) > Math.abs(xvel)) {
            // scrolling up, i.e. expanding
            if (shouldExpand(child, yvel)) {

                if(mPrevState == STATE_COLLAPSED) {
                    top = mAnchorOffset;
                    targetState = STATE_ANCHORED;
                }
                else {
                    top = mMinOffset;
                    targetState = STATE_EXPANDED;
                }
            } else {
                top = mAnchorOffset;
                targetState = STATE_ANCHORED;
            }
        } else if (mHideable && shouldHide(child, yvel)) {
            top = mParentHeight;
            targetState = STATE_HIDDEN;
        } else if (yvel > 0 && Math.abs(yvel) > mMinimumVelocity && Math.abs(yvel) > Math.abs(xvel)) {
            // scrolling down, i.e. collapsing
            if (shouldCollapse(child, yvel)) {

                if(mPrevState == STATE_EXPANDED) {
                    top = mAnchorOffset;
                    targetState = STATE_ANCHORED;
                }
                else {
                    top = mMaxOffset;
                    targetState = STATE_COLLAPSED;
                }
            } else {
                top = mAnchorOffset;
                targetState = STATE_ANCHORED;
            }
        } else {
            // not scrolling much, i.e. stationary
            int currentTop = child.getTop();
            int distanceToExpanded = Math.abs(currentTop - mMinOffset);
            int distanceToCollapsed = Math.abs(currentTop - mMaxOffset);
            int distanceToAnchor = Math.abs(currentTop - mAnchorOffset);
            if (mAnchorOffset > mMinOffset
                    && distanceToAnchor < distanceToExpanded
                    && distanceToAnchor < distanceToCollapsed) {
                top = mAnchorOffset;
                targetState = STATE_ANCHORED;
            } else if (distanceToExpanded < distanceToCollapsed) {
                top = mMinOffset;
                targetState = STATE_EXPANDED;
            } else {
                top = mMaxOffset;
                targetState = STATE_COLLAPSED;
            }
        }

        out[0] = top + mParentTopPadding;
        out[1] = targetState;
    }

    boolean shouldHide(View child, float yvel) {
        if (mSkipCollapsed) {
            return true;
        }
        if (child.getTop() < mMaxOffset) {
            // It should not hide, but collapse.
            return false;
        }
        final float newTop = child.getTop() + yvel * HIDE_FRICTION;
        return Math.abs(newTop - mMaxOffset) / (float) mPeekHeight > HIDE_THRESHOLD;
    }

    boolean shouldExpand(View child, float yvel) {
        if (mSkipAnchored || mMinOffset >= mAnchorOffset) {
            return true;
        }
        int currentTop = child.getTop();
        if (currentTop < mAnchorOffset) {
            return true;
        }
        final float newTop = currentTop + yvel * EXPAND_FRICTION;
        return newTop < mAnchorOffset;
    }

    boolean shouldCollapse(View child, float yvel) {
        if (mSkipAnchored || mMinOffset >= mAnchorOffset) {
            return true;
        }
        int currentTop = child.getTop();
        if (currentTop > mAnchorOffset) {
            return true;
        }
        final float newTop = currentTop + yvel * COLLAPSE_FRICTION;
        return newTop > mAnchorOffset;
    }

    private View findScrollingChild(View view) {
        if (view instanceof NestedScrollView) {
            return ((ViewGroup)view).getChildAt(0);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0, count = group.getChildCount(); i < count; i++) {
                View scrollingChild = findScrollingChild(group.getChildAt(i));
                if (scrollingChild != null) {
                    return scrollingChild;
                }
            }
        }
        return null;
    }

    public int getTopByState(int state) {
        int top;
        switch (state) {
            case STATE_COLLAPSED:
                top = mMaxOffset;
                break;
            case STATE_ANCHORED:
                top = (mAnchorOffset > mMinOffset) ? mAnchorOffset : mMinOffset;
                state = (mAnchorOffset > mMinOffset) ? state : STATE_EXPANDED;
                break;
            case STATE_EXPANDED:
                top = mMinOffset;
                break;

            default:
                if (mHideable && state == STATE_HIDDEN) {
                    top = mParentHeight;
                }
                else {
                    throw new IllegalArgumentException("Illegal state argument: " + state);
                }
                break;
        }

        return top;
    }

    public float getSlideOffsetByTop(int top) {
        top -= mParentTopPadding;

        float slideOffset = -1F;
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null) {
            if (top > mMaxOffset) {
                slideOffset = (float) (mMaxOffset - top) / (mParentHeight - mMaxOffset);
            }
            else {
                slideOffset = (float) (mMaxOffset - top) / (mMaxOffset - mMinOffset);
            }
        }

        return slideOffset;
    }

    void startSettlingAnimation(View child, int state) {
        int top = getTopByState(state);
        
        setStateInternal(STATE_SETTLING);
        if (mViewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
            ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
        }
    }

    private final ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (mState == STATE_DRAGGING) {
                return false;
            }
            if (mTouchingScrollingChild) {
                return false;
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                View scroll = mNestedScrollingChildRef.get();
                if (scroll != null && scroll.canScrollVertically(-1)) {
                    // Let the content scroll up
                    return false;
                }
            }

            return mViewRef != null && mViewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            dispatchOnSlide(top);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            int[] out = new int[2];
            calculateTopAndTargetState(releasedChild, xvel, yvel, out);
            int top = out[0];
            @State int targetState = out[1];

            if (mViewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
                setStateInternal(STATE_SETTLING);
                ViewCompat.postOnAnimation(releasedChild,
                        new SettleRunnable(releasedChild, targetState));
            } else {
                setStateInternal(targetState);
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if(mPrevState == STATE_COLLAPSED) {
                int maxTop = getAnchorOffset() - getPeekHeight();

                return Math.max(top, maxTop);
            }
            if(mPrevState == STATE_EXPANDED) {
                int maxTop = getAnchorOffset() + getPeekHeight();

                return Math.min(Math.max(top, mMinOffset), maxTop);
            }
            else {
                return constrain(top, mMinOffset, mHideable ? mParentHeight : mMaxOffset);
            }
        }

        private int constrain(int amount, int low, int high) {
            return amount < low ? low : (amount > high ? high : amount);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            if (mHideable) {
                return mParentHeight - mMinOffset;
            } else {
                return mMaxOffset - mMinOffset;
            }
        }
    };

    void dispatchOnSlide(int top) {
        View bottomSheet = mViewRef.get();
        if (bottomSheet != null) {
            float slideOffset = getSlideOffsetByTop(top);

            for (int i = 0; i < mCallbacks.size(); i++) {
                mCallbacks.get(i).onSlide(bottomSheet, slideOffset);
            }
        }
    }

    @VisibleForTesting
    int getPeekHeightMin() {
        return mPeekHeightMin;
    }

    private class SettleRunnable implements Runnable {

        private final View mView;

        @State
        private final int mTargetState;

        SettleRunnable(View view, @State int targetState) {
            mView = view;
            mTargetState = targetState;
        }

        @Override
        public void run() {
            if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this);
            } else {
                setStateInternal(mTargetState);
            }
        }
    }

    protected static class SavedState extends AbsSavedState {
        @State
        final int state;

        public SavedState(Parcel source) {
            this(source, null);
        }

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            //noinspection ResourceType
            state = source.readInt();
        }

        public SavedState(Parcelable superState, @State int state) {
            super(superState);
            this.state = state;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });
    }

    /**
     * A utility function to get the {@link CustomBottomSheetBehavior} associated with the {@code view}.
     *
     * @param view The {@link View} with {@link CustomBottomSheetBehavior}.
     * @return The {@link CustomBottomSheetBehavior} associated with the {@code view}.
     */
    @SuppressWarnings("unchecked")
    public static <V extends View> CustomBottomSheetBehavior<V> from(V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof CustomBottomSheetBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with AnchorBottomSheetBehavior");
        }
        return (CustomBottomSheetBehavior<V>) behavior;
    }
}
