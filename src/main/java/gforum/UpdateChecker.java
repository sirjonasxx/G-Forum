package gforum;

import gearth.Main;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;

public class UpdateChecker {

    public static String version = "1.0";
    private static String gitApi = "https://api.github.com/repos/sirjonasxx/G-Forum/releases/latest";

    static void check() {
        new Thread(() -> {
            try {
                String s = Jsoup.connect(gitApi).ignoreContentType(true).get().body().toString();
                s = s.substring(6, s.length() - 7);
                JSONObject object = new JSONObject(s);
                String gitv = (String)object.get("tag_name");
                if (!gitv.equals(version)) {
                    Platform.runLater(() -> {
                        String body = (String)object.get("body");
                        boolean isForcedUpdate = body.contains("(!)");

                        Alert alert = new Alert(isForcedUpdate ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION, "G-Forum is outdated!", ButtonType.OK);

                        FlowPane fp = new FlowPane();
                        Label lbl = new Label("A new version of G-Forum has been found ("+gitv+")" + System.lineSeparator()+ System.lineSeparator() + "Update to the latest version:");
                        Hyperlink link = new Hyperlink("https://github.com/sirjonasxx/G-Forum/releases");
                        fp.getChildren().addAll( lbl, link);
                        link.setOnAction(event -> {
                            Main.main.getHostServices().showDocument(link.getText());
                            event.consume();
                        });



                        WebView webView = new WebView();
                        webView.getEngine().loadContent("<html>A new version of G-Forum has been found ("+gitv+")<br><br>Update to the latest version:<br><a href=\"https://github.com/sirjonasxx/G-Forum/releases\">https://github.com/sirjonasxx/G-Forum/releases</a></html>");
                        webView.setPrefSize(500, 200);

                        alert.setResizable(false);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        alert.getDialogPane().setContent(fp);
                        if (isForcedUpdate) {
                            alert.setOnCloseRequest(event -> System.exit(0));
                        }
                        alert.show();

                    });
                }

            } catch (IOException e) {
//                e.printStackTrace();
            }
        }).start();
    }

}
