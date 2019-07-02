package gr.invision.gpstracker.internettracker;

public interface InternetListener {
    void checkConnection(boolean isConnected);
    void checkWifiConnection(boolean isConnected);
    void checkMobileConnection(boolean isConnected);
    void checkSpeedConnection(boolean isConnectedFast);
}
