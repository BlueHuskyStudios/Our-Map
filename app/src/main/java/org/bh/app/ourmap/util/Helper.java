package org.bh.app.ourmap.util;

/**
 * Made for Our Map by and copyrighted to Blue Husky Programming, ©2014 GPLv3.<hr/>
 *
 * @author Kyli Rouge of Blue Husky Programming
 * @version 1.0.0
 * @since 2014-08-05
 */
public /*static*/ class Helper { private Helper(){}

    /**
     * Converts meter-based accuracy to Google Maps Zoom -based accuracy. Values based on the table
     * at http://bit.ly/maps-accuracy-table:
     * <table>
     *     <thead>
     *         <tr>
     *             <th>Value</th>
     *             <th>Description</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr><td>0</td><td>Unknown location.</td></tr>
     *         <tr><td>1</td><td>Country level accuracy.</td></tr>
     *         <tr><td>2</td><td>Region (state, province, prefecture, etc.) level accuracy.</td></tr>
     *         <tr><td>3</td><td>Sub-region (county, municipality, etc.) level accuracy.</td></tr>
     *         <tr><td>4</td><td>Town (city, village) level accuracy.</td></tr>
     *         <tr><td>5</td><td>Post code (zip code) level accuracy.</td></tr>
     *         <tr><td>6</td><td>Street level accuracy.</td></tr>
     *         <tr><td>7</td><td>Intersection level accuracy.</td></tr>
     *         <tr><td>8</td><td>Address level accuracy.</td></tr>
     *         <tr><td>9</td><td>Premise (building name, property name, shopping center, etc.) level accuracy.</td></tr>
     *     </tbody>
     * </table>
     * @param accuracyInMeters The accuracy, in meters, to be converted to a camera zoom level
     * @return a camera zoom level
     */
    public static float locationAccuracyToCameraZoom(float accuracyInMeters)
    {
        if (accuracyInMeters < 10)
            return 9;
        if (accuracyInMeters < 100)
            return 8;
        return 0;
    }
}
