package gforum.entities;

import gearth.protocol.HPacket;

public class HForumStats {

    private final HForum hForum;

    private final HForumPermission readForumPermission;
    private final HForumPermission postMessagesPermission;
    private final HForumPermission postThreadsPermission;
    private final HForumPermission modForumPermission;

    // if error == "", there are no permission issues
    private final String errorRead;
    private final String errorPost;
    private final String errorStartThread;
    private final String errorModerate;
    private final String errorCitizen; // I think

    private final boolean isOwner;
    private final boolean isOwnerOrStaff;

    private volatile int updateReadMarker = -1;

    public HForumStats(HPacket hPacket) {
        hForum = new HForum(hPacket);

        readForumPermission = HForumPermission.fromValue(hPacket.readInteger());
        postMessagesPermission = HForumPermission.fromValue(hPacket.readInteger());
        postThreadsPermission = HForumPermission.fromValue(hPacket.readInteger());
        modForumPermission = HForumPermission.fromValue(hPacket.readInteger());

        errorRead = hPacket.readString();
        errorPost = hPacket.readString();
        errorStartThread = hPacket.readString();
        errorModerate = hPacket.readString();
        errorCitizen = hPacket.readString();

        isOwner = hPacket.readBoolean();
        isOwnerOrStaff = hPacket.readBoolean();
    }

    public HForum gethForum() {
        return hForum;
    }

    public HForumPermission getReadForumPermission() {
        return readForumPermission;
    }

    public HForumPermission getPostMessagesPermission() {
        return postMessagesPermission;
    }

    public HForumPermission getPostThreadsPermission() {
        return postThreadsPermission;
    }

    public HForumPermission getModForumPermission() {
        return modForumPermission;
    }

    public String getErrorRead() {
        return errorRead;
    }

    public String getErrorPost() {
        return errorPost;
    }

    public String getErrorStartThread() {
        return errorStartThread;
    }

    public String getErrorModerate() {
        return errorModerate;
    }

    public String getErrorCitizen() {
        return errorCitizen;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public boolean isOwnerOrStaff() {
        return isOwnerOrStaff;
    }


    public int getUpdateReadMarker() {
        return updateReadMarker;
    }

    public void setUpdateReadMarker(int updateReadMarker) {
        this.updateReadMarker = updateReadMarker;
    }
}
