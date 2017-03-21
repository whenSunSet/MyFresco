package com.facebook.drawee.componnents;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 这个class保存在Drawee中运行的记录
 * 有记录的最后几个事件用于调试目的。如果你想禁用它那么调用{@link DraweeEventTracker.disable()}
 * 在{@link Fresco.initialize()}之前
 * This class keeps a record of internal events that take place in the Drawee.
 * <p/> Having a record of a last few events is useful for debugging purposes. If you want to
 * disable it, call {@link DraweeEventTracker.disable()} before {@link Fresco.initialize()}.
 */
public class DraweeEventTracker {

    private final Queue<Event> mEventQueue = new ArrayBlockingQueue<>(MAX_EVENTS_TO_TRACK);

    private static final int MAX_EVENTS_TO_TRACK = 20;
    private static final DraweeEventTracker sInstance = new DraweeEventTracker();

    private static boolean sEnabled = true;

    public enum Event {
        ON_SET_HIERARCHY,
        ON_CLEAR_HIERARCHY,
        ON_SET_CONTROLLER,
        ON_CLEAR_OLD_CONTROLLER,
        ON_CLEAR_CONTROLLER,
        ON_INIT_CONTROLLER,
        ON_ATTACH_CONTROLLER,
        ON_DETACH_CONTROLLER,
        ON_RELEASE_CONTROLLER,
        ON_DATASOURCE_SUBMIT,
        ON_DATASOURCE_RESULT,
        ON_DATASOURCE_RESULT_INT,
        ON_DATASOURCE_FAILURE,
        ON_DATASOURCE_FAILURE_INT,
        ON_HOLDER_ATTACH,
        ON_HOLDER_DETACH,
        ON_DRAWABLE_SHOW,
        ON_DRAWABLE_HIDE,
        ON_ACTIVITY_START,
        ON_ACTIVITY_STOP,
        ON_RUN_CLEAR_CONTROLLER,
        ON_SCHEDULE_CLEAR_CONTROLLER,
        ON_SAME_CONTROLLER_SKIPPED,
        ON_SUBMIT_CACHE_HIT
    }

    private DraweeEventTracker() {
    }

    public static DraweeEventTracker newInstance() {
        return sEnabled ? new DraweeEventTracker() : sInstance;
    }

    /**
     * Disable DraweeEventTracker. Need to call before initialize Fresco.
     */
    public static void disable() {
        sEnabled = false;
    }

    public void recordEvent(Event event) {
        if (!sEnabled) {
            return;
        }
        if (mEventQueue.size() + 1 > MAX_EVENTS_TO_TRACK) {
            mEventQueue.poll();
        }
        mEventQueue.add(event);
    }

    @Override
    public String toString() {
        return mEventQueue.toString();
    }
}
