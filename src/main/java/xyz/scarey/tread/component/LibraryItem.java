package xyz.scarey.tread.component;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.File;

public class LibraryItem extends ImageView{

    private int id;
    private boolean isComic;

    public LibraryItem(int id, int comicId, boolean isComic) {
        this.id = id;
        this.isComic = isComic;

        File f = new File(System.getProperty("user.home"), ".tread/covers/" + comicId);
        Image i = new Image(f.toURI().toString(), 300, 300, true, false);
        this.setImage(i);
        this.getStyleClass().add("library-item");
    }
}
