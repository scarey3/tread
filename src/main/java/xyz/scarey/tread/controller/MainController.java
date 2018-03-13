package xyz.scarey.tread.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import xyz.scarey.tread.Database;
import xyz.scarey.tread.component.LibraryItem;
import xyz.scarey.tread.model.Series;
import xyz.scarey.tread.util.ArchiveUtil;
import xyz.scarey.tread.util.SqlUtil;

import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainController {

    private Database database;
    private Connection connection;
    private ObservableList<Series> library;
    private int lastSeries = 0;

    @FXML private BorderPane mainBorderPane;
    @FXML private FlowPane libraryPane;
    @FXML private Button toolbarBack;
    @FXML private Button toolbarForward;

    public MainController() {}

    @FXML void initialize() {
        database = new Database();
        try {
            connection = database.getConnection();
        } catch(ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        showSeries();
    }

    @FXML private void onBack() {
        showSeries();
    }

    @FXML private void addFolder() {
        int directoryId = 0;
        int seriesId = 0;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());

        if(selectedDirectory != null) {
            if(directoryExists(selectedDirectory.getAbsolutePath())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Folder Already Added");
                alert.setHeaderText(null);
                alert.setContentText("This folder is already in your library. Try refreshing to see new items.");

                alert.showAndWait();
                return;
            } else {
                try {
                    Statement statement = connection.createStatement();

                    // Insert directory
                    String SQL = String.format("INSERT INTO Directories (uri) VALUES ('%s')", selectedDirectory.getAbsolutePath());
                    statement.executeUpdate(SQL);

                    // Insert series
                    SQL = String.format("INSERT INTO Series (title) VALUES ('%s')", selectedDirectory.getName());
                    statement.executeUpdate(SQL);

                    directoryId = SqlUtil.getLastIntRow(connection, "Directories", "id");
                    seriesId = SqlUtil.getLastIntRow(connection, "Series", "id");


                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            File[] files = findFiles(selectedDirectory);
            int volume = 1;
            for(File f : files) {
                try {
                    int comicId = addComic(f, directoryId, seriesId, volume++);
                    getComicCover(f, comicId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            showSeries();
        }
    }

    @FXML private void exit() {
        Platform.exit();
    }

    private void showSeries() {
        libraryPane.getChildren().clear();
        toolbarBack.setDisable(true);
        if(lastSeries != 0) {
            toolbarForward.setDisable(false);
            toolbarForward.setOnAction(event -> showComics(lastSeries));
        }
        try {
            String SQL = "SELECT Series.id as s_id, Comics.id as c_id FROM Series, Comics WHERE Series.id = Comics.series_id AND volume = 1";
            ResultSet rs = connection.createStatement().executeQuery(SQL);
            while(rs.next()) {
                int seriesId = rs.getInt("s_id");
                LibraryItem libraryItem = new LibraryItem(seriesId, rs.getInt("c_id"), false);

                libraryItem.setOnMouseClicked(event -> showComics(seriesId));

                libraryPane.getChildren().add(libraryItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showComics(int seriesId) {
        libraryPane.getChildren().clear();
        toolbarBack.setDisable(false);
        lastSeries = seriesId;
        try {
            String SQL = String.format("SELECT * FROM Comics WHERE series_id = %d", seriesId);
            ResultSet rs = connection.createStatement().executeQuery(SQL);
            while(rs.next()) {
                LibraryItem libraryItem = new LibraryItem(seriesId, rs.getInt("id"), true);
                libraryPane.getChildren().add(libraryItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean directoryExists(String uri) {
        try {
            String SQL = "SELECT uri FROM Directories";
            ResultSet rs = connection.createStatement().executeQuery(SQL);
            while(rs.next()) {
                if(rs.getString("uri").equals(uri)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int addComic(File f, int directoryId, int seriesId, int volume) {
        int comicId = 0;
        String filename = f.getName();
        try {
            Statement statement = connection.createStatement();
            String SQL = String.format("INSERT INTO Comics (directory_id, series_id, volume, filename) VALUES (%d, %d, %d, '%s')", directoryId, seriesId, volume, filename);
            statement.executeUpdate(SQL);
            comicId = SqlUtil.getLastIntRow(connection, "Comics", "id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comicId;
    }

    private File[] findFiles(File dir) {
        return dir.listFiles((dir1, name) -> name.endsWith(".cbz"));
    }

    private void getComicCover(File f, int comicId) {
        Path path = Paths.get(f.getAbsolutePath());
        String page = ArchiveUtil.getPage(this.getClass().getClassLoader(), path, 1);
        new File(System.getProperty("user.home"), ".tread/covers/").mkdirs();
        Path dest = Paths.get(System.getProperty("user.home"), ".tread/covers/" + comicId);
        ArchiveUtil.extractPage(this.getClass().getClassLoader(), path, page, dest);
    }
}
