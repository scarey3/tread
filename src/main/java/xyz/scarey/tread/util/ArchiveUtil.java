package xyz.scarey.tread.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class ArchiveUtil {
    public static String getPage(ClassLoader loader, Path zip, int page) {
        final ArrayList<String> list = new ArrayList<>();
        try {
            FileSystems.newFileSystem(zip, loader)
                    .getRootDirectories()
                    .forEach(root -> {
                        try {
                            Files.walk(root).forEach(path -> {
                                if(path.toString().endsWith(".jpg")) {
                                    list.add(path.toString());
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        list.sort(String::compareToIgnoreCase);

        return list.get(page-1);
    }

    public static void extractPage(ClassLoader loader, Path zip, String filename, Path dest) {
        try {
            FileSystems.newFileSystem(zip, loader)
                    .getRootDirectories()
                    .forEach(root -> {
                        try {
                            Files.walk(root).forEach(path -> {
                                if(path.toString().endsWith(filename)) {
                                    try {
                                        Path newDest = Paths.get(dest.toString());
                                        Files.copy(path, newDest, StandardCopyOption.REPLACE_EXISTING);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
