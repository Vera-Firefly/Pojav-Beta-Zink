package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.utils.JREUtils;

import org.lwjgl.glfw.CallbackBridge;

import top.fifthlight.touchcontroller.proxy.data.Offset;
import top.fifthlight.touchcontroller.proxy.message.AddPointerMessage;
import top.fifthlight.touchcontroller.proxy.message.ClearPointerMessage;
import top.fifthlight.touchcontroller.proxy.message.RemovePointerMessage;

public class InGameEventProcessor implements TouchEventProcessor {
    private final Handler mGestureHandler = new Handler(Looper.getMainLooper());
    private final double mSensitivity;
    private boolean mEventTransitioned = true;
    private final PointerTracker mTracker = new PointerTracker();
    private final LeftClickGesture mLeftClickGesture = new LeftClickGesture(mGestureHandler);
    private final RightClickGesture mRightClickGesture = new RightClickGesture(mGestureHandler);
    private static final SparseIntArray pointerIdMap = new SparseIntArray();
    private static int nextPointerId = 1;

    public InGameEventProcessor(double sensitivity) {
        mSensitivity = sensitivity;
    }

    @Override
    public boolean processTouchEvent(MotionEvent motionEvent, View view) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("InGameEventProcessor", "ACTION_DOWN");
                if (JREUtils.touchControllerProxy != null) {
                    int pointerId = nextPointerId++;
                    pointerIdMap.put(motionEvent.getPointerId(0), pointerId);
                    JREUtils.touchControllerProxy.trySend(
                            new AddPointerMessage(
                                    pointerId,
                                    new Offset(
                                            motionEvent.getX(0) / view.getWidth(),
                                            motionEvent.getY(0) / view.getHeight()
                                    )
                            )
                    );
                }
                mTracker.startTracking(motionEvent);
                if (LauncherPreferences.PREF_DISABLE_GESTURES) break;
                mEventTransitioned = false;
                checkGestures();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("InGameEventProcessor", "ACTION_POINTER_DOWN");
                if (JREUtils.touchControllerProxy != null) {
                    int pointerId = nextPointerId++;
                    int i = motionEvent.getActionIndex();
                    pointerIdMap.put(motionEvent.getPointerId(i), pointerId);
                    JREUtils.touchControllerProxy.trySend(
                            new AddPointerMessage(
                                    pointerId,
                                    new Offset(
                                            motionEvent.getX(i) / view.getWidth(),
                                            motionEvent.getY(i) / view.getHeight()
                                    )
                            )
                    );
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (JREUtils.touchControllerProxy != null) {
                    for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                        int pointerId = pointerIdMap.get(motionEvent.getPointerId(i));
                        if (pointerId == 0) {
                            Log.d("InGameEventProcessor", "Move pointerId is 0");
                        }
                        JREUtils.touchControllerProxy.trySend(
                                new AddPointerMessage(
                                        pointerId,
                                        new Offset(
                                                motionEvent.getX(i) / view.getWidth(),
                                                motionEvent.getY(i) / view.getHeight()
                                        )
                                )
                        );
                    }
                }
                mTracker.trackEvent(motionEvent);
                float[] motionVector = mTracker.getMotionVector();
                CallbackBridge.mouseX += motionVector[0] * mSensitivity;
                CallbackBridge.mouseY += motionVector[1] * mSensitivity;
                CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);
                if (LauncherPreferences.PREF_DISABLE_GESTURES) break;
                checkGestures();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d("InGameEventProcessor", "ACTION_UP | ACTION_CANCEL");
                if (JREUtils.touchControllerProxy != null) {
                    JREUtils.touchControllerProxy.trySend(ClearPointerMessage.INSTANCE);
                    pointerIdMap.clear();
                }
                mTracker.cancelTracking();
                cancelGestures(false);
            case MotionEvent.ACTION_POINTER_UP:
                Log.d("InGameEventProcessor", "ACTION_POINTER_UP");
                if (JREUtils.touchControllerProxy != null) {
                    int i = motionEvent.getActionIndex();
                    int pointerId = pointerIdMap.get(motionEvent.getPointerId(i));
                    if (pointerId == 0) {
                        Log.d("InGameEventProcessor", "Remove pointerId is 0");
                    } else {
                        pointerIdMap.delete(pointerId);
                        JREUtils.touchControllerProxy.trySend(new RemovePointerMessage(pointerId));
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void cancelPendingActions() {
        if (JREUtils.touchControllerProxy != null) {
            Log.d("InGameEventProcessor", "Clear pointers");
            JREUtils.touchControllerProxy.trySend(ClearPointerMessage.INSTANCE);
            pointerIdMap.clear();
        }
        pointerIdMap.clear();
        cancelGestures(true);
    }

    private void checkGestures() {
        mLeftClickGesture.inputEvent();
        // Only register right click events if it's a fresh event stream, not one after a transition.
        // This is done to avoid problems when people hold the button for just a bit too long after
        // exiting a menu for example.
        if (!mEventTransitioned) mRightClickGesture.inputEvent();
    }

    private void cancelGestures(boolean isSwitching) {
        mEventTransitioned = true;
        mLeftClickGesture.cancel(isSwitching);
        mRightClickGesture.cancel(isSwitching);
    }
}
