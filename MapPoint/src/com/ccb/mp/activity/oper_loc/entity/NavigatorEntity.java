package com.ccb.mp.activity.oper_loc.entity;

/**
 * 导航实体对象 2015/7/2 16:04
 */
public class NavigatorEntity {

    private int id;
    private String searchId;
    private String lat;
    private String lng;
    private String time;
    private String srcLoc;
    private String decLoc;

    public int getId() {
        return id;
    }

    public NavigatorEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getSearchId() {
        return searchId;
    }

    public NavigatorEntity setSearchId(String searchId) {
        this.searchId = searchId;
        return this;
    }

    public String getLat() {
        return lat;
    }

    public NavigatorEntity setLat(String lat) {
        this.lat = lat;
        return this;
    }

    public String getLng() {
        return lng;
    }

    public NavigatorEntity setLng(String lng) {
        this.lng = lng;
        return this;
    }

    public String getTime() {
        return time;
    }

    public NavigatorEntity setTime(String time) {
        this.time = time;
        return this;
    }

    public String getSrcLoc() {
        return srcLoc;
    }

    public NavigatorEntity setSrcLoc(String srcLoc) {
        this.srcLoc = srcLoc;
        return this;
    }

    public String getDecLoc() {
        return decLoc;
    }

    public NavigatorEntity setDecLoc(String decLoc) {
        this.decLoc = decLoc;
        return this;
    }
}
