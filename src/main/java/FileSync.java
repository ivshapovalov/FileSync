import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FileSync {
    public static void main(String[] args) {
        if (IllegalParameters(args)) {
            return;
        }

        System.out.println("Синхронизация начата: " + Calendar.getInstance().getTime());

        Path srcPath = FileSystems.getDefault().getPath(args[0]);
        Path destPath = FileSystems.getDefault().getPath(args[1]);
        if (!Files.exists(destPath)) {
            try {
                Files.createDirectory(destPath);
                System.out.println("Destination каталог отсутствует: " + destPath);
                System.out.println("Создаем каталог: " + destPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateNewFiles(srcPath, destPath);
        deleteRemovedFiles(srcPath, destPath);
        System.out.println("Синхронизация окончена: " + Calendar.getInstance().getTime());
    }

    private static boolean IllegalParameters(String[] args) {
        if (args.length != 2) {
            System.out.println("Количество параметров должно быть равно двум. Source каталог и Destination каталог");
        }
        String srcDir = args[0];
        String destDir = args[1];
        Path srcPath = FileSystems.getDefault().getPath(srcDir);
        if (!Files.exists(srcPath)) {
            System.out.println("Errors: Source directory does not exists!. Exit program");
            return true;
        }
        return false;
    }

    private static void updateNewFiles(Path srcPath, Path destPath) {
        int srcPathCount = srcPath.getNameCount();
        List<Path> sourceFiles = new ArrayList<>();
        try {
            sourceFiles = listSourceFiles(srcPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Path srcPathNew : sourceFiles
                ) {
            int currentPathCount = srcPathNew.getNameCount();
            Path relativePath = srcPathNew.subpath(srcPathCount, currentPathCount);
            Path destPathNew = destPath.resolve(relativePath);
            if (!Files.exists(destPathNew)) {
                try {
                    if (Files.isDirectory(srcPathNew)) {
                        Files.createDirectory(destPathNew);
                        System.out.println("Новый каталог: " + srcPathNew);
                        System.out.println("Создаем каталог:" + destPathNew);
                    } else {
                        Files.copy(srcPathNew, destPathNew);
                        System.out.println("Новый файл: " + srcPathNew);
                        System.out.println("Создаем файл: " + destPathNew);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (!Files.isDirectory(destPathNew)) {
                    try {
                        if (Files.size(srcPathNew) != Files.size(destPathNew)) {
                            Files.copy(srcPathNew, destPathNew, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Обновлен файл: " + srcPathNew);
                            System.out.println("Обновляем файл: " + destPathNew);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void deleteRemovedFiles(Path srcPath, Path destPath) {
        int destPathCount = destPath.getNameCount();
        List<Path> sourceFiles = new ArrayList<>();
        try {
            sourceFiles = listSourceFiles(destPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Path destPathNew : sourceFiles
                ) {
            int currentPathCount = destPathNew.getNameCount();
            Path relativePath = destPathNew.subpath(destPathCount, currentPathCount);
            Path srcPathNew = srcPath.resolve(relativePath);
            if (!Files.exists(srcPathNew)) {
                try {
                    Files.deleteIfExists(destPathNew);
                    System.out.println("Удален каталог: " + srcPathNew);
                    System.out.println("Удаляем каталог: " + destPathNew);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Path> listSourceFiles(Path dir) throws IOException {
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                result.add(entry);
                if (Files.isDirectory(entry)) {
                    result.addAll(listSourceFiles(entry));
                }
            }
        } catch (DirectoryIteratorException ex) {
            throw ex.getCause();
        }
        return result;
    }
}
