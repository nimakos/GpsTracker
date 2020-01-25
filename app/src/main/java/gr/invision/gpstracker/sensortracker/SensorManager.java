package gr.invision.gpstracker.sensortracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.lang.ref.WeakReference;

public class SensorManager implements SensorEventListener {

    public interface SensorListener {
        void onSensorChanged(SensorEvent sensorEvent);
        void onAccuracyChanged(Sensor sensor, int accuracy);
        void getAcceleration(double acceleration);
        void getLight(float light);
    }

    private SensorListener sensorListener;

    private android.hardware.SensorManager sensorManager;

    private static SensorManager INSTANCE;

    private SensorManager(Context context) {
        initSensors(context);
    }

    public synchronized static SensorManager getInstance(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        if (INSTANCE == null) {
            INSTANCE = new SensorManager(contextWeakReference.get());
        }
        return INSTANCE;
    }

    public void destroyInstance() {
        setSensorListener(null);
        sensorManager.unregisterListener(this);
        INSTANCE = null;
    }

    public void setSensorListener(SensorListener sensorListener) {
        this.sensorListener = sensorListener;
    }

    private void initSensors(Context context) {
        sensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null){
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, lightSensor, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorListener.onSensorChanged(event);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                final double holeAcceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                sensorListener.getAcceleration(holeAcceleration);
                break;
            case Sensor.TYPE_LIGHT:
                float light = event.values[0];
                sensorListener.getLight(light);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorListener.onAccuracyChanged(sensor, accuracy);
    }
}
