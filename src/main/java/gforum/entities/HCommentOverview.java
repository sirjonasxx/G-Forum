package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;

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
        return gForum.getController().getCurrentForumStats().getErrorPost().equals("");
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
    public ContentItem getContentItem(int i) {
        return getComments().get(i);
    }

    @Override
    public int getMaxAmount() {
        // TODO
        return 0;
    }

    @Override
    public void onReturn(GForum gForum, HOverview parent) {
        // TODO
    }

    @Override
    public void onAdd(GForum gForum) {
        // TODO
    }

    @Override
    public void request(GForum gForum, int start, int amount) {
        // TODO
    }

    @Override
    public int internalRank() {
        return 2;
    }

    public List<HComment> getComments() {
        return comments;
    }
}
