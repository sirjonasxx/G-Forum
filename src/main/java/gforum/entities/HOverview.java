package gforum.entities;

import gforum.GForum;

import java.util.List;

public interface HOverview {
    String addElementText();
    boolean addElementEnabled();
    String returnText();

    int getStartIndex();
    int getAmount();
    List<? extends ContentItem> getContentItems();

    int getMaxAmount();

    void returnClick(GForum gForum);
    void addClick(GForum gForum);
    void request(GForum gForum, int start);

    int internalRank();
}
