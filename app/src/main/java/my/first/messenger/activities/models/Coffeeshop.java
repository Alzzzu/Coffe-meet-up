package my.first.messenger.activities.models;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

public class Coffeeshop implements Serializable {
    public String id, name, address;
    public GeoPoint geoPoint;

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public String getName() {
        return name;
    }
}
