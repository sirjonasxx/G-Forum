package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;

public class HThread implements ContentItem {

    private final int threadId;

    private final long authorId;
    private final String authorName;
    private final String subject;

    private final boolean pinned;
    private final boolean locked;

    private final int passedTime;
    private final int ammountComments;
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
        authorName = hPacket.readString();
        subject = hPacket.readString();

        pinned = hPacket.readBoolean();
        locked = hPacket.readBoolean();

        passedTime = hPacket.readInteger();
        ammountComments = hPacket.readInteger();
        unreadComments = hPacket.readInteger();

        lastCommentIndexInForum = hPacket.readInteger();
        lastCommentAuthorId = hPacket.readLong();
        lastCommentAuthorName = hPacket.readString();
        lastCommentPassedTime = hPacket.readInteger();

        byte b = hPacket.readByte();
        state = HThreadState.fromValue(b);

        // sulake did an oopsie here
        if (hPacket.readInteger(hPacket.getReadIndex()) == 0
                && hPacket.readUshort(hPacket.getReadIndex() + 4) == 7
                && hPacket.readString(hPacket.getReadIndex() + 4).equals("unknown")) {
            adminId = 0;
            adminName = "unknown";
            hPacket.setReadIndex(hPacket.getReadIndex() + 4 + 2 + 7);
        }
        else {
            adminId = hPacket.readLong();
            adminName = hPacket.readString();
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

    public int getAmmountComments() {
        return ammountComments;
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

    @Override
    public void addHtml(int i, GForum gForum) {

    }

}
