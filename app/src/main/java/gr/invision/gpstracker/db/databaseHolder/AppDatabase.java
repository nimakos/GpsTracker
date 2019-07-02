package gr.invision.gpstracker.db.databaseHolder;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import gr.invision.gpstracker.db.dataAccessObject.GpsDao;
import gr.invision.gpstracker.db.entity.GpsRecord;

@Database(entities = {GpsRecord.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract GpsDao userDao();
    //private static Migration migration1_2 = new MigrationClass(1,2);

    /**
     * Singleton pattern
     * Create one and only instance for the program
     *
     * @param context The application context
     * @return The instance of the class
     */
    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "gps-database")
                            //.addMigrations(migration1_2)
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    /**
     * Called when program finishing to destroy the instance of the class
     * to be collected later by the garbage collector
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
