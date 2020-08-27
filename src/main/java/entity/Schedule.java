package entity;

import org.json.JSONObject;

import java.util.ArrayList;

public class Schedule {
    private String scheduleID;
    private String NGOID;
    private String scheduleTime;
    private ArrayList<String> ItemIDList;
    private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up
    private ArrayList<Item> itemList; // optional for My_schedule
    
    public String getScheduleID() {
        return scheduleID;
    }

    public String getNGOID() {
        return NGOID;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public ArrayList<String> getItemIDList() {
        return ItemIDList;
    }

    public int getStatus() {
        return status;
    }
    
    public ArrayList<Item> getItemList() {
        return itemList;
    }

    private Schedule(ScheduleBuilder builder) {
        this.scheduleID = builder.scheduleID;
        this.NGOID = builder.NGOID;
        this.ItemIDList = builder.ItemIDList;
        this.scheduleTime = builder.scheduleTime;
        this.status = builder.status;
        this.itemList = builder.itemList;
    }


    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("scheduleID", scheduleID);
        obj.put("scheduleTime", scheduleTime);
        obj.put("status", status);
        obj.put("itemList", itemList);
        return obj;
    }



    public static class ScheduleBuilder {
        private String scheduleID;
        private String NGOID;
        private ArrayList<String> ItemIDList;
        private String scheduleTime;
        private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up
        private ArrayList<Item> itemList; // optional for My_schedule

        public Schedule build() {
            return new Schedule(this);
        }

        public void setScheduleID(String scheduleID) {
            this.scheduleID = scheduleID;
        }

        public void setNGOID(String NGOID) {
            this.NGOID = NGOID;
        }

        public void setItemIDList(ArrayList<String> itemIDList) {
            ItemIDList = itemIDList;
        }

        public void setScheduleTime(String scheduleTime) {
            this.scheduleTime = scheduleTime;
        }

        public void setStatus(int status) {
            this.status = status;
        }
        
        public void setItemList(ArrayList<Item> itemList) {
            this.itemList = itemList;
        }
    }
}
