package gr.vaggelis.myapplication.db.dataAccessObject;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import gr.vaggelis.myapplication.db.entity.GpsRecord;

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
