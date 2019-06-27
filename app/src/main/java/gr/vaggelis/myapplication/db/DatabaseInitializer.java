package gr.vaggelis.myapplication.db;

import android.graphics.Movie;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import gr.vaggelis.myapplication.db.databaseHolder.AppDatabase;
import gr.vaggelis.myapplication.db.entity.GpsRecord;

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



    private static class PopulateDbAsync extends AsyncTask<Movie, Void, Void> {

        private final AppDatabase mDb;

        PopulateDbAsync(AppDatabase db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(final Movie... movies) {
            return null;
        }

    }
}
