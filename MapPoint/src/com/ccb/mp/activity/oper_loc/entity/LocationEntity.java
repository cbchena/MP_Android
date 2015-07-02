package com.ccb.mp.activity.oper_loc.entity;

/**
 * 地点实体
 */
public class LocationEntity {

    private int id;
    private String searchId;
    private String lat;
    private String lng;
    private String time;
    private String loc;
    private String desc;
    private int type;
    private String name;
    private String tel;

    public int getId() {
        return id;
    }

    public LocationEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getSearchId() {
        return searchId;
    }

    public LocationEntity setSearchId(String searchId) {
        this.searchId = searchId;
        return this;
    }

    public String getLat() {
        return lat;
    }

    public LocationEntity setLat(String lat) {
        this.lat = lat;
        return this;
    }

    public String getLng() {
        return lng;
    }

    public LocationEntity setLng(String lng) {
        this.lng = lng;
        return this;
    }

    public String getTime() {
        return time;
    }

    public LocationEntity setTime(String time) {
        this.time = time;
        return this;
    }

    public String getLoc() {
        return loc;
    }

    public LocationEntity setLoc(String loc) {
        this.loc = loc;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public LocationEntity setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public int getType() {
        return type;
    }

    public LocationEntity setType(int type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public LocationEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getTel() {
        return tel;
    }

    public LocationEntity setTel(String tel) {
        this.tel = tel;
        return this;
    }
}
