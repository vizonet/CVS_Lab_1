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

    static { // загрузка библиотеки OpenCV в статическую память
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // nu.pattern.OpenCV.loadShared(); // для maven
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // главное окно программы
        //Parent root = FXMLLoader.load(getClass().getResource("../resources/spatial_filter.fxml"));
        Scene scene = new Scene(loadFXML("../resources/spatial_filter"));
        primaryStage.setScene(scene); // , 300, 275
        primaryStage.setTitle("Linear spatial filtering"); // заголовок окна
        InputStream iconStream = getClass().getResourceAsStream("../resources/icon.ico");
        Image icon = new Image(iconStream);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
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

    @Override
    public void stop(){
        // Все действия при закрытии приложения
    }

    @Override
    public void init(){
        // Инициализация любых данных, до включения основного потока Start в работу
    }

    private static Parent loadFXML(String fxml) throws IOException { // загрузчик fxml-шаблонов
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
}
