package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;

import java.nio.charset.StandardCharsets;

public class HComment implements ContentItem {

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

    @Override
    public void addHtml(int i, GForum gForum) {

    }

}
