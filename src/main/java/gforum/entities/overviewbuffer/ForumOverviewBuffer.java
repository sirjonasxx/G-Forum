package gforum.entities.overviewbuffer;

import gforum.GForum;
import gforum.entities.HForum;
import gforum.entities.HForumOverview;

public class ForumOverviewBuffer extends OverviewBuffer<HForumOverview, HForum> {
    public ForumOverviewBuffer(GForum gForum) {
        super(gForum, "GetForumsList", 0, 7);
    }

    @Override
    protected boolean objMatchesParams(HForumOverview overview, Object... requestParams) {
        if (requestParams.length != 1) return false;
        if (((int)(requestParams[0])) != overview.getViewMode().getVal()) return false;
        return true;
    }

    @Override
    protected void setOverview(int start, int amount) {
        HForumOverview hForumOverview = new HForumOverview(
                objFromRealPacket.getViewMode(),
                objFromRealPacket.getSize(),
                objFromRealPacket.getStartIndex() + start,
                objFromRealPacket.getForums().subList(start, amount)
        );

        gForum.getController().setForumOverview(hForumOverview);
    }
}
