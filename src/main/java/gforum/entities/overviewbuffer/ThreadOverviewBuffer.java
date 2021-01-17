package gforum.entities.overviewbuffer;

import gforum.GForum;
import gforum.entities.HThread;
import gforum.entities.HThreadOverview;

public class ThreadOverviewBuffer extends OverviewBuffer<HThreadOverview, HThread> {
    public ThreadOverviewBuffer(GForum gForum) {
        super(gForum, "GetForumThreads", 1, 8);
    }

    @Override
    protected boolean objMatchesParams(HThreadOverview overview, Object... requestParams) {
        if (requestParams.length != 1) return false;
        if (((long)(requestParams[0])) != overview.getGuildId()) return false;
        return true;
    }

    @Override
    protected void setOverview(int start, int amount) {
        HThreadOverview hThreadOverview = new HThreadOverview(
                objFromRealPacket.getGuildId(),
                objFromRealPacket.getStartIndex() + start,
                objFromRealPacket.getThreads().subList(start, amount),
                objFromRealPacket.getForumStats()
        );

        gForum.getController().setThreadOverview(hThreadOverview);
    }
}
