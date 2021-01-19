package gforum.add_entity;

import gearth.ui.GEarthController;
import gforum.entities.HForum;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AddEntity {

    public AddEntityController controller;
    public Stage stage;

    public AddEntity() throws Exception {
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("add_entity.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("../webview/images/logo.png")));

        stage.setTitle("Add forum message");
        stage.setScene(new Scene(root, 455, 415));
        stage.setMinWidth(400);
        stage.setMinHeight(340);
        stage.getScene().getStylesheets().add(GEarthController.class.getResource("/gearth/ui/bootstrap3.css").toExternalForm());

//        stage.show();

        stage.setOnCloseRequest(event -> {
            event.consume();
            stage.hide();
        });
    }

    public Stage getStage() {
        return stage;
    }

    public AddEntityController getController() {
        return controller;
    }

    public void open(boolean isThread, String title, String message, HForum hForum) {
        stage.show();
        stage.requestFocus();
    }
}
