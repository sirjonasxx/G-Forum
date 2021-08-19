package gforum.entities;

import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gforum.GForum;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HForumOverview implements HOverview {

    private final HForumOverviewType viewMode;
    private final int size;
    private final int startIndex;
    private final List<HForum> forums;

    public HForumOverview(HPacket hPacket) {
        viewMode = HForumOverviewType.fromValue(hPacket.readInteger());
        size = hPacket.readInteger();
        startIndex = hPacket.readInteger();

        forums = new ArrayList<>();
        int forumsPageSize = hPacket.readShort();
        for (int i = 0; i < forumsPageSize; i++) {
            forums.add(new HForum(hPacket));
        }
    }

    public HForumOverview(HForumOverviewType viewMode, int size, int startIndex, List<HForum> forums) {
        this.viewMode = viewMode;
        this.size = size;
        this.startIndex = startIndex;
        this.forums = forums;
    }

    public HForumOverviewType getViewMode() {
        return viewMode;
    }

    public int getSize() {
        return size;
    }


    @Override
    public String addElementText() {
        return null;
    }

    @Override
    public boolean addElementEnabled() {
        return false;
    }

    @Override
    public String returnText() {
        return "Mark As Read";
    }

    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getAmount() {
        return forums.size();
    }

    @Override
    public List<HForum> getContentItems() {
        return getForums();
    }

    @Override
    public int getMaxAmount() {
        return size;
    }

    @Override
    public void request(GForum gForum, int start) {
        gForum.getForumOverviewBuffer().request(false, start, viewMode.getVal());
    }

    @Override
    public void returnClick(GForum gForum) {
        HPacket markersUpdate = new HPacket("UpdateForumReadMarkers", HMessage.Direction.TOSERVER);
        List<HForum> readForums = forums.stream().filter(hForum -> hForum.getUnreadComments() > 0).collect(Collectors.toList());

        markersUpdate.appendShort((short)readForums.size());
        for(HForum hForum : readForums) {
            markersUpdate.appendLong(hForum.getGuildId());
            markersUpdate.appendInt(hForum.getLastCommentIndexInForum());
            markersUpdate.appendBoolean(true);
        }
        gForum.sendToServer(markersUpdate);

//        gForum.getHashSupport().sendToServer("UpdateForumReadMarkers", (short)1,  forums.get(0).getGuildId(),  forums.get(0).getLastCommentIndexInForum(), true);
        gForum.getPrimaryStage().hide();
    }

    @Override
    public void addClick(GForum gForum) {
        // TODO
    }

    @Override
    public int internalRank() {
        return 0;
    }

    public List<HForum> getForums() {
        return forums;
    }
}
