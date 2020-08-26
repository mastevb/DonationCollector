package entity;

import java.util.Date;

import org.elasticsearch.common.geo.GeoPoint;
import org.json.JSONObject;

public class Item {
    private String name;
    private String itemID;
    private String residentID;
    private String description;
    private String imageUrl;
    private String address;
    private GeoPoint location;
    private String postTime;

    private String NGOID;
    private String scheduleID;
    private String scheduleTime;
    private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up

    public String getName() {
        return name;
    }

    public String getResidentID() {
        return residentID;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAddress() {
        return address;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getNGOID() {
        return NGOID;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public int getStatus() {
        return status;
    }
    
    public String getItemID() {
        return itemID;
    }

    public String getPostTime() {
        return postTime;
    }

    private Item(ItemBuilder builder) {
        this.name = builder.name;
        this.itemID = builder.itemID;
        this.residentID = builder.residentID;
        this.description = builder.description;
        this.imageUrl = builder.imageUrl;
        this.address = builder.address;
        this.location = builder.location;
        this.postTime = builder.postTime;
        
        this.NGOID = builder.NGOID;
        this.scheduleID = builder.scheduleID;
        this.scheduleTime = builder.scheduleTime;
        this.status = builder.status;
    }


    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("itemID", itemID);
        obj.put("residentID", residentID);
        obj.put("description", description);
        obj.put("imageUrl", imageUrl);
        obj.put("address", address);
        obj.put("location", location);
        obj.put("NGOID", NGOID);
        obj.put("postTime", postTime);
        
        obj.put("scheduleTime", scheduleTime);
        obj.put("scheduleID", scheduleID);
        obj.put("item status", status);
        return obj;
    }

    public static class ItemBuilder {
        private String name;
        private String itemID;
        private String residentID;
        private String description;
        private String imageUrl;
        private String address;
        private GeoPoint location;
        private String postTime;

        private String NGOID;
        private String scheduleID;
        private String scheduleTime;
        private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up


        public Item build() {
            return new Item(this);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setResidentID(String residentID) {
            this.residentID = residentID;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setLocation(GeoPoint location) {
            this.location = location;
        }

        public void setNGOID(String NGOID) {
            this.NGOID = NGOID;
        }

        public void setScheduleID(String scheduleID) {
            this.scheduleID = scheduleID;
        }

        public void setScheduleTime(String string) {
            this.scheduleTime = string;
        }

        public void setStatus(int status) {
            this.status = status;
        }
        
        public void setItemID(String id) {
            this.itemID = id;
        }

        public void setPostTime(String curTime) {
            this.postTime = curTime;
        }
    }
}