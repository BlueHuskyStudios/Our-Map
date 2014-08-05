package org.bh.app.ourmap.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.google.android.gms.maps.LocationSource;

import java.util.ArrayList;

/**
 * Made for Our Map by and copyrighted to Blue Husky Programming, ©2014 GPLv3.<hr/>
 *
 * @author Kyli Rouge of Blue Husky Programming
 * @version 1.0.0
 * @since 2014-07-22
 */
public class Stats implements LocationSource {
    public static final Stats CACHE = new Stats(null);

    public Location geoLocation;
    public double signalStrength;
    public String providerName, providerID;
    public SignalType signalType;
    protected Context context;

    private TelephonyManager telephonyManager = null;
    private ConnectivityManager connectivityManager = null;
    private ArrayList<OnLocationChangedListener> onLocationChangedListeners;
    private float delay;
    private float lastTime;

    public Stats(Context context) {
        this.context = context;
        onLocationChangedListeners = new ArrayList<OnLocationChangedListener>();
    }

    /**
     * Returns the minimum delay between polls, in seconds
     * @return the minimum delay between polls, in seconds
     */
    public float getDelay() {
        return delay;
    }

    /**
     * Sets the minimum delay between polls, in seconds
     * @param newDelay the minimum delay between polls, in seconds
     */
    public Stats setDelay(float newDelay) {
        delay = newDelay;
        return this;
    }

    private void guaranteeConnectivityManager() {
        if (connectivityManager == null)
            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void guaranteeTelephonyManager() {
        if (telephonyManager == null)
            telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public Location fetchGeoLocation() {
        return fetchGeoLocation(LocationManager.PASSIVE_PROVIDER);
    }

    public Location fetchGeoLocation(String provider) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        CACHE.geoLocation = geoLocation = lm.getLastKnownLocation(provider);
        System.out.println("Alerting " + onLocationChangedListeners.size() + " location listeners...");
        for(OnLocationChangedListener olcl : onLocationChangedListeners)
            olcl.onLocationChanged(geoLocation);
        return geoLocation;
    }

    public SignalType fetchSignalType() {
        guaranteeConnectivityManager();
        guaranteeTelephonyManager();
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return signalType = SignalType.DEAD;
        }

        if (telephonyManager == null)
            return signalType = SignalType.UNKNOWN;

        /* this never changes
        System.out.println("Phone type is: " + telephonyManager.getPhoneType());
        switch (telephonyManager.getPhoneType())
        {
            case TelephonyManager.PHONE_TYPE_NONE:
                return signalType = SignalType.DEAD;
            case TelephonyManager.PHONE_TYPE_GSM:
                return signalType = SignalType.DATA_2G; // GSM is only 2G
            case TelephonyManager.PHONE_TYPE_CDMA:
            case TelephonyManager.PHONE_TYPE_SIP:
                break;
        }*/

        System.out.println("Network info type is: " + networkInfo.getType());
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return signalType = SignalType.UNKNOWN;
            case ConnectivityManager.TYPE_WIMAX:
                return signalType = SignalType.DATA_3G_OR_4G;
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
            case ConnectivityManager.TYPE_BLUETOOTH:
            case ConnectivityManager.TYPE_DUMMY:
            case ConnectivityManager.TYPE_ETHERNET:
                break;
        }



        System.out.println("Telephony network type is: " + telephonyManager.getNetworkType());
        switch (telephonyManager.getNetworkType())
        {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return signalType = SignalType.DATA_UNKNOWN;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return signalType = SignalType.DATA_EDGE;
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return signalType = SignalType.DATA_2G;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return signalType = SignalType.DATA_2G_OR_3G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return signalType = SignalType.DATA_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return signalType = SignalType.DATA_4G_LTE;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return signalType = SignalType.DATA_4G;
        }
        System.out.println("All else failed.");
        return
            CACHE.signalType
            = signalType
            = SignalType.UNKNOWN;
    }

    /**
     * Sets a {@link org.bh.app.ourmap.util.SignalStateListener} to be tripped when the signal
     * strength is calculated or changes.
     *
     * @param ssl the listener to be put on alert
     * @param onlyOnce if {@code true}, the signal strength will only be polled once, instead of on
     *                 every change
     * @see org.bh.app.ourmap.util.SignalStateListener
     */
    public void awaitSignalStrength(final SignalStateListener ssl, final boolean onlyOnce) {
        guaranteeTelephonyManager();
        telephonyManager.listen(new PhoneStateListener()
        {
            @SuppressWarnings("SpellCheckingInspection")
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                float now = SystemClock.elapsedRealtime() / 1000f;
                if (now - lastTime < delay)
                    return;
                lastTime = now;

                double val = -1;
                if (signalStrength.isGsm()) {
                    /**
                     * If we're dealing with GSM, Android defines it as TS 27.007 8.5 does:
                     *
                     *            0: -113 dBm or less
                     *            1: -111 dBm (what about -112?)
                     * 2 through 30: -109 through -53 dBm (they msut only be odd somehow...)
                     *           31: -51 dBm or higher
                     *           99: not known or not detectable
                     *
                     * We will use this knowledge to try to parse the signal into an easy-to-use
                     * scale as we define in the documentation for
                     * {@link SignalStateListener#signalStrengthUpdate(double)}.
                     */
                    val = signalStrength.getGsmSignalStrength();
                    System.out.println("GSM: " + val);
                    if (val == 99)
                        val = Double.NaN;
                    else
                        /* this scales it so:
                         *
                         * 0 == negligible (var => 0.0)
                         * 10 == nominal   (var => 1.0)
                         * 30 == excellent (var => 3.0)
                         */
                        val = val / 10.0;
                }


                else {
                    val = signalStrength.getCdmaDbm();
                    System.out.println("CMDA RSSI: " + val);
                    val = signalStrength.getCdmaEcio();
                    System.out.println("CMDA Ec/Io: " + val);
                    val = signalStrength.getEvdoDbm();
                    System.out.println("EVDO RSSI: " + val);
                    val = signalStrength.getEvdoEcio();
                    System.out.println("EVDO Ec/Io: " + val);
                }


                ssl.signalStrengthUpdate(
                    CACHE.signalStrength =
                    Stats.this.signalStrength =
                    val);
                if (onlyOnce)
                    telephonyManager.listen(this, LISTEN_NONE);
            }
        },
        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public String fetchProviderName() {
        guaranteeTelephonyManager();
        return
            CACHE.providerName
            = providerName
            = telephonyManager.getNetworkOperatorName();
    }

    public String fetchProviderID() {
        guaranteeTelephonyManager();
        return
            CACHE.providerID =
            providerID =
            telephonyManager.getNetworkOperator();
    }

    public Stats setContext(Context context) {
        this.context = context;
        return this;
    }

    public Stats fetchAll() {
        fetchGeoLocation();
//        fetchSignalStrength();
        fetchSignalType();
        fetchProviderName();
        fetchProviderID();
        return this;
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        onLocationChangedListeners.add(onLocationChangedListener);
    }

    @Override
    public void deactivate() {
        onLocationChangedListeners.clear();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static enum SignalType {
        /** Signifies 5G signal (not currently used) */
        DATA_5G,
        /** Signifies 4G signal */
        DATA_4G,
        /** Signifies 4G LTE signal */
        DATA_4G_LTE,
        /** Signifies what might be either 3G or 4G signal */
        DATA_3G_OR_4G,
        /** Signifies 3G signal */
        DATA_3G,
        /** Signifies what might be either 2G or 3G signal */
        DATA_2G_OR_3G,
        /** Signifies 2G signal */
        DATA_2G,
        /** Signifies 1G signal (not currently used) */
        DATA_1G,
        /** Signifies Edge signal */
        DATA_EDGE,
        /** Signifies Unknown signal */
        DATA_UNKNOWN,
        /** Signifies Basic (talk-and-text) signal */
        BASIC,
        /** Signifies zero signal */
        DEAD,
        /** Signifies an unknown, but not necessarily dead, signal */
        UNKNOWN
    }
}
