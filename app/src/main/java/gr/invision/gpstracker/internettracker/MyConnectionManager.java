package gr.invision.gpstracker.internettracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.lang.ref.WeakReference;

/**
 * Check device's network connectivity and speed
 *
 * @author emil http://stackoverflow.com/users/220710/emil
 */
public class MyConnectionManager {

    public interface InternetListener {
        void checkConnection(boolean isConnected);
        void checkWifiConnection(boolean isConnected);
        void checkMobileConnection(boolean isConnected);
        void checkSpeedConnection(boolean isConnectedFast);
    }

    private static NetworkInfo info;
    private InternetListener listener;

    public MyConnectionManager(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        ConnectivityManager cm = (ConnectivityManager) contextWeakReference.get().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) info = cm.getActiveNetworkInfo();
    }

    public void setInternetListener(InternetListener listener) {
        this.listener = listener;
        isConnected();
        isConnectedFast();
        isConnectedMobile();
        isConnectedWifi();
    }

    /**
     * Check if there is any connectivity
     */
    public boolean isConnected() {
        boolean isConnected = info != null && info.isConnected();
        listener.checkConnection(isConnected);
        return isConnected;
    }

    /**
     * Check if there is any connectivity to a Wifi network
     */
    public boolean isConnectedWifi() {
        boolean isConnectedWifi = info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
        listener.checkWifiConnection(isConnectedWifi);
        return isConnectedWifi;
    }

    /**
     * Check if there is any connectivity to a mobile network
     */
    public boolean isConnectedMobile() {
        boolean isConnectedMobile = info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE;
        listener.checkMobileConnection(isConnectedMobile);
        return isConnectedMobile;
    }

    /**
     * Check if there is fast connectivity
     */
    public boolean isConnectedFast() {
        boolean isConnectedFast = info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype());
        listener.checkSpeedConnection(isConnectedFast);
        return isConnectedFast;
    }

    /**
     * Check if the connection is fast
     */
    private boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps (E->Edge signal)
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps (H+->High_Speed_Packet_Access signal)
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:

                default:
                    return false;
            }
        } else {
            return false;
        }
    }
}