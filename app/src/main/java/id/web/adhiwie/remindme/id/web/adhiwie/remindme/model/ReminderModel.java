package id.web.adhiwie.remindme.id.web.adhiwie.remindme.model;

/**
 * Created by adhiwie on 01/03/15.
 */
public class ReminderModel {
    private String key;
    private String name;
    private String place;
    private Double latitude;
    private Double longitude;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
