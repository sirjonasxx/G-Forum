package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;
import gforum.webview.WebUtils;
import netscape.javascript.JSObject;

public class HForum implements ContentItem {

    public static final String BADGE_URL = "https://www.habbo.com/habbo-imaging/badge/%s.gif";

    private final long guildId;
    private final String guildName;
    private final String guildDescription;
    private final String guildBadge;

    private final int amountThreads;
    private final int rating;
    private final int amountComments;
    private final int unreadComments;

    private final int lastCommentIndexInForum;
    private final long lastCommentUserId;
    private final String lastCommentUserName;
    private final int lastCommentPassedTime;


    public HForum(HPacket hPacket) {
        guildId = hPacket.readLong();
        guildName = hPacket.readString();
        guildDescription = hPacket.readString();
        guildBadge = hPacket.readString();

        amountThreads = hPacket.readInteger();
        rating = hPacket.readInteger();
        amountComments = hPacket.readInteger();
        unreadComments = hPacket.readInteger();

        lastCommentIndexInForum = hPacket.readInteger();
        lastCommentUserId = hPacket.readLong();
        lastCommentUserName = hPacket.readString();
        lastCommentPassedTime = hPacket.readInteger();
    }

    public long getGuildId() {
        return guildId;
    }

    public String getGuildName() {
        return guildName;
    }

    public String getGuildDescription() {
        return guildDescription;
    }

    public String getGuildBadge() {
        return guildBadge;
    }

    public int getAmountThreads() {
        return amountThreads;
    }

    public int getRating() {
        return rating;
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

    public long getLastCommentUserId() {
        return lastCommentUserId;
    }

    public String getLastCommentUserName() {
        return lastCommentUserName;
    }

    public int getLastCommentPassedTime() {
        return lastCommentPassedTime;
    }



    private GForum gForum = null;
    public void onClick() {
        gForum.getController().requestOverview(1);
        gForum.getHashSupport().sendToServer("GetForumStats", guildId);
//        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_FORUMSTATS, guildId));
        gForum.getHashSupport().sendToServer("GetForumThreads", guildId, 0, GForum.PAGESIZE);
//        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_THREADOVERVIEW, guildId, 0, GForum.PAGESIZE));
    }

    @Override
    public void addHtml(int i, GForum gForum) {
        this.gForum = gForum;

        boolean bold = unreadComments > 0;
        String id = "forum" + i + "_" + System.currentTimeMillis();

        StringBuilder htmlBuilder = new StringBuilder()
                .append("<div class=\"overview_item ").append(i % 2 == 0 ? "item_lightblue" : "item_darkblue").append(" content_item\">")

                .append("<div class=\"overview_item_logo\">")
                .append("<img src=\"").append(String.format(BADGE_URL, guildBadge)).append("\" alt=\"\">")
                .append("</div>")

                .append("<div class=\"overview_item_info\">")
                .append("<div onclick=\"").append(id).append(".onClick()\" class=\"oii_name clickable\">").append(bold ? "<b>" : "").append(WebUtils.escapeMessage(guildName)).append(bold ? "</b>" : "").append("</div>")
                .append("<div class=\"oii_desc\">Score ").append(rating).append(", last message by ").append(WebUtils.escapeMessage(lastCommentUserName)).append(" ").append(WebUtils.elapsedTime(lastCommentPassedTime)).append(" ago</div>")
                .append("</div>")

                .append("<div onclick=\"").append(id).append(".onClick()\" class=\"overview_item_msgs clickable\">")
                .append("<div class=\"oim_msgs\">").append(bold ? "<b>" : "").append(amountComments).append(" messages</div>").append(bold ? "</b>" : "")
                .append("<div class=\"oim_unread\">").append(bold ? "<b>" : "").append(unreadComments).append(" unread</div>").append(bold ? "</b>" : "")
                .append("</div>")

                .append("</div>");

        String forum = htmlBuilder.toString();
        gForum.getController().getWebView().getEngine().executeScript(
                "document.getElementById('" + gForum.getController().getContentItemsContainer() + "').innerHTML += '" + forum + "';");

        JSObject window = (JSObject) gForum.getController().getWebView().getEngine().executeScript("window");
        window.setMember(id, this);
    }
}
