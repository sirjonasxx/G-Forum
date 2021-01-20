package gforum.entities.overviewbuffer;

import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gforum.GForum;
import gforum.entities.ContentItem;
import gforum.entities.HOverview;

public abstract class OverviewBuffer<T extends HOverview, V extends ContentItem> {

    private final int amountPages;
    private final String requestPacketName;
    protected final GForum gForum;
    private final int rank;

    protected volatile int requestedStartIndex;

    protected T objFromRealPacket;
    protected Object[] requestParams;


    protected OverviewBuffer(GForum gForum, String requestPacketName, int rank, int pages) {
        amountPages = pages;
        this.requestPacketName = requestPacketName;
        this.gForum = gForum;
        this.rank = rank;

        requestedStartIndex = 0;

        objFromRealPacket = null;
    }

    public void refill(T overview) {
        if (!objMatchesParams(overview, requestParams)) return;

        objFromRealPacket = overview;
        int listOffset = requestedStartIndex - overview.getStartIndex();
        int listAmount = Math.min(requestedStartIndex - overview.getStartIndex() + GForum.PAGESIZE, objFromRealPacket.getContentItems().size());
        setOverview(listOffset, listAmount);
    }

    public void request(boolean forceReload, int requestIndex, Object... requestParams) {
        if (!forceReload && objFromRealPacket != null && objMatchesParams(objFromRealPacket, requestParams) &&
                requestIndex >= objFromRealPacket.getStartIndex() && requestIndex < objFromRealPacket.getStartIndex() + objFromRealPacket.getAmount()) {

            gForum.getController().requestOverview(rank);

            int realStartIndex = objFromRealPacket.getStartIndex();
            int listOffset = requestIndex - realStartIndex;
            int listAmount = Math.min(requestIndex - realStartIndex + GForum.PAGESIZE, objFromRealPacket.getContentItems().size());
            setOverview(listOffset, listAmount);
        }
        else if (gForum.isConnectedToGame()) {
            gForum.getController().requestOverview(rank);

            this.requestParams = requestParams;

            int startPage = requestIndex/GForum.PAGESIZE;
            int cacheStartPage = Math.max(startPage - amountPages, 0);

            int headerId = gForum.getHashSupport().getHarbleAPI().getHarbleMessageFromName(HMessage.Direction.TOSERVER, requestPacketName).getHeaderId();
            HPacket hPacket = new HPacket(headerId);
            for (Object param : requestParams) hPacket.appendObject(param);
            hPacket.appendInt(cacheStartPage * GForum.PAGESIZE);
            hPacket.appendInt(amountPages * 2 * GForum.PAGESIZE);
            requestedStartIndex = requestIndex;

            gForum.sendToServer(hPacket);
        }

    }

    protected abstract boolean objMatchesParams(T obj, Object... requestParams);
    protected abstract void setOverview(int start, int amount);
}
