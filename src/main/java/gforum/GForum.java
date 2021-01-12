package gforum;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.extra.harble.HashSupport;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.ui.GEarthController;
import gforum.entities.*;
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

    @Override
    protected void initExtension() {
        hashSupport = new HashSupport(this);
//        hashSupport.intercept(HMessage.Direction.TOCLIENT, "GuildForumList", this::onForumOverview);
//        hashSupport.intercept(HMessage.Direction.TOCLIENT, "GuildForumThreads", this::onThreadOverview);
//        hashSupport.intercept(HMessage.Direction.TOCLIENT, "GuildForumComments", this::onCommentOverview);
//        hashSupport.intercept(HMessage.Direction.TOCLIENT, "GuildForumData", this::onForumStats);
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
                gForumController.setOverview(threadOverview, false);
            }
        }
    }


    private volatile boolean doOnce = true;
    @Override
    protected void onStartConnection() {
        if (doOnce) {
            doOnce = false;
            gForumController.requestOverview(0);
            HForumOverview.requestFirst(this, HForumOverviewType.MY_FORUMS, GForum.PAGESIZE);
        }
    }

    private void onForumStats(HMessage hMessage) {
        HForumStats forumStats = new HForumStats(hMessage.getPacket());
        gForumController.setForumStats(forumStats);
    }

    private void onCommentOverview(HMessage hMessage) {
        HCommentOverview commentOverview = new HCommentOverview(hMessage.getPacket());
        commentOverview.setgForum(this);
    }

    private void onThreadOverview(HMessage hMessage) {
        HThreadOverview threadOverview = new HThreadOverview(hMessage.getPacket());
        gForumController.setThreadOverview(threadOverview);
    }

    private void onForumOverview(HMessage hMessage) {
        HForumOverview forumOverview = new HForumOverview(hMessage.getPacket());
        gForumController.setForumOverview(forumOverview);
    }

    public HashSupport getHashSupport() {
        return hashSupport;
    }

    public GForumController getController() {
        return gForumController;
    }
}
