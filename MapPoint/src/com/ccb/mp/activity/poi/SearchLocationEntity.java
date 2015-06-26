package com.ccb.mp.activity.poi;

import com.baidu.mapapi.model.LatLng;

/**
 * 地点实体 2015/6/9 10:49
 */
public class SearchLocationEntity {

    private String city;
    private String name;
    private String address;
    private LatLng latLng;

    public String getCity() {
        return city;
    }

    public SearchLocationEntity setCity(String city) {
        this.city = city;
        return this;
    }

    public String getName() {
        return name;
    }

    public SearchLocationEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public SearchLocationEntity setAddress(String address) {
        this.address = address;
        return this;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public SearchLocationEntity setLatLng(LatLng latLng) {
        this.latLng = latLng;
        return this;
    }
}
