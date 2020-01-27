package gr.invision.gpstracker.db;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import gr.invision.gpstracker.db.databaseHolder.AppDatabase;
import gr.invision.gpstracker.db.entity.GpsRecord;

public class DatabaseInitializer {

    private static final String TAG = DatabaseInitializer.class.getName();

    public static List<GpsRecord> getAllGpsRecords(@NonNull final AppDatabase db) {
        return db.userDao().getAllGpsRecords();
    }

    public static void insertGpsRecord(@NonNull final AppDatabase db, GpsRecord... gpsRecords) {
        db.userDao().insertGpsRecords(gpsRecords);
        List<GpsRecord> movieList = getAllGpsRecords(db);
        Log.d(DatabaseInitializer.TAG, "Rows Count: " + movieList.size());
    }

    public static int countGpsRecords(@NonNull final AppDatabase db) {
        return db.userDao().countGpsRecords();
    }

    public static void deleteGpsRecords(@NonNull final AppDatabase db, int road) {
        db.userDao().deleteElement(road);
    }
}
