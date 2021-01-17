package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;
import gforum.webview.WebUtils;
import netscape.javascript.JSObject;

import java.nio.charset.StandardCharsets;

public class HThread implements ContentItem {

    private final int threadId;

    private final long authorId;
    private final String authorName;
    private final String subject;

    private final boolean pinned;
    private final boolean locked;

    private final int passedTime;
    private final int amountComments;
    private final int unreadComments;

    private final int lastCommentIndexInForum;
    private final long lastCommentAuthorId;
    private final String lastCommentAuthorName;
    private final int lastCommentPassedTime;

    private final HThreadState state;
    private final long adminId;
    private final String adminName;

    private final int unknownThreadId;

    public HThread(HPacket hPacket) {
        threadId = hPacket.readInteger();

        authorId = hPacket.readLong();
        authorName = hPacket.readString(StandardCharsets.UTF_8);
        subject = hPacket.readString(StandardCharsets.UTF_8);

        pinned = hPacket.readBoolean();
        locked = hPacket.readBoolean();

        passedTime = hPacket.readInteger();
        amountComments = hPacket.readInteger();
        unreadComments = hPacket.readInteger();

        lastCommentIndexInForum = hPacket.readInteger();
        lastCommentAuthorId = hPacket.readLong();
        lastCommentAuthorName = hPacket.readString(StandardCharsets.UTF_8);
        lastCommentPassedTime = hPacket.readInteger();

        byte b = hPacket.readByte();
        state = HThreadState.fromValue(b);

        // sulake did an oopsie here
        if (hPacket.readInteger(hPacket.getReadIndex()) == 0
                && hPacket.readUshort(hPacket.getReadIndex() + 4) == 7
                && hPacket.getReadIndex() + 4 + 2 + 7 <= hPacket.getBytesLength()
                && hPacket.readString(hPacket.getReadIndex() + 4).equals("unknown")) {
            adminId = 0;
            adminName = "unknown";
            hPacket.setReadIndex(hPacket.getReadIndex() + 4 + 2 + 7);
        }
        else {
            adminId = hPacket.readLong();
            adminName = hPacket.readString(StandardCharsets.UTF_8);
        }

        unknownThreadId = hPacket.readInteger();
    }

    public int getThreadId() {
        return threadId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getPassedTime() {
        return passedTime;
    }

    public int getAmountComments() {
        return amountComments;
    }

    public int getUnreadComments() {
        return unreadComments;
    }

    public int getLastCommentIndexInForum() {
        return lastCommentIndexInForum;
    }

    public long getLastCommentAuthorId() {
        return lastCommentAuthorId;
    }

    public String getLastCommentAuthorName() {
        return lastCommentAuthorName;
    }

    public int getLastCommentPassedTime() {
        return lastCommentPassedTime;
    }

    public HThreadState getState() {
        return state;
    }

    public long getAdminId() {
        return adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public int getUnknownThreadId() {
        return unknownThreadId;
    }





    private GForum gForum = null;
    private boolean hasPermissions() {
        HForumStats hForumStats = gForum.getController().getCurrentForumStats();
        boolean canModerate = hForumStats.getErrorModerate().equals("");
        boolean staffHidden = state == HThreadState.HIDDEN_BY_STAFF;

        return (canModerate && !staffHidden);
    }

    public void stickyClick() {
        if (hasPermissions()) {
            HForum forum = gForum.getController().getCurrentForumStats().gethForum();
            gForum.getHashSupport().sendToServer("UpdateForumThread",
                    forum.getGuildId(),
                    threadId,
                    !isPinned(),
                    isLocked()
            );
        }
    }

    public void lockClick() {
        if (hasPermissions()) {
            HForum forum = gForum.getController().getCurrentForumStats().gethForum();
            gForum.getHashSupport().sendToServer("UpdateForumThread",
                    forum.getGuildId(),
                    threadId,
                    isPinned(),
                    !isLocked()
            );
        }
    }

    public void hideClick() {
        if (hasPermissions()) {
            HForum forum = gForum.getController().getCurrentForumStats().gethForum();
            gForum.getHashSupport().sendToServer("ModerateForumThread",
                    forum.getGuildId(),
                    threadId,
                    state == HThreadState.HIDDEN_BY_ADMIN ? 1 : 10
            );
        }
    }

    public void report() {
        System.out.println("report");
        // TODO
    }

    public void onClick() {
        int startComment = amountComments - unreadComments;
        if (unreadComments == 0) startComment -= 1;
        int startPage = startComment / GForum.PAGESIZE;

        gForum.getCommentOverviewBuffer().request(
                true,
                startPage * GForum.PAGESIZE,
                gForum.getController().getCurrentForumStats().gethForum().getGuildId(),
                threadId
        );
    }

    @Override
    public void addHtml(int i, GForum gForum) {
        this.gForum = gForum;

        boolean bold = unreadComments > 0;
        String id = "thread" + i + "_" + System.currentTimeMillis();

        HForumStats hForumStats = gForum.getController().getCurrentForumStats();
        boolean canModerate = hForumStats.getErrorModerate().equals("");
        boolean staffHidden = state == HThreadState.HIDDEN_BY_STAFF;
        boolean adminHidden = state == HThreadState.HIDDEN_BY_ADMIN;
        boolean access = ((canModerate && adminHidden) || state == HThreadState.OPEN);

        StringBuilder htmlBuilder = new StringBuilder()
                .append("<div class=\"thread_item ").append(staffHidden ? "item_red" : (adminHidden ? "item_grey" : (i % 2 == 0 ? "item_lightblue" : "item_darkblue"))).append(" content_item\">")

                .append("<div class=\"thread_settings\">")
                .append("<img ").append(canModerate && !staffHidden ? ("onclick=\"" + id +".lockClick()\" class=\"clickable\" ") : "").append("src=\"").append(locked && !staffHidden ? "images/threads/lock_closed.png" : (canModerate && !staffHidden ? "images/threads/lock_open.png" : "images/threads/placeholder.png")).append("\" alt=\"\">")
                .append("<img ").append(canModerate && !staffHidden ? ("onclick=\"" + id +".stickyClick()\" class=\"clickable\" ") : "").append("src=\"").append(pinned && !staffHidden ? "images/threads/sticky.png" : (canModerate && !staffHidden ? "images/threads/unsticky.png" : "images/threads/placeholder.png")).append("\" alt=\"\">")
                .append("</div>")

                .append("<div class=\"thread_info\">");

        if ((adminHidden && !canModerate) || staffHidden) {
            htmlBuilder.append("<div class=\"oii_name\">").append("Thread hidden by ").append(/*adminHidden ?*/ WebUtils.escapeMessage(adminName) /*: "Habbo Staff"*/).append("</div>");
        }
        else {
            htmlBuilder.append("<div onclick=\"").append(id).append(".onClick()\" class=\"oii_name clickable\">").append(bold ? "<b>" : "").append(WebUtils.escapeMessage(subject)).append(bold ? "</b>" : "").append("</div>");
        }
        htmlBuilder.append("<div class=\"oii_desc\">By ").append(WebUtils.escapeMessage(authorName)).append(" ").append(WebUtils.elapsedTime(passedTime)).append(" ago, last message by ").append(WebUtils.escapeMessage(lastCommentAuthorName)).append(" ").append(WebUtils.elapsedTime(lastCommentPassedTime)).append(" ago").append("</div>")
                .append("</div>")

                .append("<div onclick=\"").append(id).append(".onClick()\" class=\"thread_msgs clickable\">")
                .append("<div class=\"oim_msgs\">").append(bold && access ? "<b>" : "").append(amountComments).append(" messages</div>").append(bold && access ? "</b>" : "")
                .append("<div class=\"oim_unread\">").append(bold && access ? "<b>" : "").append(unreadComments).append(" unread</div>").append(bold && access ? "</b>" : "")
                .append("</div>");

        htmlBuilder.append("<div class=\"thread_delete_report\">")
                .append("<img ").append(canModerate && !staffHidden ? ("onclick=\"" + id +".hideClick()\" class=\"clickable\" ") : "").append("src=\"").append(canModerate && !staffHidden ? (adminHidden ? "images/threads/unhide.png": "images/threads/hide.png") : "images/threads/placeholder2.png").append("\" alt=\"\">")
                .append("<img onclick=\"").append(id).append(".report()\" class=\"clickable\" src=\"images/threads/report.png\" alt=\"\">")
                .append("</div>")

                .append("</div>");


        String forum = htmlBuilder.toString();
        gForum.getController().getWebView().getEngine().executeScript(
                "document.getElementById('" + gForum.getController().getContentItemsContainer() + "').innerHTML += '" + forum + "';");

        JSObject window = (JSObject) gForum.getController().getWebView().getEngine().executeScript("window");
        window.setMember(id, this);
    }

}
