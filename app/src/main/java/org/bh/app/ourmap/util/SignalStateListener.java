package org.bh.app.ourmap.util;

/**
 * Made for Our Map by and copyrighted to Blue Husky Programming, ©2014 GPLv3.<hr/>
 *
 * @author Kyli Rouge of Blue Husky Programming
 * @version 1.0.0
 * @since 2014-08-04
 */
public interface SignalStateListener {
    /**
     * Called when a signal strength change is detected, where 0 is dead, 1 is nominal, and higher
     * is excellent. Negative values should never appear. Nominal (1.0) for GSM is -73 dBm (10).
     *
     * @param signalStrength The current signal strength
     *                      <ul>
     *                           <li>{@code 0.0} represents negligibly low signal</li>
     *                           <li>{@code 1.0} represents nominal signal</li>
     *                           <li>{@link Double#NaN} represents dead or unknown signal</li>
     *                      </ul>
     */
    public void signalStrengthUpdate(double signalStrength);
}
