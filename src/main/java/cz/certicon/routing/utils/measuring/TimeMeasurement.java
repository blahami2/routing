/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils.measuring;

import cz.certicon.routing.model.basic.Time;
import cz.certicon.routing.model.basic.TimeUnits;

/**
 * Time measurement class. Uses {@link TimeUnits} to determine the time unit.
 * Default is NANOSECONDS.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class TimeMeasurement {

    private TimeUnits timeUnits = TimeUnits.MILLISECONDS;
    private long start = -1;
    private long time;
    private long accumulated = 0;

    public void start() {
        accumulated = 0;
        start = System.nanoTime();
    }

    public void setTimeUnits( TimeUnits timeUnits ) {
        this.timeUnits = timeUnits;
    }

    /**
     * Stops the timer, saves the elapsed time and returns it.
     *
     * @return elapsed time in {@link TimeUnits}
     */
    public long stop() {
        if ( start == -1 ) {
            return 0;
        }
        time = System.nanoTime() - start;
        return timeUnits.fromNano( accumulated + time );
    }

    /**
     * Returns the last saved elapsed time. Does not start nor stop the timer.
     *
     * @return last saved elapsed time in (@link TimeUnits}
     */
    public long getTimeElapsed() {
        if ( start == -1 ) {
            return 0;
        }
        return timeUnits.fromNano( accumulated + time );
    }

    public Time getTime() {
        return new Time( TimeUnits.NANOSECONDS, accumulated + time );
    }

    /**
     * Returns the elapsed time. Does not stop the timer (does not save it).
     *
     * @return elapsed time in (@link TimeUnits}
     */
    public long getCurrentTimeElapsed() {
        if ( start == -1 ) {
            return 0;
        }
        return timeUnits.fromNano( accumulated + ( System.nanoTime() - start ) );
    }

    public long restart() {
        long a = stop();
        start();
        return a;
    }

    public void clear() {
        start = -1;
        accumulated = 0;
    }

    public long pause() {
        if ( start == -1 ) {
            return 0;
        }
        time = System.nanoTime() - start;
        accumulated += time;
        return timeUnits.fromNano( accumulated );
    }

    public void continue_() {
        start = System.nanoTime();
    }

    public String getTimeString() {
        return getTimeElapsed() + " " + timeUnits.getUnit();
    }

    public String getCurrentTimeString() {
        return getCurrentTimeElapsed() + " " + timeUnits.getUnit();
    }
}
