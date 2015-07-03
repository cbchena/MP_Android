package com.ccb.mp.activity.poi;

import com.baidu.mapapi.model.LatLng;

/**
 * 地点实体 2015/6/9 10:49
 */
public class SearchLocationEntity {

    private int id;
    private String searchId;
    private String time;
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

    public int getId() {
        return id;
    }

    public SearchLocationEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getSearchId() {
        return searchId;
    }

    public SearchLocationEntity setSearchId(String searchId) {
        this.searchId = searchId;
        return this;
    }

    public String getTime() {
        return time;
    }

    public SearchLocationEntity setTime(String time) {
        this.time = time;
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
