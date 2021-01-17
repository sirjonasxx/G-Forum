package gforum.entities.overviewbuffer;

import gforum.GForum;
import gforum.entities.HComment;
import gforum.entities.HCommentOverview;

public class CommentOverviewBuffer extends OverviewBuffer<HCommentOverview, HComment> {
    public CommentOverviewBuffer(GForum gForum) {
        super(gForum, "GetForumThreadMessages", 2, 4);
    }

    @Override
    protected boolean objMatchesParams(HCommentOverview overview, Object... requestParams) {
        if (requestParams.length != 2) return false;
        if (((long)(requestParams[0])) != overview.getGuildId()) return false;
        if (((int)(requestParams[1])) != overview.getThreadId()) return false;
        return true;
    }

    @Override
    protected void setOverview(int start, int amount) {
        HCommentOverview newOverview = new HCommentOverview(
                objFromRealPacket.getGuildId(),
                objFromRealPacket.getThreadId(),
                objFromRealPacket.getStartIndex() + start,
                objFromRealPacket.getComments().subList(start, amount),
                gForum
        );

        gForum.getController().setCommentOverview(newOverview);
    }


}
