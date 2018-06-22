package com.huanlezhang.dtcblescanner;


class MyBleDeviceInfoStore {

    public String name;
    public String address;
    public int rssi;
    public long timestamp;

    public MyBleDeviceInfoStore(String name, String address, int rssi, long timestamp) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
        this.timestamp = timestamp;
    }

}