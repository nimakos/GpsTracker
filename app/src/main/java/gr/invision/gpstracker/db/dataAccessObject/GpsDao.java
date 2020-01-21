package gr.invision.gpstracker.db.dataAccessObject;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import gr.invision.gpstracker.db.entity.GpsRecord;

@Dao
public interface GpsDao {

    @Query("SELECT * FROM GpsRecord")
    List<GpsRecord> getAllGpsRecords();

    @Query("SELECT COUNT(*) from GpsRecord")
    int countGpsRecords();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long gpsRecord(GpsRecord gpsRecord);

    @Insert
    void insertGpsRecords(GpsRecord... gpsRecords);

    @Query("DELETE FROM gpsRecord WHERE road LIKE :roadId ")
    void deleteElement(int roadId);
}
