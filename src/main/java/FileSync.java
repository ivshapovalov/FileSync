import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class FileSync {
    private final Path srcPath;
    private final Path destPath;

    private FileSync(Path srcPath, Path destPath) {
        this.destPath = destPath;
        this.srcPath = srcPath;
    }

    public static void main(String[] args) {

        if (IllegalParameters(args)) {
            return;
        }

        Path srcPath = FileSystems.getDefault().getPath(args[0]);
        Path destPath = FileSystems.getDefault().getPath(args[1]);

        new FileSync(srcPath, destPath).synchronize();
    }

    private static boolean IllegalParameters(String[] args) {
        if (args.length != 2) {
            System.out.println("Количество параметров должно быть равно двум. Source каталог и Destination каталог");
            return true;
        }
        String srcDir = args[0];
        Path srcPath = FileSystems.getDefault().getPath(srcDir);
        if (!Files.exists(srcPath)) {
            System.out.println("Errors: Source directory does not exists!. Exit program");
            return true;
        }
        return false;
    }


    private void synchronize() {
        System.out.println("Синхронизация начата: " + Calendar.getInstance().getTime());

        if (!Files.exists(destPath)) {
            try {
                System.out.println("Destination каталог отсутствует: " + destPath);
                createDirectoriesRecursively(destPath);
            } catch (IOException e) {

                System.out.println("Вторым параметром задано имя файла. Укажите имя каталога");
            }
        }

        if (!Files.isDirectory(srcPath)) {
            updateFileFromSourceFile();
            return;
        }
        updateNewFiles();
        deleteRemovedFiles();
        System.out.println("Синхронизация окончена: " + Calendar.getInstance().getTime());
    }

    private void createDirectoriesRecursively(Path destPath) throws IOException {
        Path destPathNew = destPath.getParent();
        if (!Files.exists(destPathNew)) {
            createDirectoriesRecursively(destPathNew);
        }
        Files.createDirectory(destPath);
        System.out.println("Создаем каталог: " + destPath);
    }

    private void updateNewFiles() {

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
                    copyFilesFromDirToDir(srcPathNew, destPathNew);
                }
            }
        }
    }

    private void copyFilesFromDirToDir(Path srcPathNew, Path destPathNew) {
        try {
            if (Files.size(srcPathNew) != Files.size(destPathNew)) {
                Files.copy(srcPathNew, destPathNew, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Измененный файл: " + srcPathNew);
                System.out.println("Обновляем файл: " + destPathNew);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFileFromSourceFile() {
        if (Files.isDirectory(destPath)) {
            int srcPathCount = srcPath.getNameCount();
            Path relativePath = srcPath.subpath(srcPathCount - 1, srcPathCount);
            Path destPathNew = destPath.resolve(relativePath);
            if (Files.exists(destPathNew)) {
                copyFilesFromDirToDir(srcPath, destPathNew);
            } else {
                try {
                    Files.copy(srcPath, destPathNew, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Новый файл: " + srcPath);
                    System.out.println("Создаем файл: " + destPathNew);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void deleteRemovedFiles() {
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

    private List<Path> listSourceFiles(Path dir) throws IOException {
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

