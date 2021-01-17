package main;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller {
    Path tmpPath = null;
    private final Desktop desktop = Desktop.getDesktop();
    InputStream picStream = getClass().getResourceAsStream("../resources/empty_img.png");
    Image picture = new Image(picStream);

    /** способ активизации шаблона .fxml
     @FXML
     private void panel() throws IOException {
     Main.setRoot("spatial_filter");
     }
     */

    @FXML
    public Mat load_image() throws IOException { // обработчик кнопки "Load Image" MouseEvent mouseEvent
        // загрузка изображений (Прохоренок Н.)
        // public static Mat imread(String filename);
        // public static Mat imread(String filename, int flags); // сигнатура вызова
        List<File> files = choiceFileDialog("load"); // список файлов
        tmpPath = setTempPath(files.get(0)); // копирование файла в path на латиннице (OpenCV не понимает кириллицу в пути)
        Mat img = Imgcodecs.imread(tmpPath.toString()); // "C:\\book\\opencv\\foto1.jpg"
        if (img.empty()) {
            System.out.println("Изображение не загружено (возможно, кириллица в пути файла)");
        }
        System.out.println("Тип изображения: " + CvType.typeToString(img.type()));
        System.out.println("Ширина: " + img.width());
        System.out.println("Высота: " + img.height());
        System.out.println("Число каналов: " + img.channels());

        closeApp();
        return img;
    }

    void closeApp() { // окончание работы программы
        delTempPath(tmpPath); // Удаление временного каталога с файлом изображения
    }

    /* Обработка загрузки и сохранения файлов */
    public List<File> choiceFileDialog(String mode) { // ActionEvent event
        File file = new File(""); // загруженный файл
        List<File> flist = new ArrayList<File>(); // список файлов
        FileChooser fileChooser = new FileChooser(); // Класс работы с диалогом выборки и сохранения
        /** // для одного варианта выбора типа
         fileChooser.setTitle("Open Image");     // Заголовок диалога
         FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html"); // Расширение
         fileChooser.getExtensionFilters().add(extFilter);
         */

        configureFileChooser(fileChooser); // конфигурация параметров окна выбора файла
        switch (mode) {
            case "load" -> { // Открытие файла
                fileChooser.setTitle("Open file"); // заголовок диалога
                file = fileChooser.showOpenDialog(Main.primaryStage); // Указываем окно текущей сцены
                if (file != null) {
                    if (openFile(file))
                        flist.add(file);
                    System.out.println("Открыт файл: " + file.getAbsolutePath());
                }
            }
            case "multiple" -> { // загрузка нескольких файлов - showOpenMultipleDialog
                fileChooser.setTitle("Open some files"); // заголовок диалога
                flist = fileChooser.showOpenMultipleDialog(Main.primaryStage);
                if (flist != null) {
                    for (File nextfile : flist) {
                        openFile(nextfile);
                    }
                    System.out.println("Открыты несколько файлов из: " + flist.get(0).getParent());
                }
            }
            case "save" -> { // сохранение файла
                fileChooser.setTitle("Save the file"); // заголовок диалога
                file = fileChooser.showSaveDialog(Main.primaryStage);
                if (file != null) {
                    flist.add(file);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(picture, null), getFileExtension(file), file);
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
        return flist;
    }

    private boolean openFile(File file) { // Открыть файл
        boolean result = true;
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                    Main.class.getName()).log(
                    Level.SEVERE, null, ex
            );
            result = false;
        }
        return result;
    }

    Path setTempPath(File file) throws IOException { // Создание временного каталога с файлом
        Path newfile = null;
        File tmpPath = new File("C:/__tmp");
        if (tmpPath.exists() || tmpPath.mkdir()) {
            Path source = FileSystems.getDefault().getPath(file.getAbsolutePath());
            Path to = FileSystems.getDefault().getPath(tmpPath.getPath(), "__image." + getFileExtension(file));
            newfile = Files.copy(source, to, REPLACE_EXISTING); // to.resolve(source.getFileName())
            System.out.println("Создан временный каталог с файлом: " + newfile.toString());
        } else {
            System.out.println("Временный каталог не создан!");
        }
        return newfile;
    }

    void delTempPath(Path tmpPath) {
        File file = new File(tmpPath.toString());
        if (file.exists()) {
            if (file.delete() && new File(file.getParent()).delete()) { // удаляется файл, затем каталог
                System.out.println("Временный каталог удалён: " + file.getParent());
            } else {
                System.out.println("Невозможно удалить временный файл и/или каталог: " + file.getAbsolutePath());
            }
        } else {
            System.out.println("Временный каталог не существует.");
        }
    }

    private static String getFileExtension(File file) { // Расширение файла
        String fileName = file.getName();
        // если в имени файла есть точка и она не является первым символом в названии файла
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то возвращаем все знаки после последней точки в названии файла: ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".") + 1);
            // иначе - расширения нет
        else return "";
    }
    private static void configureFileChooser(FileChooser fileChooser) { // Конфигурация окна открытия файла
        // Set title for FileChooser
        fileChooser.setTitle("Select Pictures");
        // Set Initial Directory
        fileChooser.setInitialDirectory(new File("C:/"));
        // Add Extension Filters
        fileChooser.getExtensionFilters().addAll( // типы загружаемых файлов
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                // JPEG files
                new FileChooser.ExtensionFilter("JP2", "*.jp2"), // JPEG 2000 files
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("JPE", "*.jpe"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
                // Windows bitmaps
                new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                new FileChooser.ExtensionFilter("DIB", "*.dib"),
                // TIFF files
                new FileChooser.ExtensionFilter("TIF", "*.tif"),
                new FileChooser.ExtensionFilter("TIFF", "*.tiff"),
                // Portable Network Graphics
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                // Portable image format
                new FileChooser.ExtensionFilter("PPM", "*.ppm"),
                new FileChooser.ExtensionFilter("PGM", "*.pgm"),
                new FileChooser.ExtensionFilter("PBM", "*.pbm"),
                // Sun roasters
                new FileChooser.ExtensionFilter("SM", "*.sm"),
                // OpenCV version 3.3.0
                new FileChooser.ExtensionFilter("PIC", "*.pic"),
                new FileChooser.ExtensionFilter("PXM", "*.PXM"),
                new FileChooser.ExtensionFilter("PNM", "*.pnm"),
                new FileChooser.ExtensionFilter("HDR", "*.hdr"),
                new FileChooser.ExtensionFilter("EXR", "*.exr"),
                new FileChooser.ExtensionFilter("WEBP", "*.webp") //,
        );
    }

}
