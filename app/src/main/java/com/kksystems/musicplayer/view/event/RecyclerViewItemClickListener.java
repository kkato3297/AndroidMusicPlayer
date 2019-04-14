package com.kksystems.musicplayer.view.event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;

    public RecyclerViewItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    childView.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (childView.isPressed()) {
                        mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
                    }
                    childView.setPressed(false);
                    break;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // nothing to do...
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // nothing to do...
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
