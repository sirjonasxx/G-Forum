package gforum.entities;

import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gforum.GForum;
import gforum.webview.WebUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import netscape.javascript.JSObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HComment implements ContentItem {

    public static final String OUTFIT_URL = "https://www.habbo.com/habbo-imaging/avatarimage?&figure=%s&direction=2&head_direction=2";

    private final int commentId;
    private final int indexInThread;
    private final long userId;
    private final String userName;
    private final String look;
    private final int passedTime;
    private final String message;
    private final HThreadState state;
    private final long adminId;
    private final String adminName;

    private final int irrelevantId;
    private final int authorPostCount;

    public HComment(HPacket hPacket) {
        commentId = hPacket.readInteger();
        indexInThread = hPacket.readInteger();
        userId = hPacket.readLong();
        userName = hPacket.readString(StandardCharsets.UTF_8);
        look = hPacket.readString();
        passedTime = hPacket.readInteger();
        message = hPacket.readString(StandardCharsets.UTF_8);
        state = HThreadState.fromValue(hPacket.readByte());

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

        irrelevantId = hPacket.readInteger();
        authorPostCount = hPacket.readInteger();
    }

    public int getCommentId() {
        return commentId;
    }

    public int getIndexInThread() {
        return indexInThread;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getLook() {
        return look;
    }

    public int getPassedTime() {
        return passedTime;
    }

    public String getMessage() {
        return message;
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

    public int getIrrelevantId() {
        return irrelevantId;
    }

    public int getAuthorPostCount() {
        return authorPostCount;
    }

    private GForum gForum = null;
    private String id = null;

    private String commentInHtml() {
        String comment = WebUtils.escapeMessage(message);
        List<String> lines = new ArrayList<>(Arrays.asList(comment.split("<br>")));

        boolean isquoting = false;
        boolean justEndedQuoting = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (!isquoting && line.startsWith("&gt;")) {
                isquoting = true;
                line = "<div class=\"cbc_quote\">" + line.substring(line.startsWith("&gt; ") ? 5 : 4);
            }
            else if (isquoting && line.startsWith("&gt;")) {
                line = line.substring(line.startsWith("&gt; ") ? 5 : 4);
            }
            else if (isquoting && !line.startsWith("&gt;")) {
                justEndedQuoting = true;
                isquoting = false;
                String prev = lines.get(i - 1);
                lines.set(i-1, prev.substring(0, prev.length() - 4) + "</div>");
            }

            if (justEndedQuoting && line.length() == 0) continue;
            justEndedQuoting = false;

            line = line.replaceAll("\\*([^*]*)\\*", "<b>$1</b>")
                    .replaceAll("_([^_<>]*)_", "<i>$1</i>")
                    .replaceAll("(^| |>)@([^ <>]*)($| |<)", "$1<u>$2</u>$3");

            lines.set(i, line + (i == lines.size() - 1 ? "" : "<br>"));
        }

        return String.join("", lines);
    }

    public void hideClick() {
        HForum forum = gForum.getController().getCurrentForumStats().gethForum();
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HCommentOverview currentCommentOverview = gForum.getController().getCurrentCommentOverview();
        int threadId = currentCommentOverview.getThreadId();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == threadId).findFirst().get();

        gForum.sendToServer(new HPacket("ModerateForumMessage", HMessage.Direction.TOSERVER,
                forum.getGuildId(),
                hThread.getThreadId(),
                commentId,
                state == HThreadState.HIDDEN_BY_ADMIN ? 1 : 10)
        );
    }

    public void report() {
        HCommentOverview currentCommentOverview = gForum.getController().getCurrentCommentOverview();
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == currentCommentOverview.getThreadId()).findFirst().get();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Report message");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Are you sure you want to report this message?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            gForum.sendToServer(new HPacket(
                    "ReportForumMessage", HMessage.Direction.TOSERVER,
                    gForum.getController().getCurrentForumStats().gethForum().getGuildId(),
                    hThread.getThreadId(),
                    commentId,
                    22,
                    "This forum message is against the rules") // xd
            );
        } else {
            // do nothing
        }
    }

    public void quote() {
        HCommentOverview currentCommentOverview = gForum.getController().getCurrentCommentOverview();
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == currentCommentOverview.getThreadId()).findFirst().get();
        HForum hForum = gForum.getController().getCurrentForumStats().gethForum();

        String newline = "&#13;&#10;";
        String preQuote = "&gt; ";

        StringBuilder quoteMessage = new StringBuilder();
        quoteMessage.append(WebUtils.elapsedTime(passedTime)).append(" ago ").append(userName).append(" wrote:").append(newline);

        boolean wasQuoted = false;
        for (String line : message.split("\r")) {
            if (line.equals(">") || line.startsWith("> ")) {
                if (!wasQuoted) {
                    wasQuoted = true;
                    quoteMessage.append(preQuote).append("[quote skipped]").append(newline);
                }
                continue;
            }
            wasQuoted = false;

            quoteMessage.append(preQuote).append(WebUtils.escapeMessage(line)).append(newline);
        }
        quoteMessage.append(newline);

        gForum.getAddEntity().open(hThread.getSubject(), quoteMessage.toString(), hForum, hThread);
    }

    @Override
    public void addHtml(int i, GForum gForum) {
        this.gForum = gForum;

        HCommentOverview currentCommentOverview = gForum.getController().getCurrentCommentOverview();
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        HForumStats hForumStats = gForum.getController().getCurrentForumStats();
        int threadId = currentCommentOverview.getThreadId();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == threadId).findFirst().get();
        if (commentId > hForumStats.gethForum().getLastCommentIndexInForum()) {
            hForumStats.gethForum().setLastCommentIndexInForum(commentId);
        }

        boolean staffLocked = state == HThreadState.HIDDEN_BY_STAFF;
        boolean hidden = state == HThreadState.HIDDEN_BY_ADMIN;
        boolean open = state == HThreadState.OPEN;

        boolean isUnread = indexInThread >= hThread.getAmountComments() - hThread.getUnreadComments();

        boolean canComment = !staffLocked && (hForumStats.getErrorModerate().equals("") || (open && !hThread.isLocked() && hForumStats.getErrorPost().equals("")));
        boolean canModerate = !staffLocked && (hForumStats.getErrorModerate().equals(""));


        id = "comment" + i + "_" + System.currentTimeMillis();

        StringBuilder htmlBuilder = new StringBuilder()
                .append("<div id=\"").append(id).append("\" class=\"comment_item content_item\">")

                .append("<div class=\"comment_header\">")
                .append("<div class=\"ch_timeago\">").append(WebUtils.elapsedTime(passedTime)).append(" ago</div>")
                .append("<div class=\"ch_index\">#").append(indexInThread + 1).append("</div>")
                .append("<div class=\"ch_buttons\">")
                .append("<img ").append(canModerate ? "onclick=\"" + id + ".hideClick()\" class=\"clickable\" " : "").append("src=\"images/topics/").append(!canModerate ? "placeholder" : (open ? "delete" : "undelete")).append(".png\" alt=\"\">")
                .append("<img onclick=\"").append(id).append(".report()\" class=\"clickable\" src=\"images/topics/report.png\" alt=\"\">")
                .append("<img ").append(canComment ? "onclick=\"" + id +".quote()\" class=\"clickable\" " : "").append("src=\"images/topics/").append(canComment ? "quote" : "placeholder").append(".png\" alt=\"\">")
                .append("</div>")
                .append("</div>")

                .append("<div class=\"comment_body ").append(staffLocked ? "comment_staffhidden" : (hidden ? "comment_hidden" : (isUnread ? "comment_unread" : "comment_open"))).append("\">")
                .append("<div class=\"cb_author\">")
                .append("<div class=\"cba_name\">").append(WebUtils.escapeMessage(userName)).append("</div>")
                .append("<div class=\"cba_messages\">").append(authorPostCount).append(" messages</div>")
                .append("<div class=\"cba_look\"><img src=\"").append(String.format(OUTFIT_URL, look)).append("\" alt=\"\"></div>")
                .append("</div>")
                .append("<div class=\"cb_content\">")
                .append((staffLocked || (hidden && !canModerate)) ? "Comment hidden by " + WebUtils.escapeMessage(adminName) : commentInHtml())
                .append("</div>")
                .append("</div>")

                .append("</div>")
                .append("<div class=\"comment_itemcontent_item\"></div>");

        String forum = htmlBuilder.toString();
        gForum.getController().getWebView().getEngine().executeScript(
                "document.getElementById('" + gForum.getController().getContentItemsContainer() + "').innerHTML += '" + forum + "';");

        JSObject window = (JSObject) gForum.getController().getWebView().getEngine().executeScript("window");
        window.setMember(id, this);
    }

    public String getId() {
        return id;
    }

    public boolean isUnread() {
        HCommentOverview currentCommentOverview = gForum.getController().getCurrentCommentOverview();
        HThreadOverview hThreadOverview = gForum.getController().getCurrentThreadOverview();
        int threadId = currentCommentOverview.getThreadId();
        HThread hThread = hThreadOverview.getThreads().stream().filter(hThread1 -> hThread1.getThreadId() == threadId).findFirst().get();

        return indexInThread >= hThread.getAmountComments() - hThread.getUnreadComments();
    }
}
