package gforum.entities;

import gearth.misc.harble_api.HarbleAPI;
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

    private final int startOffset;
    private final int maskAmount;

    public HForumOverview(HPacket hPacket) {
        viewMode = HForumOverviewType.fromValue(hPacket.readInteger());
        size = hPacket.readInteger();
        startIndex = hPacket.readInteger();

        forums = new ArrayList<>();
        int forumsPageSize = hPacket.readShort();
        for (int i = 0; i < forumsPageSize; i++) {
            forums.add(new HForum(hPacket));
        }
        startOffset = 0;
        maskAmount = forums.size();
    }

    public HForumOverview(HForumOverviewType viewMode, int size, int startIndex, List<HForum> forums, int offset, int maskAmount) {
        this.viewMode = viewMode;
        this.size = size;
        this.startIndex = startIndex;
        this.forums = forums;
        this.startOffset = offset;
        this.maskAmount = maskAmount;
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
        return maskAmount;
    }

    @Override
    public ContentItem getContentItem(int i) {
        return getForums().get(startOffset + i);
    }

    @Override
    public int getMaxAmount() {
        return size;
    }

    @Override
    public void request(GForum gForum, int start, int amount) {
        gForum.getHashSupport().sendToServer("GetForumsList", viewMode.getVal(), start, amount);
//        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_FORUMOVERVIEW, viewMode.getVal(), start, amount));
    }

    @Override
    public void returnClick(GForum gForum) {
        HarbleAPI.HarbleMessage harbleMessage = gForum.getHashSupport().getHarbleAPI().getHarbleMessageFromName(HMessage.Direction.TOSERVER, "UpdateForumReadMarkers");
        HPacket markersUpdate = new HPacket(harbleMessage.getHeaderId());
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

        new Thread(() -> {
            try {
                Thread.sleep(100);
                gForum.getController().requestOverview(0);
                requestFirst(gForum, HForumOverviewType.MY_FORUMS, 20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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


    public static void requestFirst(GForum gForum, HForumOverviewType viewMode, int amount){
        gForum.getHashSupport().sendToServer("GetForumsList", viewMode.getVal(), 0, amount);
//        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_FORUMOVERVIEW, viewMode.getVal(), 0, amount));
    }
}
