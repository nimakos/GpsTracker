package gr.invision.gpstracker.sensortracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

public interface SensorListener {
    void onSensorChanged(SensorEvent sensorEvent);
    void onAccuracyChanged(Sensor sensor, int accuracy);
    void getAcceleration(double acceleration);
    void getLight(float light);
}
