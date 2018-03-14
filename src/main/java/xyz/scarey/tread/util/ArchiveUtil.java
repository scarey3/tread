package xyz.scarey.tread.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class ArchiveUtil {
    public static String getPageFileName(ClassLoader loader, Path zip, int page) {
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

    public static void extractPage(ClassLoader loader, int page, Path zip, Path dest) {
        String filename = getPageFileName(loader, zip, page);
        try {
            FileSystems.newFileSystem(zip, loader)
                    .getRootDirectories()
                    .forEach(root -> {
                        try {
                            Files.walk(root).forEach(path -> {
                                if(path.toString().endsWith(filename)) {
                                    try {
                                        //Path newDest = Paths.get(dest.toString());
                                        //Files.createDirectories(dest);
                                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
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
