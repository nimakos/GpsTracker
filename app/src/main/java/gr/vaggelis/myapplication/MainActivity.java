package gr.vaggelis.myapplication;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import gr.vaggelis.myapplication.build.Constructor;
import gr.vaggelis.myapplication.db.DatabaseInitializer;
import gr.vaggelis.myapplication.db.databaseHolder.AppDatabase;
import gr.vaggelis.myapplication.db.entity.GpsRecord;
import gr.vaggelis.myapplication.gpstracker.GPSListener;
import gr.vaggelis.myapplication.gpstracker.GPSManager;
import gr.vaggelis.myapplication.internettracker.InternetConnectionManager;
import gr.vaggelis.myapplication.internettracker.InternetListener;
import gr.vaggelis.myapplication.permissionstracker.PermissionsManager;
import gr.vaggelis.myapplication.sensortracker.MySensorManager;
import gr.vaggelis.myapplication.sensortracker.SensorListener;
import gr.vaggelis.myapplication.spinner.DataEntry;
import gr.vaggelis.myapplication.spinner.Spinner;

public class MainActivity extends PermissionsManager implements GPSListener, SensorListener, InternetListener, CompoundButton.OnCheckedChangeListener {

    Spinner selectRoadSpinner;
    private static final int REQUEST_PERMISSION = 10;
    private static boolean HAVE_ALL_PERMISSIONS = false;
    GPSManager myGpsManager;
    InternetConnectionManager internetConnectionManager;
    MySensorManager mySensorManager;
    TextView speed, longitude, latitude;
    String[] permissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WAKE_LOCK};
    GpsRecord gpsRecord;
    Switch on_off_switch, meters_seconds_switch;
    EditText selectMilliSeconds, selectMeters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPermissions();
    }

    private List<DataEntry<String>> getSpinnerItems() {
        List<DataEntry<String>> items = new ArrayList<>();
        items.add(new DataEntry<>(1, "Σχηματάρι"));
        items.add(new DataEntry<>(2, "Χαλκίδα"));
        items.add(new DataEntry<>(3, "Τέστ"));
        return items;
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        HAVE_ALL_PERMISSIONS = true;
        setUI();
        startInternetManager();
        startSensor();
        Constructor.createDatafilesOut();
    }

    private void setPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestAppPermissions(permissions, R.string.permission, REQUEST_PERMISSION);
    }

    private void setUI() {
        setContentView(R.layout.activity_main);
        speed = findViewById(R.id.speedValue);
        longitude = findViewById(R.id.longitudeValue);
        latitude = findViewById(R.id.latitudeValue);
        selectRoadSpinner = findViewById(R.id.road_spinner);
        on_off_switch = findViewById(R.id.openGpsSwitch);
        meters_seconds_switch = findViewById(R.id.meters_seconds_switch);
        meters_seconds_switch.setOnCheckedChangeListener(this);
        selectMilliSeconds = findViewById(R.id.secondsEditText);
        selectMeters = findViewById(R.id.metersEditText);
        on_off_switch.setOnCheckedChangeListener(this);
        selectRoadSpinner.setItems(getSpinnerItems());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startGps(int seconds, int meters) {
        myGpsManager = GPSManager.getInstance(this, seconds, meters);
        myGpsManager.setGPSListener(this);
    }

    private void startInternetManager() {
        internetConnectionManager = new InternetConnectionManager(this);
        internetConnectionManager.setInternetListener(this);
    }

    private void startSensor() {
        mySensorManager = new MySensorManager(this);
        mySensorManager.setSensorListener(this);
    }

    private void stopSensor() {
        try {
            if (mySensorManager != null)
                mySensorManager.stopSensors();
        } catch (NullPointerException ignore) {
        }
    }

    private void stopInternetManager() {
        internetConnectionManager = null;
    }

    private void stopGps() {
        try {
            if (myGpsManager != null)
                myGpsManager.stopLocation();
        } catch (NullPointerException ignore) {
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        selectRoadSpinner.clear();
        AppDatabase.destroyInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HAVE_ALL_PERMISSIONS) {
            mySensorManager.resumeSensors();
            startInternetManager();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensor();
        stopInternetManager();
    }

    @Override
    public void getLocation(Location location) {
        gpsRecord = new GpsRecord();
        gpsRecord.setLatitude(location.getLatitude());
        gpsRecord.setLongitude(location.getLongitude());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            gpsRecord.setDateTime(LocalDateTime.now().toString());
        else
            gpsRecord.setDateTime(getDatetime());

        gpsRecord.setRoad(selectRoadSpinner.getSelectedItem().id);
        DatabaseInitializer.insertGpsRecord(AppDatabase.getAppDatabase(this), gpsRecord);

        longitude.setText(String.valueOf(location.getLongitude()));
        latitude.setText(String.valueOf(location.getLatitude()));
    }

    private String getDatetime() {
        Calendar mCalendar = new GregorianCalendar();
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss", Locale.getDefault());
        return dateFmt.format(mCalendar.getTime());
    }

    @Override
    public void getSpeed(float myCurrentSpeed) {
        speed.setText(String.valueOf(myCurrentSpeed));
    }

    @Override
    public void onGpsNetworkStatusChanged(String status) {
        Snackbar.make(findViewById(R.id.login_coord_layout), status, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void getLocationAsynchronous(final Location location) {
        /*runOnUiThread(() -> {
            longitude.setText(String.valueOf(location.getLongitude()));
            latitude.setText(String.valueOf(location.getLatitude()));
        });*/
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void getAcceleration(double acceleration) {

    }

    @Override
    public void getLight(float light) {
    }

    @Override
    public void checkConnection(boolean isConnected) {

    }

    @Override
    public void checkWifiConnection(boolean isConnected) {

    }

    @Override
    public void checkMobileConnection(boolean isConnected) {

    }

    @Override
    public void checkSpeedConnection(boolean isConnectedFast) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.openGpsSwitch) {
            if (on_off_switch.isChecked()) {
                if (selectRoadSpinner.getSelectedItem().id != -1)
                    if (meters_seconds_switch.isChecked()) {
                        if (!selectMeters.getText().toString().equals("")){
                            startGps(0, Integer.valueOf(selectMeters.getText().toString()));
                        }
                        else {
                            Toast.makeText(this, "Παρακαλώ συμπληρώστε πρώτα τα μέτρα", Toast.LENGTH_LONG).show();
                            on_off_switch.setChecked(false);
                        }
                    } else {
                        if (!selectMilliSeconds.getText().toString().equals("")){
                            startGps(Integer.valueOf(selectMilliSeconds.getText().toString()), 0);
                        }
                        else {
                            Toast.makeText(this, "Παρακαλώ συμπληρώστε πρώτα τα δευτερόλεπτα", Toast.LENGTH_LONG).show();
                            on_off_switch.setChecked(false);
                        }
                    }
                else {
                    Toast.makeText(this, "Παρακαλώ επιλέξτε πρώτα την κατεύθυνση", Toast.LENGTH_LONG).show();
                    on_off_switch.setChecked(false);
                }
            } else {
                stopGps();
                longitude.setText("0.0");
                latitude.setText("0.0");
                speed.setText("0.0");
                selectRoadSpinner.setSelectedItem(-1);
            }
        }
    }

    public void sendRecords(View view) {
        List<GpsRecord> listEntity = DatabaseInitializer.getAllGpsRecords(AppDatabase.getAppDatabase(this));
        Constructor constructor = new Constructor();
        String contents = constructor.createRequestData(9998, listEntity);
        constructor.writeFile(constructor.getDatafilesOut(), new Date() + "_dbList.xml", contents.getBytes());
        Toast.makeText(this, "Οι εγγραφές εστάλησαν", Toast.LENGTH_LONG).show();
    }

    public void deleteRecords(View view) {
        if (selectRoadSpinner.getSelectedItem().id != -1) {
            DatabaseInitializer.deleteGpsRecords(AppDatabase.getAppDatabase(this), selectRoadSpinner.getSelectedItem().id);
            Toast.makeText(this, "Οι εγγραφές διεγράφησαν", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Παρακαλώ επιλέξτε πρώτα την κατεύθυνση", Toast.LENGTH_LONG).show();
        }
    }
}
