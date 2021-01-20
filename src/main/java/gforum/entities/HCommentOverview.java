package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;
import gforum.webview.WebUtils;

import java.util.ArrayList;
import java.util.List;

public class HCommentOverview implements HOverview {

    private final long guildId;
    private final int threadId;
    private final int startIndex;
    private final List<HComment> comments;

    private GForum gForum;

    public HCommentOverview(HPacket packet) {
        guildId = packet.readLong();
        threadId = packet.readInteger();
        startIndex = packet.readInteger();

        comments = new ArrayList<>();
        int length = packet.readShort();
        for (int i = 0; i < length; i++) {
            comments.add(new HComment(packet));
        }
    }

    public HCommentOverview(long guildId, int threadId, int startIndex, List<HComment> comments, GForum gForum) {
        this.guildId = guildId;
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.comments = comments;
        this.gForum = gForum;
    }

    public long getGuildId() {
        return guildId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setgForum(GForum gForum) {
        this.gForum = gForum;
    }

    @Override
    public String addElementText() {
        return "Reply";
    }

    @Override
    public boolean addElementEnabled() {
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HForumStats hForumStats = gForum.getController().getCurrentForumStats();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == threadId).findFirst().get();

        return hForumStats.getErrorModerate().equals("") || (!hThread.isLocked() && hForumStats.getErrorPost().equals(""));
    }

    @Override
    public String returnText() {
        return "Back";
    }

    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getAmount() {
        return getComments().size();
    }

    @Override
    public List<HComment> getContentItems() {
        return getComments();
    }

    @Override
    public int getMaxAmount() {
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == threadId).findFirst().get();

        return hThread.getAmountComments();
    }

    @Override
    public void returnClick(GForum gForum) {
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HForumStats forumStats = gForum.getController().getCurrentForumStats();

        if (hThreadOverview.isInvalidated()) {
            forumStats.setUpdateReadMarker(-1);

            gForum.getHashSupport().sendToServer(
                    "UpdateForumReadMarkers",
                    (short)1,
                    forumStats.gethForum().getGuildId(),
                    getComments().get(getComments().size() - 1).getCommentId(),
                    false
            );
        }

        gForum.getThreadOverviewBuffer().request(
                hThreadOverview.isInvalidated(),
                hThreadOverview.getStartIndex(),
                hThreadOverview.getForumStats().gethForum().getGuildId()
        );
    }

    @Override
    public void addClick(GForum gForum) {
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == threadId).findFirst().get();
        HForum hForum = gForum.getController().getCurrentForumStats().gethForum();

        gForum.getAddEntity().open(hThread.getSubject(), "", hForum, hThread);
    }

    @Override
    public void request(GForum gForum, int start) {
        gForum.getCommentOverviewBuffer().request(false, start, guildId, threadId);
    }

    @Override
    public int internalRank() {
        return 2;
    }

    public List<HComment> getComments() {
        return comments;
    }
}
