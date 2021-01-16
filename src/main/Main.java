package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Core;

public class Main extends Application {
    private static Scene scene;
    public static final String title = "Linear spatial filtering";
    // установка библиотеки в статическую память
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // nu.pattern.OpenCV.loadShared();
    }
    @Override
    public void start(Stage rootStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("../resources/spatial_filter.fxml"));
        scene = new Scene(loadFXML("../resources/spatial_filter"));  // primary secondary
        rootStage.setScene(scene); // root, 300, 275
        rootStage.setTitle(title);
        InputStream iconStream = getClass().getResourceAsStream("../resources/icon.ico");
        Image image = new Image(iconStream);
        rootStage.getIcons().add(image);
        rootStage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        /* проверка библиотеки OpenCV
        System.out.println(Core.VERSION); // 3.3.0
        System.out.println(Core.VERSION_MAJOR); // 3
        System.out.println(Core.VERSION_MINOR); // 3
        System.out.println(Core.VERSION_REVISION); // 0
        System.out.println(Core.NATIVE_LIBRARY_NAME); // opencv_java
        System.out.println(Core.getBuildInformation());
        */
        // запуск окна программы
        launch(args);
    }
}
