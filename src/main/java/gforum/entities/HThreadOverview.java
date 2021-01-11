package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;

import java.util.ArrayList;
import java.util.List;

public class HThreadOverview implements HOverview {

    private final int guildId;
    private final int startIndex;
    private final List<HThread> threads;

    public HThreadOverview(HPacket hPacket) {
        guildId = hPacket.readInteger();
        startIndex = hPacket.readInteger();

        threads = new ArrayList<>();
        int threadsSize = hPacket.readInteger();
        for (int i = 0; i < threadsSize; i++) {
            threads.add(new HThread(hPacket));
        }
    }

    public int getGuildId() {
        return guildId;
    }



    @Override
    public String addElementText() {
        return "New Thread";
    }

    @Override
    public String returnText() {
        return "Mark As Read";
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getAmount() {
        return contentItems().size();
    }

    @Override
    public List<? extends ContentItem> contentItems() {
        return getThreads();
    }

    @Override
    public int getMaxAmount() {
        // TODO
        return 0;
    }

    @Override
    public void request(GForum gForum, int start, int amount) {
        // TODO
    }

    @Override
    public int internalRank() {
        return 1;
    }

    @Override
    public void onReturn(GForum gForum, HOverview parent) {
        // TODO
    }

    public List<HThread> getThreads() {
        return threads;
    }
}
