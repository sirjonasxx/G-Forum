package gforum.entities;

import gforum.GForum;

import java.util.List;

public interface HOverview {
    String addElementText();
    String returnText();

    int getStartIndex();
    int getAmount();
    List<? extends ContentItem> contentItems();

    int getMaxAmount();

    void onReturn(GForum gForum, HOverview parent);
    void request(GForum gForum, int start, int amount);

    int internalRank();
}
