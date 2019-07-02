package gr.invision.gpstracker.sensortracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MySensorManager implements SensorEventListener {

    private SensorListener sensorListener;
    private SensorManager sensorManager;
    private Sensor accelerometer, lightSensor;

    public MySensorManager(Context context) {
        initSensors(context);
        registerSensors();
    }

    private void registerSensors() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void setSensorListener(SensorListener sensorListener) {
        this.sensorListener = sensorListener;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorListener.onSensorChanged(event);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            final double holeAcceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
            sensorListener.getAcceleration(holeAcceleration);
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float light = event.values[0];
            sensorListener.getLight(light);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorListener.onAccuracyChanged(sensor, accuracy);
    }

    public void stopSensors() {
        sensorManager.unregisterListener(this);
    }

    public void resumeSensors() {
        registerSensors();
    }
}
