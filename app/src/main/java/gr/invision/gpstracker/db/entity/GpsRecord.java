package gr.invision.gpstracker.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("GpsRecord")
@Entity(tableName = "gpsRecord")
public class GpsRecord {

    @XStreamAlias("id")
    @PrimaryKey(autoGenerate = true)
    private int id;

    @XStreamAlias("longitude")
    @ColumnInfo(name = "longitude")
    private double longitude;

    @XStreamAlias("latitude")
    @ColumnInfo(name = "latitude")
    private double latitude;

    @XStreamAlias("dateTime")
    @ColumnInfo(name = "dateTime")
    private String dateTime;

    @XStreamAlias("road")
    @ColumnInfo(name = "road")
    private int road;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getRoad() {
        return road;
    }

    public void setRoad(int road) {
        this.road = road;
    }
}
