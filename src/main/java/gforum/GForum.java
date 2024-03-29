package gforum;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gforum.add_entity.AddEntity;
import gforum.entities.*;
import gforum.entities.overviewbuffer.CommentOverviewBuffer;
import gforum.entities.overviewbuffer.ForumOverviewBuffer;
import gforum.entities.overviewbuffer.ThreadOverviewBuffer;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.List;

@ExtensionInfo(
        Title =  "G-Forum",
        Description =  "Legacy forum browser",
        Version =  "1.0",
        Author =  "sirjonasxx"
)
public class GForum extends ExtensionForm {

    public static final int PAGESIZE = 20;

    public Button button;
    private GForumController gForumController;
    private Stage primaryStage = null;
    private AddEntity addEntity = null;

    public GForum() throws Exception {
        addEntity = new AddEntity(this);
    }

    private CommentOverviewBuffer commentOverviewBuffer = new CommentOverviewBuffer(this);
    private ThreadOverviewBuffer threadOverviewBuffer = new ThreadOverviewBuffer(this);
    private ForumOverviewBuffer forumOverviewBuffer = new ForumOverviewBuffer(this);

    private volatile long latestResponse = -1;
    private volatile boolean shown = false;

    @Override
    protected void initExtension() {
        intercept(HMessage.Direction.TOCLIENT, "ForumsList", this::onForumOverview);
        intercept(HMessage.Direction.TOCLIENT, "ForumThreads", this::onThreadOverview);
        intercept(HMessage.Direction.TOCLIENT, "ForumThreadMessages", this::onCommentOverview);
        intercept(HMessage.Direction.TOCLIENT, "ForumStats", this::onForumStats);

        intercept(HMessage.Direction.TOCLIENT, "ForumThread", this::onForumThread);
        intercept(HMessage.Direction.TOCLIENT, "ForumMessage", this::onForumMessage);
        intercept(HMessage.Direction.TOCLIENT, "PostForumThreadOk", this::postForumThreadOk);
        intercept(HMessage.Direction.TOCLIENT, "PostForumMessageOk", this::postForumMessageOk);


        intercept(HMessage.Direction.TOCLIENT, "Notification", this::onNotification);

        new Thread(() -> {
            while (true) {
                try { Thread.sleep(50000);
                } catch (InterruptedException e) { e.printStackTrace(); }

                long passedTime = System.currentTimeMillis() - latestResponse;
                if(shown && isConnectedToGame && passedTime > 35000 &&
                        gForumController.getCurrentOverview() != null &&
                        gForumController.getCurrentOverview() instanceof HForumOverview) {

                    HForumOverview current = gForumController.getCurrentForumOverview();
                    forumOverviewBuffer.request(true, current.getStartIndex(), current.getViewMode().getVal());
                }
            }
        }).start();
    }

    private void onNotification(HMessage hMessage) {
        if (hMessage.getPacket().readString().contains("forum")) {
            hMessage.setBlocked(true);
        }
    }


    // received after update(/moderation)
    private void onForumThread(HMessage hMessage) {
        hMessage.setBlocked(true);

        HPacket hPacket = hMessage.getPacket();
        long guild = hPacket.readLong();
        HThread hThread = new HThread(hPacket);
        if (getController().getCurrentForumStats() != null &&
                getController().getCurrentThreadOverview() != null &&
                getController().getCurrentForumStats().gethForum().getGuildId() == guild &&
                getController().getCurrentOverview() instanceof HThreadOverview) {
            HThreadOverview threadOverview = getController().getCurrentThreadOverview();
            List<HThread> threads = threadOverview.getThreads();
            for (int i = 0; i < threads.size(); i++) {
                HThread maybeThread = threads.get(i);
                if (maybeThread.getThreadId() == hThread.getThreadId()) {
                    threads.set(i, hThread);
                    Platform.runLater(() -> gForumController.setOverview(threadOverview, false));
                    break;
                }
            }
        }
    }

    // received after update(/moderation)
    private void onForumMessage(HMessage hMessage) {
        hMessage.setBlocked(true);

        HPacket hPacket = hMessage.getPacket();
        long guild = hPacket.readLong();
        int threadId = hPacket.readInteger();
        HComment hComment = new HComment(hPacket);

        if (getController().getCurrentCommentOverview() != null &&
                getController().getCurrentCommentOverview().getGuildId() == guild &&
                getController().getCurrentCommentOverview().getThreadId() == threadId &&
                getController().getCurrentOverview() instanceof HCommentOverview) {
            HCommentOverview commentOverview = getController().getCurrentCommentOverview();
            List<HComment> comments = commentOverview.getComments();
            for (int i = 0; i < comments.size(); i++) {
                HComment maybeComment = comments.get(i);
                if (maybeComment.getCommentId() == hComment.getCommentId()) {
                    comments.set(i, hComment);
                    Platform.runLater(() -> gForumController.setOverview(commentOverview, false));
                    break;
                }
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
        shown = true;
        if (doOnce && isConnectedToGame) {
            UpdateChecker.check();
            doOnce = false;
            forumOverviewBuffer.request(true, 0, HForumOverviewType.MY_FORUMS.getVal());
        }
        else if (isConnectedToGame) {
            long passedTime = System.currentTimeMillis() - latestResponse;
            if(passedTime > 6000 &&
                    gForumController.getCurrentOverview() != null &&
                    gForumController.getCurrentOverview() instanceof HForumOverview) {

                HForumOverview current = gForumController.getCurrentForumOverview();
                forumOverviewBuffer.request(true, current.getStartIndex(), current.getViewMode().getVal());
            }
        }
    }

    @Override
    protected void onHide() {
        shown = false;
        getController().maybeUpdateRemoteCommentReadMarker();
    }

    private void onForumStats(HMessage hMessage) {
        hMessage.setBlocked(true);

        HForumStats forumStats = new HForumStats(hMessage.getPacket());
        gForumController.setForumStats(forumStats);
    }

    private void onCommentOverview(HMessage hMessage) {
        latestResponse = System.currentTimeMillis();
        hMessage.setBlocked(true);

        HCommentOverview commentOverview = new HCommentOverview(hMessage.getPacket());
        commentOverview.setgForum(this);
        commentOverviewBuffer.refill(commentOverview);
    }

    private void onThreadOverview(HMessage hMessage) {
        latestResponse = System.currentTimeMillis();
        hMessage.setBlocked(true);

        if (gForumController.getCurrentForumStats() == null) return;
        HThreadOverview threadOverview = new HThreadOverview(hMessage.getPacket());
        threadOverview.setForumStats(gForumController.getCurrentForumStats());
        threadOverviewBuffer.refill(threadOverview);
    }

    private void onForumOverview(HMessage hMessage) {
        latestResponse = System.currentTimeMillis();
        hMessage.setBlocked(true);

        HForumOverview forumOverview = new HForumOverview(hMessage.getPacket());
        forumOverviewBuffer.refill(forumOverview);
    }

    private void postForumMessageOk(HMessage hMessage) {
        hMessage.setBlocked(true);

        HPacket hPacket = hMessage.getPacket();
        long guildId = hPacket.readLong();
        int threadId = hPacket.readInteger();
        HComment hComment = new HComment(hPacket);

        postOk(guildId, threadId, hComment.getIndexInThread());
    }

    private void postForumThreadOk(HMessage hMessage) {
        hMessage.setBlocked(true);

        HPacket hPacket = hMessage.getPacket();
        long guildId = hPacket.readLong();
        HThread hThread = new HThread(hPacket);

        HThreadOverview threadOverview = gForumController.getCurrentThreadOverview();
        if (threadOverview != null && threadOverview.getGuildId() == guildId) { // add to context, but won't be used anymore
            threadOverview.getThreads().add(hThread);
        }

        postOk(guildId, hThread.getThreadId(), 0);
    }

    private void postOk(long guildId, int threadId, int index) {
        addEntity.onSuccess();
        HThreadOverview threadOverview = gForumController.getCurrentThreadOverview();
        HForumOverview forumOverview = gForumController.getCurrentForumOverview();
        HOverview currentOverview = gForumController.getCurrentOverview();

        if (threadOverview != null && threadOverview.getGuildId() == guildId) {
            getController().maybeUpdateRemoteCommentReadMarker();

            int startPage = index / GForum.PAGESIZE;

            threadOverview.setInvalidated(true);
            commentOverviewBuffer.request(
                    true,
                    startPage * GForum.PAGESIZE,
                    guildId,
                    threadId
            );
        }
        else if(threadOverview == null && forumOverview != null && currentOverview == forumOverview) {
            forumOverviewBuffer.request(true, forumOverview.getStartIndex(), forumOverview.getViewMode().getVal());
        }

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

    public AddEntity getAddEntity() {
        return addEntity;
    }

    public boolean isConnectedToGame() {
        return isConnectedToGame;
    }

    public void setgForumController(GForumController gForumController) {
        this.gForumController = gForumController;
    }
}
