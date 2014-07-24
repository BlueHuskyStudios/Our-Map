package org.bh.app.ourmap.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

/**
 * Made for Our Map by and copyrighted to Blue Husky Programming, ©2014 GPLv3.<hr/>
 *
 * @author Kyli Rouge of Blue Husky Programming
 * @version 1.0.0
 * @since 2014-07-22
 */
public class Stats {
    public static final Stats CACHE = new Stats();
    public Location location;
    public double signalStrength;
    public String providerName, providerID;
    public SignalType signalType;

    private static TelephonyManager telephonyManager = null;
    private static ConnectivityManager connectivityManager = null;

    private static void guaranteeConnectivityManager(Context context)
    {
        if (connectivityManager == null)
            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private static void guaranteeTelephonyManager(Context context)
    {
        if (telephonyManager == null)
            telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static Location fetchGeoLocation(Context context)
    {
        return fetchGeoPosition(context, LocationManager.PASSIVE_PROVIDER);
    }

    public static Location fetchGeoPosition(Context context, String provider)
    {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.getLastKnownLocation(provider);
    }

    public static SignalType fetchSignalType(Context context) {
        guaranteeConnectivityManager(context);
        guaranteeTelephonyManager(context);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return SignalType.DEAD;
        }

        switch (telephonyManager.getPhoneType())
        {
            case TelephonyManager.PHONE_TYPE_NONE:
                return SignalType.DEAD;
            case TelephonyManager.PHONE_TYPE_GSM:
                return SignalType.DATA_2G; // GSM is only 2G
            case TelephonyManager.PHONE_TYPE_CDMA:
            case TelephonyManager.PHONE_TYPE_SIP:
                break;
        }

        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return SignalType.UNKNOWN;
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
            case ConnectivityManager.TYPE_WIMAX:
                return SignalType.DATA_3G_OR_4G;
            case ConnectivityManager.TYPE_BLUETOOTH:
            case ConnectivityManager.TYPE_DUMMY:
            case ConnectivityManager.TYPE_ETHERNET:
                break;
        }




        if (telephonyManager == null)
            return SignalType.UNKNOWN;

        switch (telephonyManager.getNetworkType())
        {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return SignalType.DATA_UNKNOWN;
            case TelephonyManager.NETWORK_TYPE_EDGE:
            return SignalType.DATA_EDGE;
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return SignalType.DATA_2G;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return SignalType.DATA_2G_OR_3G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            return SignalType.DATA_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return SignalType.DATA_4G_LTE;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return SignalType.DATA_4G;
        }
        return SignalType.UNKNOWN;
    }

    public static double fetchSignalStrength(Context context) {
        guaranteeTelephonyManager(context);
        if (telephonyManager == null)
            return 0;

        PhoneStateListener psl = new PhoneStateListener()
        {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                System.out.println("CDMA RSSI dBm: "    + signalStrength.getCdmaDbm());
                System.out.println("CDMA Ec/Io dB*10: " + signalStrength.getCdmaEcio());
                System.out.println("EVDO RSSI dBm: "    + signalStrength.getEvdoDbm());
                System.out.println("EVDO Ec/Io dB*10: " + signalStrength.getEvdoEcio());
                System.out.println("EVDO noise: "       + signalStrength.getEvdoSnr());
                System.out.println("GSM error rate: "   + signalStrength.getGsmBitErrorRate());
                System.out.println("GSM signal str: "   + signalStrength.getGsmSignalStrength());
            }
        };
        return .5;
    }

    public static String fetchProviderName(Context context)
    {
        guaranteeTelephonyManager(context);
        return telephonyManager.getNetworkOperatorName();
    }

    public static String fetchProviderID(Context context)
    {
        guaranteeTelephonyManager(context);
        return telephonyManager.getNetworkOperator();
    }

    public static Stats fetchAll(Context context) {
        CACHE.location = fetchGeoLocation(context);
        CACHE.signalStrength = fetchSignalStrength(context);
        CACHE.signalType = fetchSignalType(context);
        CACHE.providerName = fetchProviderName(context);
        CACHE.providerID = fetchProviderID(context);
        return CACHE;
    }

    public static enum SignalType
    {
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
