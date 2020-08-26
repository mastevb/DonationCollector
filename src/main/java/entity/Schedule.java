package entity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Schedule {
    private String scheduleID;
    private String NGOID;
    private String scheduleTime;
    private String[] ItemIDList;
    private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up

    public String getScheduleID() {
        return scheduleID;
    }

    public String getNGOID() {
        return NGOID;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public String[] getItemIDList() {
        return ItemIDList;
    }

    public int getStatus() {
        return status;
    }


    private Schedule(ScheduleBuilder builder) {
        this.scheduleID = builder.scheduleID;
        this.NGOID = builder.NGOID;
        this.ItemIDList = builder.ItemIDList;
        this.scheduleTime = builder.scheduleTime;
        this.status = builder.status;
    }


    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("scheduleID", scheduleID);
        obj.put("NGOID", NGOID);
        obj.put("ITEM_ID[]", ItemIDList);
        obj.put("scheduleTime", scheduleTime);
        obj.put("item status", status);
        return obj;
    }



    public static class ScheduleBuilder {
        private String scheduleID;
        private String NGOID;
        private String[] ItemIDList;
        private String scheduleTime;
        private int status;  // 0 for pending, 1 for scheduled, 2 for picked-up


        public Schedule build() {
            return new Schedule(this);
        }

        public void setScheduleID(String scheduleID) {
            this.scheduleID = scheduleID;
        }

        public void setNGOID(String NGOID) {
            this.NGOID = NGOID;
        }

        public void setItemIDList(String[] items) {
            ItemIDList = items;
        }

        public void setScheduleTime(String string) {
            this.scheduleTime = string;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
