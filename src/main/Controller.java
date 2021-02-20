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
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import static org.opencv.imgproc.Imgproc.filter2D;

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
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller  implements Initializable {
    // связи элементов окна с переменными по имени fx:id
    @FXML
    private ImageView originalImg;
    @FXML
    private ImageView grayscaleImg;
    @FXML
    private ImageView apply1Img;
    @FXML
    private ImageView apply2Img;

    @FXML                   // инциализация спиннеров (filter matrix)
    List<Spinner> spArray;  // список элементов окна типа Spinner
    int filterSize;         // размер матрицы свёртки

    Mat imgSrcMat, imgGrayscaleMat;  // матрицы оригинала, обработанного и в оттенках серого изображений
    Mat filterMatrixMat;             // матрица свёртки
    Mat apply1Mat, apply2Mat;        // для записи на диск
    // private final Desktop desktop = Desktop.getDesktop(); // для открытия файла сопоставленным по типу приложением

    private final String tempDir = "C:/__tmp"; // временный каталог для решения проблем с кириллицей в путях файлов в OpenCV
    // поиск в папке ресурсов, если подключено в fxml
    InputStream picEmpty = getClass().getResourceAsStream("../resources/empty_img.png");
    Image saveImg = new Image(picEmpty);
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss > "); // формат даты

    @FXML
    public void load_image(ActionEvent actionEvent) throws IOException { // обработчик кнопки "Load Image" MouseEvent mouseEvent../resources/empty_img.png
        // загрузка изображений (Прохоренок Н.)
        // public static Mat imread(String filename);
        // public static Mat imread(String filename, int flags); // сигнатура вызова
        List<File> files = choiceFileDialog("load"); // список файлов
        Path tmpPath = setTempPath(files.get(0), "load"); // копирование файла в path на латиннице (OpenCV не понимает кириллицу в пути)
        Image image = new Image(tmpPath.toUri().toString()); // формат пути: file:/C:/folder/file.jpg
        originalImg.setImage(image); // установка изображения в слот

        // преобразования изображения к матрице
        imgSrcMat = Imgcodecs.imread(tmpPath.toString(), Imgcodecs.IMREAD_UNCHANGED); // оригинальное изображение
        imgGrayscaleMat = Imgcodecs.imread(tmpPath.toString(), Imgcodecs.IMREAD_GRAYSCALE); // оттенки серого
        if (imgSrcMat.empty()) {
            // кириллица в пути в OpenCV недопустима и решена размещением файлов во временном каталоге
            System.out.println("Изображение не загружено!");
        } else {
            img_info(imgSrcMat);
            // установка изображения в слот интерфейса окна
            BufferedImage grayscaleBufImg = MatToBufferedImage(imgGrayscaleMat);
            grayscaleImg.setImage(SwingFXUtils.toFXImage(grayscaleBufImg, null));
        }
        delTempPath(tmpPath); // удаление временного каталога с файлом изображения
    }

    @FXML
    public void save_grayscale(ActionEvent actionEvent) throws IOException { // сохранение серого изображения на диск
        saveFile(imgGrayscaleMat);
    }
    @FXML
    public void save_apply1(ActionEvent actionEvent) throws IOException { // сохранение Apply1 на диск
        saveFile(apply1Mat);
    }
    @FXML
    public void save_apply2(ActionEvent actionEvent) throws IOException { // сохранение Apply2 на диск
        saveFile(apply2Mat);
    }
    @FXML
    public void Apply1(ActionEvent actionEvent) {
        apply1Mat = apply(apply1Img);
    }
    @FXML
    public void Apply2(ActionEvent actionEvent) {
        apply2Mat = apply(apply2Img);
    }

    /* способ активизации шаблона .fxml
     @FXML
     private void panel() throws IOException {
     Main.setRoot("spatial_filter");
     }
     @param mouseEvent
     */

    @Override //@FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /**
         * Initializes the controller class. This method is automatically called
         * after the fxml file has been loaded.
         */
        filterSize = (int) Math.sqrt(spArray.size());
        initInputMatrix();
    }

    private Mat apply(ImageView applyImg) {
        Mat fmatr = initFilterMatrix(spArray);
        viewMatrix(fmatr);
        Mat applyMat = filter(imgSrcMat, new Mat(), fmatr); //imgSrcMat.rows(), imgSrcMat.cols(), imgSrcMat.type() фильтрация изображения матрицей свёртки
        // установка изображения в слот интерфейса окна
        applyImg.setImage(SwingFXUtils.toFXImage(MatToBufferedImage(applyMat), null));
        return applyMat;
    }

    // преобразование изображения с помощью фильтра
    public Mat filter(Mat imgSrc, Mat imgDst, Mat kernel) {
        // метод filter2D() - фильтр с произвольными значениями на основе мартицы свёртки
        // (OpenCV Java, Прохорёнок Н., с.200)
        int ddepth = -1; // глубина целевого изображения (по умолчанию - глубина оригинала)
        Point anchor = new Point(-1, -1); // координаты ядра свёртки (по умолчанию - центр матрицы)
        double delta = 0; // прибавление к результату (по умолчанию - 0)
        // тип рамки вокруг изображения (разд. 4.9). По умолчанию - BORDER_DEFAULT // borderInterpolate - интерполяция
        int borderType = Core.BORDER_REPLICATE; // BORDER_REPLICATE — повтор крайних пикселов
        // фильтрация
        filter2D(imgSrc, imgDst, ddepth, kernel, anchor, delta, borderType);
        return imgDst;
    }

    private Mat initFilterMatrix(List<Spinner> matrix) { // инициализация значениями (после нажатия на Apply)
        // матрица свёртки из массива спиннеров
        filterMatrixMat = new Mat(filterSize, filterSize, imgGrayscaleMat.type()); // CvType.CV_32F инициализация матрицы свёртки
        double[] data = new double[matrix.size()];  // данные для матрицы
        System.out.print("\nИнициализация матрицы свёртки (data):\n");
        for (int i=0; i<matrix.size(); i++) {
            data[i] = 1.0 * (int) spArray.get(i).getValue();
            System.out.print("  " + data[i] + (((i+1) % filterSize == 0) ? "\n" : ""));
        }
        filterMatrixMat.put(0, 0, normalized(data));
        return filterMatrixMat;
    }

    double arrSumm(double arr[]) { // Сумма элементов массива
        double summ = 0;
        for (int i=0; i<arr.length; i++) { summ += arr[i]; }
        return summ;
    }

    private double[] normalized(double[] data) { // Нормализация матрицы свёртки
        // поиск суммы элементов
        double summ = arrSumm(data);
        // нормализация
        System.out.print("\nНормализованная матрица свёртки:\n");
        for (int i=0; i<data.length; i++) {
            data[i] = (summ > 0) ? data[i]/summ : 0;
            System.out.print("  " + data[i] + (((i+1) % filterSize == 0) ? "\n" : ""));
        }
        summ = arrSumm(data);
        System.out.print("Сумма элементов после нормализации: " + summ + "\n");
        return data;
    }

    void initInputMatrix() { // инициализация Spinner-контролов
        Pattern p = Pattern.compile("^-?\\d+$"); // целые числа (в т.ч. отрицательные) // (\d+\.?\d*)? - вещественные чисел
        for (int i = 0; i < spArray.size(); i++) {
            // параметры спиннера // new Spinner(-5, 5, 1, 2)); // min, max, initial, step
            spArray.get(i).setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-255, 255, 0, 1));
            spArray.get(i).setEditable(true); // ввод пользовательских значений
            spArray.get(i).getValueFactory().setConverter(
                new StringConverter() { // проверка на введённое значение и -> конвертация значения
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
            //System.out.println("sp" + i + ": " + spArray.get(i).getValue());
            // Дополнительные обработчики ввода (работают аналогично конвертеру)
            /*
            int finalI = i; // счётчик -> в константу для использования внутри обработчиков
            spArray.get(i).valueProperty().addListener((obs, oldValue, newValue) -> {
                System.out.println("sp" + finalI + " = " + spArray.get(finalI).getValue());
                if (!p.matcher(spArray.get(finalI).getValue().toString()).matches())
                    spArray.get(finalI).getValueFactory().setValue(oldValue);
            });*/
            // 1. Проверка по regexp (когда он вызывается?)
            /*
            spArray.get(i).valueProperty().addListener((observable, oldValue, newValue) -> {
                if (!p.matcher(newValue.toString()).matches())
                    spArray.get(finalI).getValueFactory().setValue(oldValue);
                System.out.println("sp" + finalI + " = " + spArray.get(finalI).getValue()); // вывод значения при изменении
            });*/
            /*
            // 2. Потеря фокуса
            spArray.get(i).focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    spArray.get(finalI).increment(0); // won't change value, but will commit editor
                }
                System.out.println("sp" + finalI + " = " + spArray.get(finalI).getValue()); // вывод значения при изменении
            });*/
            // вывод значений инициализированных спиннеров
        }
    }

    private void viewMatrix(Mat matrix){
        System.out.println("\nМатрица свёртки " + matrix.size() + " типа " + CvType.typeToString(matrix.type()) + ":\n"
                + matrix.dump());
        /*
        for (int j = 0, r = matrix.rows(); j < r; j++) {
            for (int i = 0, c = matrix.cols(); i < c; i++) {
                System.out.printf(matrix.get(j, i) + " ");
            }
            System.out.println();
        }*/
    }

    private void img_info(Mat imgMat) {
        System.out.println("Type: " + CvType.typeToString(imgMat.type())
                + "; Size WxH: " + imgMat.width() + "x" + imgMat.height()
                + "px; Channels: " + imgMat.channels());
    }

    /* Обработка загрузки и сохранения файлов */
    public List<File> choiceFileDialog(String mode) throws IOException { // ActionEvent event
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
                flist.add(fileChooser.showOpenDialog(Main.primaryStage)); // Указываем окно текущей сцены
                if (flist.size() != 0) {
                    /*
                    if (openFile(file)) {
                    }
                    // else -> вывод ошибки из openFile()
                    */
                    System.out.println(dateFormat.format(new Date()) + "Выбран файл: " + flist.get(0).getAbsolutePath());
                }
            }
            case "multiple" -> { // загрузка нескольких файлов - showOpenMultipleDialog
                fileChooser.setTitle("Open some files"); // заголовок диалога
                flist.addAll(fileChooser.showOpenMultipleDialog(Main.primaryStage));
                if (flist.size() != 0) {
                    /*
                    for (File nextfile : flist) {
                        openFile(nextfile);
                    }*/
                    System.out.println(dateFormat.format(new Date()) + "Выбрано несколько файлов из: " + flist.get(0).getParent());
                }
            }
            case "save" -> { // сохранение файла
                fileChooser.setTitle("Save the file"); // заголовок диалога
                flist.add(file = fileChooser.showSaveDialog(Main.primaryStage));
            }
        }
        return flist;
    }

    // Не применяется
    /*
    private boolean openFile(File file) { // Открыть файл (открывает системное проиложение, сопоставленное с типом файла)
        boolean result = true;
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                    Main.class.getName()).log(
                    Level.SEVERE, null, ex
            );
            System.out.println(ex.getMessage()); // сообщение об ошибке
            result = false;
        }
        return result;
    }*/

    private boolean saveFile(Mat matImg) throws IOException { // Сохранить файл
        boolean saved = false;
        List<File> files = choiceFileDialog("save"); // список файлов
        Path tmpPath = setTempPath(files.get(0), "save"); // копирование файла в path на латиннице (OpenCV не понимает кириллицу в пути)
        Path to = FileSystems.getDefault().getPath(files.get(0).getAbsolutePath());
        /*
        if (files.size() != 0) {
            try { // сохранение изображения saveImg
                ImageIO.write(SwingFXUtils.fromFXImage(saveImg, null), getFileExtension(files.get(0)), tmpPath.toFile());
                saved = true;
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        */
        saved = Imgcodecs.imwrite(tmpPath.toString(), matImg); // сохранение во временный каталог без кириллицы в Path
        Files.copy(tmpPath, to, REPLACE_EXISTING); // to.resolve(source.getFileName())
        System.out.println(dateFormat.format(new Date())
                + (saved ? "Изображение сохранено в " + files.get(0).toString(): "Не удалось сохранить изображение!"));

        delTempPath(tmpPath); // удаление временного каталога с файлом изображения
        return saved;
    }

    Path setTempPath(File file, String mode) throws IOException { // Создание временного каталога с файлом
        Path source = null, to = null;
        File tmpPath = new File(tempDir);
        if (tmpPath.exists() || tmpPath.mkdir()) {
            switch (mode) {
                case "load":
                    source = FileSystems.getDefault().getPath(file.getAbsolutePath());
                    to = FileSystems.getDefault().getPath(tmpPath.getPath(), "__image." + getFileExtension(file));
                    System.out.println(dateFormat.format(new Date()) + "Создан временный каталог с файлом: " + file.toString());
                    return Files.copy(source, to, REPLACE_EXISTING); // to.resolve(source.getFileName())
                case "save":
                    source = FileSystems.getDefault().getPath(tmpPath.getPath(), "__image." + getFileExtension(file));
                    System.out.println(dateFormat.format(new Date()) + "Создан временный каталог: " + tmpPath.toString());
                    return source;
            }
        } else {
            System.out.println("Временный каталог не создан!");
        }
        return null;
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
