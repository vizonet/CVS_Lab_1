package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

import javafx.stage.Window;
import org.opencv.core.Core;

public class Main extends Application {
    public static Window primaryStage;

    @Override
    public void init() throws Exception {
        super.init();
        // Инициализация любых данных, до включения основного потока Start в работу
    }

    @Override
    public void start(Stage stage) throws Exception { // запуск главного окна программы
        /** // параметры командной строки
        Application.Parameters params = getParameters();
        List<String> unnamedParams = getParameters().getUnnamed();
        int i =0;
        for(String param: unnamedParams){
            i++;
            System.out.printf("%d - %s \n", i, param);
        }*/
        window_init(stage,"Linear spatial filtering","../resources/spatial_filter");
        set_icon("../resources/icon.jpg", stage);
    }

    @Override
    public void stop() throws Exception {
        // Все действия при закрытии приложения
        super.stop();
    }

    static { // загрузка библиотеки OpenCV в статическую память
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // nu.pattern.OpenCV.loadShared(); // для maven
    }

    public static void main(String[] args) {
        /** // проверка библиотеки OpenCV
        System.out.println(Core.VERSION); // 3.3.0
        System.out.println(Core.VERSION_MAJOR); // 3
        System.out.println(Core.VERSION_MINOR); // 3
        System.out.println(Core.VERSION_REVISION); // 0
        System.out.println(Core.NATIVE_LIBRARY_NAME); // opencv_java
        System.out.println(Core.getBuildInformation());
        */
        launch(args);// запуск окна программы
    }

    public static Parent loadFXML(String fxml) throws IOException { // загрузчик fxml-шаблонов
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void window_init(Stage stage, String title, String fxml_path) throws Exception { // инициализация окон
        // Parent root = FXMLLoader.load(getClass().getResource(fxml_path));
        Scene scene = new Scene(loadFXML(fxml_path));
        stage.setScene(scene); // , 300, 275
        stage.setTitle(title); // заголовок окна
        stage.show();
    }

    public void set_icon(String icon_path, Stage stage) {
        // вывод иконки окна
        InputStream iconStream = getClass().getResourceAsStream(icon_path);
        Image icon = new Image(iconStream);
        stage.getIcons().add(icon);
    }
}

