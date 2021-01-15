package gforum.entities;

import gforum.GForum;

import java.util.List;

public interface HOverview {
    String addElementText();
    boolean addElementEnabled();
    String returnText();

    int getStartIndex();
    int getAmount();
    ContentItem getContentItem(int i);

    int getMaxAmount();

    void returnClick(GForum gForum);
    void addClick(GForum gForum);
    void request(GForum gForum, int start, int amount);

    int internalRank();
}
