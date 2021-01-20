package gforum.add_entity;

import gearth.Main;
import gearth.ui.GEarthController;
import gforum.GForum;
import gforum.entities.HForum;
import gforum.entities.HThread;
import gforum.webview.WebUtils;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class AddEntity {

    private AddEntityController controller;
    private Stage stage;
    private GForum gForum;

    private HForum currentForum = null;
    private HThread currentThread = null;

    private volatile long latestSuccess = -1;
    private volatile long latestAttempt = -1;

    public AddEntity(GForum gForum) throws Exception {
        this.gForum = gForum;
        stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("add_entity.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setgForum(gForum);
        controller.setAddEntity(this);

        stage.getIcons().add(new Image(GForum.class.getResourceAsStream("webview/images/logo.png")));

        stage.setTitle("Forum Message");
        stage.setScene(new Scene(root, 455, 415));
        stage.setMinWidth(410);
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

    public void open(String title, String message, HForum hForum, HThread hThread) {
        currentForum = hForum;
        currentThread = hThread;

        controller.setContents(hThread == null, title, message, hForum);

        stage.show();
        stage.requestFocus();
    }

    void publish(String title, String message) {
        message = message.replaceAll("\r\n", "\r").replaceAll("\n", "\r");

        if (!checkErrors(title, message)) {
         latestAttempt = System.currentTimeMillis();
         gForum.getHashSupport().sendToServer(
                 "PostForumMessage",
                 currentForum.getGuildId(),
                 currentThread == null ? 0 : currentThread.getThreadId(),
                 currentThread == null ? title : "",
                 message
         );

         updateAddBtnVisibility();
         new Thread(() -> {
             try { Thread.sleep(1200);
             } catch (InterruptedException ignored) {}
             updateAddBtnVisibility();
         }).start();
        }
    }


    private boolean checkErrors(String title, String message) {
        List<String> errors = new ArrayList<>();
        long passedTime = System.currentTimeMillis() - latestAttempt;
        if (passedTime < 31000) errors.add("* Wait " + (31 - (passedTime/1000)) + " more seconds");
        if (currentThread == null && title.length() < 2) errors.add("* Subject is too short");
        if (message.length() < 2) errors.add("* Message is too short");
        if (currentThread == null && title.length() > 50) errors.add("* Subject is too long");
        if (message.length() > 4096) errors.add("* Message is too long");

        if (errors.isEmpty()) return false;

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Group Message Error", ButtonType.OK);
            alert.setTitle("Group Message Error");

            FlowPane fp = new FlowPane();
            Label lbl = new Label("The following errors occurred: " +
                    System.lineSeparator() + System.lineSeparator() + String.join(System.lineSeparator(), errors));
            fp.getChildren().addAll(lbl);
//            link.setOnAction(event -> {
//                Main.main.getHostServices().showDocument(link.getText());
//                event.consume();
//            });

            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setContent(fp);
            alert.show();
        });

        return true;
    }

    private void updateAddBtnVisibility() {
        Platform.runLater(() -> {
            Element add_btn = getController().getWebView().getEngine().getDocument().getElementById("add_btn");
            WebUtils.removeClass(add_btn, "gdisabled");

            if (latestAttempt > System.currentTimeMillis() - 1000 /*||
                    latestSuccess > System.currentTimeMillis() - 30000*/) {
                WebUtils.addClass(add_btn, "gdisabled");
            }
        });
    }

    public void onSuccess() {
        latestSuccess = System.currentTimeMillis();
        new Thread(() -> {
            try { Thread.sleep(31000);
            } catch (InterruptedException ignored) {}
            updateAddBtnVisibility();
        }).start();

        Platform.runLater(() -> stage.hide());

    }
}
