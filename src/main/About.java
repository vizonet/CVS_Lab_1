package main;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class About {
    public void onClickOK(ActionEvent actionEvent) { // закрытие окна
        System.out.println("Закрытие окна About");
        final Node source = (Node) actionEvent.getSource();
        final Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
