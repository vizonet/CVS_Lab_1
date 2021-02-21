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
    public void start(Stage primaryStage) throws Exception { // запуск главного окна программы
        /** // получаем переданные параметры
        Application.Parameters params = getParameters();
        List<String> unnamedParams = getParameters().getUnnamed();
        int i =0;
        for(String param: unnamedParams){
            i++;
            System.out.printf("%d - %s \n", i, param);
        }*/
        //Parent root = FXMLLoader.load(getClass().getResource("../resources/spatial_filter.fxml"));
        Scene scene = new Scene(loadFXML("../resources/spatial_filter"));
        primaryStage.setScene(scene); // , 300, 275
        primaryStage.setTitle("Linear spatial filtering"); // заголовок окна
        InputStream iconStream = getClass().getResourceAsStream("../resources/icon.jpg");
        Image icon = new Image(iconStream);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
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

    private static Parent loadFXML(String fxml) throws IOException { // загрузчик fxml-шаблонов
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
}
