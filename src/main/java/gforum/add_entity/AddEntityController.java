package gforum.add_entity;

import gforum.GForum;
import javafx.concurrent.Worker;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.ResourceBundle;

public class AddEntityController implements Initializable {


    private GForum gForum = null;

    private volatile boolean initialized = false;

    public BorderPane borderPane;
    private WebView webView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView = new WebView();
        borderPane.setCenter(webView);

        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webView.getEngine().executeScript("window");
                window.setMember("app", gForum);

                initialized = true;
            }
        });

        webView.getEngine().load(AddEntity.class.getResource("index.html").toString());
    }

    public void setgForum(GForum gForum) {
        this.gForum = gForum;
    }
}
