package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;
import javafx.util.StringConverter;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller  implements Initializable {
    // связка элементов окна с переменными по fx:id-имени
    @FXML
    private ImageView originalImg;
    @FXML
    private ImageView grayscaleImg;
    @FXML
    private ImageView apply1Img;
    @FXML
    private ImageView apply2Img;

    // инциализация спиннеров (filter matrix)
    @FXML
    List<Spinner> spArray; // список элементов окна типа Spinner

    void initFilterMatrix() { // инициализация Spinner-контролов
        Pattern p = Pattern.compile("^-?\\d+$"); // целые числа (в т.ч. отрицательные) // (\d+\.?\d*)? - вещественные чисел
        for (int i = 0; i < spArray.size(); i++) {
            // параметры спиннера // new Spinner(-5, 5, 1, 2)); // min, max, initial, step
            spArray.get(i).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-255, 255, 0, 1));
            spArray.get(i).getValueFactory().setConverter(
                    new StringConverter() { // проверка на введённое значение и -> конвертирование
                        @Override
                        public String toString(Object obj) {
                            return (obj == null) ? "0" : obj.toString();
                        }
                        @Override
                        public Integer fromString(String s) {
                            if (s.matches(p.toString())) {
                                try {
                                    return Integer.valueOf(s);
                                }
                                catch (NumberFormatException e) {
                                    return 0;
                                }
                            }
                            return 0;
                        }
                    }
            );
            spArray.get(i).setEditable(true); // ввод пользовательских значений
            // обработчик ввода -> проверка по regexp
            int finalI = i; // счёткик -> в константу
            spArray.get(i).valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!p.matcher(newValue.toString()).matches())
                    spArray.get(finalI).getValueFactory().setValue(oldValue);
                System.out.println("sp" + finalI + " = " + spArray.get(finalI).getValue());
            });
            System.out.println("sp" + i + ": " + spArray.get(i).getValue());
        }
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @Override //@FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initFilterMatrix();
        // тестовый вывод спиннера
    }
    private int[][] getFilterMatrix() { // инициализация значениями (после нажатия на Apply)
        int[][] matrix = new int[3][3]; // матрица 3х3
        int n = 0; // линейный индекс
        for (int j=0; j<3; j++) {
            for (int i=0; i<3; i++) {
                matrix[j][i] = (int) spArray.get(n).getValue();
                n++;
            }
        }
        return matrix;
    }
    private void viewMatrix(List<Spinner> matrix){
        System.out.println("\nМатрица 3х3:");
        int n = 0; // линейный счётчик
        for (int j = 0; j < Math.sqrt(matrix.size()); j++) {
            for (int i = 0; i < Math.sqrt(matrix.size()); i++) {
                System.out.printf("%4d  ", matrix.get(n).getValue());
                n++;
            }
            System.out.println();
        }
    }
    // преобразование изображения с помощью фильтра
    public void filter(Mat img, int[][] fmatrix) {

    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss > "); // формат даты
    Path tmpPath = null; // временный путь
    Mat grayscaleMat, apply1Mat, apply2Mat; // для записи на диск
    private final Desktop desktop = Desktop.getDesktop();
    //для записи на диск
    // поиск в папке ресурсов, если подключено в fxml
    InputStream picStream = getClass().getResourceAsStream("../resources/empty_img.png");
    Image saveImg = new Image(picStream);


    /** способ активизации шаблона .fxml
     @FXML
     private void panel() throws IOException {
     Main.setRoot("spatial_filter");
     }
     */

    @FXML
    public Mat load_image() throws IOException { // обработчик кнопки "Load Image" MouseEvent mouseEvent../resources/empty_img.png
        // загрузка изображений (Прохоренок Н.)
        // public static Mat imread(String filename);
        // public static Mat imread(String filename, int flags); // сигнатура вызова
        List<File> files = choiceFileDialog("load"); // список файлов
        // установка изображения в слот
        tmpPath = setTempPath(files.get(0)); // копирование файла в path на латиннице (OpenCV не понимает кириллицу в пути)
        Image image = new Image(tmpPath.toUri().toString()); // формат пути: file:/C:/folder/file.jpg
        originalImg.setImage(image);

        // преобразования изображения
        Mat imgMat = Imgcodecs.imread(tmpPath.toString(), Imgcodecs.IMREAD_UNCHANGED); // как есть // "C:\\book\\opencv\\foto1.jpg"
        grayscaleMat = Imgcodecs.imread(tmpPath.toString(), Imgcodecs.IMREAD_GRAYSCALE); // оттенки серого
        if (imgMat.empty()) {
            System.out.println("Изображение не загружено (возможно, кириллица в пути файла)");
        } else {
            img_info(imgMat);
            //
            BufferedImage grayscaleBufImg = MatToBufferedImage(grayscaleMat);
            grayscaleImg.setImage(SwingFXUtils.toFXImage(grayscaleBufImg, null));
        }


        delTempPath(tmpPath); // Удаление временного каталога с файлом изображения
        return imgMat;
    }

    private void img_info(Mat imgMat) {
        System.out.println("Type: " + CvType.typeToString(imgMat.type())
                + "; Size WxH: " + imgMat.width() + "x" + imgMat.height()
                + "px; Channels: " + imgMat.channels());
    }

    public void save_grayscale(MouseEvent mouseEvent) { // сохранение серого изображения на диск
        saveFile(grayscaleMat);
    }

    public void save_apply1(MouseEvent mouseEvent) { // сохранение Apply1 на диск
        apply1Mat = Imgcodecs.imread("../resources/empty_img.png", Imgcodecs.IMREAD_UNCHANGED);
        saveFile(apply1Mat);
    }

    public void save_apply2(MouseEvent mouseEvent) { // сохранение Apply2 на диск
        apply2Mat = Imgcodecs.imread("../resources/empty_img.png", Imgcodecs.IMREAD_UNCHANGED);
        saveFile(apply2Mat);
    }

    public void Apply1(ActionEvent actionEvent) {
        viewMatrix(spArray);
    }

    public void Apply2(ActionEvent actionEvent) {
        viewMatrix(spArray);
    }


    private boolean saveFile(Mat matImg) { // Сохранить файл
        List<File> files = choiceFileDialog("save"); // список файлов
        boolean saved = Imgcodecs.imwrite(files.get(0).toString(), matImg);
        System.out.println(dateFormat.format(new Date())
                + (saved ? "Изображение сохранено в " + files.get(0).toString(): "Не удалось сохранить изображение!"));
        return saved;
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
                    System.out.println(dateFormat.format(new Date()) + "Выбран файл: " + file.getAbsolutePath());
                }
            }
            case "multiple" -> { // загрузка нескольких файлов - showOpenMultipleDialog
                fileChooser.setTitle("Open some files"); // заголовок диалога
                flist = fileChooser.showOpenMultipleDialog(Main.primaryStage);
                if (flist != null) {
                    for (File nextfile : flist) {
                        openFile(nextfile);
                    }
                    System.out.println(dateFormat.format(new Date()) + "Выбрано несколько файлов из: " + flist.get(0).getParent());
                }
            }
            case "save" -> { // сохранение файла
                fileChooser.setTitle("Save the file"); // заголовок диалога
                file = fileChooser.showSaveDialog(Main.primaryStage);
                if (file != null) {
                    flist.add(file);
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(saveImg, null), getFileExtension(file), file);
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
            System.out.println(dateFormat.format(new Date()) + "Создан временный каталог с файлом: " + newfile.toString());
        } else {
            System.out.println("Временный каталог не создан!");
        }
        return newfile;
    }

    void delTempPath(Path tmpPath) {
        File file = new File(tmpPath.toString());
        if (file.exists()) {
            if (file.delete() && new File(file.getParent()).delete()) { // удаляется файл, затем каталог
                System.out.println(dateFormat.format(new Date()) + "Временный каталог удалён: " + file.getParent());
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

    // методы преобразования изображения (Прохорёнок Н., с.104-105)
    public static BufferedImage MatToBufferedImage(Mat m) { // из матрицы в буфер в формате изображения
        if (m == null || m.empty()) return null;
        if (m.depth() == CvType.CV_8U) {}
        else if (m.depth() == CvType.CV_16U) { // CV_16U => CV_8U
            Mat m_16 = new Mat();
            m.convertTo(m_16, CvType.CV_8U, 255.0 / 65535);
            m = m_16;
        }
        else if (m.depth() == CvType.CV_32F) { // CV_32F => CV_8U
            Mat m_32 = new Mat();
            m.convertTo(m_32, CvType.CV_8U, 255);
            m = m_32;
        }
        else
            return null;
        int type = 0;
        if (m.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else if (m.channels() == 3)
            type = BufferedImage.TYPE_3BYTE_BGR;
        else if (m.channels() == 4)
            type = BufferedImage.TYPE_4BYTE_ABGR;
        else
            return null;
        byte[] buf = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, buf);
        byte tmp = 0;
        if (m.channels() == 4) { // BGRA => ABGR
            for (int i = 0; i < buf.length; i += 4) {
                tmp = buf[i + 3];
                buf[i + 3] = buf[i + 2];
                buf[i + 2] = buf[i + 1];
                buf[i + 1] = buf[i];
                buf[i] = tmp;
            }
        }
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] data =
                ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buf, 0, data, 0, buf.length);
        return image;
    }

    public static Mat BufferedImageToMat(BufferedImage img) { // из буфера в матрицу
        if (img == null) return new Mat();
        int type = 0;
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            type = CvType.CV_8UC1;
        }
        else if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            type = CvType.CV_8UC3;
        }
        else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            type = CvType.CV_8UC4;
        }
        else return new Mat();
        Mat m = new Mat(img.getHeight(), img.getWidth(), type);
        byte[] data =
                ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        if (type == CvType.CV_8UC1 || type == CvType.CV_8UC3) {
            m.put(0, 0, data);
            return m;
        }
        byte[] buf = Arrays.copyOf(data, data.length);
        byte tmp = 0;
        for (int i = 0; i < buf.length; i += 4) { // ABGR => BGRA
            tmp = buf[i];
            buf[i] = buf[i + 1];
            buf[i + 1] = buf[i + 2];
            buf[i + 2] = buf[i + 3];
            buf[i + 3] = tmp;
        }
        m.put(0, 0, buf);
        return m;
    }
    // --------------
}
