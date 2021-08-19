package gforum;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionFormCreator;
import gearth.ui.GEarthController;
import gforum.add_entity.AddEntity;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GForumLauncher extends ExtensionFormCreator {
    @Override
    protected ExtensionForm createForm(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GForum.class.getResource("gforum.fxml"));
        Parent root = loader.load();

//        System.out.println(new File(GForum.class.getResource("gforum.fxml").toURI()).getPath());

        stage.getIcons().add(new Image(getClass().getResourceAsStream("webview/images/logo.png")));
        stage.setTitle("G-Forum");
        stage.setMinWidth(420);
        stage.setMinHeight(500);

        stage.setWidth(550);
        stage.setHeight(530);

        stage.setScene(new Scene(root));
        stage.getScene().getStylesheets().add(GEarthController.class.getResource("/gearth/ui/bootstrap3.css").toExternalForm());

        GForum gForum = new GForum();

        GForumController gForumController = loader.getController();
        gForum.setgForumController(gForumController);
        gForumController.setgForum(gForum);

        return gForum;
    }

    public static void main(String[] args) {
        runExtensionForm(args, GForumLauncher.class);
    }
}
