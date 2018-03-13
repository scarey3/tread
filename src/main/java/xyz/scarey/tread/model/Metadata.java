package xyz.scarey.tread.model;

public class Metadata {
    private String title;
    private String volume;
    private String chapter;
    private String filename;

    public Metadata(String title, String volume, String chapter, String filename) {
        this.title = title;
        this.volume = volume;
        this.chapter = chapter;
        this.filename = filename;
    }

    public Metadata(String filename) {
        this.filename = filename;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTitle() {
        return title;

    }

    public void setTitle(String title) {
        this.title = title;
    }
}
