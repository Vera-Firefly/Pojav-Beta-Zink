package net.kdt.pojavlaunch.customcontrols.mouse;

import android.view.MotionEvent;
import android.view.View;

public interface TouchEventProcessor {
    default boolean processTouchEvent(MotionEvent motionEvent, View view) {
        return processTouchEvent(motionEvent);
    }
    default boolean processTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    void cancelPendingActions();
}
