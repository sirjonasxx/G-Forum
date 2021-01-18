package gforum;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.extra.harble.HashSupport;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.ui.GEarthController;
import gforum.entities.*;
import gforum.entities.overviewbuffer.CommentOverviewBuffer;
import gforum.entities.overviewbuffer.ForumOverviewBuffer;
import gforum.entities.overviewbuffer.ThreadOverviewBuffer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.List;

@ExtensionInfo(
        Title =  "G-Forum",
        Description =  "",
        Version =  "0.1",
        Author =  "sirjonasxx"
)
public class GForum extends ExtensionForm {

    public static final int PAGESIZE = 20;

    public Button button;
    private GForumController gForumController;
    private HashSupport hashSupport = null;
    private Stage primaryStage = null;

    public static void main(String[] args) {
        runExtensionForm(args, GForum.class);
    }

    //initialize javaFX elements
    public void initialize() {
        button.setText("Click me!");
    }

    @Override
    public ExtensionForm launchForm(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(GForum.class.getResource("gforum.fxml"));
        Parent root = loader.load();
        this.primaryStage = primaryStage;

//        System.out.println(new File(GForum.class.getResource("gforum.fxml").toURI()).getPath());

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("webview/images/logo.png")));
        primaryStage.setTitle("G-Forum");
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(500);

        primaryStage.setWidth(550);
        primaryStage.setHeight(530);

        primaryStage.setScene(new Scene(root));
        primaryStage.getScene().getStylesheets().add(GEarthController.class.getResource("/gearth/ui/bootstrap3.css").toExternalForm());

        gForumController = loader.getController();
        gForumController.setgForum(this);
        return this;
    }


    private CommentOverviewBuffer commentOverviewBuffer = new CommentOverviewBuffer(this);
    private ThreadOverviewBuffer threadOverviewBuffer = new ThreadOverviewBuffer(this);
    private ForumOverviewBuffer forumOverviewBuffer = new ForumOverviewBuffer(this);

    @Override
    protected void initExtension() {
        hashSupport = new HashSupport(this);
        hashSupport.intercept(HMessage.Direction.TOCLIENT, "ForumsList", this::onForumOverview);
        hashSupport.intercept(HMessage.Direction.TOCLIENT, "ForumThreads", this::onThreadOverview);
        hashSupport.intercept(HMessage.Direction.TOCLIENT, "ForumThreadMessages", this::onCommentOverview);
        hashSupport.intercept(HMessage.Direction.TOCLIENT, "ForumStats", this::onForumStats);
        hashSupport.intercept(HMessage.Direction.TOCLIENT, "ForumThread", this::onForumThread);
        hashSupport.intercept(HMessage.Direction.TOCLIENT, "Notification", this::onNotification);
    }

    private void onNotification(HMessage hMessage) {
        if (hMessage.getPacket().readString().contains("forum")) {
            hMessage.setBlocked(true);
        }
    }


    // received after update
    private void onForumThread(HMessage hMessage) {
        HPacket hPacket = hMessage.getPacket();
        long guild = hPacket.readLong();
        HThread hThread = new HThread(hPacket);
        if (getController().getCurrentForumStats().gethForum().getGuildId() == guild &&
        getController().getCurrentOverview() instanceof HThreadOverview) {
            HThreadOverview threadOverview = (HThreadOverview) getController().getCurrentOverview();
            List<HThread> threads = threadOverview.getThreads();
            boolean succes = false;
            for (int i = 0; i < threads.size(); i++) {
                HThread maybeThread = threads.get(i);
                if (maybeThread.getThreadId() == hThread.getThreadId()) {
                    threads.set(i, hThread);
                    succes = true;
                    break;
                }
            }
            if (succes) {
                Platform.runLater(() -> gForumController.setOverview(threadOverview, false));
            }
        }
    }


    private volatile boolean doOnce = true;

    private boolean isConnectedToGame = false;
    @Override
    protected void onStartConnection() {
        isConnectedToGame = true;
    }

    @Override
    protected void onEndConnection() {
        isConnectedToGame = false;
    }

    @Override
    protected void onShow() {
        if (doOnce && isConnectedToGame) {
            doOnce = false;
            forumOverviewBuffer.request(true, 0, HForumOverviewType.MY_FORUMS.getVal());
        }
    }

    private void onForumStats(HMessage hMessage) {
        HForumStats forumStats = new HForumStats(hMessage.getPacket());
        gForumController.setForumStats(forumStats);
    }

    private void onCommentOverview(HMessage hMessage) {
        HCommentOverview commentOverview = new HCommentOverview(hMessage.getPacket());
        commentOverview.setgForum(this);
        commentOverviewBuffer.refill(commentOverview);
    }

    private void onThreadOverview(HMessage hMessage) {
        if (gForumController.getCurrentForumStats() == null) return;
        HThreadOverview threadOverview = new HThreadOverview(hMessage.getPacket());
        threadOverview.setForumStats(gForumController.getCurrentForumStats());
        threadOverviewBuffer.refill(threadOverview);
    }

    private void onForumOverview(HMessage hMessage) {
        HForumOverview forumOverview = new HForumOverview(hMessage.getPacket());
        forumOverviewBuffer.refill(forumOverview);
    }

    public HashSupport getHashSupport() {
        return hashSupport;
    }

    public GForumController getController() {
        return gForumController;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public CommentOverviewBuffer getCommentOverviewBuffer() {
        return commentOverviewBuffer;
    }

    public ThreadOverviewBuffer getThreadOverviewBuffer() {
        return threadOverviewBuffer;
    }

    public ForumOverviewBuffer getForumOverviewBuffer() {
        return forumOverviewBuffer;
    }
}
