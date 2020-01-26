package gr.invision.gpstracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.LocationRequest;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import gr.invision.gpstracker.build.Constructor;
import gr.invision.gpstracker.db.DatabaseInitializer;
import gr.invision.gpstracker.db.databaseHolder.AppDatabase;
import gr.invision.gpstracker.db.entity.GpsRecord;
import gr.invision.gpstracker.gpstracker.androidapi.MyGPSManager;
import gr.invision.gpstracker.gpstracker.googleapi.GoogleLocation;
import gr.invision.gpstracker.internettracker.MyConnectionManager;
import gr.invision.gpstracker.permissions.PermissionsManager;
import gr.invision.gpstracker.sensortracker.MySensorManager;
import gr.invision.gpstracker.spinner.DataEntry;
import gr.invision.gpstracker.spinner.Spinner;

public class MainActivity extends PermissionsManager implements MyGPSManager.GPSListener, MySensorManager.SensorListener, MyConnectionManager.InternetListener, CompoundButton.OnCheckedChangeListener, GoogleLocation.OnLocationUpdateListener {

    Spinner selectRoadSpinner;
    private static final int REQUEST_PERMISSION = 10;
    MyGPSManager myGpsManager;
    MyConnectionManager myConnectionManager;
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
    EditText selectSeconds, selectMeters;
    GoogleLocation googleLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setPermissions();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        setUI();
        startInternetManager();
        startSensor();
        Constructor.createDatafilesOut();
        startGoogleGps();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSensor();
        stopInternetManager();
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        selectRoadSpinner.clear();
        AppDatabase.destroyInstance();
        stopGoogleGps();
        stopSensor();
        stopInternetManager();
        System.gc();
    }


    @Override
    public void getGoogleLocationUpdate(Location location) {

    }

    @Override
    public void getLocationUpdate(Location location) {
        gpsRecord = new GpsRecord();
        gpsRecord.setLatitude(location.getLatitude());
        gpsRecord.setLongitude(location.getLongitude());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            gpsRecord.setDateTime(LocalDateTime.now().toString());
        else
            gpsRecord.setDateTime(getDatetime());

        gpsRecord.setRoad(selectRoadSpinner.getSelectedItem().id);
        DatabaseInitializer.insertGpsRecord(AppDatabase.getAppDatabase(this), gpsRecord);

        longitude.setText(String.valueOf(round((float)location.getLongitude(), 4)));
        latitude.setText(String.valueOf(round((float)location.getLatitude(), 4)));
    }

    @Override
    public void getSpeedUpdate(float myCurrentSpeed) {
        speed.setText(String.valueOf(round(myCurrentSpeed, 2)));
    }

    @Override
    public void onGpsNetworkStatusUpdate(String status) {
        Snackbar.make(findViewById(R.id.login_coord_layout), status, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void getLocationAsynchronousUpdate(final Location location) {
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
            if (isChecked) {
                if (selectRoadSpinner.getSelectedItem().id != -1)
                    if (meters_seconds_switch.isChecked()) {
                        if (!selectMeters.getText().toString().equals("")) {
                            startGps(0, Integer.valueOf(selectMeters.getText().toString()));
                        } else {
                            displayToastMessage("Παρακαλώ συμπληρώστε πρώτα τα μέτρα");
                            on_off_switch.setChecked(false);
                        }
                    } else {
                        if (!selectSeconds.getText().toString().equals("")) {
                            startGps(Integer.valueOf(selectSeconds.getText().toString()) * 1000, 0);
                        } else {
                            displayToastMessage("Παρακαλώ συμπληρώστε πρώτα τα δευτερόλεπτα");
                            on_off_switch.setChecked(false);
                        }
                    }
                else {
                    displayToastMessage("Παρακαλώ επιλέξτε πρώτα την κατεύθυνση");
                    on_off_switch.setChecked(false);
                }
            } else {
                stopGps();
                longitude.setText("0.0");
                latitude.setText("0.0");
                speed.setText("0.0");
                selectRoadSpinner.setSelectedItem(-1);
            }
        } else if (buttonView.getId() == R.id.meters_seconds_switch) {
            if (!isChecked) {
                selectMeters.setVisibility(View.GONE);
                selectSeconds.setVisibility(View.VISIBLE);
                selectMeters.setText("");
            } else {
                selectSeconds.setVisibility(View.GONE);
                selectMeters.setVisibility(View.VISIBLE);
                selectSeconds.setText("");
            }
        }
    }


    private List<DataEntry<String>> getSpinnerItems() {
        List<DataEntry<String>> items = new ArrayList<>();
        items.add(new DataEntry<>(1, "Σχηματάρι"));
        items.add(new DataEntry<>(2, "Χαλκίδα"));
        items.add(new DataEntry<>(3, "Τέστ"));
        return items;
    }

    private void setPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestAppPermissions(permissions, R.string.permission, REQUEST_PERMISSION);
    }

    private void setUI() {
        speed = findViewById(R.id.speedValue);
        longitude = findViewById(R.id.longitudeValue);
        latitude = findViewById(R.id.latitudeValue);
        selectRoadSpinner = findViewById(R.id.road_spinner);
        on_off_switch = findViewById(R.id.openGpsSwitch);
        meters_seconds_switch = findViewById(R.id.meters_seconds_switch);
        meters_seconds_switch.setOnCheckedChangeListener(this);
        selectSeconds = findViewById(R.id.secondsEditText);
        selectMeters = findViewById(R.id.metersEditText);
        on_off_switch.setOnCheckedChangeListener(this);
        selectRoadSpinner.setItems(getSpinnerItems());
        selectMeters.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startGps(int milliSeconds, int meters) {
        myGpsManager = MyGPSManager.getInstance(this, this, milliSeconds, meters);
    }

    private void stopGps() {
        myGpsManager.destroyInstance();
    }

    private void startGoogleGps() {
        googleLocation = new GoogleLocation
                .Builder(this, this)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(17000)
                .setUpdateInterval(11000)
                .build();
    }

    private void stopGoogleGps() {
        googleLocation.destroyInstance();
    }

    private void startSensor() {
        mySensorManager = MySensorManager.getInstance(this, this);
    }

    private void stopSensor() {
        mySensorManager.destroyInstance();
    }

    private void startInternetManager() {
        myConnectionManager = new MyConnectionManager(this);
        myConnectionManager.setInternetListener(this);
    }

    private void stopInternetManager() {
        myConnectionManager = null;
    }

    private String getDatetime() {
        Calendar mCalendar = new GregorianCalendar();
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss", Locale.getDefault());
        return dateFmt.format(mCalendar.getTime());
    }

    /**
     * Round to certain number of decimals
     *
     * @param d            The number you want to round
     * @param decimalPlace The number of decimal points
     * @return The rounded number
     */
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void sendRecords(View view) {
        List<GpsRecord> listEntity = DatabaseInitializer.getAllGpsRecords(AppDatabase.getAppDatabase(this));
        Constructor constructor = new Constructor();
        String contents = constructor.createRequestData(9998, listEntity);
        constructor.writeFile(constructor.getDatafilesOut(), new Date() + "_dbList.xml", contents.getBytes());
        displayToastMessage("Οι εγγραφές εστάλησαν");
    }

    public void deleteRecords(View view) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (selectRoadSpinner.getSelectedItem().id != -1) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DatabaseInitializer.deleteGpsRecords(AppDatabase.getAppDatabase(MainActivity.this), selectRoadSpinner.getSelectedItem().id);
                        displayToastMessage("Οι εγγραφές διεγράφησαν");
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                }
            } else {
                displayToastMessage("Παρακαλώ επιλέξτε πρώτα την κατεύθυνση");
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Είσαι σίγουρος ότι θέλεις να διαγραφούν οι εγγραφές που αφορούν " + "'" + selectRoadSpinner.getSelectedItem().data + "'").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void displayToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
