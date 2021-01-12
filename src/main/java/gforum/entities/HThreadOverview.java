package gforum.entities;

import gearth.protocol.HPacket;
import gforum.GForum;

import java.util.ArrayList;
import java.util.List;

public class HThreadOverview implements HOverview {

    private final long guildId;
    private final int startIndex;
    private final List<HThread> threads;
    private HForumStats forumStats = null;

    public HThreadOverview(HPacket hPacket) {
        guildId = hPacket.readLong();
        startIndex = hPacket.readInteger();

        threads = new ArrayList<>();
        int threadsSize = hPacket.readShort();
        for (int i = 0; i < threadsSize; i++) {
            threads.add(new HThread(hPacket));
        }
    }

    public void setForumStats(HForumStats forumStats) {
        this.forumStats = forumStats;
    }

    public long getGuildId() {
        return guildId;
    }



    @Override
    public String addElementText() {
        return "New Thread";
    }

    @Override
    public boolean addElementEnabled() {
        return forumStats.getErrorStartThread().equals("");
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
        return getThreads().size();
    }

    @Override
    public ContentItem getContentItem(int i) {
        return getThreads().get(i);
    }

    @Override
    public int getMaxAmount() {
        return forumStats.gethForum().getAmountThreads();
    }

    @Override
    public void request(GForum gForum, int start, int amount) {
        gForum.getHashSupport().sendToServer("GetForumThreads",
                forumStats.gethForum().getGuildId(), start, amount);
    }

    @Override
    public int internalRank() {
        return 1;
    }

    @Override
    public void onReturn(GForum gForum, HOverview parent) {
        // TODO
    }

    @Override
    public void onAdd(GForum gForum) {
        // TODO
    }

    public List<HThread> getThreads() {
        return threads;
    }
}
