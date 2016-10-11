package com.basmapp.marshal.ui.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AutoScrollViewPager extends ViewPager {

    public static final int DEFAULT_INTERVAL = 1500;

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    private long interval = DEFAULT_INTERVAL;
    private int direction = LEFT;

    private Handler handler;
    private boolean isAutoScroll = false;
    private boolean isStopByTouch = false;

    public static final int SCROLL_WHAT = 0;

    private PageTransformer pageTransformer;

    public AutoScrollViewPager(Context paramContext) {
        super(paramContext);
        init();
    }

    public AutoScrollViewPager(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    // Fix for PageTransform to work on ViewPager with padding //

    @Override
    public void setPageTransformer(boolean reverseDrawingOrder, ViewPager.PageTransformer transformer) {
        this.pageTransformer = transformer;
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        fixedPageScrolled();
    }

    protected void fixedPageScrolled() {

        int clientWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

        if (pageTransformer != null) {
            final int scrollX = getScrollX();
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                final ViewPager.LayoutParams lp = (ViewPager.LayoutParams) child.getLayoutParams();

                if (lp.isDecor) continue;
                // Note the getPaddingLeft() that now exists
                final float transformPos = (float) (child.getLeft() - getPaddingLeft() - scrollX) / clientWidth;
                pageTransformer.transformPage(child, transformPos);
            }
        }
    }

    // Fix for PageTransform to work on ViewPager with padding //

    private void init() {
        handler = new MyHandler(this);
    }

    // start auto scroll, first scroll delay time is interval
    public void startAutoScroll() {
        isAutoScroll = true;
        sendScrollMessage(interval);
    }

    // stop auto scroll
    public void stopAutoScroll() {
        isAutoScroll = false;
        handler.removeMessages(SCROLL_WHAT);
    }

    private void sendScrollMessage(long delayTimeInMills) {
        // remove messages before, keeps one message is running at most
        handler.removeMessages(SCROLL_WHAT);
        handler.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills);
    }

    public void scrollOnce() {
        PagerAdapter adapter = getAdapter();
        int currentItem = getCurrentItem();
        int totalCount;
        if (adapter == null || (totalCount = adapter.getCount()) <= 1) {
            return;
        }

        int nextItem = (direction == LEFT) ? --currentItem : ++currentItem;
        if (nextItem < 0) {
            setCurrentItem(totalCount - 1, true);
        } else if (nextItem == totalCount) {
            setCurrentItem(0, true);
        } else {
            setCurrentItem(nextItem, true);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);

        if ((action == MotionEvent.ACTION_DOWN) && isAutoScroll) {
            isStopByTouch = true;
            stopAutoScroll();
        } else if (ev.getAction() == MotionEvent.ACTION_UP && isStopByTouch) {
            startAutoScroll();
        }

        getParent().requestDisallowInterceptTouchEvent(true);

        return super.dispatchTouchEvent(ev);
    }

    private static class MyHandler extends Handler {

        private final WeakReference<AutoScrollViewPager> autoScrollViewPager;

        public MyHandler(AutoScrollViewPager autoScrollViewPager) {
            this.autoScrollViewPager = new WeakReference<>(autoScrollViewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SCROLL_WHAT:
                    AutoScrollViewPager pager = this.autoScrollViewPager.get();
                    if (pager != null) {
                        pager.scrollOnce();
                        pager.sendScrollMessage(pager.interval);
                    }
                default:
                    break;
            }
        }
    }

    // set auto scroll time in milliseconds, default is DEFAULT_INTERVAL
    public void setInterval(long interval) {
        this.interval = interval;
    }

    // set auto scroll direction, default is LEFT (for RTL)
    public void setDirection(int direction) {
        this.direction = direction;
    }

}