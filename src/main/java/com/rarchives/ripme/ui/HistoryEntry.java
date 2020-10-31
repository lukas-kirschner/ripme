package com.rarchives.ripme.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class HistoryEntry {

    public static String prettyDate(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDir() {
        return dir;
    }

    public int getCount() {
        return count;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getStartDatePretty() {
        return prettyDate(startDate);
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public String getModifiedDatePretty() {
        return prettyDate(modifiedDate);
    }

    public String  url          = "",
                   title        = "",
                   dir          = "";
    public int     count        = 0;
    public Date    startDate    = new Date(),
                   modifiedDate = new Date();

    public HistoryEntry() {
    }

    public HistoryEntry fromJSON(JSONObject json) {
        this.url          = json.getString("url");
        this.startDate    = new Date(json.getLong("startDate"));
        this.modifiedDate = new Date(json.getLong("modifiedDate"));
        if (json.has("title")) {
            this.title    = json.getString("title");
        }
        if (json.has("count")) {
            this.count    = json.getInt("count");
        }
        if (json.has("dir")) {
            this.dir      = json.getString("dir");
        }
        return this;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("url",          this.url);
        json.put("startDate",    this.startDate.getTime());
        json.put("modifiedDate", this.modifiedDate.getTime());
        json.put("title",        this.title);
        json.put("count",        this.count);
        return json;
    }

    @Override
    public String toString() {
        return this.url;
    }
}
