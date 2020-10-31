package com.rarchives.ripme.ui;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class History extends SimpleListProperty<HistoryEntry> {
    //    private final List<HistoryEntry> list;//Refactored to extend list instead
//    private static final String[] COLUMNS = new String[]{
//            "URL",
//            "created",
//            "modified",
//            "#",
//            ""
//    };

    public History() {
        super(FXCollections.observableArrayList());
    }

//    @Override
//    public boolean add(HistoryEntry entry) {
//        return super.add(entry);
//    }
//
//    @Override
//    public boolean remove(Object entry) {
//        return super.remove(entry);
//    }

    public boolean remove(String url) { // TODO performance
        for (int i = 0; i < super.size(); i++) {
            if (super.get(i).url.equals(url)) {
                super.remove(i);
                return true;
            }
        }
        return false;
    }

//    @Override
//    public HistoryEntry remove(int index) {
//        return super.remove(index);
//    }
//
//    @Override
//    public void clear() {
//        super.clear();
//    }

//    public HistoryEntry get(int index) {
//        return list.get(index);
//    }

//    public String getColumnName(int index) {
//        return COLUMNS[index];
//    }
//
//    public int getColumnCount() {
//        return COLUMNS.length;
//    }

    public boolean containsURL(String url) { //TODO This gets really slow with large lists
        for (HistoryEntry entry : this) {
            if (entry.url.equals(url)) {
                return true;
            }
        }
        return false;
    }

    public HistoryEntry getEntryByURL(String url) throws RuntimeException {
        for (HistoryEntry entry : this) {
            if (entry.url.equals(url)) {
                return entry;
            }
        }
        throw new RuntimeException("Could not find URL " + url + " in History");
    }

    private void fromJSON(JSONArray jsonArray) {
        JSONObject json;
        for (int i = 0; i < jsonArray.length(); i++) {
            json = jsonArray.getJSONObject(i);
            super.add(new HistoryEntry().fromJSON(json));
        }
    }

    /**
     * Loads the history from a file
     *
     * @param filename File to load
     * @throws IOException if something goes wrong
     */
    public void fromFile(String filename) throws IOException {
        String jsonString = Files.readString(Paths.get(filename), StandardCharsets.UTF_8);
        JSONArray jsonArray = new JSONArray(jsonString);
        fromJSON(jsonArray);
    }

    public void fromList(List<String> stringList) {
        for (String item : stringList) {
            HistoryEntry entry = new HistoryEntry();
            entry.url = item;
            super.add(entry);
        }
    }

    private JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();
        for (HistoryEntry entry : this) {
            jsonArray.put(entry.toJSON());
        }
        return jsonArray;
    }

//    public List<HistoryEntry> toList() {
//        return list;
//    }
//
//    public boolean isEmpty() {
//        return list.isEmpty();
//    }

    /**
     * Save this history to a file
     *
     * @param filename File name to save
     * @return the path of the new file
     * @throws IOException if something went wrong
     */
    public Path toFile(String filename) throws IOException {
        return Files.write(Paths.get(filename), toJSON().toString(1).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
