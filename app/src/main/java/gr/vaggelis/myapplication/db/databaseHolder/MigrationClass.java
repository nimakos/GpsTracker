package gr.vaggelis.myapplication.db.databaseHolder;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

public class MigrationClass extends Migration {
    /**
     * Creates a new migration between {@code startVersion} and {@code endVersion}.
     *
     * @param startVersion The start version of the database.
     * @param endVersion   The end version of the database after this migration is applied.
     */
    public MigrationClass(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Create the new table
        database.execSQL(
                "CREATE TABLE movie_new (movie_id INTEGER, movie_selected INTEGER, last_update INTEGER, PRIMARY KEY(movie_id))");
        // Copy the data
        database.execSQL(
                "INSERT INTO movie_new (movie_id, movie_selected, last_update) SELECT movie_id, movie_selected, last_update FROM movie");
        // Remove the old table
        database.execSQL("DROP TABLE movie");
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE movie_new RENAME TO movie");
    }
}
