package gforum.entities;

import gearth.protocol.HPacket;
import gforum.Constants;
import gforum.GForum;
import gforum.webview.WebUtils;
import netscape.javascript.JSObject;

public class HForum implements ContentItem {

    private static final String BADGE_URL = "https://www.habbo.com/habbo-imaging/badge/%s.gif";

    private final int guildId;
    private final String guildName;
    private final String guildDescription;
    private final String guildBadge;

    private final int unknown1;
    private final int rating;
    private final int amountComments;
    private final int unreadComments;

    private final int lastCommentIndexInForum;
    private final int lastCommentUserId;
    private final String lastCommentUserName;
    private final int lastCommentPassedTime;


    public HForum(HPacket hPacket) {
        guildId = hPacket.readInteger();
        guildName = hPacket.readString();
        guildDescription = hPacket.readString();
        guildBadge = hPacket.readString();

        unknown1 = hPacket.readInteger();
        rating = hPacket.readInteger();
        amountComments = hPacket.readInteger();
        unreadComments = hPacket.readInteger();

        lastCommentIndexInForum = hPacket.readInteger();
        lastCommentUserId = hPacket.readInteger();
        lastCommentUserName = hPacket.readString();
        lastCommentPassedTime = hPacket.readInteger();
    }

    public int getGuildId() {
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

    public int getUnknown1() {
        return unknown1;
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

    public int getLastCommentUserId() {
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
        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_FORUMSTATS, guildId));
        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_THREADOVERVIEW, guildId, 0, GForum.PAGESIZE));
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
                .append("<div onclick=\"").append(id).append(".onClick()\" class=\"oii_name clickable\">").append(bold ? "<b>" : "").append(guildName).append(bold ? "</b>" : "").append("</div>")
                .append("<div class=\"oii_desc\">Score ").append(rating).append(", last message by ").append(lastCommentUserName).append(" ").append(WebUtils.elapsedTime(lastCommentPassedTime)).append(" ago</div>")
                .append("</div>")

                .append("<div onclick=\"").append(id).append(".onClick()\" class=\"overview_item_msgs clickable\">")
                .append("<div class=\"oim_msgs\">").append(bold ? "<b>" : "").append(amountComments).append(" messages</div>").append(bold ? "</b>" : "")
                .append("<div class=\"oim_unread\">").append(bold ? "<b>" : "").append(unreadComments).append(" unread</div>").append(bold ? "</b>" : "")
                .append("</div>")

                .append("</div>");

        String forum = htmlBuilder.toString();
        gForum.getController().getWebView().getEngine().executeScript(
                "document.getElementById('" + gForum.getController().getContentItemsContainer() + "').innerHTML += '" + WebUtils.escapeMessage(forum) + "';");

        JSObject window = (JSObject) gForum.getController().getWebView().getEngine().executeScript("window");
        window.setMember(id, this);
    }
}
