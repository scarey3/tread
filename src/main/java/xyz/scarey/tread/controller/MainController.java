package xyz.scarey.tread.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import net.coobird.thumbnailator.Thumbnails;
import xyz.scarey.tread.Database;
import xyz.scarey.tread.component.LibraryItem;
import xyz.scarey.tread.model.Series;
import xyz.scarey.tread.util.ArchiveUtil;
import xyz.scarey.tread.util.SqlUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    private final static Logger log = Logger.getLogger(MainController.class.getName());

    private Database database;
    private Connection connection;
    private ObservableList<Series> library;
    private int lastSeries = 0;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private FlowPane libraryPane;
    @FXML
    private Button toolbarBack;
    @FXML
    private Button toolbarForward;
    @FXML
    private Label toolbarLabel;
    @FXML
    private ProgressBar progressBar;

    public MainController() {
    }

    @FXML
    void initialize() {
        // create directories for user data.
        try {
            Files.createDirectories(Paths.get(System.getProperty("user.home") + "/.tread/covers"));
            Files.createDirectories(Paths.get(System.getProperty("user.home") + "/.tread/tmp/covers"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        database = new Database();
        try {
            connection = database.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        showSeries();
    }

    @FXML
    private void onBack() {
        showSeries();
    }

    @FXML
    private void addFolder() {
        int directoryId = 0;
        int seriesId = 0;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());

        log.log(Level.INFO, "Adding folder");

        if (selectedDirectory != null) {
            if (directoryExists(selectedDirectory.getAbsolutePath())) {
                log.log(Level.INFO, "Directory already exists!");

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
                log.log(Level.INFO, "Directory and series added to database");
            }
            File[] files = findFiles(selectedDirectory);
            addFiles(files, directoryId, seriesId);
        }
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    private void showSeries() {
        libraryPane.getChildren().clear();
        toolbarBack.setDisable(true);
        toolbarLabel.setText("Series");
        if (lastSeries != 0) {
            toolbarForward.setDisable(false);
            toolbarForward.setOnAction(event -> showComics(lastSeries));
        }
        try {
            String SQL = "SELECT Series.id AS s_id, Comics.id AS c_id FROM Series, Comics WHERE Series.id = Comics.series_id AND volume = 1";
            ResultSet rs = connection.createStatement().executeQuery(SQL);
            while (rs.next()) {
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
        toolbarForward.setDisable(true);
        lastSeries = seriesId;
        try {
            String SQL = String.format("SELECT title FROM Series WHERE id = %d", seriesId);
            ResultSet rs = connection.createStatement().executeQuery(SQL);
            while (rs.next()) {
                toolbarLabel.setText(rs.getString("title"));
            }
            SQL = String.format("SELECT * FROM Comics WHERE series_id = %d", seriesId);
            rs = connection.createStatement().executeQuery(SQL);
            while (rs.next()) {
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
            while (rs.next()) {
                if (rs.getString("uri").equals(uri)) {
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

    private void addFiles(File[] files, int directoryId, int seriesId) {
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int volume = 1;
                for (File f : files) {
                    try {
                        int comicId = addComic(f, directoryId, seriesId, volume++);
                        getComicCover(f, comicId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateProgress(volume-1, files.length);
                    log.log(Level.INFO, "Volume " + (volume - 1) + " added");
                }

                return null;
            }

            @Override
            protected void succeeded() {
                progressBar.setVisible(false);
                showSeries();
            }
        };

        progressBar.setVisible(true);
        progressBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    private void getComicCover(File f, int comicId) throws IOException {
        Path path = Paths.get(f.getAbsolutePath());
        Path dest = Paths.get(System.getProperty("user.home"), ".tread/tmp/covers/" + comicId);
        ArchiveUtil.extractPage(this.getClass().getClassLoader(), 1, path, dest);

        // Resize image
        File in = dest.toFile();
        BufferedImage image = ImageIO.read(in);
        //BufferedImage resized = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, 300);
        BufferedImage resized = Thumbnails.of(image)
                .size(300, 300)
                .keepAspectRatio(true)
                .asBufferedImage();

        File out = new File(System.getProperty("user.home"), ".tread/covers/" + comicId);
        ImageIO.write(resized, "jpg", out);
        dest.toFile().delete();
    }
}
