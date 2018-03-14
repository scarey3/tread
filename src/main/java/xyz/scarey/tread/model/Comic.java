package xyz.scarey.tread.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Comic {

    private IntegerProperty id = new SimpleIntegerProperty();
    private IntegerProperty seriesId = new SimpleIntegerProperty();
    private IntegerProperty volume = new SimpleIntegerProperty();
    private IntegerProperty chapter = new SimpleIntegerProperty();
    private StringProperty uri = new SimpleStringProperty();

    public Comic(int id, int seriesId, int volume, String uri) {
        this.id.set(id);
        this.seriesId.set(seriesId);
        this.volume.set(volume);
        this.uri.set(uri);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getSeriesId() {
        return seriesId.get();
    }

    public IntegerProperty seriesIdProperty() {
        return seriesId;
    }

    public void setSeriesId(int seriesId) {
        this.seriesId.set(seriesId);
    }

    public int getVolume() {
        return volume.get();
    }

    public IntegerProperty volumeProperty() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume.set(volume);
    }

    public int getChapter() {
        return chapter.get();
    }

    public IntegerProperty chapterProperty() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter.set(chapter);
    }

    public String getUrl() {
        return uri.get();
    }

    public StringProperty urlProperty() {
        return uri;
    }

    public void setUrl(String url) {
        this.uri.set(url);
    }
}
