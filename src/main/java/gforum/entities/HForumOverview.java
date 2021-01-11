package gforum.entities;

import gearth.protocol.HPacket;
import gforum.Constants;
import gforum.GForum;

import java.util.ArrayList;
import java.util.List;

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
        int forumsPageSize = hPacket.readInteger();
        for (int i = 0; i < forumsPageSize; i++) {
            forums.add(new HForum(hPacket));
        }
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
    public String returnText() {
        return "Mark As Read";
    }

    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getAmount() {
        return getForums().size();
    }

    @Override
    public List<? extends ContentItem> contentItems() {
        return getForums();
    }

    @Override
    public int getMaxAmount() {
        return size;
    }

    @Override
    public void request(GForum gForum, int start, int amount) {
        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_FORUMOVERVIEW, viewMode.getVal(), start, amount));
    }

    @Override
    public void onReturn(GForum gForum, HOverview parent) {
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
        gForum.sendToServer(new HPacket(Constants.OUT_REQUEST_FORUMOVERVIEW, viewMode.getVal(), 0, amount));
    }
}
