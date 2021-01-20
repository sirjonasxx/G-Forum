package gforum.add_entity;

import gforum.GForum;
import gforum.entities.HForum;
import gforum.webview.WebUtils;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;

import java.net.URL;
import java.util.ResourceBundle;

public class AddEntityController implements Initializable {


    private GForum gForum = null;
    private AddEntity addEntity = null;

    private volatile boolean initialized = false;

    public BorderPane borderPane;
    private WebView webView;

    private String subject;
    private String message;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView = new WebView();
        borderPane.setCenter(webView);

        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webView.getEngine().executeScript("window");
                window.setMember("app", this);


                Element add_btn = webView.getEngine().getDocument().getElementById("add_btn");
                ((EventTarget) add_btn).addEventListener("click", event -> {
                    if (!add_btn.getAttribute("class").contains("gdisabled")) {
                        addEntity.publish(subject, message);


                    }
                }, true);


                Element cancel_btn = webView.getEngine().getDocument().getElementById("cancel_btn");
                ((EventTarget) cancel_btn).addEventListener("click", event -> {
                    addEntity.getStage().hide();
                }, true);

                initialized = true;
            }
        });

        webView.getEngine().load(AddEntity.class.getResource("index.html").toString());
    }

    public void setContents(boolean isThread, String title, String message, HForum hForum) {
        Platform.runLater(() -> webView.getEngine().executeScript(String.format("setContents('%s','%s','%s','%s',%s,'%s','%s')",
                String.format(HForum.BADGE_URL, hForum.getGuildBadge()),
                WebUtils.escapeMessage(hForum.getGuildName()),
                WebUtils.escapeMessage(hForum.getGuildDescription()),
                isThread ? "Subject" : "Replying to",
                isThread ? "true" : "false",
                WebUtils.escapeMessage(title),
                message
        )));
    }

    public void subjectValueChange(String value) {
        subject = value;
    }

    public void messageValueChange(String value) {
        message = value;
    }


    public void setgForum(GForum gForum) {
        this.gForum = gForum;
    }

    public void setAddEntity(AddEntity addEntity) {
        this.addEntity = addEntity;
    }

    public WebView getWebView() {
        return webView;
    }
}
