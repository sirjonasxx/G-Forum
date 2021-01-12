package gforum;

import gforum.entities.*;
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

public class GForumController implements Initializable {

    private GForum gForum = null;

    private volatile boolean initialized = false;

    public BorderPane borderPane;
    private WebView webView;

    private final String contentItemsContainer = "content_items_container";


    private HForumOverview currentForumOverview = null;
    private HThreadOverview currentThreadOverview = null;
    private HCommentOverview currentCommentOverview = null;

    private HForumStats currentForumStats = null;
    private HOverview currentOverview = null;
    private volatile int requestingOverview = -1;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webView = new WebView();
        borderPane.setCenter(webView);

        webView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webView.getEngine().executeScript("window");
                window.setMember("app", gForum);

                Element my_forums_click = webView.getEngine().getDocument().getElementById("overview_my_forums");
                Element most_active_click = webView.getEngine().getDocument().getElementById("overview_most_active");
                Element most_viewed_click = webView.getEngine().getDocument().getElementById("overview_most_viewed");

                ((EventTarget) my_forums_click).addEventListener("click", event -> {
                    requestOverview(0);
                    HForumOverview.requestFirst(gForum, HForumOverviewType.MY_FORUMS, GForum.PAGESIZE);
                }, true);

                ((EventTarget) most_active_click).addEventListener("click", event -> {
                    requestOverview(0);
                    HForumOverview.requestFirst(gForum, HForumOverviewType.MOST_ACTIVE, GForum.PAGESIZE);
                }, true);

                ((EventTarget) most_viewed_click).addEventListener("click", event -> {
                    requestOverview(0);
                    HForumOverview.requestFirst(gForum, HForumOverviewType.MOST_VIEWED, GForum.PAGESIZE);
                }, true);

                Element first_btn = webView.getEngine().getDocument().getElementById("first_btn");
                Element prev_btn = webView.getEngine().getDocument().getElementById("prev_btn");
                Element next_btn = webView.getEngine().getDocument().getElementById("next_btn");
                Element last_btn = webView.getEngine().getDocument().getElementById("last_btn");

                ((EventTarget) first_btn).addEventListener("click", event -> {
                    if (first_btn.getAttribute("class").contains("gdisabled")) return;
                    requestOverview(currentOverview.internalRank());
                    currentOverview.request(gForum, 0, GForum.PAGESIZE);
                }, true);
                ((EventTarget) prev_btn).addEventListener("click", event -> {
                    if (prev_btn.getAttribute("class").contains("gdisabled")) return;
                    requestOverview(currentOverview.internalRank());
                    currentOverview.request(gForum, currentOverview.getStartIndex() - currentOverview.getAmount(), GForum.PAGESIZE);
                }, true);
                ((EventTarget) next_btn).addEventListener("click", event -> {
                    if (next_btn.getAttribute("class").contains("gdisabled")) return;
                    requestOverview(currentOverview.internalRank());
                    currentOverview.request(gForum, currentOverview.getStartIndex() + currentOverview.getAmount(), GForum.PAGESIZE);
                }, true);
                ((EventTarget) last_btn).addEventListener("click", event -> {
                    if (last_btn.getAttribute("class").contains("gdisabled")) return;
                    requestOverview(currentOverview.internalRank());
                    currentOverview.request(gForum, currentOverview.getMaxAmount() - currentOverview.getMaxAmount() % GForum.PAGESIZE, GForum.PAGESIZE);
                }, true);


                initialized = true;
                if (currentOverview != null && currentOverview == currentForumOverview) {
                    Platform.runLater(() -> {
                        webView.getEngine().executeScript(String.format("setOverview(\"%s\")", currentForumOverview.getViewMode().toString().toLowerCase()));
                        setOverview(currentOverview);
                    });
                }
            }
        });

        webView.getEngine().load(GForum.class.getResource("webview/index.html").toString());
    }


    public void setgForum(GForum gForum) {
        this.gForum = gForum;
    }


    private void setOverview(HOverview overview) {
        Platform.runLater(() -> {
            Element content_items_container = webView.getEngine().getDocument().getElementById(contentItemsContainer);
            WebUtils.clearElement(content_items_container);
            for (int i = 0; i < overview.contentItems().size(); i++) {
                ContentItem contentItem = overview.contentItems().get(i);
                contentItem.addHtml(i, gForum);
            }
            webView.getEngine().executeScript("document.getElementById('" + contentItemsContainer + "').scrollTop = 0");

            Element first_btn = webView.getEngine().getDocument().getElementById("first_btn");
            Element prev_btn = webView.getEngine().getDocument().getElementById("prev_btn");
            Element next_btn = webView.getEngine().getDocument().getElementById("next_btn");
            Element last_btn = webView.getEngine().getDocument().getElementById("last_btn");
            WebUtils.removeClass(first_btn, "gdisabled");
            WebUtils.removeClass(prev_btn, "gdisabled");
            WebUtils.removeClass(next_btn, "gdisabled");
            WebUtils.removeClass(last_btn, "gdisabled");

            boolean isLast = overview.getMaxAmount() <= overview.getAmount() + overview.getStartIndex();
            boolean isFirst = overview.getStartIndex() < overview.getAmount();
            if (isLast) {
                WebUtils.addClass(next_btn, "gdisabled");
                WebUtils.addClass(last_btn, "gdisabled");
            }
            if (isFirst) {
                WebUtils.addClass(first_btn, "gdisabled");
                WebUtils.addClass(prev_btn, "gdisabled");
            }
            int thispage = Math.max(1, 1 + (overview.getStartIndex() / GForum.PAGESIZE));
            int lastpage = Math.max(1, 1 + ((overview.getMaxAmount() - 1) / GForum.PAGESIZE));
            webView.getEngine().executeScript("document.getElementById('paging_lbl').innerHTML = '" + thispage + " / " + lastpage + "';");


            Element return_or_mark_read_btn = webView.getEngine().getDocument().getElementById("return_or_mark_read_btn");
            Element add_btn = webView.getEngine().getDocument().getElementById("add_btn");
            WebUtils.removeClass((Element) return_or_mark_read_btn.getParentNode(), "invisible");
            WebUtils.removeClass((Element) add_btn.getParentNode(), "invisible");

            if (overview.returnText() == null) WebUtils.addClass((Element) return_or_mark_read_btn.getParentNode(), "invisible");
            else webView.getEngine().executeScript("document.getElementById('return_or_mark_read_btn').innerHTML = '" + overview.returnText() + "';");
            if (overview.addElementText() == null) WebUtils.addClass((Element) add_btn.getParentNode(), "invisible");
            else webView.getEngine().executeScript("document.getElementById('add_btn').innerHTML = '" + overview.addElementText() + "';");


        });
    }

    public void setForumOverview(HForumOverview forumOverview) {
        if (requestingOverview != 0) {
            return;
        }
        clearRequest();
        currentThreadOverview = null;
        currentCommentOverview = null;
        currentForumOverview = forumOverview;
        currentOverview = forumOverview;

        if (initialized) {
            Platform.runLater(() -> {
                webView.getEngine().executeScript(String.format("setOverview(\"%s\")", forumOverview.getViewMode().toString().toLowerCase()));
                setOverview(forumOverview);
            });
        }
    }

    public void setForumStats(HForumStats forumStats) {
        currentForumStats = forumStats;
    }

    public void setThreadOverview(HThreadOverview threadOverview) {
        if (requestingOverview != 1 || currentForumStats == null) {
            return;
        }
        threadOverview.setForumStats(currentForumStats);
        clearRequest();
        currentCommentOverview = null;
        currentThreadOverview = threadOverview;
        currentOverview = threadOverview;

    }


    public void clearRequest() {
        requestingOverview = -1;
    }

    public void requestOverview(int rank) {
        requestingOverview = rank;
    }





    public WebView getWebView() {
        return webView;
    }

    public String getContentItemsContainer() {
        return contentItemsContainer;
    }
}
