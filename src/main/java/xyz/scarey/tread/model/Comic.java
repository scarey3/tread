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
    private StringProperty url = new SimpleStringProperty();

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
        return url.get();
    }

    public StringProperty urlProperty() {
        return url;
    }

    public void setUrl(String url) {
        this.url.set(url);
    }
}
