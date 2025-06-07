package com.shapeeditor.util;

/**
 * A utility class for throttling events to limit the frequency of operations.
 * This is useful for reducing the number of expensive operations during rapid events like mouse dragging.
 */
public class EventThrottler {
    /** The minimum time between throttled events in milliseconds */
    private final long throttleInterval;
    
    /** The timestamp of the last throttled event */
    private long lastEventTime;
    
    /**
     * Constructs a new EventThrottler with the specified throttle interval.
     *
     * @param throttleIntervalMs The minimum time between throttled events in milliseconds
     */
    public EventThrottler(long throttleIntervalMs) {
        this.throttleInterval = throttleIntervalMs;
        this.lastEventTime = 0;
    }
    
    /**
     * Checks if an event should be processed based on the throttling rules.
     * If the event should be processed, updates the last event time.
     *
     * @return true if the event should be processed, false if it should be throttled
     */
    public boolean shouldProcessEvent() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEventTime >= throttleInterval) {
            lastEventTime = currentTime;
            return true;
        }
        return false;
    }
    
    /**
     * Resets the throttler, allowing the next event to be processed regardless of time.
     */
    public void reset() {
        lastEventTime = 0;
    }
}