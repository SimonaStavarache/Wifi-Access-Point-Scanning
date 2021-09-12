package com.example.findaccesspoint;

// ORM Style Database Object Representation
public class ObjectsAWS {

    // all variables are included in the database schema, except the id which is automatically provided by the amplify api through Cognito
    // The object directly maps to "FindAccessPoint\amplify\backend\api\findaccesspoint\schema.graphql"
    private String SSID;
    private String BSSID;
    private int RSSI;
    private int frequency;
    private String location;

    // constructor
    public ObjectsAWS(String ssid, String bssid, int rssi, int frequency, String location) {
        SSID = ssid;
        BSSID = bssid;
        RSSI = rssi;
        this.frequency = frequency;
        this.location = location;
    }

    // Getters and Setters

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public int getRSSI() {
        return RSSI;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getLocation() {
        return location;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
